plugins { id("meldrite.java") }
dependencies {
    api(project(":bridge-spi"))
    api(project(":plugin-spi"))
    api(project(":adventure"))
    testImplementation(project(":testkit-runtime"))
}
