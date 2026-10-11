import java.util.Properties
import java.io.ByteArrayOutputStream
import groovy.json.JsonOutput

plugins { base; `java-base` }
val localRepositories = Properties()
val localRepositoryFile = file(".gradle/meldrite/local-repositories.properties")
if (localRepositoryFile.isFile) localRepositoryFile.inputStream().use(localRepositories::load)
val moduleTests = subprojects.map { "${it.path}:test" }
tasks.register("test") { group = "verification"; dependsOn(moduleTests) }
tasks.register("fixtureTest") { group = "verification"; dependsOn(subprojects.map { "${it.path}:fixtureTest" }) }
tasks.register("integrationTest") {
    group = "verification"
    description = "Run the selected integration matrix."
    dependsOn(":testkit-integration:test")
    doFirst {
        val mc = providers.gradleProperty("mc").getOrElse("1.20.1")
        val profile = providers.gradleProperty("profile").getOrElse("paper-$mc")
        require(mc in setOf("1.20.1", "1.21.1")) { "Unsupported mc=$mc" }
        require(profile == "paper-$mc") { "profile must match mc: paper-$mc" }
        require(providers.gradleProperty("modset").getOrElse("fixture").isNotBlank()) { "modset must be non-empty" }
        require(providers.gradleProperty("scenario").getOrElse("all").isNotBlank()) { "scenario must be non-empty" }
    }
}
tasks.register<JavaExec>("verifyRelease") {
    group = "verification"
    description = "Verify the selected release policy and compatibility report."
    dependsOn(":testkit-contract:classes")
    mainClass.set("dev.meldrite.contract.ReleaseVerifier")
    doFirst {
        args("verify", rootDir.absolutePath,
            "--profile", providers.gradleProperty("profile").getOrElse("paper-1.20.1"),
            "--distribution", providers.gradleProperty("distribution").getOrElse("mc-1.20.1"))
    }
}
tasks.register<JavaExec>("lockTools") {
    group = "verification"
    dependsOn(":testkit-contract:classes")
    mainClass.set("dev.meldrite.contract.ArtifactLocker")
    doFirst {
        require(!gradle.startParameter.isOffline) { "Contract resolution is unavailable offline; run verifyContracts" }
        args("lock", "contracts/tools-input.json")
        localRepositories.getProperty("centralRepository")?.let { args("--central-repository", it) }
    }
}
tasks.register<JavaExec>("lockContracts") {
    group = "verification"
    dependsOn(":testkit-contract:classes", "lockTools")
    mainClass.set("dev.meldrite.contract.ArtifactLocker")
    doFirst {
        val profile = providers.gradleProperty("profile").getOrElse("paper-1.20.1")
        require(profile in setOf("paper-1.20.1", "paper-1.21.1")) { "Unsupported profile=$profile" }
        require(!gradle.startParameter.isOffline) { "Contract resolution is unavailable offline; run verifyContracts" }
        args("lock", if (profile == "paper-1.20.1") "contracts/lock-input.json" else "contracts/lock-input-$profile.json")
        localRepositories.getProperty("centralRepository")?.let { args("--central-repository", it) }
    }
}
tasks.register<JavaExec>("verifyContracts") {
    group = "verification"
    dependsOn(subprojects.map { "${it.path}:classes" })
    mainClass.set("dev.meldrite.contract.ContractAudit")
    doFirst {
        val profile = providers.gradleProperty("profile").getOrElse("paper-1.20.1")
        require(profile in setOf("paper-1.20.1", "paper-1.21.1")) { "Unsupported profile=$profile" }
        args("verify", rootDir.absolutePath, "--profile", profile)
    }
    jvmArgs("-Xmx2g")
}
tasks.register<JavaExec>("verifyContractArtifacts") {
    group = "verification"
    dependsOn(":testkit-contract:classes")
    mainClass.set("dev.meldrite.contract.LockVerifier")
    doFirst {
        val profile = providers.gradleProperty("profile").getOrElse("paper-1.20.1")
        args("verify", "contracts/locks/$profile.lock.json", "contracts/locks/tools.lock.json", "--cache", rootDir.absolutePath)
    }
}
tasks.register<JavaExec>("generateContracts") {
    group = "verification"
    description = "Explicitly regenerate the selected locked inventory and initial unsupported bindings."
    dependsOn(":testkit-contract:classes")
    mainClass.set("dev.meldrite.contract.ContractAudit")
    jvmArgs("-Xmx2g")
    doFirst {
        val profile = providers.gradleProperty("profile").getOrElse("paper-1.20.1")
        require(profile in setOf("paper-1.20.1", "paper-1.21.1")) { "Unsupported profile=$profile" }
        args("generate", rootDir.absolutePath, "--profile", profile)
    }
}
tasks.register<JavaExec>("verifyArchitecture") {
    group = "verification"
    dependsOn(subprojects.map { "${it.path}:classes" })
    mainClass.set("dev.meldrite.contract.ArchitectureVerifier")
    doFirst {
        val projectEdges = linkedMapOf<String, List<String>>()
        val directExternal = linkedMapOf<String, List<String>>()
        val resolvedExternal = linkedMapOf<String, List<String>>()
        val runtimeEdges = linkedMapOf<String, List<String>>()
        val runtimeDirect = linkedMapOf<String, List<String>>()
        val runtimeResolved = linkedMapOf<String, List<String>>()
        val compileFiles = linkedMapOf<String, List<String>>()
        val runtimeFiles = linkedMapOf<String, List<String>>()
        rootProject.allprojects.filter { it != rootProject }.forEach { candidate ->
            val compile = candidate.configurations.getByName("compileClasspath")
            projectEdges[candidate.path] = compile.allDependencies.withType(org.gradle.api.artifacts.ProjectDependency::class.java)
                .map { it.path }.sorted()
            directExternal[candidate.path] = compile.allDependencies.withType(org.gradle.api.artifacts.ExternalModuleDependency::class.java)
                .map { "${it.group}:${it.name}" }.sorted()
            resolvedExternal[candidate.path] = compile.incoming.resolutionResult.allComponents.mapNotNull { component ->
                val id = component.id
                if (id is org.gradle.api.artifacts.component.ModuleComponentIdentifier) "${id.group}:${id.module}:${id.version}" else null
            }.distinct().sorted()
            val runtime = candidate.configurations.getByName("runtimeClasspath")
            runtimeEdges[candidate.path] = runtime.allDependencies.withType(org.gradle.api.artifacts.ProjectDependency::class.java)
                .map { it.path }.sorted()
            runtimeDirect[candidate.path] = runtime.allDependencies.withType(org.gradle.api.artifacts.ExternalModuleDependency::class.java)
                .map { "${it.group}:${it.name}" }.sorted()
            runtimeResolved[candidate.path] = runtime.incoming.resolutionResult.allComponents.mapNotNull { component ->
                val id = component.id
                if (id is org.gradle.api.artifacts.component.ModuleComponentIdentifier) "${id.group}:${id.module}:${id.version}" else null
            }.distinct().sorted()
            fun fileArtifacts(configuration: org.gradle.api.artifacts.Configuration): List<String> = configuration.incoming.artifacts.artifacts
                .filter { artifact ->
                    val id = artifact.id.componentIdentifier
                    id !is org.gradle.api.artifacts.component.ModuleComponentIdentifier && id !is org.gradle.api.artifacts.component.ProjectComponentIdentifier
                }.map { it.file.absolutePath }.distinct().sorted()
            compileFiles[candidate.path] = fileArtifacts(compile)
            runtimeFiles[candidate.path] = fileArtifacts(runtime)
        }
        val graphFile = rootProject.layout.buildDirectory.file("reports/meldrite/verifyArchitecture/gradle-graph.json").get().asFile
        graphFile.parentFile.mkdirs()
        graphFile.writeText(JsonOutput.prettyPrint(JsonOutput.toJson(mapOf("schemaVersion" to 2,"projectEdges" to projectEdges, "directExternalCompileDependencies" to directExternal, "resolvedExternalCompileDependencies" to resolvedExternal, "runtimeProjectEdges" to runtimeEdges, "directExternalRuntimeDependencies" to runtimeDirect, "resolvedExternalRuntimeDependencies" to runtimeResolved,"resolvedFileCompileDependencies" to compileFiles,"resolvedFileRuntimeDependencies" to runtimeFiles))))
        args(rootDir.absolutePath, graphFile.absolutePath)
    }
}
tasks.withType<JavaExec>().configureEach {
        val output = ByteArrayOutputStream()
        standardOutput = output
        errorOutput = output
        isIgnoreExitValue = true
        val reportDirectory = rootProject.layout.buildDirectory.dir("reports/meldrite/$name")
        doLast {
            val report = reportDirectory.get().asFile
            report.mkdirs()
            val processOutput = output.toString("UTF-8")
            report.resolve("process.log").writeText(processOutput)
            val exitCode = executionResult.get().exitValue
            report.resolve("execution.json").writeText("""{"exitCode":$exitCode,"java":17,"processLog":"process.log"}""")
            logger.lifecycle(processOutput.trimEnd())
            if (exitCode != 0) throw GradleException("$name failed with exit code $exitCode; see ${report.resolve("process.log")}")
        }
        classpath = files(provider {
            project(":testkit-contract").extensions.getByType<SourceSetContainer>()
                .getByName("main").runtimeClasspath
        })
        javaLauncher.convention(project.extensions.getByType<JavaToolchainService>().launcherFor {
            languageVersion.set(JavaLanguageVersion.of(17))
        })
}
