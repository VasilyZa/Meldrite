plugins { id("net.fabricmc.fabric-loom-remap") version "1.17.21"; id("meldrite.java") }
dependencies {
    minecraft("com.mojang:minecraft:1.20.1")
    mappings("net.fabricmc:yarn:1.20.1+build.10:v2")
    modImplementation("net.fabricmc:fabric-loader:0.19.5")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.92.12+1.20.1")
    api(project(":bridge-spi"))
    api(project(":extension-api"))
}
