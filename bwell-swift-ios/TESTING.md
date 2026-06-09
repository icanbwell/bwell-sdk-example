# Testing the b.well Swift iOS Example App

## Prerequisites

- Xcode with iOS Simulator
- Node.js with `jsonwebtoken` available (for dev JWT generation)
- A local EC (ES256) signing key for dev JWTs, obtained from the
  `bwell-identity-gateway` maintainers and stored **outside** this repo. Never
  commit the key.
- A `.env` file: copy `bwell-swift-ios/.env.example` → `.env` and fill in your
  values. `.env` is gitignored and must never be committed.

## Simulator Setup

The default test simulator is iPhone 16 (iOS 18.6):
```
Device UDID: 0C084754-DF2E-4CC9-960C-44EB0B7C1629
```

## Build & Install

```bash
cd bwell-swift-ios   # from the bwell-sdk-example repo root

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

> All credentials come from your local, gitignored `.env` (copy
> `bwell-swift-ios/.env.example` → `.env` first). **Never** hardcode client keys,
> signing keys, or tokens in this repo — they are real and must stay local.
> See `.env.example` for the variables referenced below.

### Step 1: Paste the Client Key

Copy your client key to the **simulator** clipboard:
```bash
# Load your local credentials
set -a; source bwell-swift-ios/.env; set +a

echo -n "$BWELL_CLIENT_KEY" | xcrun simctl pbcopy "0C084754-DF2E-4CC9-960C-44EB0B7C1629"
```

Tap the API Key field in the app → Paste (long-press or Cmd+V) → Tap **Submit**.

### Step 2: Generate and Paste a JWT

Generate a fresh JWT (valid 24 hours) signed with your **local** dev key and copy
it to the **simulator** clipboard. The signing key is read from a path on your
machine (`BWELL_DEV_SIGNING_KEY_PATH`); it is never stored in this repo:
```bash
set -a; source bwell-swift-ios/.env; set +a

node -e "
const jwt = require('jsonwebtoken');
const fs  = require('fs');
const pk  = fs.readFileSync(process.env.BWELL_DEV_SIGNING_KEY_PATH, 'utf8');
console.log(jwt.sign(
  { guid: process.env.BWELL_DEV_GUID, otid: false,
    iat: Math.floor(Date.now() / 1000), exp: Math.floor(Date.now() / 1000) + 86400 },
  pk, { algorithm: 'ES256', keyid: process.env.BWELL_DEV_SIGNING_KID }));
" | xcrun simctl pbcopy "0C084754-DF2E-4CC9-960C-44EB0B7C1629"
```

Tap the JWT Token field in the app → Paste → Tap **Login**.

> **Important:** Use `xcrun simctl pbcopy <UDID>` to copy to the *simulator* clipboard.
> Regular `pbcopy` copies to the *macOS* clipboard, which doesn't paste into the simulator app.

### One-Liner (both steps)

```bash
# Loads your local .env, then copies the client key to the simulator clipboard
set -a; source bwell-swift-ios/.env; set +a
echo -n "$BWELL_CLIENT_KEY" | xcrun simctl pbcopy "0C084754-DF2E-4CC9-960C-44EB0B7C1629"

# After submitting the client key, generate + copy a JWT, then paste and tap Login
node -e "const jwt=require('jsonwebtoken');const fs=require('fs');const pk=fs.readFileSync(process.env.BWELL_DEV_SIGNING_KEY_PATH,'utf8');console.log(jwt.sign({guid:process.env.BWELL_DEV_GUID,otid:false,iat:Math.floor(Date.now()/1000),exp:Math.floor(Date.now()/1000)+86400},pk,{algorithm:'ES256',keyid:process.env.BWELL_DEV_SIGNING_KID}));" | xcrun simctl pbcopy "0C084754-DF2E-4CC9-960C-44EB0B7C1629"
```

## Test User Data (seeded sandbox user)

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
