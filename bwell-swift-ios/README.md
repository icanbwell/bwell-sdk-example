# bwell SDK iOS Sample App

This repository contains the example application for iOS demonstrating the capabilities and usage of the b.well Swift SDK.

**Note:** The Swift SDK is currently undergoing development and has not been released for GA yet, so the sample app is not ready for public consumption.

## Features Demonstrated

### SDK Lifecycle
- **Initialization** with client key, log level, token storage, and telemetry configuration
- **Authentication** via email/password credentials (also supports OAuth tokens and auth codes)
- **Consent creation** (Terms of Service) after authentication
- **Session validation** with retry logic
- **Credential persistence** using Keychain

### HealthDataManager
- **Health Summary** -- aggregated record counts across all FHIR resource categories (allergies, conditions, encounters, immunizations, labs, medications, procedures, vital signs, care plans)
- **Group views** -- grouped health data with drill-down into individual records
- **Detail sheets** -- detailed views for each health record type
- **Goals** -- patient health goals with lifecycle and achievement status (EA-2153)

### SearchManager
- **Search Providers** -- find healthcare providers and facilities by name, location, and specialty
- **Submit Provider for Review** -- request addition of a new provider to the directory
- **OAuth connection flow** -- web-based OAuth for provider connections

### ConnectionManager
- **Member Connections** -- view connected healthcare providers with sync status
- **Create Connection** -- establish a new provider connection
- **Delete/Disconnect** -- manage existing connections
- **OAuth and basic authentication** connection types

### FinancialManager
- **Insurance Coverages** -- view coverage records with period and subscriber details
- **Explanation of Benefits** -- view EOB records with billable periods and claim details

### UserManager
- **Profile retrieval and update** -- get and modify authenticated user profile
- **Consent management** -- create and retrieve user consents
- **Identity verification** -- IAL2 verification flow
- **Account deletion** -- full account removal

### DeviceManager
- **Push notification registration** -- register iOS devices for notifications

### Error Handling
- Typed `SDKError` enum with user-friendly messages and recovery suggestions
- State machine (`SDKState`) tracking SDK lifecycle
- Loading state management throughout all views

## Architecture

The example app uses MVVM with SwiftUI and a navigation router:

- `BWellSDKManager` -- SDK lifecycle manager with typed accessors for each manager
- `NavigationRouter` -- stack-based navigation with `NavigationStack`
- `SideMenuView` -- side navigation for main sections
- `HealthSummaryViewModel` -- demonstrates all health data operations including groups
- `FinancialViewModel` -- demonstrates coverage and EOB retrieval
- `KeychainTokenStorageAdapter` -- secure token persistence conforming to SDK's `TokenStorage`

## Set up Instructions

1. Clone the repository
2. Ensure the SDK source is available at `../../bwell-sdk/bwell-sdk-swift` (for local development) or configure SPM to use the package URL
3. Open `bwell-swift-ios.xcodeproj` in Xcode
4. Go to File > Packages > Resolve Package Versions
5. Build and run the app on a simulator or device

## Common Requirements

- macOS 13+ with Xcode 15+
- iOS 15+ deployment target
- Valid b.well API credentials (client key, email, password)
- Network connectivity for API calls
