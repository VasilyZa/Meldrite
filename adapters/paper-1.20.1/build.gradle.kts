import groovy.json.JsonSlurper
import java.io.File
plugins { id("meldrite.java") }
sourceSets.main { resources.srcDir(rootProject.file("versions/v1_20_1/mappings/resources")) }
// Keep incubator references separate so the ordinary runtime does not require the module.
val vector = sourceSets.create("vector")
val compileMain = tasks.named<JavaCompile>("compileJava")
vector.compileClasspath += sourceSets.main.get().compileClasspath + files(compileMain.flatMap { it.destinationDirectory }).builtBy(compileMain)
tasks.named<JavaCompile>(vector.compileJavaTaskName) {
    options.release.unset()
    sourceCompatibility = "17"
    targetCompatibility = "17"
    options.compilerArgs.add("--add-modules=jdk.incubator.vector")
}
tasks.jar { from(vector.output) }
val contractIndex = JsonSlurper().parse(rootProject.file("contracts/profiles/paper-1.20.1/api-index.json")) as Map<*, *>
val contractArtifacts = (contractIndex["artifacts"] as List<*>).map { artifact -> rootProject.file((artifact as Map<*, *>)["cachePath"] as String) }
val runtimeArtifacts = contractArtifacts.filterNot {
    val normalized = it.path.replace('\\', '/')
    normalized.contains("/org.ow2.asm/") || normalized.contains("/org.apache.logging.log4j/log4j-api/")
            || normalized.contains("/io.papermc.paper/paper-api/")
}
val paperContract = contractArtifacts.single { it.path.replace('\\', '/').contains("/io.papermc.paper/paper-api/") }
val projectedPaperApi = layout.buildDirectory.file("api/paper-api-1.20.1-meldrite.jar")
val projectPaperApi by tasks.registering(JavaExec::class) {
    dependsOn(tasks.named("classes"))
    inputs.file(paperContract)
    inputs.files(sourceSets.main.get().output)
    outputs.file(projectedPaperApi)
    classpath = sourceSets.main.get().output + configurations.compileClasspath.get()
    mainClass.set("dev.meldrite.paper.compat.PotionEffectRegistryTransform")
    args(paperContract.absolutePath, projectedPaperApi.get().asFile.absolutePath)
}
dependencies {
    compileOnly("com.mojang:authlib:4.0.43") { isTransitive = false }
    testImplementation("com.mojang:authlib:4.0.43") { isTransitive = false }
    implementation("org.ow2.asm:asm:9.7.1")
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
    runtimeOnly(files(runtimeArtifacts))
    runtimeOnly(files(projectedPaperApi).builtBy(projectPaperApi))
    testImplementation(files(contractArtifacts))
    testImplementation(project(":testkit-runtime"))
}
repositories { maven("https://libraries.minecraft.net/") }
tasks.named("compileJava") { dependsOn(rootProject.tasks.named("verifyContractArtifacts")) }

apply(from = rootProject.file("testkit/fixtures/paper-provider/build.gradle.kts"))
tasks.test {
    systemProperty("meldrite.fixtureRoot", rootProject.layout.buildDirectory.dir("fixtures/paper-provider").get().asFile.absolutePath)
    systemProperty("meldrite.apiJars", contractArtifacts.joinToString(File.pathSeparator))
    systemProperty("meldrite.paperContractJar", paperContract.absolutePath)
    val nativeMappingCache = File(System.getProperty("user.home"), ".gradle/caches")
    systemProperty("meldrite.mappingProbe.spigotJar", providers.gradleProperty("legacySpigotJar").getOrElse(
        File(System.getProperty("user.home"), ".m2/repository/org/spigotmc/spigot/1.20.1-R0.1-SNAPSHOT/spigot-1.20.1-R0.1-SNAPSHOT.jar").absolutePath))
    systemProperty("meldrite.mappingProbe.nativeJar", providers.gradleProperty("legacyNativeJar").getOrElse(
        File(nativeMappingCache, "fabric-loom/minecraftMaven/net/minecraft/minecraft-merged-intermediary/1.20.1-net.fabricmc.yarn.1_20_1.1.20.1+build.10-v2/minecraft-merged-intermediary-1.20.1-net.fabricmc.yarn.1_20_1.1.20.1+build.10-v2.jar").absolutePath))
    systemProperty("meldrite.mappingProbe.libraryCache", providers.gradleProperty("legacyLibraryCache").getOrElse(
        File(nativeMappingCache, "modules-2/files-2.1").absolutePath))
}
