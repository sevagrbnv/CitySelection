# CitySelection

Приложение для просмотра городов по тестовому заданию. Проект использует Kotlin
Multiplatform и общий интерфейс Compose для Android и iOS.

Сейчас реализован сетевой слой: получение городов с поиском и пагинацией,
преобразование DTO, обработка ошибок и внедрение зависимостей. Реализован экран
списка с Orbit, поиском, пагинацией, загрузкой и повтором после ошибок.
Информация о городе и дополнительная карта ещё не реализованы.

## Структура

- `shared` — общий код, Compose-интерфейс и платформенные сетевые движки.
- `androidApp` — Android-приложение и его конфигурация.
- `iosApp` — iOS-приложение и проект Xcode.
- `docs/network-layer.md` — контракт API, обработка ошибок и жизненный цикл DI.

## Стек

Kotlin, Compose Multiplatform, Ktor, kotlinx.serialization, Coroutines, Koin.
Orbit управляет состоянием списка городов.
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
