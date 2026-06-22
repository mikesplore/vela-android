# Vela Android Client

Vela is a powerful remote system monitoring and management application. This Android client allows you to monitor and control your Vela-enabled devices from anywhere, providing a comprehensive dashboard and remote management tools.

Built with modern Android practices: Clean Architecture, MVVM, Jetpack Compose, and an offline-first approach.

## 🚀 Key Features

- **System Monitoring**: Real-time tracking of CPU, RAM, GPU, and Disk I/O. Monitor hardware health including temperatures and fan speeds.
- **Process Management**: View running processes, monitor their resource usage, and track the active window.
- **Network Management**: Detailed network information, Wi-Fi status, Bluetooth device management, and integrated ping/speed test tools.
- **Remote Control**: Adjust system volume, display brightness, screen resolution, and control media playback remotely.
- **Filesystem Browser**: Remote access to the host's filesystem, including disk usage statistics.
- **System Maintenance**: Monitor system services, view logs, and track available package updates.
- **Clipboard Sync**: Seamlessly synchronize clipboard content between your Android device and the remote system.
- **Task Scheduling**: Manage and monitor scheduled tasks on the remote host.
- **Offline-First**: Reliable data access using Room for local caching and background synchronization.

## 🛠 Tech Stack

- **UI**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3.
- **Navigation**: [Navigation Compose](https://developer.android.com/jetpack/compose/navigation).
- **Dependency Injection**: [Hilt](https://developer.android.com/training/dependency-injection/hilt-android).
- **Database**: [Room](https://developer.android.com/training/data-storage/room) for persistent caching.
- **Networking**: [Retrofit](https://square.github.io/retrofit/) & [OkHttp](https://square.github.io/okhttp/).
- **JSON Parsing**: [Moshi](https://github.com/square/moshi).
- **Local Storage**: [DataStore Preferences](https://developer.android.com/topic/libraries/architecture/datastore).
- **Async & Streams**: Kotlin Coroutines and Flow/StateFlow.

## 🏗 Architecture

The project follows **Clean Architecture** principles to ensure maintainability and testability:

### 1. Presentation Layer (`presentation/`)
- **Composables**: Declarative UI components.
- **ViewModels**: Manage UI state and handle user interactions via Use Cases.
- **UI State**: Encapsulated in `StateFlow` for reactive UI updates.

### 2. Domain Layer (`domain/`)
- **Models**: Pure Kotlin data classes representing the Vela system state.
- **Repository Interfaces**: Define the contracts for data operations.
- **Use Cases**: Encapsulate specific business logic and orchestration.

### 3. Core/Data Layer (`core/`)
- **Data Sources**: Implementation of repositories, coordinating between `VelaApiService` and `VelaDao`.
- **Sync**: `DataSyncManager` handles periodic background synchronization of system metrics.
- **Network**: Retrofit configuration for communicating with the Vela API.

## 🚦 Getting Started

### Prerequisites
- Android Studio Ladybug (or newer)
- JDK 17
- A running [Vela Server](https://github.com/your-repo/vela-server) (configurable via `BASE_URL`)

### Build Flavors
- `staging`: Configured for development and testing environments.
- `production`: Configured for live environments.

### Installation
1. Clone the repository.
2. Open in Android Studio.
3. Update `BASE_URL` in the respective build variant if necessary.
4. Sync Gradle and run the `:app` module.

## 📁 Project Structure

```text
app/src/main/java/com/template/app/
├── core/
│   ├── data/             # Local (Room) and Remote (Retrofit) implementations
│   ├── di/               # Hilt Dependency Injection modules
│   ├── sync/             # Background synchronization logic
│   └── utils/            # Shared utilities
├── domain/
│   ├── model/            # Vela domain models
│   ├── repository/       # Repository interfaces
│   └── usecase/          # Business logic executors
└── presentation/
    ├── ui/               # Compose Screens, Components, and Theme
    └── viewmodel/        # ViewModels and UI State
```

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
