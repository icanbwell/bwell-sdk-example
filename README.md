# bwell-sdk-example
This repository contains a Kotlin-based example application designed to demonstrate the capabilities and usage of the BWell SDK. It serves as a practical guide for developers looking to integrate BWell services into their own applications. Future examples for other languages and platforms may be added.

## Pre-requisites
1. Install Android Studio (https://developer.android.com/studio)
   1. After download, run it and choose to install the Android SDK 
2. Open the Project in Android Studio
3. Click the Play button in the top right
4. Add OAuthToken (see below)

## Add OAuth Token

1. Create an `env.properties` file in the `./src/main/assets` directory.
2. paste this into the first line: `authToken=auth_token`
3. Replace `auth_token` with your intended JWT token
