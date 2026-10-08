import UIKit
import CoreText
import CoreLocation
import MapLibre
import Shared

/// Внедряется в Compose-контроллер; зависимости SDK остаются в iOS-приложении.
final class MapLibreCityMapFactory: IosCityMapFactory {
    func create(styleUrl: String, latitude: Double, longitude: Double, zoom: Double,
                events: IosMapEvents) -> IosCityMap {
        MapLibreCityMap(styleUrl: styleUrl, latitude: latitude, longitude: longitude,
                        zoom: zoom, events: events)
    }
}

final class MapLibreCityMap: NSObject, IosCityMap, MLNMapViewDelegate, UIGestureRecognizerDelegate {
    let mapView: MLNMapView
    var view: UIView { mapView }
    private var pinFont = UIFont.systemFont(ofSize: CGFloat(CityPinStyle.shared.fontSize), weight: .semibold)
    private var events: IosMapEvents?
    private var cities: [CityMapMarker] = []
    private var selectedId: Int64?
    private var source: MLNShapeSource?
    private let sourceId = "city-selection-source"
    private let selectedLabelsId = "city-selection-selected-labels"
    private var pinImages: [Int64: PinAppearance] = [:]
    private let labelsId = "city-selection-labels"
    private var tapRecognizer: UITapGestureRecognizer?

