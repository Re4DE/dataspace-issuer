rootProject.name = "dataspace-issuer"

// this is needed to have access to snapshot builds of plugins
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
        maven {
            url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
        }
    }
}

// add dependencies
include(":runtimes:issuer")
include(":runtimes:local-dev")

include(":extensions:issuer-seed")
include(":extensions:membership-issuance-seed")