import groovy.json.JsonSlurper
plugins { id("meldrite.java") }
val manifest = JsonSlurper().parse(rootProject.file("contracts/profiles/paper-1.20.1/adventure-providers.json")) as Map<*, *>
val providers = (manifest["artifacts"] as List<*>).map { rootProject.file((it as Map<*, *>)["cachePath"] as String) }
dependencies {
    api(project(":bridge-spi"))
    api(files(providers))
    testImplementation(project(":testkit-runtime"))
}
tasks.named("compileJava") { dependsOn(rootProject.tasks.named("verifyContractArtifacts")) }
