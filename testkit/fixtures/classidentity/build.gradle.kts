import groovy.json.JsonSlurper
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.tasks.Jar
import org.gradle.jvm.toolchain.JavaToolchainService
val fixtureIndex = JsonSlurper().parse(rootProject.file("contracts/profiles/paper-1.20.1/api-index.json")) as Map<*, *>
val fixtureApi = (fixtureIndex["artifacts"] as List<*>).map { rootProject.file((it as Map<*, *>)["cachePath"] as String) }
val fixtureRoot = rootProject.layout.buildDirectory.dir("fixtures/classidentity")
val fixtureJars = listOf("libraryA", "libraryB", "pluginA", "pluginB").associateWith { name ->
    val compilation = tasks.register<JavaCompile>("compileFixture${name.replaceFirstChar(Char::uppercaseChar)}") {
        source(rootProject.fileTree("testkit/fixtures/classidentity/src/$name/java"))
        destinationDirectory.set(fixtureRoot.map { it.dir("$name/classes") })
        classpath = if(name.startsWith("plugin")) files(fixtureApi, fixtureRoot.map { it.file("library${if(name == "pluginB") "B" else "A"}.jar") }) else files(fixtureApi)
        options.release.set(17)
        javaCompiler.set(project.extensions.getByType<JavaToolchainService>().compilerFor { languageVersion.set(JavaLanguageVersion.of(17)) })
        if(name.startsWith("plugin")) dependsOn("fixtureLibrary${if(name == "pluginB") "B" else "A"}Jar")
    }
    tasks.register<Jar>("fixture${name.replaceFirstChar(Char::uppercaseChar)}Jar") {
        dependsOn(compilation)
        from(compilation.flatMap { it.destinationDirectory })
        from(rootProject.file("testkit/fixtures/classidentity/src/$name/resources"))
        destinationDirectory.set(fixtureRoot)
        archiveFileName.set("$name.jar")
    }
}
tasks.named("test") { dependsOn(fixtureJars.values) }
