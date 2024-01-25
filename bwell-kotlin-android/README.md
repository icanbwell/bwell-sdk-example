# bwell-sdk-kotlin
This document outlines the usage of b.well's Kotlin SDK in the context of an Android app.

## Reference the Maven Repository
In the application's settings.gradle file (or settings.gradle.kts if using Kotlin with gradle) add the following:

```
repositories {
    // Other repositories
    google()
    mavenCentral()

    // BWell's SDK Maven Repository
    maven {
        url = uri("https://bwell-maven-repo.s3.amazonaws.com/")
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
import com.bwell.BWellSdk

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

