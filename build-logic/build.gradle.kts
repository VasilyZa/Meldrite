import java.util.Properties

plugins { `kotlin-dsl` }
val localRepositories = Properties()
val localRepositoryFile = file("../.gradle/meldrite/local-repositories.properties")
if (localRepositoryFile.isFile) localRepositoryFile.inputStream().use(localRepositories::load)
repositories {
    val localMirror = localRepositories.getProperty("centralRepository")
    if (localMirror != null) maven(localMirror) else mavenCentral()
    gradlePluginPortal()
}
dependencyLocking { lockAllConfigurations() }
