# Testing the b.well Swift iOS Example App

## Prerequisites

- Xcode with iOS Simulator
- Node.js (for JWT generation)
- The `bwell-identity-gateway` repo cloned at `/Users/billfield/dev/bwell-identity-gateway`

## Simulator Setup

The default test simulator is iPhone 16 (iOS 18.6):
```
Device UDID: 0C084754-DF2E-4CC9-960C-44EB0B7C1629
```

## Build & Install

```bash
cd /Users/billfield/dev/bwell-sdk-example/bwell-swift-ios

# Build
xcodebuild build -scheme bwell-swift-ios -sdk iphonesimulator \
  -destination 'platform=iOS Simulator,name=iPhone 16,OS=18.6' 2>&1 | grep -E "error:|BUILD"

# Find the app
APP=$(find ~/Library/Developer/Xcode/DerivedData/bwell-swift-ios-*/Build/Products/Debug-iphonesimulator \
  -name "bwell-swift-ios.app" -not -path "*/Index.noindex/*" -maxdepth 1 | head -1)

# Install and launch
xcrun simctl install "0C084754-DF2E-4CC9-960C-44EB0B7C1629" "$APP"
xcrun simctl terminate "0C084754-DF2E-4CC9-960C-44EB0B7C1629" "com.bwell.sdkSampleApp" 2>/dev/null
xcrun simctl launch "0C084754-DF2E-4CC9-960C-44EB0B7C1629" "com.bwell.sdkSampleApp"
```

## Authentication

### Step 1: Paste the Client Key

Copy the Samsung client-sandbox key to the **simulator** clipboard:
```bash
echo -n "***REMOVED***" | xcrun simctl pbcopy "0C084754-DF2E-4CC9-960C-44EB0B7C1629"
```

Tap the API Key field in the app → Paste (long-press or Cmd+V) → Tap **Submit**.

### Step 2: Generate and Paste a JWT

Generate a fresh JWT (valid 24 hours) and copy it to the **simulator** clipboard:
```bash
node -e "
const jwt = require('/Users/billfield/dev/bwell-identity-gateway/node_modules/jsonwebtoken');
const pk = \`-----BEGIN EC PRIVATE KEY-----
***REMOVED***
***REMOVED***
***REMOVED***
-----END EC PRIVATE KEY-----\`;
console.log(jwt.sign({guid:'***REMOVED***_xxHi79qUfx5JGHWkGdet/g==',otid:false,
  exp:Math.floor(Date.now()/1000)+86400,iat:Math.floor(Date.now()/1000)},pk,
  {algorithm:'ES256',keyid:'***REMOVED***'}));
" | xcrun simctl pbcopy "0C084754-DF2E-4CC9-960C-44EB0B7C1629"
```

Tap the JWT Token field in the app → Paste → Tap **Login**.

> **Important:** Use `xcrun simctl pbcopy <UDID>` to copy to the *simulator* clipboard.
> Regular `pbcopy` copies to the *macOS* clipboard, which doesn't paste into the simulator app.

### One-Liner (both steps)

```bash
# Step 1 - run this, then paste in API Key field and tap Submit
echo -n "***REMOVED***" | xcrun simctl pbcopy "0C084754-DF2E-4CC9-960C-44EB0B7C1629"

# Step 2 - run this after submitting the client key, then paste in JWT field and tap Login
node -e "const jwt=require('/Users/billfield/dev/bwell-identity-gateway/node_modules/jsonwebtoken');const pk=\`-----BEGIN EC PRIVATE KEY-----\n***REMOVED***\n***REMOVED***\n***REMOVED***\n-----END EC PRIVATE KEY-----\`;console.log(jwt.sign({guid:'***REMOVED***_xxHi79qUfx5JGHWkGdet/g==',otid:false,exp:Math.floor(Date.now()/1000)+86400,iat:Math.floor(Date.now()/1000)},pk,{algorithm:'ES256',keyid:'***REMOVED***'}));" | xcrun simctl pbcopy "0C084754-DF2E-4CC9-960C-44EB0B7C1629"
```

## Test User Data (samsung-client-sandbox)

| Category | Count |
|---|---|
| Allergies | 7 |
| Care Plans | 2 |
| Conditions | 4 |
| Encounters | 8 |
| Immunizations | 1 |
| Labs | 12 |
| Medications | 5 |
| Procedures | 2 |
| Vital Signs | 23 |

## Screenshots

```bash
xcrun simctl io "0C084754-DF2E-4CC9-960C-44EB0B7C1629" screenshot /tmp/screen.png
```

## Debug Logging

Health data fetch operations log to the system console with the `[HealthData]` prefix.
Filter in Console.app or:
```bash
xcrun simctl spawn "0C084754-DF2E-4CC9-960C-44EB0B7C1629" log stream --predicate 'eventMessage contains "[HealthData]"'
```
