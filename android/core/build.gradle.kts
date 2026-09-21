plugins { `java-library` }
java { toolchain { languageVersion.set(JavaLanguageVersion.of(17)) } }
tasks.withType<JavaCompile>().configureEach { options.compilerArgs.addAll(listOf("-Xlint:all", "-Werror")) }
val goldenTest by tasks.registering(JavaExec::class) {
    dependsOn(tasks.testClasses)
    classpath = sourceSets["test"].runtimeClasspath
    mainClass.set("org.sicdic.core.VectorRunner")
    args(rootProject.file("../shared-test-vectors/generated/vectors.tsv").absolutePath)
}
tasks.check { dependsOn(goldenTest) }
