# b.well SDK iOS Sample App

Native iOS application demonstrating the b.well Swift SDK across all supported API surfaces.

## Features

- **SwiftUI** with TabView navigation (Home, Health Records, Find Care, Profile)
- **All 65 SDK methods** exercised across 9 managers (health, activity, provider, questionnaire, search, connections, user, financial, device)
- **SwiftUI Charts** for vitals (line) and labs (bar) with reference range shading
- **Auto-login** from `.env` file in debug builds
- **Protocol-oriented DI** with no singletons

## Setup

1. Clone the repository
2. Copy `.env.example` to `.env` and fill in your credentials:
   ```
   BWELL_CLIENT_KEY=<your-base64-client-key>
   BWELL_JWT_TOKEN=<your-jwt-token>
   ```
3. Add the SDK package dependency:
   - In Xcode: File > Add Package Dependencies
   - URL: `https://github.com/icanbwell/bwell-sdk-swift-package`
   - Or for local development: File > Add Local... and select your local `bwell-sdk-swift` directory
4. Build and run on iOS Simulator (iPhone 16 recommended)

## Requirements

- Xcode (latest stable)
- iOS 15.0+ simulator or device
- Valid b.well API credentials (client key + JWT token)

## SDK Module

The SDK module is `BWell` (not `BWellSDK`):

```swift
import BWell
```
