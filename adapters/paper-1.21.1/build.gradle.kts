import groovy.json.JsonSlurper
import java.io.File

plugins { id("meldrite.java") }

java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))
val contractIndex = JsonSlurper().parse(rootProject.file("contracts/profiles/paper-1.21.1/api-index.json")) as Map<*, *>
val contractArtifacts = (contractIndex["artifacts"] as? List<*>)?.map { artifact -> rootProject.file((artifact as Map<*, *>) ["cachePath"] as String) } ?: emptyList()
dependencies {
    api(project(":plugin-spi"))
    implementation(project(":plugin-runtime"))
    implementation(project(":plugin-classloading"))
    implementation(project(":api-services"))
    implementation(project(":craft-common"))
    implementation(project(":registry"))
    implementation(project(":adventure"))
    implementation(project(":events"))
    implementation(project(":scheduler"))
    implementation(project(":commands"))
    compileOnly(files(contractArtifacts))
    testImplementation(project(":testkit-runtime"))
}
tasks.named("compileJava") { dependsOn(rootProject.tasks.named("verifyContractArtifacts")) }
tasks.withType<JavaCompile>().configureEach { options.release.set(21) }
tasks.test { systemProperty("meldrite.root", rootProject.projectDir.absolutePath) }
