# Vela Android — Agent Project Index

Read this file before scanning the repo. Prefer opening the paths below over recursive filesystem walks.

## What this is

Android client for **Vela** (remote system monitoring & management). Single module `:app`. Package: `com.template.app`.

Stack: Kotlin, Jetpack Compose, Material 3, Navigation Compose, Hilt, Room, Retrofit/OkHttp, Moshi, DataStore, Coroutines/Flow. Clean Architecture + MVVM, offline-first.

**Multi-device (local):** one phone can store many paired agents (`relayBaseUrl` + long-lived `relaySecret` + label). Active device drives API auth and scoped Room cache. No cloud app account.

## Ignore (do not scan)

| Path | Why |
|------|-----|
| `.git/`, `.gradle/`, `.idea/`, `.kotlin/` | Tooling / IDE |
| `**/build/` | Generated |
| `app/production/`, `app/staging/` | Build outputs / release artifacts |
| `local.properties` | Machine-local SDK paths |
| `*.apk`, `*.sqlite`, `*.jks` | Binaries / secrets |

## Repo layout

```
vela-android/
├── AGENTS.md                 ← this index
├── README.md                 ← product overview
├── build.gradle.kts          ← root Gradle
├── settings.gradle.kts       ← includes :app only
├── gradle/libs.versions.toml ← version catalog
├── .github/                  ← Copilot / commit notes (stubs)
└── app/
    ├── build.gradle.kts      ← flavors, signing, deps, BASE_URL
    ├── proguard-rules.pro
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml
        │   ├── java/com/template/app/   ← all Kotlin source
        │   └── res/                     ← drawables, values, xml
        └── test/                        ← (empty / sparse)
```

## Source root

`app/src/main/java/com/template/app/`

| Area | Path | Role |
|------|------|------|
| Entry | `MainActivity.kt` (`VelaTopBar`, health + device chip), `MyApplication.kt` (`BaseApplication`) | App bootstrap; Hilt `@HiltAndroidApp` on `BaseApplication` |
| Presentation UI | `presentation/ui/` | Compose screens, components, theme, nav |
| ViewModels | `presentation/viewmodel/` | UI state / events (`MainViewModel` owns top-bar device switch) |
| Domain models | `domain/model/` | Pure models (`PairedDevice`, …) |
| Domain repos | `domain/repository/` | Interfaces (`DeviceRepository`, …) |
| Use cases | `domain/usecase/` | `DeviceUseCases`, `SettingsUseCases`, `UserUseCases` |
| Room | `core/data/local/` | DB, DAOs, entities, DataStore, legacy migrator |
| Device session | `core/device/` | `ActiveConnectionProvider`, scoped Flow helpers |
| Network API | `core/data/remote/api/` | Retrofit services |
| DTOs | `core/data/remote/dto/` | Wire models |
| Repo impls | `core/data/repository/` | Offline-first implementations (scoped by `connectionId`) |
| DI | `core/di/` | Hilt modules |
| Network infra | `core/network/` | Interceptors (`VelaInterceptor` uses active device), adapters, errors |
| Sync | `core/sync/DataSyncManager.kt` | Background sync for active device only |
| Utils | `core/utils/` | `Resource`, `SafeApiCall`, `AppEventManager` |

## Entry & navigation

| Concern | File |
|---------|------|
| Launcher / deep link pairing | `AndroidManifest.xml`, `MainActivity.kt` |
| Top bar (title, health dot, device switcher) | `MainActivity.kt` → `VelaTopBar` trailing: device chip + `ConnectionStatusIndicator` |
| Application class | `MyApplication.kt` → `BaseApplication` |
| Routes + `NavHost` | `presentation/ui/AppNavHost.kt` (`Routes` object) |
| Tab shell | `presentation/ui/screens/MainScreen.kt` |
| Theme | `presentation/ui/theme/{Color,Theme,Type}.kt` |
| Shared UI | `presentation/ui/components/` (`DeviceSwitcherSheet`, …) |
| Uptime display | `DashboardComponents.kt` → `StatusCard` (segmented hrs/min/sec or days/hrs/min) |

