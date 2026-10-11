import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import java.security.MessageDigest
import java.util.Properties

plugins { id("meldrite.java") }

group = "dev.meldrite"
val productionMappings = file("${System.getProperty("user.home")}/.gradle/caches/modules-2/files-2.1/net.fabricmc/intermediary/1.20.1/20ba9399cb5a23aa06ea901eff0a84e648d866db/intermediary-1.20.1-v2.jar")
val sqliteJdbc = fileTree("${System.getProperty("user.home")}/.gradle/caches/modules-2/files-2.1/org.xerial/sqlite-jdbc/3.42.0.0")
    .matching { include("**/sqlite-jdbc-3.42.0.0.jar") }
    .singleFile
val commonsLang = fileTree("${System.getProperty("user.home")}/.gradle/caches/modules-2/files-2.1/commons-lang/commons-lang/2.6-mohist")
    .matching { include("**/commons-lang-2.6-mohist.jar") }
    .singleFile

val bootstrap = project(":v1201-bootstrap")
evaluationDependsOn(bootstrap.path)
val packageMod by tasks.registering(Copy::class) {
    val remappedBootstrap = bootstrap.tasks.named("remapJar")
    dependsOn(remappedBootstrap)
    from(remappedBootstrap)
    into(layout.buildDirectory.dir("mods"))
    rename { "meldrite.jar" }
}

