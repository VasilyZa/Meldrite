plugins { id("meldrite.java") }

java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))
dependencies {
    api(project(":bridge-spi"))
    api(project(":extension-api"))
}
tasks.withType<JavaCompile>().configureEach { options.release.set(21) }
