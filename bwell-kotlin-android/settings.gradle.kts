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
            setUrl(uri("s3://bwell-maven-repo/"))
            credentials<AwsCredentials>(AwsCredentials::class.java) {
                accessKey = System.getenv("AWS_ACCESS_KEY_ID")
                secretKey = System.getenv("AWS_SECRET_ACCESS_KEY")
            }
        }
    }
}

rootProject.name = "MyTestApp"
include(":app")
