plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // Firebase notifications - uncomment to enable push notifications
    // Requires google-services.json file to be configured
    // id("com.google.gms.google-services")
}

// Health Sync on-device adapter local test overlay - see
// healthsync-local.app.gradle.kts.example. Adds the published adapter
// coordinate (and its AndroidManifest merge) ONLY when a developer has
// created this file locally; absent by default, so this file is never
// committed and a fresh clone never declares the adapter dependency.
rootProject.file("healthsync-local.app.gradle.kts").takeIf { it.exists() }?.let { apply(from = it) }

configurations.all {
    resolutionStrategy.cacheChangingModulesFor(0, TimeUnit.SECONDS)
}

android {
    namespace = "com.bwell.sampleapp"
    // 36 is required by the Health Sync on-device adapter's AndroidX Health
    // Connect dependency (confirmed via its AAR metadata: minCompileSdk=36).
    compileSdk = 36

    sourceSets {
        // Health Sync on-device adapter bridge: exactly one of these two
        // sibling source dirs compiles, chosen by whether the untracked
        // local overlay directory exists. This is the Gradle analogue of
        // Swift's `#if canImport(BWellHealthSyncAdapterAggregator)` - one
        // call site (HealthSyncAdapterBridge), two mutually exclusive
        // implementations, resolved here instead of at compile time, so a
        // committed clone never even sees the adapter-configuring code.
        // See healthsync-local.app.gradle.kts.example for how to opt in.
        val healthSyncLocalSrc = file("src/healthSyncLocal/java")
        val healthSyncLocalConfigured = healthSyncLocalSrc.exists()
        getByName("main").java.srcDir(
            if (healthSyncLocalConfigured) healthSyncLocalSrc else file("src/healthSyncNeutral/java")
        )

        // Same swap for the manifest fragment carrying the adapter's Health
        // Connect <queries>/permissions-rationale entries - the debug variant
        // manifest merges with main, so the committed manifest never
        // mentions any of it. Kept here (not in the applied overlay script)
        // because scripts loaded via apply(from=) don't get AGP's
        // precompiled `android { }` type-safe accessors.
        getByName("debug").manifest.srcFile(
            if (healthSyncLocalConfigured) {
                file("src/healthSyncLocal/AndroidManifest.xml")
            } else {
                file("src/healthSyncNeutral/AndroidManifest.xml")
            }
        )
    }

    defaultConfig {
        applicationId = "com.bwell.sampleapp"
        // 29 is required by the Health Sync adapter AAR's own manifest
        // (uses-sdk minSdkVersion=29) - this is an app-wide floor, not
        // scoped to the Health Sync feature alone.
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
        // The Health Sync / adapter AARs ship Kotlin 1.9 metadata; without
        // this the compiler refuses to read them.
        freeCompilerArgs += "-Xskip-metadata-version-check"
    }
    buildFeatures {
        viewBinding = true
        dataBinding = true
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            animationsDisabled = true
        }
    }
}



dependencies {

    // BWell SDK Usage
    implementation("com.bwell:bwell-sdk-kotlin:1.20.0")
    // Vendor-neutral Health Sync surface (connect/sync/read on-device health
    // data). No vendor dependency, no gated repo needed - this artifact is
    // fully public. The on-device adapter that actually talks to a vendor
    // SDK is deliberately NOT declared here; see healthsync-local.app.gradle.kts.example
    // for how to add it locally.
    implementation("com.bwell:bwell-sdk-kotlin-healthsync:1.20.0")

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    // Health Sync Playground: real StateFlow-to-Compose lifecycle wiring
    // (this app's only other Compose screen, the orphaned MainActivity,
    // reads state directly rather than via a ViewModel) and the copy-to-
    // clipboard icon (not in material3's bundled icon set).
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation("androidx.compose.material:material-icons-extended")
    // Playground-only: pretty-printed JSON for endpoint responses, for
    // visibility while testing - not exposed as SDK surface anywhere.
    // Matches the internal healthsync-sample's own use of the same library.
    implementation("com.google.code.gson:gson:2.10.1")
    implementation ("androidx.core:core-ktx:1.12.0")
    implementation ("androidx.appcompat:appcompat:1.6.1")
    implementation ("com.google.android.material:material:1.11.0")
    implementation ("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation ("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("io.coil-kt:coil:2.2.2")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.6")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.6")
    implementation("androidx.paging:paging-runtime-ktx:3.2.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("androidx.annotation:annotation:1.7.1")
    // Firebase notifications - uncomment to enable push notifications
    // Requires google-services.json file to be configured
    // implementation(platform("com.google.firebase:firebase-bom:32.7.1"))
    // implementation("com.google.firebase:firebase-messaging")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.11.1")
    // HealthSyncPlaygroundViewModelTest/HealthSyncDashboardViewModelTest use
    // runTest/StandardTestDispatcher to drive viewModelScope deterministically.
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("androidx.test.ext:junit:1.1.5")
    testImplementation("androidx.test.espresso:espresso-core:3.5.1")
    testImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.10.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    implementation(kotlin("reflect"))
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.5.1")
    implementation("com.google.android.gms:play-services-location:21.1.0")

}