    init(styleUrl: String, latitude: Double, longitude: Double, zoom: Double, events: IosMapEvents) {
        mapView = CityMapView(frame: .zero, styleURL: URL(string: styleUrl))
        self.events = events
        super.init()
        (mapView as? CityMapView)?.onSizeChanged = { [weak self] in self?.emitViewport() }
        mapView.delegate = self
        mapView.minimumZoomLevel = CityMapConfig.shared.minZoom
        mapView.maximumZoomLevel = CityMapConfig.shared.maxZoom
        mapView.isRotateEnabled = false
        mapView.isPitchEnabled = false
        mapView.logoView.isHidden = true
        mapView.showsAttributionButton = false
        mapView.setCenter(CLLocationCoordinate2D(latitude: latitude, longitude: longitude),
                          zoomLevel: zoom, animated: false)
        let tap = UITapGestureRecognizer(target: self, action: #selector(selectCity(_:)))
        tap.cancelsTouchesInView = false
        tap.delegate = self
        for recognizer in mapView.gestureRecognizers ?? [] {
            if let other = recognizer as? UITapGestureRecognizer, other.numberOfTapsRequired > 1 {
                tap.require(toFail: other)
            }
        }
        mapView.addGestureRecognizer(tap)
        tapRecognizer = tap
    }

    func setPinFont(bytes: KotlinByteArray) {
        let data = Data((0..<Int(bytes.size)).map { UInt8(bitPattern: bytes.get(index: Int32($0))) })
        guard let provider = CGDataProvider(data: data as CFData), let font = CGFont(provider) else { return }
        let size = CGFloat(CityPinStyle.shared.fontSize)
        pinFont = UIFont(descriptor: CTFontCopyFontDescriptor(CTFontCreateWithGraphicsFont(font, size, nil, nil)) as UIFontDescriptor, size: size)
        pinImages.removeAll()
        updateFeatures()
    }

    func update(cities: [CityMapMarker], selectedId: KotlinLong?) {
        let id = selectedId?.int64Value
        guard self.cities != cities || self.selectedId != id else { return }
        self.cities = cities
        self.selectedId = id
        updateFeatures()
    }

    func mapView(_ mapView: MLNMapView, didFinishLoading style: MLNStyle) {
        let source = MLNShapeSource(identifier: sourceId, features: [], options: nil)
        style.addSource(source)
        self.source = source
        func pinLayer(_ id: String, selected: Bool) -> MLNSymbolStyleLayer {
            let layer = MLNSymbolStyleLayer(identifier: id, source: source)
            layer.predicate = NSPredicate(format: "selected == %d", selected ? 1 : 0)
            layer.iconImageName = NSExpression(forKeyPath: "pinImage")
            layer.iconAnchor = NSExpression(forConstantValue: "bottom")
            layer.iconOffset = NSExpression(forConstantValue: NSValue(cgVector: CGVector(dx: 0, dy: CGFloat(CityPinStyle.shared.iconOffsetY))))
            layer.iconAllowsOverlap = NSExpression(forConstantValue: selected)
            layer.iconIgnoresPlacement = NSExpression(forConstantValue: selected)
            return layer
        }
        style.addLayer(pinLayer(labelsId, selected: false))
        style.addLayer(pinLayer(selectedLabelsId, selected: true))
        updateFeatures()
        events?.didFinishLoading()
        emitViewport()
    }

    func mapViewDidFailLoadingMap(_ mapView: MLNMapView, withError error: Error) {
        events?.didFailLoading()
    }

    func mapView(_ mapView: MLNMapView, regionDidChangeAnimated animated: Bool) {
        emitViewport()
    }

    private func emitViewport() {
        guard source != nil, mapView.bounds.width > 0, mapView.bounds.height > 0 else { return }
        let bounds = mapView.visibleCoordinateBounds
        events?.viewportChanged(latitude: mapView.centerCoordinate.latitude,
            longitude: mapView.centerCoordinate.longitude, zoom: mapView.zoomLevel,
            northLatitude: bounds.ne.latitude, eastLongitude: bounds.ne.longitude,
            southLatitude: bounds.sw.latitude, westLongitude: bounds.sw.longitude)
    }

    private func updateFeatures() {
        guard let style = mapView.style, let source = source else { return }
        let ids = Set(cities.map { $0.id })
        for id in Array(pinImages.keys) where !ids.contains(id) {
            style.removeImage(forName: "city-pin-\(id)")
            pinImages.removeValue(forKey: id)
        }
        for city in cities {
            let appearance = PinAppearance(name: city.name, selected: city.id == selectedId)
            if pinImages[city.id] != appearance {
                style.setImage(cityPinImage(appearance, font: pinFont), forName: "city-pin-\(city.id)")
                pinImages[city.id] = appearance
            }
        }
        let features = cities.map { city -> MLNPointFeature in
            let point = MLNPointFeature()
            point.coordinate = CLLocationCoordinate2D(latitude: city.latitude, longitude: city.longitude)
            point.attributes = ["cityId": String(city.id), "pinImage": "city-pin-\(city.id)", "selected": city.id == selectedId ? 1 : 0]
            return point
        }
        source.shape = MLNShapeCollectionFeature(shapes: features)
    }

    // Встроенный обработчик одиночного касания SDK не должен подавлять выбор нашей метки.
    func gestureRecognizer(_ gestureRecognizer: UIGestureRecognizer,
                           shouldRecognizeSimultaneouslyWith otherGestureRecognizer: UIGestureRecognizer) -> Bool {
        gestureRecognizer === tapRecognizer && otherGestureRecognizer is UITapGestureRecognizer
    }

    @objc private func selectCity(_ tap: UITapGestureRecognizer) {
        guard tap.state == .ended else { return }
        let point = tap.location(in: mapView)
        let radius = CGFloat(CityPinStyle.shared.hitTestRadius)
        let hitRect = CGRect(x: point.x - radius, y: point.y - radius, width: radius * 2, height: radius * 2)
        if let feature = mapView.visibleFeatures(in: hitRect,
            styleLayerIdentifiers: Set([selectedLabelsId, labelsId])).first,
           let value = feature.attribute(forKey: "cityId") as? String, let id = Int64(value) {
            events?.citySelected(id: id)
        } else {
            events?.mapTapped()
        }
    }

    func zoomBy(delta: Double) {
        mapView.setZoomLevel(min(CityMapConfig.shared.maxZoom, max(CityMapConfig.shared.minZoom, mapView.zoomLevel + delta)), animated: true)
    }

    func dispose() {
        events = nil
        mapView.delegate = nil
        (mapView as? CityMapView)?.onSizeChanged = nil
        if let tap = tapRecognizer { mapView.removeGestureRecognizer(tap) }
        tapRecognizer = nil
        source = nil
        pinImages = [:]
        cities = []
    }
}

/// Смена размеров (например, поворот устройства) тоже изменяет область запроса.
private final class CityMapView: MLNMapView {
    var onSizeChanged: (() -> Void)?
    private var previousSize: CGSize = .zero
    override func layoutSubviews() {
        super.layoutSubviews()
        if bounds.size != previousSize {
            previousSize = bounds.size
            onSizeChanged?()
        }
    }
}

private struct PinAppearance: Equatable {
    let name: String
    let selected: Bool
}

/// Единый контур плашки с хвостиком; точка привязки находится снизу по центру.
private func cityPinImage(_ appearance: PinAppearance, font: UIFont) -> UIImage {
    let pin = CityPinStyle.shared
    let foreground = UIColor(argb: pin.foregroundArgb)
    let attributes: [NSAttributedString.Key: Any] = [.font: font, .foregroundColor: foreground]
    let text = appearance.name as NSString
    let width = max(CGFloat(pin.minimumWidth), ceil(text.size(withAttributes: attributes).width + CGFloat(pin.horizontalPadding) * 2))
    let center = width / 2
    let bodyHeight = CGFloat(pin.bodyHeight)
    let capRadius = bodyHeight / 2
    let top = CGFloat(pin.strokeWidth) / 2
    let bottom = bodyHeight - top
    let tailBottom = bottom + CGFloat(pin.tailHeight)
    let tailHalfWidth = CGFloat(pin.tailHalfWidth)
    let tailControl = CGFloat(pin.tailControlOffset)
    let curve = CGFloat(pin.capsuleControlOffset)
    let padding = CGFloat(pin.imagePadding)
    let imageHeight = bodyHeight + CGFloat(pin.tailHeight) + padding * 2
    return UIGraphicsImageRenderer(size: CGSize(width: width + padding * 2, height: imageHeight)).image { renderer in
        renderer.cgContext.translateBy(x: padding, y: padding)
        let path = UIBezierPath()
        path.move(to: CGPoint(x: capRadius, y: top))
        path.addLine(to: CGPoint(x: width - capRadius, y: top))
        path.addCurve(to: CGPoint(x: width - capRadius, y: bottom),
            controlPoint1: CGPoint(x: width + curve, y: top), controlPoint2: CGPoint(x: width + curve, y: bottom))
        path.addLine(to: CGPoint(x: center + tailHalfWidth, y: bottom))
        path.addCurve(to: CGPoint(x: center, y: tailBottom),
            controlPoint1: CGPoint(x: center + tailControl, y: bottom), controlPoint2: CGPoint(x: center + tailControl, y: tailBottom))
        path.addCurve(to: CGPoint(x: center - tailHalfWidth, y: bottom),
            controlPoint1: CGPoint(x: center - tailControl, y: tailBottom), controlPoint2: CGPoint(x: center - tailControl, y: bottom))
        path.addLine(to: CGPoint(x: capRadius, y: bottom))
        path.addCurve(to: CGPoint(x: capRadius, y: top),
            controlPoint1: CGPoint(x: -curve, y: bottom), controlPoint2: CGPoint(x: -curve, y: top))
        path.close()
        UIColor(argb: appearance.selected ? pin.selectedBackgroundArgb : pin.backgroundArgb).setFill()
        // Коэффициент сохраняет прежнюю мягкость тени Core Graphics.
        renderer.cgContext.setShadow(offset: CGSize(width: 0, height: CGFloat(pin.shadowOffsetY)),
                                     blur: CGFloat(pin.shadowRadius) * 2, color: UIColor(argb: pin.shadowArgb).cgColor)
        path.fill()
        renderer.cgContext.setShadow(offset: .zero, blur: 0, color: nil)
        foreground.setStroke()
        path.lineWidth = CGFloat(pin.strokeWidth)
        path.stroke()
        let size = text.size(withAttributes: attributes)
        text.draw(at: CGPoint(x: (width - size.width) / 2, y: (bodyHeight - size.height) / 2), withAttributes: attributes)
    }
}

private extension UIColor {
    convenience init(argb: Int32) {
        let value = UInt32(bitPattern: argb)
        self.init(red: CGFloat((value >> 16) & 0xFF) / 255,
                  green: CGFloat((value >> 8) & 0xFF) / 255,
                  blue: CGFloat(value & 0xFF) / 255,
                  alpha: CGFloat((value >> 24) & 0xFF) / 255)
    }
}
