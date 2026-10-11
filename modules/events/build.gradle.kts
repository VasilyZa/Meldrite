plugins { id("meldrite.java") }
dependencies {
    api(project(":bridge-spi"))
    api(project(":plugin-spi"))
    testImplementation(project(":testkit-runtime"))
    testImplementation(project(":api-services"))
}
