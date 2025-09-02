# ⚡ Real Internet Speed Tester

A modern Android app built with Jetpack Compose that performs real internet speed tests (no fake floating indicators) with a beautiful, data‑rich UI.

![Android](https://img.shields.io/badge/Android-6.0%2B-3DDC84?logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-Compose-7F52FF?logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack-Compose-4285F4?logo=jetpackcompose&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-informational)

## ✨ Features

- 🚀 Real Speed Testing: Time‑bounded download and upload using real HTTP transfers
- 📊 Comprehensive Metrics: Download, upload, ping, jitter, and packet loss
- 🎨 Modern UI: Compose with enhanced cards, charts, dark theme, and animations
- 🔴 Live Readings: Instant Mbps updates during the test (sparkline + gauge)
- 📶 Network Validation: Detects connectivity and network type (WiFi/Mobile/Ethernet)
- 🔐 Privacy & Ads Settings: In‑app privacy policy, data details, ad preferences (DataStore)
- 🧰 Resilient: Clear errors, fallbacks, and robust handling

## 🧪 How It Works

### 🔁 Speed Test Process

1. ✅ Network Check: Verifies internet connectivity and detects network type
2. 🛰️ Ping Test: 3 HEAD requests to multiple hosts (httpbin, Google, Cloudflare); average reported
3. ⬇️ Download (Timed): Parallel streams continuously read for ~4.5s from large files (100MB); instant windows every ~200–500ms
4. ⬆️ Upload (Timed): Streams random data for ~2.5s to upload endpoints; instant windows every ~200ms
5. 📈 Results: Final Mbps = total bytes / time; plus jitter, packet loss, min/avg/max from live samples

### 🌐 Test Servers

The app uses reliable public endpoints:
- ⬇️ Download (large files): Cloudflare, Hetzner, ThinkBroadband, OVH
- ⬆️ Upload: Cloudflare `__up` (fallback: httpbin.org/post)
- 🛰️ Ping: httpbin.org, google.com, cloudflare.com

### 🔍 Metrics Explained

- Download Speed: How fast you can download data (Mbps)
- Upload Speed: How fast you can upload data (Mbps)
- Ping: Round‑trip time to test servers (ms)
- Jitter: Variation in ping times (ms)
- Packet Loss: Percentage of data lost during transmission

## 🧱 Technical Implementation

### 🏗️ Architecture

- MVVM Pattern: Uses ViewModel for state management
- Coroutines: Asynchronous speed testing operations
- StateFlow: Reactive UI state updates
- OkHttp: Low‑level HTTP client for speed testing
- DataStore: Persist user preferences (theme, privacy, ads)

### Key Components

- `SpeedTestService`: Core speed testing logic
- `SpeedTestViewModel`: State management and business logic
- `SpeedTestScreen`: UI components and user interaction
- `SpeedTestUiState`: Data class for UI state
- `Prefs`: DataStore wrapper for theme, privacy, and ad preferences

### Dependencies

- Jetpack Compose
- OkHttp
- ViewModel
- Coroutines

## 📲 Usage

1. Launch the app
2. Tap "Start" to begin the test
3. Watch live readings (gauge, sparkline, instant Mbps)
4. View results (download, upload, ping, jitter, packet loss)
5. Tap "Run again" to retest
6. Adjust preferences in Settings (theme, privacy policy, ad options)

## 🖼️ Screenshots

| Home (Gauge) | Live Sparkline | Results Cards |
| --- | --- | --- |
| ![Home](screenshots/1.png) | ![Chart](screenshots/2.png) | ![Results](screenshots/3.png) |

| Settings | Privacy | Status |
| --- | --- | --- |
| ![Settings](screenshots/4.png) | ![Privacy](screenshots/4.png) | ![Status](screenshots/3.png) |

## Requirements

- Android 6.0 (API level 23) or higher
- Internet connection
- Network permissions

## Permissions

- `INTERNET`: Required for speed testing
- `ACCESS_NETWORK_STATE`: Required for network availability checks

## 🛠️ Building the App

1. Clone the repository
2. Open in Android Studio
3. Sync Gradle files
4. Build and run on device or emulator

## ❓ Why Real Speed Testing?

Unlike floating speed indicators that show fake or estimated speeds, this app:

- Measures actual network performance through real data transfers
- Provides accurate metrics for network troubleshooting
- Helps diagnose connection issues with detailed measurements
- Gives reliable results for comparing different networks or ISPs

## 🔮 Future Enhancements

- Server selection and regional affinity
- Historical test results and charts
- 5G/NR‑specific hints and radio metrics
- Export/share results
- Advanced configuration (duration, streams, endpoints)

## 🧰 Troubleshooting

- Speed looks constant (e.g., 18.6 Mbps): We use large files and time‑bounded reads with multiple streams. If values are still flat, try switching networks, disabling VPNs, or adjusting parallel streams in code.
- Upload fails: The app falls back from Cloudflare to httpbin. Check network restrictions or captive portals.
- Ping is high: Some networks proxy HEAD requests; try again on a different network.

## 🤝 Contributing

Feel free to submit issues, feature requests, or pull requests to improve the app.

## 📜 License

This project is open source and available under the MIT License.
# Real Internet Speed Tester

A modern Android app built with Jetpack Compose that performs actual internet speed tests instead of showing fake floating speed indicators.

## Features

- **Real Speed Testing**: Time-bounded download and upload using real HTTP transfers
- **Comprehensive Metrics**: Download, upload, ping, jitter, and packet loss
- **Modern UI**: Jetpack Compose with enhanced cards, charts, and dark mode
- **Live Readings**: Instant Mbps updates during the test (sparkline + gauge)
- **Network Validation**: Verifies connectivity and network type (WiFi/Mobile/Ethernet)
- **Privacy & Ads Settings**: In-app privacy policy, data details, and ad preferences (DataStore)
- **Error Handling**: Clear messages for connectivity or server errors

## How It Works

### Speed Test Process

1. **Network Check**: Verifies internet connectivity and detects network type
2. **Ping Test**: 3 HEAD requests to multiple hosts (httpbin, Google, Cloudflare); average reported
3. **Download Test (Timed)**: Parallel HTTP streams continuously read data for ~4.5s from large files (100MB); instant speed windows every ~200–500ms
4. **Upload Test (Timed)**: Streams random data for ~2.5s to upload endpoints; instant speed reported on 200ms windows
5. **Results**: Final Mbps computed from total bytes/time; jitter and packet loss estimated; min/avg/max from live samples

### Test Servers

The app uses reliable public endpoints:
- Download (large files): Cloudflare, Hetzner, ThinkBroadband, OVH
- Upload: Cloudflare `__up` (fallback: httpbin.org/post)
- Ping: httpbin.org, google.com, cloudflare.com

### Metrics Explained

- **Download Speed**: How fast you can download data (Mbps)
- **Upload Speed**: How fast you can upload data (Mbps)
- **Ping**: Round-trip time to test servers (ms)
- **Jitter**: Variation in ping times (ms)
- **Packet Loss**: Percentage of data packets lost during transmission

## Technical Implementation

### Architecture

- **MVVM Pattern**: Uses ViewModel for state management
- **Coroutines**: Asynchronous speed testing operations
- **StateFlow**: Reactive UI state updates
- **OkHttp**: Low-level HTTP client for speed testing

### Key Components

- `SpeedTestService`: Core speed testing logic
- `SpeedTestViewModel`: State management and business logic
- `SpeedTestScreen`: UI components and user interaction
- `SpeedTestUiState`: Data class for UI state

### Dependencies

- **Jetpack Compose**: Modern UI toolkit
- **OkHttp**: HTTP client for network operations
- **ViewModel**: Lifecycle-aware state management
- **Coroutines**: Asynchronous programming

## Usage

1. Launch the app
2. Tap "Start" to begin the test
3. Watch live readings (gauge, sparkline, instant Mbps)
4. View results (download, upload, ping, jitter, packet loss)
5. Tap "Run again" to retest
6. Adjust preferences in Settings (theme, privacy policy, ad options)

## Requirements

- Android 6.0 (API level 23) or higher
- Internet connection
- Network permissions

## Permissions

- `INTERNET`: Required for speed testing
- `ACCESS_NETWORK_STATE`: Required for network availability checks

## Building the App

1. Clone the repository
2. Open in Android Studio
3. Sync Gradle files
4. Build and run on device or emulator

## Why Real Speed Testing?

Unlike floating speed indicators that show fake or estimated speeds, this app:

- **Measures actual network performance** through real data transfers
- **Provides accurate metrics** for network troubleshooting
- **Helps diagnose connection issues** with detailed measurements
- **Gives reliable results** for comparing different networks or ISPs

## Future Enhancements

- Server selection and regional affinity
- Historical test results and charts
- 5G/NR-specific hints and radio metrics
- Export/share results
- Advanced configuration (duration, streams, endpoints)

## Troubleshooting

- Speed looks constant (e.g., 18.6 Mbps): We use large files and time-bounded reads with multiple streams. If values are still flat, try switching networks, disabling VPNs, or adjusting parallel streams in code.
- Upload fails: The app falls back from Cloudflare to httpbin. Check network restrictions or captive portals.
- Ping is high: Some networks proxy HEAD requests; try again on a different network.

## Contributing

Feel free to submit issues, feature requests, or pull requests to improve the app.

## License

This project is open source and available under the MIT License.