### Routes (from `AppNavHost.kt`)

Primary tabs: `dashboard`, `chat`, `display`, `audio`, `network`, `media`  
More menu: `files`, `processes`, `security`, `scheduler`, `maintenance`, `power`, `clipboard`, `input_control`, `notifications`, `settings`, `network_logs`, `monitor`  
Flows: `onboarding` → `main`; `add_device` (pair additional agent); start destination = main if any `paired_devices`, else onboarding

## Multi-device map

| Concern | Path |
|---------|------|
| Domain model | `domain/model/PairedDevice.kt` |
| Room entity / DAO | `entities/PairedDeviceEntity.kt`, `dao/PairedDeviceDao.kt` |
| Repository | `domain/repository/DeviceRepository.kt`, `DeviceRepositoryImpl.kt` |
| Use cases / pairing | `domain/usecase/DeviceUseCases.kt` (`PairDeviceUseCase`, switch/rename/remove) |
| Active session | `core/device/ActiveConnectionProvider.kt` |
| API auth | `core/network/VelaInterceptor.kt` → active device relay URL + `X-Secret` |
| App prefs (theme only) | `SettingsEntity` / `ConnectionSettings` / `SettingsRepository` |
| Legacy upgrade | `LegacyConnectionMigrator.kt` + `LegacyConnectionRestorer.kt` (v25 → v26) |
| Add device UI | `screens/AddDeviceScreen.kt`, `AddDeviceViewModel.kt` |
| Device switcher UI | `MainActivity` top-bar chip (beside health) → `DeviceSwitcherSheet`; manage list in `SettingsScreen` (flat rows) |
| Switch / list state | `MainViewModel` (`activeDevice`, `pairedDevices`, `switchDevice`) |

Room cache (Vela + assistant) is keyed by `connectionId`. Repos inject `ActiveConnectionProvider` and pass it to DAO calls.

## Feature → files map

For a feature, open Screen → ViewModel → domain repo → impl → API/DAO. Names are consistent.

| Feature | Screen | ViewModel | Domain repo | Impl | Notes |
|---------|--------|-----------|-------------|------|-------|
| Dashboard / health | `screens/DashboardScreen.kt` (+ `DashboardComponents.kt`) | `DashboardViewModel.kt` | `HealthRepository`, `MonitorRepository`, … | matching `*RepositoryImpl` | Segmented uptime; sync via `DataSyncManager`; device switch is in `MainActivity` top bar |
| Top-bar devices | `MainActivity.kt` | `MainViewModel.kt` | `DeviceRepository` | | Chip beside health → `DeviceSwitcherSheet` |
| Chat / assistant | `screens/chat/` | `AssistantViewModel.kt` | `AssistantRepository` | `AssistantRepositoryImpl` | Chat scoped per `connectionId` |
| Display | `screens/DisplayScreen.kt` | `DisplayViewModel.kt` | `DisplayRepository` | `DisplayRepositoryImpl` | |
| Audio | `screens/AudioScreen.kt` | `AudioViewModel.kt` | `AudioRepository` | `AudioRepositoryImpl` | |
| Network | `screens/NetworkScreen.kt` | `NetworkViewModel.kt` | `NetworkRepository` | `NetworkRepositoryImpl` | |
| Network logs | `screens/NetworkLogsScreen.kt` | `NetworkLogsViewModel.kt` | (network) | | |
| Media | `screens/MediaScreen.kt` | `MediaViewModel.kt` | `MediaRepository` | `MediaRepositoryImpl` | |
| Files | `screens/FilesScreen.kt` | `FilesViewModel.kt` | `FilesystemRepository` | `FilesystemRepositoryImpl` | |
| Processes | `screens/ProcessesScreen.kt` | `ProcessesViewModel.kt` | `ProcessesRepository` | `ProcessesRepositoryImpl` | |
| Scheduler | `screens/SchedulerScreen.kt` | `SchedulerViewModel.kt` | `SchedulesRepository` | `SchedulesRepositoryImpl` | Shell jobs: create needs `command`+`args`+`run_at` (+ cron in `recurring`); optimistic Room upsert after create; list wipe only on explicit `jobs`/`tasks` array |
| Maintenance | `screens/MaintainanceScreen.kt` | `MaintainanceViewModel.kt` | `MaintenanceRepository` | `MaintenanceRepositoryImpl` | Filename spelling: Maintainance; all services in Room (`vela_services`); UI shows 5 + search; logs on expand |
| Power | `screens/PowerScreen.kt` | `PowerViewModel.kt` | `PowerRepository` | `PowerRepositoryImpl` | |
| Clipboard | `screens/ClipboardScreen.kt` | `ClipboardViewModel.kt` | `ClipboardRepository` | `ClipboardRepositoryImpl` | |
| Monitor | `screens/MonitorScreen.kt` | `MonitorViewModel.kt` | `MonitorRepository` | `MonitorRepositoryImpl` | Disk/net I/O + processes via direct `GET /monitor/disk-io`, `/network-io`, `/processes` (not snapshot-only) |
| Settings / devices | `screens/SettingsScreen.kt` | `SettingsViewModel.kt` | `DeviceRepository`, `SettingsRepository` | | Devices list, add/remove/switch |
| Onboarding / pair first | `screens/onboarding/` | `OnboardingViewModel.kt` | `PairDeviceUseCase` | | Does **not** wipe existing devices |
| Add device | `screens/AddDeviceScreen.kt` | `AddDeviceViewModel.kt` | `PairDeviceUseCase` | | Appends + activates |
| Users | (via settings/main) | `UsersViewModel.kt` | `UserRepository` | `UserRepositoryImpl` | Template API; unused for relay auth |
| Placeholders | `screens/PlaceholderScreens.kt` | — | — | — | Unfinished routes |

