# Vela Android Client

Vela is a remote system monitoring and management application. This Android client lets you monitor and control Vela-enabled hosts from your phone, with a dashboard, remote controls, and offline-first caching.

Built with Clean Architecture, MVVM, Jetpack Compose, and Room.

## Key Features

- **Multi-device**: Pair and switch between multiple agents on one phone (home PC, work laptop, etc.) without logging out. Each device keeps its own offline cache and chat history.
- **System Monitoring**: CPU, RAM, GPU, disk I/O, temperatures, fans, and uptime.
- **Process Management**: Running processes, resource usage, and active window.
- **Network Management**: Network info, Wi-Fi, Bluetooth, ping / speed tools.
- **Remote Control**: Volume, brightness, resolution, media playback.
- **Filesystem Browser**: Remote filesystem access and disk usage.
- **Clipboard Sync**: Sync clipboard with the remote host.
- **Task Scheduling**: Manage scheduled tasks on the remote host.
- **Offline-First**: Room cache + background sync (`DataSyncManager`) for the active device.

## Tech Stack

- **UI**: Jetpack Compose + Material 3
- **Navigation**: Navigation Compose
- **DI**: Hilt
- **Database**: Room (cache scoped by `connectionId` per paired device)
- **Networking**: Retrofit & OkHttp (`VelaInterceptor` attaches active device relay URL + `X-Secret`)
- **JSON**: Moshi
- **Prefs**: DataStore Preferences (legacy/template auth only)
- **Async**: Kotlin Coroutines / Flow

## Architecture

### Presentation (`presentation/`)
Compose screens, ViewModels, UI state via `StateFlow`.

### Domain (`domain/`)
Models (`PairedDevice`, Vela models), repository interfaces, use cases (`PairDeviceUseCase`, device switch/rename/remove, settings).

### Core / data (`core/`)
- Room entities/DAOs (settings = theme only; `paired_devices` registry; Vela + assistant tables keyed by `connectionId`)
- Repository implementations + `ActiveConnectionProvider`
- `DataSyncManager` syncs the **active** device only
- Pairing via `PairingApiService`; remote control via `VelaApiService`

## Multi-device

1. First launch → onboarding pairs the first agent.
2. **Add device** (Settings or device switcher sheet) pairs another agent without wiping others.
3. Switch active device from the **top bar** (name chip beside the health indicator) or Settings → Devices.
4. Logout is **Remove all devices** (clears registry + cache); single-device remove keeps the rest.

Agent-oriented file map: see [AGENTS.md](AGENTS.md).

## Getting Started

### Prerequisites
- Android Studio Ladybug (or newer)
- JDK 17
- A running Vela agent / relay (paired via QR or VPS URL + code + PIN)

### Build Flavors
- `staging` — development / testing
- `production` — live

### Installation
1. Clone the repository.
2. Open in Android Studio.
3. Sync Gradle and run the `:app` module.
4. Pair a device from onboarding (or Add device later).

Relay credentials are stored locally per device; `BuildConfig.BASE_URL` is only used by unused template User API scaffolding.

## Project Structure

```text
app/src/main/java/com/template/app/
├── MainActivity.kt           # VelaTopBar: title, device chip, health dot
├── core/
│   ├── data/                 # Room, Retrofit, repository impls, legacy migrator
│   ├── device/               # ActiveConnectionProvider, scoped Flow helpers
│   ├── di/                   # Hilt modules
│   ├── network/              # VelaInterceptor, Auth/Error interceptors
│   ├── sync/                 # DataSyncManager
│   └── utils/
├── domain/
│   ├── model/                # PairedDevice, Vela models, theme settings
│   ├── repository/
│   └── usecase/              # DeviceUseCases, SettingsUseCases, …
└── presentation/
    ├── ui/                   # Screens, components (DeviceSwitcherSheet), theme, nav
    └── viewmodel/
```

For a detailed agent index (feature → file map, ignore paths, conventions), see **[AGENTS.md](AGENTS.md)**.

## License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.
