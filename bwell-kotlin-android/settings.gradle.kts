pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // BWell SDK Usage
        // mavenLocal() //uncomment after running `./gradlew publishToMavenLocal` in the SDK for testing
        maven {
            url = uri("https://artifacts.icanbwell.com/repository/bwell-public/")
        }
    }
}

// Health Sync on-device adapter local test overlay - see
// healthsync-local.settings.gradle.kts.example. Declares the adapter's own
// JFrog repo + the gated vendor repo (via a second, additive
// dependencyResolutionManagement { repositories { ... } } block inside the
// applied file) ONLY when a developer has created this file locally; a
// fresh clone declares no vendor/gated repository at all.
file("healthsync-local.settings.gradle.kts").takeIf { it.exists() }?.let { apply(from = it) }

rootProject.name = "MyTestApp"
include(":app")