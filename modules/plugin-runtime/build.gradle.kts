plugins { id("meldrite.java") }
dependencies {
    api(project(":plugin-spi"))
    testImplementation(project(":testkit-runtime"))
    testImplementation(project(":paper-1201"))
}
