# BeamSync Mobile — Release Notes

## 1.0.0 — Initial Public Release

**Build:** 10000 · **Date:** 2026-06-18 · **Min SDK:** 26 · **Target SDK:** 36

Wireless file transfers between Android phone and desktop — no cables, no cloud, no sign-up.

---

### Overview

BeamSync Mobile is the Android companion to the BeamSync desktop server, enabling wireless file transfers over your local network. No cables, no cloud uploads, no accounts — just LAN-speed file movement between phone and desktop.

---

### What's New

#### ✨ Features

- **QR-paired connection** — Scan a QR code from the BeamSync desktop app to establish an instant HTTP session. Manual URL entry also supported.
- **Upload to desktop** — Select files via the SAF picker (or MediaStore on Android 10+) and push them to the desktop server. Real-time per-file progress with chunked streaming.
- **Download from desktop** — Browse the desktop server's shared-file listing, select files, and pull them to your device. Download location configurable via SAF tree-URI picker.
- **Transfer history** — All sends and receives are persisted to JSON with timestamps, file names, sizes, direction (SEND/RECEIVE), and status (SUCCESS/FAILED). Sortable by newest/oldest, filterable by send/receive/all.
- **On-device hotspot detection** — Correctly identifies when the phone itself is the Wi-Fi hotspot (using reflection for API 35+ compatibility where `isWifiApEnabled` was removed from the SDK).
- **QR scanner** — CameraX + ML Kit barcode scanning with viewfinder brackets. Handles both standard QR codes and URLs with embedded tokens.

#### 🎨 Design

- **Material Design 3** dark theme with a custom chartreuse (`#E4F900`) + cyan (`#00E5FF`) dichromatic palette on true-black (`#0A0A0A`).
- **Industrial brutalism-inspired UI** — Sharp corners, monospaced data values (JetBrains Mono), high information density. Full specification in [`DESIGN_SYSTEM.md`](DESIGN_SYSTEM.md).
- **Splash Screen API** with branded icon launch animation.
- **Adaptive icon** — Circular logo on black background blends with system icon shapes.
- **Dynamic theme modes** — System (follows device), Light, and Dark, toggled from Settings.

#### 🛡️ Permissions

| Permission | Scope |
|---|---|
| `INTERNET` + `ACCESS_NETWORK_STATE` + `ACCESS_WIFI_STATE` | LAN communication with desktop server |
| `CAMERA` | QR code scanning only |
| `VIBRATE` | Haptic feedback on scan/transfer events |
| `FOREGROUND_SERVICE` + `FOREGROUND_SERVICE_DATA_SYNC` | Background transfer support (Android 14+) |
| `WRITE_EXTERNAL_STORAGE` | Android 9 and below only (maxSdkVersion=28) |

No storage permission required on Android 10+ — uses `MediaStore` and SAF.

---

### Known Issues

- Multi-file download is sequential; files are transferred one at a time.
- No LAN peer discovery (mDNS/NSD) — must scan QR or enter URL manually.
- Download progress UI uses OkHttp streaming; very large files (>4 GB) may hit buffer limits on low-RAM devices.
- `isWifiApEnabled()` reflection may fail on vendor-customized Android 16 ROMs that fully remove the hidden method.

---

### Technical Notes

- **API level:** Target SDK 36 (Android 16), minimum SDK 26 (Android 8.0).
- **ProGuard / R8:** Release builds are fully minified. Three keep-rule categories required:
  1. **ML Kit** — Full class hierarchy + no-arg constructors preserved (ML Kit uses reflection-based component discovery).
  2. **Gson-serialized data classes** — `TransferRecord` fields kept un-obfuscated for JSON reflection.
  3. **Compose runtime** — All `androidx.compose.**` classes + `@Stable` annotations preserved.
- **Hotspot detection:** `WifiManager.getMethod("isWifiApEnabled")` with try/catch fallback — works on all API levels 26–36.
- **Build system:** Gradle 9.2.1 with Kotlin 2.2.10 and Compose BOM 2026.02.01.
- **APK size:** ~31 MB (release, minified + resource-shrunk).

---

### Downloads

| Artifact | Size | Signature |
|---|---|---|
| `BeamSync-v1.0.0-release.apk` | 31 MB | APK Signature Scheme v2 + v3 |

---

### Contributors

- Engineering & Design — [@PranavAgarkar07](https://github.com/PranavAgarkar07)

---


