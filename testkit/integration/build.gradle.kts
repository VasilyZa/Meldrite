import groovy.json.JsonSlurper
plugins { id("meldrite.java") }
dependencies {
    implementation(project(":bootstrap-common"))
    implementation(project(":testkit-runtime"))
    testImplementation(project(":extension-api"))
    testImplementation(project(":paper-1201"))
}
val protectionApiIndex = JsonSlurper().parse(rootProject.file("contracts/profiles/paper-1.20.1/api-index.json")) as Map<*, *>
val protectionApiJars = (protectionApiIndex["artifacts"] as List<*>).map { rootProject.file((it as Map<*, *>) ["cachePath"] as String) }
tasks.test {
    dependsOn(":mc1201:assemble")
    dependsOn(":v1201-bootstrap:remapNativeProbeJar")
    dependsOn("bootstrapSTARTUPFixtureJar", "bootstrapPOSTWORLDFixtureJar", "protectionFixtureJar")
    inputs.file(rootProject.file("versions/v1_20_1/bootstrap/build/native-probes/intermediary.jar"))
        .withPropertyName("nativeProbeJar")
    inputs.file(rootProject.file("distributions/mc-1.20.1/build/distribution.json"))
        .withPropertyName("nativeDistributionManifest")
    inputs.dir(layout.buildDirectory.dir("bootstrap-fixture")).withPropertyName("bootstrapFixtures")
    inputs.file(layout.buildDirectory.file("protection-fixture/protection.jar")).withPropertyName("protectionFixture")
    listOf("fabricModsDir", "meldrite.test.modsDir", "paperPluginsDir", "meldrite.test.pluginsDir").forEach { property ->
        providers.gradleProperty(property).orNull?.let { inputs.dir(it).withPropertyName(property) }
    }
    providers.gradleProperty("meldrite.pluginReplacements").orNull?.split(File.pathSeparator)?.forEach { replacement ->
        val separator = replacement.indexOf('=')
        if (separator > 0) inputs.file(replacement.substring(separator + 1)).withPropertyName("replacement." + replacement.substring(0, separator))
    }
    systemProperty("meldrite.root",rootProject.rootDir.absolutePath)
    systemProperty("meldrite.runtimeNamespace",providers.gradleProperty("runtimeNamespace").getOrElse("intermediary"))
    systemProperty("meldrite.bootstrapFixtures", layout.buildDirectory.dir("bootstrap-fixture").get().asFile.absolutePath)
    systemProperty("meldrite.protectionFixture", layout.buildDirectory.file("protection-fixture/protection.jar").get().asFile.absolutePath)
    systemProperty("meldrite.nativeProbeJar", rootProject.file("versions/v1_20_1/bootstrap/build/native-probes/intermediary.jar").absolutePath)
    providers.gradleProperty("meldrite.eulaFile").orNull?.let { systemProperty("meldrite.eulaFile",it) }
    providers.gradleProperty("eulaFile").orNull?.let { systemProperty("meldrite.eulaFile",it) }
    providers.gradleProperty("java17Home").orNull?.let { systemProperty("meldrite.java17Home",it) }
    providers.gradleProperty("meldrite.serverJavaHome").orNull?.let { systemProperty("meldrite.serverJavaHome", it) }
    providers.gradleProperty("java11Home").orNull?.let { systemProperty("meldrite.java11Home",it) }
    providers.gradleProperty("java21Home").orNull?.let { systemProperty("meldrite.java21Home",it) }
    providers.gradleProperty("fabricModsDir").orNull?.let { systemProperty("meldrite.fabricModsDir", it) }
    providers.gradleProperty("meldrite.test.modsDir").orNull?.let { systemProperty("meldrite.test.modsDir", it) }
    providers.gradleProperty("fabricModExclude").orNull?.let { systemProperty("meldrite.fabricModExclude", it) }
    providers.gradleProperty("paperPluginsDir").orNull?.let { systemProperty("meldrite.paperPluginsDir", it) }
    providers.gradleProperty("meldrite.test.pluginsDir").orNull?.let { systemProperty("meldrite.test.pluginsDir", it) }
    providers.gradleProperty("paperPluginExclude").orNull?.let { systemProperty("meldrite.paperPluginExclude", it) }
    providers.gradleProperty("meldrite.serverJvmArgs").orNull?.let { systemProperty("meldrite.serverJvmArgs", it) }
    providers.gradleProperty("meldrite.pluginReplacements").orNull?.let { systemProperty("meldrite.pluginReplacements", it) }
}

val compileProtectionFixture = tasks.register<JavaCompile>("compileProtectionFixture") {
    dependsOn(rootProject.tasks.named("verifyContractArtifacts"))
    source(rootProject.fileTree("testkit/fixtures/protection/src/main/java"))
    classpath = files(protectionApiJars)
    destinationDirectory.set(layout.buildDirectory.dir("protection-fixture/classes"))
    options.release.set(17)
}

val protectionFixtureJar = tasks.register<Jar>("protectionFixtureJar") {
    dependsOn(compileProtectionFixture)
    from(compileProtectionFixture.flatMap { it.destinationDirectory })
    destinationDirectory.set(layout.buildDirectory.dir("protection-fixture"))
    archiveFileName.set("protection.jar")
    val descriptor = layout.buildDirectory.file("protection-fixture/plugin.yml")
    doFirst {
        descriptor.get().asFile.apply {
            parentFile.mkdirs()
            writeText("name: MeldriteProtection\nversion: '1.0.0'\nmain: fixture.protection.ProtectionPlugin\napi-version: '1.20'\n")
        }
    }
    from(descriptor) { rename { "plugin.yml" } }
}

val apiIndex = JsonSlurper().parse(rootProject.file("contracts/profiles/paper-1.20.1/api-index.json")) as Map<*, *>
val apiJars = (apiIndex["artifacts"] as List<*>).map { rootProject.file((it as Map<*, *>)["cachePath"] as String) }
val compileBootstrapFixture = tasks.register<JavaCompile>("compileBootstrapFixture") {
    dependsOn(rootProject.tasks.named("verifyContractArtifacts"))
    source(rootProject.fileTree("testkit/fixtures/bootstrap/src/main/java"))
    classpath = files(apiJars)
    destinationDirectory.set(layout.buildDirectory.dir("bootstrap-fixture/classes"))
    options.release.set(17)
}
listOf("STARTUP", "POSTWORLD").forEach { phase ->
    tasks.register<Jar>("bootstrap${phase}FixtureJar") {
        dependsOn(compileBootstrapFixture)
        from(compileBootstrapFixture.flatMap { it.destinationDirectory })
        destinationDirectory.set(layout.buildDirectory.dir("bootstrap-fixture"))
        archiveFileName.set("$phase.jar")
        val descriptor = layout.buildDirectory.file("bootstrap-fixture/$phase/plugin.yml")
        doFirst {
            descriptor.get().asFile.apply { parentFile.mkdirs(); writeText("name: Bootstrap$phase\nversion: '1.0.0'\nmain: fixture.bootstrap.BootstrapPlugin\napi-version: '1.20'\nload: $phase\n") }
        }
        from(descriptor) { rename { "plugin.yml" } }
    }
}
