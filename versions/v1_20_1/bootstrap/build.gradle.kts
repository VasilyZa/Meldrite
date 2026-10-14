import net.fabricmc.loom.task.RemapJarTask
import groovy.json.JsonSlurper
import java.security.MessageDigest

plugins { id("net.fabricmc.fabric-loom-remap") version "1.17.21"; id("meldrite.java") }
tasks.processResources {
    inputs.property("meldriteVersion", project.version.toString())
    filesMatching("fabric.mod.json") { expand("version" to project.version.toString()) }
}
val sqliteJar = rootProject.fileTree("${System.getProperty("user.home")}/.gradle/caches/modules-2/files-2.1/org.xerial/sqlite-jdbc/3.42.0.0")
    .matching { include("**/sqlite-jdbc-3.42.0.0.jar") }
    .singleFile
val resolverRuntimeJars = rootProject.fileTree("${System.getProperty("user.dir")}/.gradle/meldrite/artifacts/org.apache.maven.resolver")
    .matching {
        include("maven-resolver-connector-basic/1.6.3/maven-resolver-connector-basic-1.6.3.jar")
        include("maven-resolver-transport-http/1.6.3/maven-resolver-transport-http-1.6.3.jar")
    }
    .files
    .toMutableSet()
    .also {
        fun cached(group: String, module: String, version: String): File = rootProject.fileTree("${System.getProperty("user.home")}/.gradle/caches/modules-2/files-2.1/$group/$module/$version")
            .matching { include("**/$module-$version.jar") }
            .singleFile
        it += cached("org.apache.httpcomponents", "httpclient", "4.5.13")
        it += cached("org.apache.httpcomponents", "httpcore", "4.4.15")
        it += cached("commons-logging", "commons-logging", "1.2")
        it += cached("javax.inject", "javax.inject", "1")
        check(it.size == 6) { "Maven Resolver connector runtime is incomplete" }
    }

val commonsLangJar = rootProject.fileTree("${System.getProperty("user.home")}/.gradle/caches/modules-2/files-2.1/commons-lang/commons-lang/2.6-mohist")
    .matching { include("**/commons-lang-2.6-mohist.jar") }
    .singleFile

val resolverRuntime by configurations.creating
// Console libraries are also part of the locked java -jar runtime classpath.
val consoleRuntime by configurations.creating
val consoleLock = JsonSlurper().parse(rootProject.file("contracts/locks/console-3.25.1.lock.json")) as Map<*, *>
val consoleJars = (consoleLock["artifacts"] as List<*>).map { entry ->
    val artifact = entry as Map<*, *>
    val module = artifact["module"] as String
    val jar = rootProject.fileTree("${System.getProperty("user.home")}/.gradle/caches/modules-2/files-2.1/org.jline/$module/3.25.1")
        .matching { include("**/$module-3.25.1.jar") }.singleFile
    val actual = MessageDigest.getInstance("SHA-256").digest(jar.readBytes()).joinToString("") { "%02x".format(it.toInt() and 0xff) }
    check(actual == artifact["sha256"]) { "Console dependency checksum mismatch: $jar" }
    jar
}
// Resolve the optional compile boundary from its official published coordinate.
repositories {
    exclusiveContent {
        forRepository { maven("https://repo.maven.apache.org/maven2") }
        filter { includeGroup("me.lucko") }
    }
}

if (providers.gradleProperty("meldrite.probeModJars").isPresent) {
    // User-supplied probe inputs have no persisted dependency coordinates.
    configurations.named("testRuntimeClasspath") { resolutionStrategy.deactivateDependencyLocking() }
}

loom {
    accessWidenerPath.set(file("src/main/resources/meldrite.accesswidener"))
    runs {
        named("server") {
            val configuredDirectory = providers.gradleProperty("meldrite.serverDir")
            if (configuredDirectory.isPresent) runDirectory.set(project.file(configuredDirectory.get()))
        }
    }
}