val distributionManifest by tasks.registering {
    dependsOn(packageMod)
    dependsOn(":paper-1201:projectPaperApi")
    inputs.file(layout.buildDirectory.file("mods/meldrite.jar"))
    inputs.property("meldriteVersion", project.version.toString())
    inputs.file(project(":paper-1201").layout.buildDirectory.file("api/paper-api-1.20.1-meldrite.jar"))
    inputs.files(
        rootProject.file("contracts/profiles/paper-1.20.1/adventure-providers.json"),
        rootProject.file("contracts/profiles/paper-1.20.1/api-index.json"),
        rootProject.file("contracts/locks/paper-1.20.1.lock.json"),
        productionMappings,
        sqliteJdbc,
        commonsLang,
        bootstrap.configurations.getByName("consoleRuntime").resolve(),
        bootstrap.configurations.getByName("resolverRuntime").resolve()
    )
    outputs.file(layout.buildDirectory.file("distribution.json"))
    doLast {
        val artifact = layout.buildDirectory.file("mods/meldrite.jar").get().asFile
        check(artifact.isFile) { "Missing remapped bootstrap artifact: $artifact" }
        fun sha256(file: java.io.File): String {
            val digest = MessageDigest.getInstance("SHA-256")
            file.inputStream().use { input ->
                val buffer = ByteArray(8192)
                while (true) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    if (count > 0) digest.update(buffer, 0, count)
                }
            }
            return digest.digest().joinToString("") { "%02x".format(it.toInt() and 0xff) }
        }
        val loader = bootstrap.configurations.getByName("modImplementation").resolve()
                .firstOrNull { it.name == "fabric-loader-0.19.5.jar" }
                ?: error("fabric-loader-0.19.5.jar was not resolved")
        val fabricApi = bootstrap.configurations.getByName("modImplementation").resolve()
                .firstOrNull { it.name == "fabric-api-0.92.12+1.20.1.jar" }
                ?: error("fabric-api-0.92.12+1.20.1.jar was not resolved")
        val serverJar = file("${System.getProperty("user.home")}/.gradle/caches/fabric-loom/1.20.1/minecraft-server.jar")
        check(serverJar.isFile) { "Loom server jar is not available: $serverJar" }
        check(productionMappings.isFile) { "Intermediary mappings are not available: $productionMappings" }
        val root = rootProject.projectDir.toPath().toAbsolutePath().normalize()
        fun path(file: java.io.File): String = file.toPath().toAbsolutePath().normalize().toString()
        val commandNamed = listOf(
            "{root}/gradlew", "--no-daemon", "--offline", "-p", "{root}",
            "-Pmeldrite.serverDir={serverDir}", "-Pmeldrite.gameJavaHome={gameJavaHome}", ":v1201-bootstrap:runServer",
            "--args=--nogui --port {port}")
        val commandIntermediary = listOf(
            "{java}", "-Xmx1G", "-XX:ActiveProcessorCount=4", "-Dfabric.gameJarPath={minecraftServerJar}", "-cp", "{runtimeClasspath}",
            "net.fabricmc.loader.impl.launch.server.FabricServerLauncher", "--nogui", "--port", "{port}")
        val providers = JsonSlurper().parse(rootProject.file("contracts/profiles/paper-1.20.1/adventure-providers.json")) as Map<*, *>
        val paperApiIndex = JsonSlurper().parse(rootProject.file("contracts/profiles/paper-1.20.1/api-index.json")) as Map<*, *>
        val paperLock = JsonSlurper().parse(rootProject.file("contracts/locks/paper-1.20.1.lock.json")) as Map<*, *>
        val paperRoot = ((paperLock["roots"] as List<*>).single() as Map<*, *>)
        val paperApiFile = rootProject.file(paperRoot["cachePath"] as String)
        check(paperApiFile.isFile) { "Paper API contract artifact is not available: $paperApiFile" }
        val projectedPaperApi = project(":paper-1201").layout.buildDirectory.file("api/paper-api-1.20.1-meldrite.jar").get().asFile
        check(projectedPaperApi.isFile) { "Projected Paper API artifact is not available: $projectedPaperApi" }
        val adventureArtifacts = (providers["artifacts"] as List<*>).map { it as Map<*, *> }
        check(adventureArtifacts.isNotEmpty()) { "Adventure runtime dependency contract is empty" }
        val paperApiArtifacts = (paperApiIndex["artifacts"] as List<*>).map { it as Map<*, *> }
                .filterNot {
                    val path = it["cachePath"] as String
                    path.contains("/org.ow2.asm/") || path.contains("/org.apache.logging.log4j/log4j-api/")
                            || path.contains("/org.slf4j/") || path.contains("/io.papermc.paper/paper-api/")
                }
        val adventureApi = adventureArtifacts.first { (it["coordinate"] as String).startsWith("net.kyori:adventure-api:") }
        val adventureApiFile = rootProject.file(adventureApi["cachePath"] as String)
        val launchLibraries = (listOf(loader, productionMappings, projectedPaperApi, sqliteJdbc, commonsLang)
                + paperApiArtifacts.map { rootProject.file(it["cachePath"] as String) }
                + adventureArtifacts.map { rootProject.file(it["cachePath"] as String) }
                + bootstrap.configurations.getByName("consoleRuntime").resolve()
                + bootstrap.configurations.getByName("resolverRuntime").resolve()
                + bootstrap.configurations.getByName("loaderLibraries").resolve()
                + bootstrap.configurations.getByName("minecraftServerRuntimeLibraries").resolve()).distinct()
        val document = linkedMapOf(
            "schemaVersion" to 1,
            "id" to "mc-1.20.1",
            "version" to project.version.toString(),
            "minecraftVersion" to "1.20.1",
            "profile" to "paper-1.20.1",
            "javaFeature" to 17,
            "mappingNamespace" to "intermediary",
            "eulaPath" to "eula.txt",
            "artifact" to mapOf("path" to root.relativize(artifact.toPath()).toString().replace('\\', '/'), "sha256" to sha256(artifact)),
            "runtime" to mapOf(
                "fabricLoaderJar" to mapOf("path" to path(loader), "sha256" to sha256(loader)),
                "minecraftServerJar" to mapOf("path" to path(serverJar), "sha256" to sha256(serverJar)),
                "classpath" to launchLibraries.map { mapOf("path" to path(it), "sha256" to sha256(it)) },
                "adventureApi" to mapOf("path" to path(adventureApiFile), "sha256" to adventureApi["sha256"]),
                "modJars" to listOf(mapOf("path" to path(fabricApi), "sha256" to sha256(fabricApi)))
            ),
            "toolchain" to mapOf(
                "loom" to "1.17.21", "loader" to "0.19.5", "fabricApi" to "0.92.12+1.20.1",
                "yarn" to "1.20.1+build.10:v2"
            ),
            "apiProjection" to mapOf("sourcePath" to path(paperApiFile), "sourceSha256" to sha256(paperApiFile),
                    "projectedPath" to path(projectedPaperApi), "projectedSha256" to sha256(projectedPaperApi),
                    "transform" to "native-potion-effect-id-overflow-v1"),
            "launch" to mapOf("named" to commandNamed, "intermediary" to commandIntermediary)
        )
        layout.buildDirectory.file("distribution.json").get().asFile.writeText(JsonOutput.prettyPrint(JsonOutput.toJson(document)) + "\n")
    }
}

