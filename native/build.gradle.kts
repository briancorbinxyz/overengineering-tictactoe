import java.io.File

plugins {
    // Apply the application plugin to add support for building a CLI application in Java.
    id("buildlogic.java-library-conventions")
    id("maven-publish")
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
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
    workingDir = file("src/main/rust")
    commandLine = listOf("cargo", "fmt")
}
// alias for formatRust
tasks.register<Exec>("cargoFmt") {
    dependsOn("formatRust")
}

tasks.register<Exec>("buildRust") {
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
    dependsOn("buildRust")
}
// clean up the Rust build artifacts
tasks.register<Delete>("cleanRust") {
    delete(cargoBuildDir)
}
tasks.named("clean") {
    dependsOn("cleanRust")
}
// Force a build before running the application
tasks.named("build") {
    dependsOn("formatRust")
}
tasks.named<Jar>("jar") {
    from("${cargoBuildDir}/debug") {
        include(libName) 
    }
}
//
// Rust Build End
//
java {
}

// https://docs.gradle.org/current/userguide/publishing_maven.html
publishing {
    repositories {
        // Publish to GitHub Packages
        // https://docs.github.com/en/actions/use-cases-and-examples/publishing-packages/publishing-java-packages-with-gradle
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/briancorbinxyz/overengineering-tictactoe")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            groupId = "org.xxdc.oss.example"
            artifactId = "tictactoe-native-$libSuffix"
            from(components["java"])
            pom {
                name.set("tictactoe")
                description.set("An Over-Engineered Tic Tac Toe Native Library")
                url.set("https://github.com/briancorbinxyz/overengineering-tictactoe")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                    developers {
                        developer {
                            id.set("briancorbinxyz")
                            name.set("Brian Corbin")
                            email.set("mail@briancorbin.xyz")
                        }
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/briancorbinxyz/overengineering-tictactoe.git")
                    developerConnection.set("scm:git:ssh://github.com/briancorbinxyz/overengineering-tictactoe.git")
                    url.set("https://github.com/briancorbinxyz/overengineering-tictactoe")
                }
            }
        }
    }
}
