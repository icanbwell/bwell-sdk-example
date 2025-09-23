# bwell-sdk-kotlin
This document outlines the usage of b.well's Kotlin SDK in the context of an Android app.

## Quick Start (No Notifications)
The app works out of the box without Firebase configuration. Push notifications are **optional** and disabled by default.

## Optional: Firebase Push Notifications Setup

**Note**: Push notifications are currently disabled by default. Follow these steps only if you want to enable them.

### Step 1: Configure Firebase
1. Create a Firebase project at [Firebase Console](https://console.firebase.google.com/)
2. Add an Android app to your Firebase project with package name: `com.bwell.sampleapp`
3. Download the `google-services.json` file

### Step 2: Add Configuration File
1. Copy the downloaded `google-services.json` file to `app/google-services.json`
2. You can use the template file `app/google-services.json.template` as a reference

### Step 3: Enable Firebase Dependencies
1. In `app/build.gradle.kts`, uncomment the Firebase plugin:
   ```kotlin
   plugins {
       id("com.android.application")
       id("org.jetbrains.kotlin.android")
       id("com.google.gms.google-services") // Uncomment this line
   }
   ```

2. In the same file, uncomment the Firebase dependencies:
   ```kotlin
   implementation(platform("com.google.firebase:firebase-bom:32.7.1"))
   implementation("com.google.firebase:firebase-messaging")
   ```

### Step 4: Enable Firebase Service
1. In `app/src/main/AndroidManifest.xml`, uncomment the Firebase service configuration:
   ```xml
   <meta-data
       android:name="com.google.android.gms.version"
       android:value="@integer/google_play_services_version" />
   <service
       android:name=".utils.BWellFirebaseMessagingService"
       android:exported="false">
       <intent-filter>
           <action android:name="com.google.firebase.MESSAGING_EVENT" />
       </intent-filter>
   </service>
   ```

2. Rename the disabled Firebase service file:
   ```bash
   mv app/src/main/java/com/bwell/sampleapp/utils/BWellFirebaseMessagingService.kt.disabled \
      app/src/main/java/com/bwell/sampleapp/utils/BWellFirebaseMessagingService.kt
   ```

### Step 5: Enable Device Token Registration and Notifications
1. In `LoginFragment.kt`, uncomment the Firebase imports and device token registration code
2. In `NavigationActivity.kt`, uncomment the notification permission and device token methods
3. Look for the commented sections marked with "Firebase notifications"

**Architecture Note**: The notification logic is consolidated into `NotificationHandler.kt` which works with or without Firebase. This keeps Firebase-specific code minimal and isolated.

## Architecture Overview

### Notification System Design
The notification system has been designed with a clean separation of concerns:

1. **`NotificationHandler.kt`** - Core notification logic that works with or without Firebase
   - Handles notification display and formatting
   - Processes deep links from notifications
   - Manages navigation to appropriate screens
   - Can be used independently of Firebase

2. **`BWellFirebaseMessagingService.kt`** - Minimal Firebase wrapper (when enabled)
   - Receives Firebase push notifications
   - Delegates to `NotificationHandler` for processing
   - Only contains Firebase-specific code

3. **`NavigationActivity.kt`** - Main app activity with minimal notification coupling
   - Uses `NotificationHandler` for deep link processing
   - Firebase-specific code is commented out by default
   - Maintains core navigation functionality independent of notifications

This architecture allows the app to:
- Work immediately without Firebase setup
- Have minimal commented code when notifications are disabled
- Easy to enable notifications by uncommenting clearly marked sections
- Keep notification logic centralized and reusable

**Security Note**: The `google-services.json` file contains sensitive Firebase configuration and is excluded from version control for security reasons.

## Reference the Maven Repository
In the application's settings.gradle file (or settings.gradle.kts if using Kotlin with gradle) add the following:

```
repositories {
    // Other repositories
    google()
    mavenCentral()

    // BWell's SDK Maven Repository
    maven {
        url = uri("https://artifacts.icanbwell.com/repository/bwell-public/")
    }
}
```

## Add the SDK Dependency
In the app's build.grade file (or build.gradle.kts if using Kotlin with gradle) add the following dependency:

```
dependencies {

    // BWell SDK Usage
    implementation("com.bwell:bwell-sdk-kotlin:1.0.0-SNAPSHOT")

    ...
}
```

In the same file ensure that the minSdk level is set to 24.

```
 defaultConfig {
        applicationId = "com.bwell.sampleapp"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        ...
    }
```

## Import the SDK and Use It
```
// BWell SDK Usage
import com.bwell.sampleapp.singletons.BWellSdk

val helloStr = BWellSdk.hello()
```

## Project Structure
[AndroidManifest.xml](bwell-kotlin-android/app/src/main/java/com/bwell/sampleapp/activities/ui) is the main config.  This specifies the main activity.

The main activity is [NavigationActivity](bwell-kotlin-android/app/src/main/java/com/bwell/sampleapp/activities/NavigationActivity.kt).

The layout for Navigation Activity is [layout/activity_navigation.xml](bwell-kotlin-android/app/src/main/res/layout/activity_navigation.xml) .

This contains the app_bar_navigation whose layout is defined in [layout/app_bar_navigation.xml](bwell-kotlin-android/app/src/main/res/layout/app_bar_navigation.xml)

This contains content_navigation defined in [layout/content_navigation.xml](bwell-kotlin-android/app/src/main/res/layout/content_navigation.xml)

This includes the fragment mobile_navigation defined in [navigation/mobile_navigation.xml](bwell-kotlin-android/app/src/main/res/navigation/mobile_navigation.xml).  This contains the fragments for all the main pages:
1. Login: [layout/fragment_login.xml](bwell-kotlin-android/app/src/main/res/layout/fragment_login.xml)
2. Home: [layout/fragment_home.xml](bwell-kotlin-android/app/src/main/res/layout/fragment_home.xml)
3. Data Connections: [layout/fragment_data_connections.xml](bwell-kotlin-android/app/src/main/res/layout/fragment_data_connections.xml)
4. Health Summary: [layout/fragment_gallery.xml](bwell-kotlin-android/app/src/main/res/layout/fragment_gallery.xml)
5. Health Journey: [layout/fragment_gallery.xml](bwell-kotlin-android/app/src/main/res/layout/fragment_gallery.xml)
6. Insurance: [layout/fragment_insurance_view.xml](bwell-kotlin-android/app/src/main/res/layout/fragment_insurance_view.xml)
7. Labs: [layout/fragment_labs.xml](bwell-kotlin-android/app/src/main/res/layout/fragment_labs.xml)
8. Medicines: [layout/fragment_medicines.xml](bwell-kotlin-android/app/src/main/res/layout/fragment_medicines.xml)
9. Profile: [layout/fragment_profile.xml](bwell-kotlin-android/app/src/main/res/layout/fragment_profile.xml)


### Data Connections
The code for this fragment is: [activities/ui/data_connections/DataConnectionsFragment.kt](bwell-kotlin-android/app/src/main/java/com/bwell/sampleapp/activities/ui/data_connections/DataConnectionsFragment.kt).

When the category is clicked, `setDataConnectionsCategoryAdapter()` function chooses the appropriate fragment to display.
1. Clinics Search: [activities/ui/data_connections/clinics/ClinicsSearchFragment.kt](bwell-kotlin-android/app/src/main/java/com/bwell/sampleapp/activities/ui/data_connections/clinics/ClinicsSearchFragment.kt)
   1. [layout/fragment_data_connections_clinics.xml](bwell-kotlin-android/app/src/main/res/layout/fragment_data_connections_clinics.xml)
2. Provider Search: [activities/ui/data_connections/providers/ProviderSearchFragment.kt](bwell-kotlin-android/app/src/main/java/com/bwell/sampleapp/activities/ui/data_connections/providers/ProviderSearchFragment.kt)
3. Labs Search: [activities/ui/data_connections/labs/LabsSearchFragment.kt](bwell-kotlin-android/app/src/main/java/com/bwell/sampleapp/activities/ui/data_connections/labs/LabsSearchFragment.kt)
   1. Layout: [layout/fragment_data_connections_labs.xml](bwell-kotlin-android/app/src/main/res/layout/fragment_data_connections_labs.xml)


### Select Connection
When the user clicks on an entry in the Clinic Search, they are navigated to:
[layout/fragment_organization_info_view.xml](bwell-kotlin-android/app/src/main/res/layout/fragment_organization_info_view.xml)


### View Hierarchy
content_navigation
-> one of the fragments in mobile_navigation.xml is selected at a time
-> when layout/fragment_data_connections.xml is selected
-> -> Includes the RecyclerView rv_suggested_data_connections

## Summary

### Default Configuration (Recommended for Testing)
- The app works immediately without any Firebase setup
- Push notifications are disabled by default
- No `google-services.json` file required
- No sensitive credentials exposed in public repository

### With Push Notifications (Optional)
- Requires Firebase project setup
- Requires `google-services.json` configuration file
- Follow the "Optional: Firebase Push Notifications Setup" section above
- Uncomment Firebase-related code in multiple files

This approach ensures the sample app can be run immediately while keeping sensitive configuration optional and secure.