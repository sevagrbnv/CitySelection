# CitySelection

Приложение для просмотра городов по тестовому заданию. Проект использует Kotlin
Multiplatform и общий интерфейс Compose для Android и iOS.

Сейчас реализован сетевой слой: получение городов с поиском и пагинацией,
преобразование DTO, обработка ошибок и внедрение зависимостей. Реализован экран
списка с Orbit, поиском, пагинацией, загрузкой и повтором после ошибок.
Реализованы переход к информации о городе, возврат к списку и внешний поиск
по имени города. Карта на MapLibre Native показывает города из API вокруг
видимой области и открывает модальную карточку выбранного города. Подложка —
OpenStreetMap через OpenFreeMap.

## Структура

Общий код находится в `shared/src/commonMain/kotlin/com/sevagrnbv/cityselection`:

```text
cities/
  data/
    remote/          API и DTO
    mapper/          преобразование DTO в модели
    repository/      реализации репозиториев
  domain/
    model/           City и CitiesPage
    repository/      контракты репозиториев
  presentation/
    list/            список, поиск и пагинация
    details/         экран информации о городе
    map/             карта, карточка и UI-состояние
    navigation/      контейнер фичи и маршруты
    common/          поля города, форматирование и названия стран
core/
  network/           клиент, обработка ошибок и результаты запросов
  designsystem/
    theme/           тема, цвета, типографика и размеры
    component/       общие UI-компоненты
    resource/        платформенная загрузка шрифтов (androidMain)
di/                  сборка графа зависимостей
```

Платформенные реализации в `androidMain` и `iosMain`, а также тесты в `commonTest`
повторяют пакеты соответствующего общего кода. Общие поля города находятся в
`presentation.common`: карта и детали используют их независимо друг от друга.

`App.kt` и платформенные точки входа остаются в корневом пакете приложения.
`androidApp` содержит Android Application, Activity и конфигурацию сборки;
`iosApp` — Swift-точку входа, интеграцию MapLibre и проект Xcode.
Новые Gradle-модули при группировке пакетов не создавались.

Документация: [сеть](docs/network-layer.md), [список](docs/cities-screen.md),
[детали](docs/city-details.md), [карта](docs/city-map.md), [дизайн-система](docs/design-system.md).

## Стек

Kotlin, Compose Multiplatform, Ktor, kotlinx.serialization, Coroutines, Koin.
Orbit управляет состоянием списка и карты городов, Navigation Compose — переходами
внутри фичи выбора города.
Версии зависимостей зафиксированы в `gradle/libs.versions.toml`.

## Сборка и проверки

Для Android необходим установленный SDK; путь к нему задаётся локально в
`local.properties` или через настройки окружения. Параметры JVM находятся в
`gradle/gradle-daemon-jvm.properties`.

Тесты общего сетевого слоя на JVM:

```sh
./gradlew :shared:testAndroidHostTest
```

Release-сборка Android:

```sh
./gradlew :androidApp:assembleRelease
```

APK: `androidApp/build/outputs/apk/release/androidApp-release-unsigned.apk`.
Подпись release и минификация пока не настроены.

Компиляция общего кода для iOS Simulator на Mac с Xcode:

```sh
./gradlew :shared:compileKotlinIosSimulatorArm64
```

Для запуска iOS откройте `iosApp/iosApp.xcodeproj` в Xcode и настройте подпись.

## Документация

Описание сетевого слоя: [docs/network-layer.md](docs/network-layer.md).

Состояния и логика списка: [docs/cities-screen.md](docs/cities-screen.md).

Информация о городе и навигация: [docs/city-details.md](docs/city-details.md).

Карта, платформенные SDK и контракт загрузки: [docs/city-map.md](docs/city-map.md).
