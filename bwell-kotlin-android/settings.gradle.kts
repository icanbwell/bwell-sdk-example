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
        maven {
            url = uri("https://bwell-maven-repo.s3.amazonaws.com/")
        }
    }
}

rootProject.name = "MyTestApp"
include(":app")
