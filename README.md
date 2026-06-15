# Android Template App

A robust, production-ready Android template implementing Clean Architecture, MVVM, and the latest Jetpack libraries. This template is designed to be offline-first and easily extensible.

## 🚀 Features

- **Clean Architecture**: Separation of concerns between Data, Domain, and Presentation layers.
- **Offline-First**: Uses Room for local caching of network data.
- **Full CRUD Support**: Complete implementation of Create, Read, Update, and Delete operations for Users.
- **Modern UI**: Built entirely with Jetpack Compose and Material 3.
- **Pull-to-Refresh**: Integrated Material 3 `PullToRefreshBox` for data synchronization.
- **Dependency Injection**: Hilt for robust and testable DI.
- **Reactive Programming**: Extensive use of Kotlin Coroutines and Flow/StateFlow.
- **Networking**: Retrofit 2 with Moshi for JSON parsing (handling camelCase server responses).
- **Build Variants**: Configured for `staging` and `production` environments with custom `BASE_URL`s.
- **Version Catalog**: Centralized dependency management via `libs.versions.toml`.

## 🛠 Tech Stack

- **UI**: [Jetpack Compose](https://developer.android.com/jetpack/compose)
- **Navigation**: [Navigation Compose](https://developer.android.com/jetpack/compose/navigation)
- **Dependency Injection**: [Hilt](https://developer.android.com/training/dependency-injection/hilt-android)
- **Database**: [Room](https://developer.android.com/training/data-storage/room)
- **Networking**: [Retrofit](https://square.github.io/retrofit/) & [OkHttp](https://square.github.io/okhttp/)
- **JSON Parsing**: [Moshi](https://github.com/square/moshi)
- **Local Storage**: [DataStore Preferences](https://developer.android.com/topic/libraries/architecture/datastore)
- **Image Loading**: (Ready for Coil/Glide integration)

## 🏗 Architecture

The project follows the **Clean Architecture** pattern:

### 1. Presentation Layer (`ui`, `viewmodel`)
- **Composables**: Stateless UI components.
- **ViewModels**: Manage UI state and handle user interactions via Use Cases.
- **UI State**: Encapsulated in `StateFlow` using sealed classes for Loading, Success, and Error states.

### 2. Domain Layer (`model`, `repository`, `usecase`)
- **Models**: Pure Kotlin data classes.
- **Repository Interfaces**: Define the contract for data operations.
- **Use Cases**: Encapsulate specific business logic (e.g., `GetUsersUseCase`, `CreateUserUseCase`).

### 3. Core/Data Layer (`data`, `di`, `network`, `utils`)
- **DTOs**: Data Transfer Objects for API communication.
- **Entities**: Room database entities.
- **Repositories**: Implementation of domain interfaces, coordinating network and local cache.
- **DI Modules**: Hilt modules for providing dependencies.

## 🚦 Getting Started

### Prerequisites
- Android Studio Ladybug (or newer)
- JDK 17
- Gradle 8.7.3

### Build Flavors
The app includes two environment flavors:
- `staging`: For testing and development.
- `production`: For the live environment.

### Installation
1. Clone the repository.
2. Open in Android Studio.
3. Sync Gradle.
4. Run the `:app` module using your desired build variant (e.g., `productionDebug`).

## 📁 Project Structure

```text
app/src/main/java/com/template/app/
├── core/
│   ├── data/             # Local and Remote data sources
│   ├── di/               # Hilt Dependency Injection modules
│   ├── network/          # Network configuration
│   └── utils/            # Shared utilities (Resource, SafeApiCall)
├── domain/
│   ├── model/            # Business models
│   ├── repository/       # Repository contracts
│   └── usecase/          # Business logic executors
└── presentation/
    ├── ui/               # Compose Screens, Components, and Theme
    └── viewmodel/        # ViewModels and UI State
```

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
