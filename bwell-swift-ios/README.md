# bwell SDK iOS Sample App

This repository contains the example application for iOS demonstrating the capabilities and usage of the b.well SDK.

**note:** The Swift SDK is currently undergoing development and hasn't been released for GA yet so the sample app is not ready for public consumption.

**Features:**
- Native UI with SwiftUI
- Authentication flow and credential's presistence with API Key followed Email & Password or JWT Token.
- Keychain for user's credentials persistence.
- View and update user's profile information.
- Health data retrieval.
- View, Search, and crate connections.


## Set up Instructions
1. Clone the repository.
2. Add the SDK as a package dependency
    a. In Xcode go to File > Add Package Dependencies
    b. In the search bar enter the SDK's name and add it to the project.
3. Once the package has been added in the project navigator locate the packge you just added, right click in "Package Dependencies" and click in "Reset Package Caches" and then click in "Resolve Package Versions"
4. Run the app 


Each example includes its own detailed README with specific setup instructions, prerequisites, and usage guidelines.

## Common Requirements

iOS Sample App require:
- Latest MacOS version
- Valid b.well API credentials
- Authentication tokens (JWT)
- Network connectivity for API calls
