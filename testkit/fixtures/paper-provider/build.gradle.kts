import groovy.json.JsonSlurper
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.tasks.Jar
import org.gradle.jvm.toolchain.JavaToolchainService
val providerIndex = JsonSlurper().parse(rootProject.file("contracts/profiles/paper-1.20.1/api-index.json")) as Map<*, *>
val providerApi = (providerIndex["artifacts"] as List<*>).map { rootProject.file((it as Map<*, *>)["cachePath"] as String) }
val providerRoot = rootProject.layout.buildDirectory.dir("fixtures/paper-provider")
val providerJars = listOf("main", "library").map { name ->
    val compilation = tasks.register<JavaCompile>("compileProvider${name.replaceFirstChar(Char::uppercaseChar)}") {
        source(rootProject.fileTree("testkit/fixtures/paper-provider/src/$name/java"))
        destinationDirectory.set(providerRoot.map { it.dir("$name/classes") })
        classpath = files(providerApi)
        options.release.set(17)
        javaCompiler.set(project.extensions.getByType<JavaToolchainService>().compilerFor { languageVersion.set(JavaLanguageVersion.of(17)) })
    }
    tasks.register<Jar>("provider${name.replaceFirstChar(Char::uppercaseChar)}Jar") {
        dependsOn(compilation)
        from(compilation.flatMap { it.destinationDirectory })
        from(rootProject.file("testkit/fixtures/paper-provider/src/$name/resources"))
        destinationDirectory.set(providerRoot)
        archiveFileName.set("$name.jar")
    }
}
tasks.named("test") { dependsOn(providerJars) }
