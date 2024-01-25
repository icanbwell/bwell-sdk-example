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
