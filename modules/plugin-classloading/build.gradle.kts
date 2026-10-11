import groovy.json.JsonSlurper
import java.io.File
plugins { id("meldrite.java") }
val index = JsonSlurper().parse(rootProject.file("contracts/profiles/paper-1.20.1/api-index.json")) as Map<*, *>
val artifacts = (index["artifacts"] as List<*>).map { rootProject.file((it as Map<*, *>)["cachePath"] as String) }
dependencies {
    api(project(":plugin-spi"))
    testImplementation(project(":testkit-runtime"))
    testImplementation(files(artifacts))
}
apply(from = rootProject.file("testkit/fixtures/classidentity/build.gradle.kts"))
tasks.test {
    systemProperty("meldrite.fixtureRoot", rootProject.layout.buildDirectory.dir("fixtures/classidentity").get().asFile.absolutePath)
    systemProperty("meldrite.apiJars", artifacts.joinToString(File.pathSeparator))
}