// Test-only native probes run in the same transformed Minecraft runtime as plugins.
// This separate source set has no dependency on Meldrite or Bukkit at runtime.
val fabricParity = sourceSets.create("fabricParity")
fabricParity.compileClasspath += sourceSets.main.get().compileClasspath
dependencies {
    add(fabricParity.compileOnlyConfigurationName, files(providers.gradleProperty("meldrite.test.mtrJar").orNull))
}
val fabricParityJar = tasks.register<Jar>("fabricParityJar") {
    dependsOn(tasks.named(fabricParity.classesTaskName))
    from(fabricParity.output)
    destinationDirectory.set(layout.buildDirectory.dir("fabric-parity"))
    archiveFileName.set("named.jar")
}
tasks.register<RemapJarTask>("remapFabricParityJar") {
    dependsOn(fabricParityJar)
    inputFile.set(fabricParityJar.flatMap { it.archiveFile })
    destinationDirectory.set(layout.buildDirectory.dir("fabric-parity"))
    archiveFileName.set("intermediary.jar")
    addNestedDependencies.set(false)
}
val nativeProbeJar = tasks.register<Jar>("nativeProbeJar") {
    dependsOn(tasks.named("testClasses"))
    from(sourceSets.test.get().output) {
        include("dev/meldrite/v1201/compat/NativeContentProbeMod*.class")
        include("dev/meldrite/v1201/compat/NativeNbtCompatibilityProbe*.class")
        include("dev/meldrite/v1201/compat/NativeItemBridgeProbe*.class")
        include("dev/meldrite/v1201/compat/NativeMetaBatchProbe*.class")
        include("dev/meldrite/v1201/compat/NativeEnchantmentProjectionProbe*.class")
        include("dev/meldrite/v1201/compat/NativeEntityLifecycleProbe*.class")
        include("dev/meldrite/v1201/compat/NativeDetachedEntityProbe*.class")
        include("dev/meldrite/v1201/compat/NativeEntityQueryProbe*.class")
        include("dev/meldrite/v1201/compat/NativeEntityPersistentDataProbe*.class")
        include("dev/meldrite/v1201/compat/NativeWorldFacadeLifetimeProbe*.class")
        include("dev/meldrite/v1201/compat/NativeLivingQueryProbe*.class")
        include("dev/meldrite/v1201/compat/NativePlayerInventoryContentsProbe*.class")
        include("dev/meldrite/v1201/compat/NativeEntitySubtypeProbe*.class")
        include("dev/meldrite/v1201/compat/NativeMobSubtypeProbe*.class")
        include("dev/meldrite/v1201/compat/NativeAnimalBatchProbe*.class")
        include("dev/meldrite/v1201/compat/NativeVehicleBatchProbe*.class")
        include("dev/meldrite/v1201/compat/NativeProjectileBatchProbe*.class")
        include("dev/meldrite/v1201/compat/NativeHostileBatchProbe*.class")
        include("dev/meldrite/v1201/compat/NativeAquaticBatchProbe*.class")
        include("dev/meldrite/v1201/compat/NativeHorseBatchProbe*.class")
        include("dev/meldrite/v1201/compat/NativeUndeadBossBatchProbe*.class")
        include("dev/meldrite/v1201/compat/NativeRaiderBatchProbe*.class")
        include("dev/meldrite/v1201/compat/NativeMerchantLivingBatchProbe*.class")
        include("dev/meldrite/v1201/compat/NativeDecorationBatchProbe*.class")
        include("dev/meldrite/v1201/compat/NativeDamageBatchProbe*.class")
        include("dev/meldrite/v1201/compat/NativeBlockDataProbe*.class")
        include("dev/meldrite/v1201/compat/NativeBlockTemplateBatchProbe*.class")
        include("dev/meldrite/v1201/compat/NativeRecipeProbe*.class")
        include("dev/meldrite/v1201/compat/NativeFunctionCommandProbe*.class")
        include("dev/meldrite/v1201/compat/NativeServicesBatchProbe*.class")
        include("dev/meldrite/v1201/compat/NativeSpecialInventoryProbe*.class")
        include("dev/meldrite/v1201/compat/NativeBlockPlacementProbe*.class")
        include("dev/meldrite/v1201/compat/NativeGSitEntityProbe*.class")
        include("dev/meldrite/v1201/compat/NativeMaterialProjectionProbe*.class")
        include("dev/meldrite/v1201/compat/NativeUnsafeValuesProbe*.class")
        include("dev/meldrite/v1201/compat/NativeWorldStateProbe*.class")
        include("dev/meldrite/v1201/compat/NativeBlockProbe*.class")
        include("dev/meldrite/v1201/compat/NativeChunkProbe*.class")
        include("dev/meldrite/v1201/compat/NativeWorldFacadeBatchProbe*.class")
        include("dev/meldrite/v1201/compat/NativeBlockEntityStateProbe*.class")
        include("dev/meldrite/v1201/compat/NativeTileBatchProbe*.class")
        include("dev/meldrite/v1201/compat/NativeScoreboardProbe*.class")
        include("org/bukkit/craftbukkit/v1_20_R1/scoreboard/ScoreboardAudienceProbe*.class")
    }
    val descriptor = layout.buildDirectory.file("native-probes/fabric.mod.json")
    doFirst {
        descriptor.get().asFile.apply {
            parentFile.mkdirs()
            writeText("""{"schemaVersion":1,"id":"meldrite-native-probes","version":"1.0.0","environment":"server","entrypoints":{"main":["dev.meldrite.v1201.compat.NativeContentProbeMod"]}}""")
        }
    }
    from(descriptor)
    destinationDirectory.set(layout.buildDirectory.dir("native-probes"))
    archiveFileName.set("named.jar")
}
tasks.register<RemapJarTask>("remapNativeProbeJar") {
    dependsOn(nativeProbeJar)
    val namedBootstrap = tasks.named<Jar>("jar")
    dependsOn(namedBootstrap)
    classpath.from(namedBootstrap.flatMap { it.archiveFile })
    inputFile.set(nativeProbeJar.flatMap { it.archiveFile })
    destinationDirectory.set(layout.buildDirectory.dir("native-probes"))
    archiveFileName.set("intermediary.jar")
    addNestedDependencies.set(false)
}
// These fixtures exercise the normal plugin descriptor and native command paths;
// neither fixture is included in the executable server distribution.
tasks.register<Jar>("commandPluginJar") {
    dependsOn(tasks.named("testClasses"))
    from(sourceSets.test.get().output) { include("dev/meldrite/v1201/command/CommandFixturePlugin*.class") }
    val descriptor = layout.buildDirectory.file("command-fixtures/plugin.yml")
    doFirst {
        descriptor.get().asFile.apply {
            parentFile.mkdirs()
            writeText("""
                name: CommandFixture
                version: '1.0.0'
                main: dev.meldrite.v1201.command.CommandFixturePlugin
                api-version: '1.20'
                commands:
                  declaredprobe:
                    aliases: [probealias]
                  implicitprobe: {}
                  secureprobe:
                    permission: fixture.command.private
                  list: {}
                  fabricprobe: {}
                permissions:
                  fixture.command.private:
                    default: false
                """.trimIndent() + "\n")
        }
    }
    from(descriptor)
    destinationDirectory.set(layout.buildDirectory.dir("command-fixtures/plugins"))
    archiveFileName.set("command-fixture.jar")
}
val commandProbeJar = tasks.register<Jar>("commandProbeJar") {
    dependsOn(tasks.named("testClasses"))
    from(sourceSets.test.get().output) { include("dev/meldrite/v1201/command/NativeCommandProbeMod*.class") }
    val descriptor = layout.buildDirectory.file("command-fixtures/fabric.mod.json")
    doFirst {
        descriptor.get().asFile.apply {
            parentFile.mkdirs()
            writeText("""{"schemaVersion":1,"id":"meldrite-command-probe","version":"1.0.0","environment":"server","entrypoints":{"main":["dev.meldrite.v1201.command.NativeCommandProbeMod"]},"depends":{"meldrite":"*"}}""")
        }
    }
    from(descriptor)
    destinationDirectory.set(layout.buildDirectory.dir("command-fixtures"))
    archiveFileName.set("named.jar")
}
tasks.register<RemapJarTask>("remapCommandProbeJar") {
    dependsOn(commandProbeJar, tasks.named("jar"))
    classpath.from(tasks.named<Jar>("jar").flatMap { it.archiveFile })
    inputFile.set(commandProbeJar.flatMap { it.archiveFile })
    destinationDirectory.set(layout.buildDirectory.dir("command-fixtures/mods"))
    archiveFileName.set("command-probe.jar")
    addNestedDependencies.set(false)
}
tasks.named<JavaExec>("runServer") {
    providers.gradleProperty("meldrite.gameJavaHome").orNull?.let { setExecutable("$it/bin/java") }
    jvmArgs("-Xmx1G", "-XX:ActiveProcessorCount=4")
}
tasks.register<JavaExec>("nativeMixinLinkageProbe") {
    dependsOn(tasks.named("testClasses"))
    classpath = sourceSets.test.get().runtimeClasspath
    mainClass.set("dev.meldrite.v1201.compat.NativeMixinLinkageProbe")
    systemProperty("fabric.development", "true")
    systemProperty("meldrite.probeWatchComposition", providers.gradleProperty("meldrite.probeWatchComposition").orElse("false").get())
    systemProperty("meldrite.probeTicketComposition", providers.gradleProperty("meldrite.probeTicketComposition").orElse("false").get())
    systemProperty("meldrite.probeLinkageOnly", providers.gradleProperty("meldrite.probeLinkageOnly").orElse("false").get())
    providers.gradleProperty("meldrite.probeJavaHome").orNull?.let { setExecutable("$it/bin/java") }
    jvmArgs("-Xmx1G", "-XX:ActiveProcessorCount=4")
    doFirst {
        val probeDirectory = layout.buildDirectory.dir("mixin-linkage-probe").get().asFile
        probeDirectory.mkdirs()
        workingDir(probeDirectory)
        val directories = classpath.files.filter { it.isDirectory }
        systemProperty("fabric.classPathGroups", directories.joinToString(File.pathSeparator) { it.absolutePath })
    }
}
tasks.withType<Test>().configureEach {
    maxHeapSize = "2g"
    // Fixtures that extend remapped native classes are loaded by NativeEntityProbeLauncher.
    // Discovering those inner classes directly bypasses the native package-access fixer.
    exclude("**/*\$*.class")
}
tasks.register<JavaExec>("nativePlaceholderExpansionProbe") {
    dependsOn(tasks.named("testClasses"))
    classpath = sourceSets.test.get().runtimeClasspath
    mainClass.set("dev.meldrite.v1201.compat.NativeEntityProbeLauncher")
    args("dev.meldrite.v1201.compat.NativePlaceholderExpansionProbe")
    providers.gradleProperty("meldrite.probeJavaHome").orNull?.let { setExecutable("$it/bin/java") }
    jvmArgs("-Xmx1G", "-XX:ActiveProcessorCount=4")
    doFirst {
        val directory = layout.buildDirectory.dir("placeholder-expansion-probe").get().asFile
        directory.mkdirs()
        workingDir(directory)
    }
}
val consoleProbeClasspath by tasks.registering {
    dependsOn(tasks.named("testClasses"))
    inputs.files(sourceSets.test.get().runtimeClasspath)
    val output = rootProject.layout.buildDirectory.file("reports/meldrite/console/classpath.txt")
    outputs.file(output)
    doLast {
        output.get().asFile.apply {
            parentFile.mkdirs()
            writeText(sourceSets.test.get().runtimeClasspath.asPath + "\n")
        }
    }
}
dependencies {
    // Optional local Mod inputs for transformer-only probes; never bundled in the server jar.
    modCompileOnly("me.lucko:fabric-permissions-api:0.3.1") { isTransitive = false }
    testImplementation(files(provider {
        sourceSets.main.get().compileClasspath.files.filter { it.name.contains("fabric-permissions-api") }
    }))
    implementation(files(consoleJars))
    add(consoleRuntime.name, files(consoleJars))
    providers.gradleProperty("meldrite.probeModJars").orNull?.let { paths ->
        modRuntimeOnly(files(paths.split(File.pathSeparator)))
    }
    compileOnly(files(rootProject.file(".gradle/meldrite/artifacts/io.papermc.paper/paper-api/1.20.1-R0.1-SNAPSHOT/paper-api-1.20.1-R0.1-20230921.165944-178.jar")))
    compileOnly(files(rootProject.file(".gradle/meldrite/artifacts/net.md-5/bungeecord-chat/1.20-R0.1-deprecated+build.14/bungeecord-chat-1.20-R0.1-deprecated+build.14.jar")))
    testImplementation(files(rootProject.file(".gradle/meldrite/artifacts/io.papermc.paper/paper-api/1.20.1-R0.1-SNAPSHOT/paper-api-1.20.1-R0.1-20230921.165944-178.jar")))
    testImplementation(files(rootProject.file(".gradle/meldrite/artifacts/net.md-5/bungeecord-chat/1.20-R0.1-deprecated+build.14/bungeecord-chat-1.20-R0.1-deprecated+build.14.jar")))
    minecraft("com.mojang:minecraft:1.20.1")
    mappings("net.fabricmc:yarn:1.20.1+build.10:v2")
    modImplementation("net.fabricmc:fabric-loader:0.19.5")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.92.12+1.20.1")
    implementation(project(path = ":v1201-bridge", configuration = "namedElements")) { isTransitive = false }
    implementation(project(":bridge-spi"))
    implementation(project(":paper-1201"))
    implementation(project(":craft-common"))
    implementation(project(":api-services"))
    implementation(project(":scheduler"))
    implementation(project(":adventure"))
    implementation(files(sqliteJar))
    implementation(files(commonsLangJar))
    add(resolverRuntime.name, files(resolverRuntimeJars))
    testRuntimeOnly(files(resolverRuntimeJars))
    implementation(project(":bootstrap-common"))
    implementation(project(":plugin-classloading"))
    listOf(":foundation", ":bridge-spi", ":plugin-spi", ":plugin-runtime", ":plugin-classloading", ":bootstrap-common", ":api-services", ":registry", ":craft-common", ":events", ":scheduler", ":paper-1201", ":adventure").forEach { include(project(it)) }
    include(project(":v1201-bridge"))
}
