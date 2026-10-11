pluginManagement {
    val localRepositories = java.util.Properties()
    val localFile = file("../.gradle/meldrite/local-repositories.properties")
    if (localFile.isFile) localFile.inputStream().use(localRepositories::load)
    repositories {
        val localMirror = localRepositories.getProperty("pluginRepository")
        if (localMirror != null) maven(localMirror)
        gradlePluginPortal()
    }

}
rootProject.name = "meldrite-build-logic"
