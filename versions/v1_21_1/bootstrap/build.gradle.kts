plugins { id("net.fabricmc.fabric-loom-remap") version "1.17.21"; id("meldrite.java") }

java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))
tasks.processResources {
    inputs.property("meldriteVersion", project.version.toString())
    filesMatching("fabric.mod.json") { expand("version" to project.version.toString()) }
}

loom {
    serverOnlyMinecraftJar()
    runs {
        named("server") {
            val configuredDirectory = providers.gradleProperty("meldrite.serverDir")
            if (configuredDirectory.isPresent) runDirectory.set(project.file(configuredDirectory.get()))
        }
    }
}
tasks.named<JavaExec>("runServer") {
    providers.gradleProperty("meldrite.gameJavaHome").orNull?.let { setExecutable("$it/bin/java") }
    jvmArgs("-Xmx1G", "-XX:ActiveProcessorCount=4")
}
dependencies {
    minecraft("com.mojang:minecraft:1.21.1")
    mappings("net.fabricmc:yarn:1.21.1+build.3:v2")
    modImplementation("net.fabricmc:fabric-loader:0.19.5")
    modImplementation("net.fabricmc.fabric-api:fabric-lifecycle-events-v1:2.3.12+6c1df3606a")
    implementation(project(":v1211-bridge")) { isTransitive = false }
    implementation(project(":bridge-spi"))
    implementation(project(":paper-1211"))
    implementation(project(":adventure"))
    implementation(project(":bootstrap-common"))
    implementation(project(":plugin-classloading"))
    listOf(":foundation", ":bridge-spi", ":plugin-spi", ":plugin-runtime", ":plugin-classloading", ":bootstrap-common", ":api-services", ":registry", ":craft-common", ":events", ":scheduler", ":paper-1211", ":adventure").forEach { include(project(it)) }
    include(project(":v1211-bridge"))
}
tasks.withType<JavaCompile>().configureEach { options.release.set(21) }
