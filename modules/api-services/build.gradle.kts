import groovy.json.JsonSlurper
import java.io.File
plugins { id("meldrite.java") }
dependencies { api(project(":bridge-spi")); api(project(":registry")); api(project(":plugin-spi")); testImplementation(project(":testkit-runtime")) }

val profileIndex = JsonSlurper().parse(rootProject.file("contracts/profiles/paper-1.20.1/api-index.json")) as Map<*, *>
val profileJars = (profileIndex["artifacts"] as List<*>).map { rootProject.file((it as Map<*, *>)["cachePath"] as String) }
dependencies {
    testImplementation(project(":paper-1201"))
    testImplementation(project(":craft-common"))
    testImplementation(project(":plugin-runtime"))
    testImplementation(project(":plugin-classloading"))
    testImplementation(project(":scheduler"))
    testImplementation(project(":events"))
    testImplementation(files(profileJars))
}
tasks.test { systemProperty("meldrite.apiJars", profileJars.joinToString(File.pathSeparator)) }
