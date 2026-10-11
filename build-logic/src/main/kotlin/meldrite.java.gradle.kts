import java.util.Properties
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle

plugins { `java-library` }
// Resolve once per build so every embedded module uses the same date/count/channel.
val buildVersionKey = "meldrite.build-version"
val buildVersion = synchronized(rootProject) {
    val extra = rootProject.extensions.extraProperties
    if (!extra.has(buildVersionKey)) {
        val dateFormat = DateTimeFormatter.ofPattern("uuuu.MM.dd").withResolverStyle(ResolverStyle.STRICT)
        val date = rootProject.providers.gradleProperty("meldrite.buildDate")
            .getOrElse(LocalDate.now(ZoneId.of("Asia/Shanghai")).format(dateFormat))
        require(date.matches(Regex("\\d{4}\\.\\d{2}\\.\\d{2}"))) { "meldrite.buildDate must be yyyy.MM.dd" }
        LocalDate.parse(date, dateFormat)
        val channel = rootProject.providers.gradleProperty("meldrite.channel").getOrElse("dev")
        require(channel in setOf("dev", "release", "pre-release")) { "meldrite.channel must be dev, release or pre-release" }
        val commits = rootProject.providers.gradleProperty("meldrite.commitCount").orNull ?: run {
            val git = rootProject.providers.exec {
                workingDir(rootProject.rootDir)
                commandLine("git", "rev-list", "--count", "HEAD")
                isIgnoreExitValue = true
            }
            check(git.result.get().exitValue == 0) { "Git history unavailable; specify -Pmeldrite.commitCount=<count>" }
            git.standardOutput.asText.get().trim()
        }
        require(commits.matches(Regex("0|[1-9][0-9]*"))) { "meldrite.commitCount must be a non-negative integer" }
        val minecraft = rootProject.providers.gradleProperty("mc").orNull
            ?: if (gradle.startParameter.taskNames.any { it.contains("1211") }) "1.21.1" else "1.20.1"
        require(minecraft in setOf("1.20.1", "1.21.1")) { "Unsupported Minecraft version: $minecraft" }
        extra[buildVersionKey] = mapOf("prefix" to "$date.$commits", "channel" to channel, "minecraft" to minecraft)
        rootProject.version = "$date.$commits-$minecraft-$channel"
    }
    @Suppress("UNCHECKED_CAST")
    extra[buildVersionKey] as Map<String, String>
}
val versionMinecraft = when {
    project.name.contains("1201") -> "1.20.1"
    project.name.contains("1211") -> "1.21.1"
    else -> buildVersion.getValue("minecraft")
}
version = "${buildVersion.getValue("prefix")}-$versionMinecraft-${buildVersion.getValue("channel")}"
tasks.withType<Jar>().configureEach {
    manifest.attributes["Implementation-Version"] = project.version.toString()
}
tasks.matching { it.name == "remapJar" }.configureEach {
    doFirst {
        check(versionMinecraft == buildVersion.getValue("minecraft")) {
            "Build one Minecraft distribution per invocation; use its specific task with -Pmc=$versionMinecraft"
        }
    }
}
val localRepositories = Properties()
val localRepositoryFile = rootProject.file(".gradle/meldrite/local-repositories.properties")
if (localRepositoryFile.isFile) localRepositoryFile.inputStream().use(localRepositories::load)
repositories {
    val localMirror = localRepositories.getProperty("centralRepository")
    if (localMirror != null) maven(localMirror)
    mavenCentral()
    // Fabric artifacts have a separately declared source; the public mirror need not contain them.
    maven("https://maven.fabricmc.net/")
}
java { toolchain { languageVersion.set(JavaLanguageVersion.of(17)) } }
tasks.withType<JavaCompile>().configureEach { options.release.set(17); options.encoding = "UTF-8" }
dependencyLocking { lockAllConfigurations() }
dependencies {
    "testImplementation"(platform("org.junit:junit-bom:5.11.4"))
    "testImplementation"("org.junit.jupiter:junit-jupiter")
    "testRuntimeOnly"("org.junit.platform:junit-platform-launcher:1.11.4")
}
tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    reports.junitXml.outputLocation.set(rootProject.layout.buildDirectory.dir("reports/meldrite/test/${project.name}/xml"))
    reports.html.outputLocation.set(rootProject.layout.buildDirectory.dir("reports/meldrite/test/${project.name}/html"))
    testLogging { events("failed", "skipped"); exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL }
}
tasks.register("fixtureTest") {
    group = "verification"
    description = "Execute the module test suite, including production-driven JSON fixtures, once."
    dependsOn(tasks.named("test"))
    doLast {
        val report = rootProject.layout.buildDirectory.file("reports/meldrite/fixtureTest/${project.name}/report.json").get().asFile
        report.parentFile.mkdirs()
        report.writeText("""{"suite":"${project.name}","testReport":"../../test/${project.name}/xml","execution":"shared-with-test"}""")
    }
}