## Data layer quick map

| Kind | Path |
|------|------|
| Room DB | `core/data/local/AppDatabase.kt` (v27), `Converters.kt` |
| DAOs | `dao/{PairedDevice,Assistant,Settings,User,Vela}Dao.kt` |
| Entities | `entities/` — Vela bulk in `VelaEntities.kt` (all scoped by `connectionId`) |
| Preferences | `core/data/local/UserPreferencesDataStore.kt` |
| Main Vela API | `core/data/remote/api/VelaApiService.kt` |
| Pairing / user API | `PairingApiService.kt`, `UserApiService.kt` |
| DTOs | `dto/{Assistant,ConnectionSettings,Pairing,User,Vela}Dto.kt` |
| Domain models | `domain/model/{PairedDevice,AssistantModels,ConnectionSettings,User,VelaModels}.kt` |
| Hilt | `core/di/{Database,Network,Repository}Module.kt` |
| Auth / errors | `core/network/{Auth,Error,Vela}Interceptor.kt`, `NetworkErrors.kt`, `MoshiAdapters.kt` |

## Build & config

| Item | Where |
|------|-------|
| Flavors | `staging`, `production` (`app/build.gradle.kts`) |
| `BASE_URL` / logging | `BuildConfig` fields in `app/build.gradle.kts` (debug/release/flavor) |
| Versions / deps | `gradle/libs.versions.toml`, `app/build.gradle.kts` |
| Manifest perms | Internet, network state, camera (QR pairing) |

## Conventions for agents

1. **Start here**, then open only the feature row above — do not glob the whole tree.
2. New features follow Screen → ViewModel → domain interface → `*RepositoryImpl` → API/DAO → Hilt bind in `RepositoryModule`.
3. All new Vela/assistant Room rows **must** include `connectionId` from `ActiveConnectionProvider`.
4. Pairing goes through `PairDeviceUseCase` — never clear the device registry on add-device / onboarding init.
5. UI is Compose-only under `presentation/ui/`; keep business logic out of composables.
6. Typo in filenames is intentional legacy: `Maintainance*` (not Maintenance) for screen/VM.

## Related docs

- Product / architecture summary: `README.md`
- This index is the agent map; keep it updated when adding screens, repos, or modules.
