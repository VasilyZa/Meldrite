plugins { id("meldrite.java") }
dependencies {
    api(project(":api-services"))
    testImplementation(project(":testkit-runtime"))
}
