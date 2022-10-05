rootProject.name = "lib-khash"

pluginManagement {
    // builds before buildSrc extensions are available :(
    repositories {
        gradlePluginPortal()
    }
}

include("hash")
