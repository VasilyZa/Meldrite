import groovy.json.JsonOutput
import java.security.MessageDigest

plugins { id("meldrite.java") }
group = "dev.meldrite"

val bootstrap = project(":v1211-bootstrap")
val packageMod = tasks.register<Copy>("packageMod") {
    val remappedBootstrap = bootstrap.tasks.named("remapJar")
    dependsOn(remappedBootstrap)
    from(remappedBootstrap)
    into(layout.buildDirectory.dir("mods"))
    rename { "meldrite.jar" }
}
val distributionManifest = tasks.register("distributionManifest") {
    dependsOn(packageMod)
    inputs.file(layout.buildDirectory.file("mods/meldrite.jar"))
    inputs.property("meldriteVersion", project.version.toString())
    val output = layout.buildDirectory.file("distribution.json")
    outputs.file(output)
    doLast {
        val artifact = layout.buildDirectory.file("mods/meldrite.jar").get().asFile
        fun sha256(file: java.io.File): String = MessageDigest.getInstance("SHA-256").digest(file.readBytes()).joinToString("") { "%02x".format(it.toInt() and 0xff) }
        val document = linkedMapOf(
            "schemaVersion" to 1, "id" to "mc-1.21.1", "version" to project.version.toString(), "minecraftVersion" to "1.21.1", "profile" to "paper-1.21.1", "javaFeature" to 21,
            "mappingNamespace" to "intermediary", "eulaPath" to "eula.txt",
            "artifact" to mapOf("path" to rootProject.projectDir.toPath().relativize(artifact.toPath()).toString().replace('\\','/'), "sha256" to sha256(artifact)),
            "runtime" to mapOf("modJars" to emptyList<Any>(), "classpath" to emptyList<Any>()),
            "toolchain" to mapOf("loom" to "1.17.21", "loader" to "0.19.5", "fabricApi" to "fabric-lifecycle-events-v1:2.3.12+6c1df3606a", "yarn" to "1.21.1+build.3:v2"),
            "launch" to mapOf("named" to listOf("{root}/gradlew", "--no-daemon", "-p", "{root}", "-Pmeldrite.serverDir={serverDir}", "-Pmeldrite.gameJavaHome={gameJavaHome}", ":v1211-bootstrap:runServer", "--args=--nogui --port {port}"), "intermediary" to listOf("{java}", "-Xmx1G", "-XX:ActiveProcessorCount=4", "-cp", "{runtimeClasspath}", "net.fabricmc.loader.impl.launch.server.FabricServerLauncher", "--nogui", "--port", "{port}"))
        )
        output.get().asFile.parentFile.mkdirs()
        output.get().asFile.writeText(JsonOutput.prettyPrint(JsonOutput.toJson(document)) + "\n")
    }
}
tasks.named("assemble") { dependsOn(distributionManifest) }