val bundleRuntime by tasks.registering {
    dependsOn(distributionManifest)
    inputs.file(layout.buildDirectory.file("distribution.json"))
    outputs.dir(layout.buildDirectory.dir("executable-runtime"))
    doLast {
        val document = JsonSlurper().parse(layout.buildDirectory.file("distribution.json").get().asFile) as Map<*, *>
        val runtime = document["runtime"] as Map<*, *>
        val output = layout.buildDirectory.dir("executable-runtime").get().asFile
        output.deleteRecursively()
        output.mkdirs()
        val properties = Properties()
        val embedded = linkedMapOf<String, Int>()
        fun embed(entry: Map<*, *>): Int {
            val source = rootProject.file(entry["path"] as String)
            val expected = entry["sha256"] as String
            val digest = MessageDigest.getInstance("SHA-256")
            source.inputStream().use { input ->
                val buffer = ByteArray(8192)
                while (true) {
                    val read = input.read(buffer)
                    if (read < 0) break
                    if (read > 0) digest.update(buffer, 0, read)
                }
            }
            val actual = digest.digest().joinToString("") { "%02x".format(it.toInt() and 0xff) }
            check(actual == expected) { "Distribution artifact checksum mismatch: $source" }
            val key = "$actual/${source.name}"
            return embedded.getOrPut(key) {
                val index = embedded.size
                val resource = "META-INF/meldrite/runtime/$key"
                source.copyTo(output.resolve(resource).apply { parentFile.mkdirs() }, overwrite = true)
                properties.setProperty("$index.resource", resource)
                properties.setProperty("$index.path", "libraries/meldrite/$key")
                properties.setProperty("$index.sha256", actual)
                index
            }
        }
        val classpath = (runtime["classpath"] as List<*>).map { embed(it as Map<*, *>) }.distinct()
        val minecraft = embed(runtime["minecraftServerJar"] as Map<*, *>)
        val mods = listOf(embed(document["artifact"] as Map<*, *>)) +
                (runtime["modJars"] as List<*>).map { embed(it as Map<*, *>) }
        properties.setProperty("artifactCount", embedded.size.toString())
        properties.setProperty("classpath", classpath.joinToString(","))
        properties.setProperty("minecraft", minecraft.toString())
        properties.setProperty("mods", mods.joinToString(","))
        properties.setProperty("minecraftVersion", document["minecraftVersion"] as String)
        properties.setProperty("meldriteVersion", document["version"] as String)
        properties.setProperty("javaFeature", document["javaFeature"].toString())
        properties.setProperty("loaderVersion", (document["toolchain"] as Map<*, *>)["loader"] as String)
        val launch = (document["launch"] as Map<*, *>)["intermediary"] as List<*>
        properties.setProperty("mainClass", launch[launch.indexOf("{runtimeClasspath}") + 1] as String)
        output.resolve("META-INF/meldrite/bundle.properties").writer(Charsets.UTF_8).use {
            properties.store(it, "Portable Meldrite server distribution")
        }
    }
}

val executableServerJar by tasks.registering(Jar::class) {
    group = "distribution"
    description = "Build a self-contained server launched with java -jar."
    dependsOn(bundleRuntime, tasks.named("classes"))
    from(sourceSets.main.get().output)
    from(layout.buildDirectory.dir("executable-runtime"))
    destinationDirectory.set(layout.buildDirectory.dir("server"))
    archiveFileName.set("meldrite-1.20.1.jar")
    manifest.attributes["Main-Class"] = "dev.meldrite.distribution.mc1201.ServerLauncher"
    val versionedJar = layout.buildDirectory.file("server/meldrite-${project.version}.jar")
    outputs.file(versionedJar)
    doLast { archiveFile.get().asFile.copyTo(versionedJar.get().asFile, overwrite = true) }
}

tasks.named("assemble") { dependsOn(distributionManifest, executableServerJar) }
