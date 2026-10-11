pluginManagement {
    val localRepositories = java.util.Properties()
    val localFile = file(".gradle/meldrite/local-repositories.properties")
    if (localFile.isFile) localFile.inputStream().use(localRepositories::load)
    repositories {
        maven("https://maven.fabricmc.net/")
        val localMirror = localRepositories.getProperty("pluginRepository")
        if (localMirror != null) maven(localMirror)
        gradlePluginPortal()
    }
    includeBuild("build-logic")
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven("https://maven.fabricmc.net/")
        val localRepositories = java.util.Properties()
        val localFile = file(".gradle/meldrite/local-repositories.properties")
        if (localFile.isFile) localFile.inputStream().use(localRepositories::load)
        localRepositories.getProperty("centralRepository")?.let { maven(it) }
    }
}
rootProject.name = "Meldrite"
include(":foundation", ":bridge-spi", ":plugin-spi", ":testkit-runtime", ":testkit-contract")
project(":foundation").projectDir = file("modules/foundation")
project(":bridge-spi").projectDir = file("modules/bridge-spi")
project(":plugin-spi").projectDir = file("modules/plugin-spi")
project(":testkit-runtime").projectDir = file("testkit/runtime")
project(":testkit-contract").projectDir = file("build-tools/contracts")
include(":plugin-runtime", ":paper-1201")
project(":plugin-runtime").projectDir = file("modules/plugin-runtime")
project(":paper-1201").projectDir = file("adapters/paper-1.20.1")
include(":paper-1211")
project(":paper-1211").projectDir = file("adapters/paper-1.21.1")

include(":plugin-classloading")
project(":plugin-classloading").projectDir = file("modules/plugin-classloading")
include(":api-services", ":craft-common")
project(":api-services").projectDir = file("modules/api-services")
project(":craft-common").projectDir = file("modules/craft-common")

include(":registry")
project(":registry").projectDir = file("modules/registry")

include(":adventure")
project(":adventure").projectDir = file("modules/adventure")

include(":events")
project(":events").projectDir = file("modules/events")

include(":scheduler")
project(":scheduler").projectDir = file("modules/scheduler")

include(":commands")
project(":commands").projectDir = file("modules/commands")

include(":bootstrap-common", ":testkit-integration")
project(":bootstrap-common").projectDir = file("modules/bootstrap-common")
project(":testkit-integration").projectDir = file("testkit/integration")

include(":v1201-bridge", ":v1201-bootstrap", ":mc1201")
project(":v1201-bridge").projectDir = file("versions/v1_20_1/bridge")
project(":v1201-bootstrap").projectDir = file("versions/v1_20_1/bootstrap")
project(":mc1201").projectDir = file("distributions/mc-1.20.1")
include(":v1211-bridge", ":v1211-bootstrap", ":mc1211")
project(":v1211-bridge").projectDir = file("versions/v1_21_1/bridge")
project(":v1211-bootstrap").projectDir = file("versions/v1_21_1/bootstrap")
project(":mc1211").projectDir = file("distributions/mc-1.21.1")

include(":extension-api", ":remap-engine", ":compatibility-spi", ":compatibility-engine")
project(":extension-api").projectDir = file("modules/extension-api")
project(":remap-engine").projectDir = file("modules/remap-engine")
project(":compatibility-spi").projectDir = file("modules/compatibility-spi")
project(":compatibility-engine").projectDir = file("modules/compatibility-engine")
