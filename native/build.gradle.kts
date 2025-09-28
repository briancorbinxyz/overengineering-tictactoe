import java.util.*
import java.io.File

plugins {
    // Apply the application plugin to add support for building a CLI application in Java.
    id("buildlogic.java-library-conventions")
    id("com.vanniktech.maven.publish")
    id("signing")
}

group = "org.xxdc.oss.example"

repositories {
    // Use Maven Central for resolving dependencies.
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(project(":api"))
}

testing {
    suites {
        // Configure the built-in test suite
        val test by getting(JvmTestSuite::class) {
            // Use TestNG test framework
            useTestNG("7.5.1")
        }
    }
}

// JDK22: Foreign Function Interface (FFI)
// Support building native Rust library using Cargo:
// https://doc.rust-lang.org/cargo/getting-started/installation.html
val osName = System.getProperty("os.name").lowercase()
val osArch = System.getProperty("os.arch").lowercase()

fun isOnPath(cmd: String): Boolean {
    val path = System.getenv("PATH") ?: return false
    val suffix = if (osName.contains("win")) ".exe" else ""
    return path.split(File.pathSeparator).any { dir -> File(dir, cmd + suffix).canExecute() }
}
val hasCargo = isOnPath("cargo")

val cargoBuildDir = file("${layout.buildDirectory.get()}/cargo")

val libPath = when {
    osName.contains("win") -> "${cargoBuildDir}/debug"
    osName.contains("mac") -> "${cargoBuildDir}/debug"
    osName.contains("nux") -> "${cargoBuildDir}/debug"
    else -> throw GradleException("Unsupported OS")
}
val libSuffix = when {
    osName.contains("win") -> "windows-$osArch"
    osName.contains("mac") -> "macos-$osArch"
    osName.contains("nux") -> "linux-$osArch"
    else -> throw GradleException("Unsupported OS")
}
val libName = System.mapLibraryName("xxdc_oss_tictactoe")

//
// Rust Build Start
//
tasks.register<Exec>("formatRust") {
    onlyIf { hasCargo }
    workingDir = file("src/main/rust")
    commandLine = listOf("cargo", "fmt")
}
// alias for formatRust
tasks.register<Exec>("cargoFmt") {
    dependsOn("formatRust")
}

tasks.register<Exec>("buildRust") {
    onlyIf { hasCargo }
    // https://doc.rust-lang.org/cargo/reference/environment-variables.html
    environment("CARGO_TARGET_DIR", cargoBuildDir.absolutePath)
    workingDir = file("src/main/rust")
    commandLine = listOf("cargo", "build")
}
// alias for buildRust
tasks.register<Exec>("cargoBuild") {
    dependsOn("buildRust")
}

tasks.named("compileJava") {
    if (hasCargo) dependsOn("buildRust")
}
// clean up the Rust build artifacts
tasks.register<Delete>("cleanRust") {
    delete(cargoBuildDir)
}
tasks.named("clean") {
    dependsOn("cleanRust")
}
tasks.named("build") {
    if (hasCargo) dependsOn("formatRust")
}
tasks.register<Copy>("copyLib") {
    onlyIf { hasCargo }
    from(libPath)
    into("src/main/resources/native")
    include(libName)
}
tasks.named("processResources") {
    if (hasCargo) dependsOn("copyLib")
}
java {
    withSourcesJar()
}
tasks.named("sourcesJar") {
    if (hasCargo) {
        dependsOn(tasks.named("copyLib"))
        inputs.files(tasks.named("copyLib").get().outputs.files)
    }
}
//
// Rust Build End
//

// Publishing
mavenPublishing {
    publishToMavenCentral(automaticRelease = true)
    val skipSigning = ((findProperty("skipSigning") as String?)?.toBoolean() == true) ||
        gradle.startParameter.taskNames.any { it.contains("publishToMavenLocal") }
    val signingKey = (findProperty("signingInMemoryKey") ?: findProperty("signing.key")) as String?
    if (!skipSigning && signingKey != null) {
        signAllPublications()
    }
    coordinates (
        project.group as String?,
        "tictactoe-native-$libSuffix",
        project.version as String?
    )
    pom {
        name.set("tictactoe-native-$libSuffix")
        description.set("An Over-Engineered Tic Tac Toe Game Native Library")
    }
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/briancorbinxyz/overengineering-tictactoe")
            credentials {
                username = findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
                password = findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

signing {
    val inMemoryKey = findProperty("signingInMemoryKey") as String?
    val inMemoryKeyId = findProperty("signingInMemoryKeyId") as String?
    val inMemoryKeyPassword = findProperty("signingInMemoryKeyPassword") as String?

    if (project.hasProperty("useGpg")) {
        useGpgCmd()
        sign(publishing.publications)
    } else if (!inMemoryKey.isNullOrBlank()) {
        useInMemoryPgpKeys(inMemoryKeyId, inMemoryKey, inMemoryKeyPassword)
        sign(publishing.publications)
    } else {
        logger.lifecycle("Signing keys not configured; skipping signing for publications.")
    }
}
