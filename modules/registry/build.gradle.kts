plugins { id("meldrite.java") }
dependencies {
    api(project(":bridge-spi"))
    testImplementation(project(":testkit-runtime"))
}
