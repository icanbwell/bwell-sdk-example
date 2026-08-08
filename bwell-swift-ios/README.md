# b.well SDK iOS Sample App

Native iOS application demonstrating the b.well Swift SDK across all supported API surfaces.

## Features

- **SwiftUI** with TabView navigation (Home, Health Records, Find Care, Profile)
- **All 65 SDK methods** exercised across 9 managers (health, activity, provider, questionnaire, search, connections, user, financial, device)
- **SwiftUI Charts** for vitals (line) and labs (bar) with reference range shading
- **Auto-login** from `.env` file in debug builds
- **Protocol-oriented DI** with no singletons
- **Feature playgrounds** — a scrollable, per-endpoint test bench for exercising an SDK
  module method-by-method (see [Playgrounds](#playgrounds) below). Health Sync is the first
  one, reachable from Health Records → Developer.

## Setup

1. Clone the repository
2. Copy `.env.example` to `.env` and fill in your credentials:
   ```
   BWELL_CLIENT_KEY=<your-base64-client-key>
   BWELL_JWT_TOKEN=<your-jwt-token>
   ```
3. Open `bwell-swift-ios.xcodeproj` in Xcode. The SDK package dependency is already
   configured in the project — no manual "Add Package Dependency" step needed. Xcode
   resolves it automatically on first open.
4. Build and run on iOS Simulator (iPhone 16 recommended) or a real device.

## Requirements

- Xcode (latest stable)
- iOS 17.0+ simulator or device
- Valid b.well API credentials (client key + JWT token)

## SDK Module

```swift
import BWellSDK
```

## Playgrounds

A "Playground" is a self-contained demo screen for one SDK module: a scrollable list of
independent cards, one per endpoint, each with its own Run button, plain-language
explanation, and inline result. Reached from Health Records → Developer.

The pattern lives in `bwell-swift-ios/Playground/` and is meant to be reused, not
re-implemented, per feature:
- Conform your endpoint enum to the `PlaygroundEndpoint` protocol.
- Render each card with the shared `PlaygroundCard` view.
- Register your screen once in `PlaygroundRegistry.all` (see
  `HealthSync/HealthSyncPlaygroundFeature.swift` for a working example).

Registering a feature this way is the *only* wiring it needs — `Router.swift`,
`BrowseView.swift`, and `MainTabView.swift` never need another edit for a new playground.

### Health Sync

The first playground, ported from the internal b.well Health Sync sample. Some of its
endpoints (on-device connect/sync) require a third-party health-data provider integration.
Credential provisioning for that integration is currently under consideration — those
endpoints show a "Health Sync Credentials Required" state until that's finalized. Every
other endpoint works against the live API today.
