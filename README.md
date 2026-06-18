# BeamSync Mobile

**Wireless file transfers between phone and desktop — no cables, no cloud, no sign-up.**

BeamSync pairs your Android phone with the [BeamSync desktop server](https://beamsync.app) over your local Wi-Fi network. Scan a QR code (or enter a URL) and instantly browse, upload, and download files at full LAN speed.

---

## Features

- **QR-pairing** — Scan a QR code from the desktop app to connect instantly; no IP-typing required.
- **Upload to desktop** — Select files via SAF/MediaStore picker and push them to the desktop server with real-time progress.
- **Download from desktop** — Browse files shared by the desktop server and pull them to your phone with progress tracking.
- **On-device hot-spot detection** — Automatically detects when the phone itself is acting as the Wi-Fi hotspot (via reflection for API 35+) and shows the band warning accordingly.
- **Transfer history** — Persisted record of all sends and receives with timestamps, statuses, and sortable history screen.
- **Material Design 3** — Dark theme with M3 `ColorScheme`, M3 `NavigationBar`, and dynamic theming via `ThemeMode` (System / Light / Dark).
- **Minimal permissions** — Camera (QR scanning only), Internet/LAN (local HTTP), Vibrate (haptic feedback). No storage permission required on Android 10+ (uses `MediaStore` / SAF).

---

## Screenshots

| Permissions | Home | History | Uploads |
|---|---|---|---|
| ![](screenshots/permissions.png) | ![](screenshots/home.png) | ![](screenshots/history.png) | ![](screenshots/uploads.png) |

---

## Tech Stack

| Layer | Technology |
|---|---|
| **Language** | Kotlin (2.2.10) |
| **UI** | Jetpack Compose + Material Design 3 (BOM 2026.02.01) |
| **Navigation** | Navigation Compose (type-safe with kotlinx.serialization) |
| **Networking** | OkHttp 4.12 |
| **JSON** | Gson 2.11 (history), JSONObject/JSONArray (upload manifest) |
| **Image loading** | Coil 2.7.0 |
| **QR scanning** | CameraX + ML Kit Barcode Scanning |
| **DI / State** | Manual (object singletons + ViewModels) |
| **Build** | Gradle with Kotlin DSL, version catalog |
| **Minimum SDK** | 26 (Android 8.0) |
| **Target SDK** | 36 (Android 16) |

---

## Architecture

```
app/
├── data/
│   └── history/
│       ├── HistoryRepository.kt      # Singleton, Gson-persisted transfer log
│       └── TransferRecord.kt         # Immutable data class with direction/status enums
│
├── network/
│   ├── BeamSyncClient.kt            # HTTP client: upload, download, heartbeat, connect
│   ├── CurrentConnection.kt         # MutableStateFlow of active connection
│   └── SavePathManager.kt          # SAF tree-URI persistence for download location
│
├── ui/
│   ├── navigation/                  # NavHost, type-safe route definitions
│   ├── screens/
│   │   ├── startup/PermissionsScreen.kt   # Camera + WiFi permission grant + status
│   │   ├── home/NewHomeScreen.kt          # Main hub: Receive / Send action cards
│   │   ├── scan/QrScannerScreen.kt        # CameraX + ML Kit scanner
│   │   ├── uploads/UploadsScreen.kt       # File picker + progress (upload to desktop)
│   │   ├── downloads/DownloadsScreen.kt   # Browse + pull files from desktop
│   │   ├── history/HistoryScreen.kt       # Transfer log with sort/filter tabs
│   │   └── settings/SettingsScreen.kt     # Theme toggle, about page
│   └── theme/                       # M3 colors, typography (Inter + JetBrains Mono), shapes
│
└── MainActivity.kt                  # SplashScreen, edge-to-edge, Compose root
```

### Data flow

```
Desktop Beamsync Server ←→ LAN HTTP ←→ OkHttp Client ←→ ViewModel ←→ Composable UI
                                            ↕
                                     HistoryRepository
                                    (Gson → file.json)
```

---

## Getting Started

### Prerequisites

- Android Studio Ladybug (2024.2) or later
- JDK 17+
- Android SDK 36 (Android 16)
- A [BeamSync desktop server](https://beamsync.app) running on the same LAN

### Clone & Build

```bash
git clone https://github.com/your-org/BeamSyncMobile.git
cd BeamSyncMobile
./gradlew assembleDebug
```

### Install & Run

```bash
# Install debug APK
./gradlew installDebug

# Or build a release APK (signed with debug keystore)
./gradlew assembleRelease
apksigner sign --ks ~/.android/debug.keystore --ks-pass pass:android \
  --out app/build/outputs/apk/release/BeamSync-v1.0-release.apk \
  app/build/outputs/apk/release/app-release-unsigned.apk
```

---

## Build Configuration

### Version catalog (`gradle/libs.versions.toml`)

| Dependency | Version |
|---|---|
| AGP | 9.2.1 |
| Kotlin | 2.2.10 |
| Compose BOM | 2026.02.01 |
| Navigation Compose | 2.8.5 |
| OkHttp | 4.12.0 |
| Gson | 2.11.0 |
| Coil | 2.7.0 |
| CameraX | 1.4.1 |
| ML Kit Barcode | 17.3.0 |

### ProGuard / R8

Release builds use R8 with full minification. The following keep rules are required:

- **ML Kit** — The full `com.google.mlkit.**` hierarchy must be kept un-obfuscated because ML Kit uses a reflection-based component discovery system (`MlKitInitProvider` → `ComponentDiscovery`). Without `-keepclassmembers ... { <init>(); }`, R8 strips no-arg constructors that ML Kit's DI calls via `Class.newInstance()`.
- **Compose** — All `androidx.compose.**` classes and `@Stable` field annotations must be kept.
- **Gson** — Data classes (`com.example.beamsyncmobile.data.history.**`) must be kept un-obfuscated so Gson can match JSON field names via reflection.
- **Coroutines** — `MainDispatcherFactory` and `CoroutineExceptionHandler` names must be preserved.

See `app/proguard-rules.pro` for the full rule set.

---

## Key Design Decisions

### Why ML Kit instead of ZXing?

ML Kit runs on-device with no network calls, supports multiple barcode formats out of the box, and integrates natively with CameraX (the recommended Android camera API). It auto-selects between camera1 and camera2 backends for broad device compatibility.

### Why Gson for history instead of Room?

Transfer history is a simple list append — single-file JSON read/write with no queries, no migrations, no relations. Introducing Room (with its schema versioning, DAOs, and coroutine wrappers) would add complexity with zero benefit for this access pattern. Gson + `TypeToken` + file I/O is a 50-line solution.

### Why manual DI instead of Hilt?

The app has exactly three singletons (`HistoryRepository`, `NetworkClient`, `ThemeManager`) and three ViewModels. Hilt's annotation processing, Gradle plugin, and runtime overhead aren't justified for this scope. Manual `object` singletons with constructor injection via ViewModel `Application` parameter work cleanly.

### Why `isWifiApEnabled` via reflection?

The `WifiManager.isWifiApEnabled()` method was removed from the public SDK in API 36 (Android 16). The app uses `getMethod("isWifiApEnabled")` with a `try/catch` — it works on all API levels (the method still exists in the framework; it's just hidden from the SDK stubs).

---

## Theming

BeamSync uses a custom M3 color scheme built around a **chartreuse (`#E4F900`) + cyan (`#00E5FF`) dichromatic palette** on a true-black (`#0A0A0A`) background. The design philosophy is defined in [`DESIGN_SYSTEM.md`](DESIGN_SYSTEM.md) — a cognitive-architecture-informed specification covering color psychometrics, typographic hierarchy, motion physics, and accessibility.

### Theme modes

- `System` — Follows the device's dark/light setting (default)
- `Light` — Forces light theme
- `Dark` — Forces dark theme

Toggle via the Settings screen.

---

## Testing

```bash
# Instrumented tests (Compose UI tests)
./gradlew connectedCheck

# Unit tests
./gradlew test
```

The app uses:
- **JUnit 4** for unit tests
- **Compose UI Test** (`createComposeRule`) for integration tests
- **Espresso** for supplemental instrumentation tests

---

## Project Status

**MVP.** The app is functional for one-to-one transfers with a BeamSync desktop server. Planned improvements:

- [ ] Concurrent multi-file transfers (currently sequential)
- [ ] Folder upload support
- [ ] Download pause/resume
- [ ] LAN peer discovery (mDNS/NSD)
- [ ] Background transfer service with notification channel
- [ ] Android TV / tablet layout

---

## License

```
Copyright 2026 BeamSync

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
