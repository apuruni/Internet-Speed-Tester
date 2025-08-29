# Real Internet Speed Tester

A modern Android app built with Jetpack Compose that performs actual internet speed tests instead of showing fake floating speed indicators.

## Features

- **Real Speed Testing**: Performs actual download and upload speed tests using real network requests
- **Comprehensive Metrics**: Measures download speed, upload speed, ping, jitter, and packet loss
- **Modern UI**: Built with Jetpack Compose for a beautiful, responsive interface
- **Real-time Progress**: Shows live progress during speed tests
- **Network Validation**: Checks network availability before running tests
- **Error Handling**: Provides clear error messages for network issues

## How It Works

### Speed Test Process

1. **Network Check**: Verifies internet connectivity before starting
2. **Ping Test**: Measures latency to test servers (5 samples)
3. **Download Test**: Downloads test files of varying sizes (1MB, 2MB, 5MB) to measure download speed
4. **Upload Test**: Uploads test data to measure upload speed
5. **Results Calculation**: Computes average speeds and network quality metrics

### Test Servers

The app uses reliable test servers:
- **httpbin.org**: For ping tests and upload tests
- **Multiple file sizes**: For accurate download speed measurement

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
- **OkHttp**: Network operations for speed testing

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

1. **Launch the app** - The main screen shows a circular progress indicator
2. **Tap "START TEST"** - Begins the speed test process
3. **Monitor progress** - Watch the circular progress indicator fill up
4. **View results** - See comprehensive speed test results
5. **Run new test** - Tap "NEW TEST" to perform another speed test

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

- Server selection options
- Historical test results
- Network type detection (WiFi, 4G, 5G)
- Export results functionality
- Custom test configurations

## Contributing

Feel free to submit issues, feature requests, or pull requests to improve the app.

## License

This project is open source and available under the MIT License.
