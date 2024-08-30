import java.io.File

plugins {
    // Apply the application plugin to add support for building a CLI application in Java.
    application
    id("org.graalvm.buildtools.native") version "0.10.2"
    id("com.diffplug.spotless") version "7.0.0.BETA1"
    `maven-publish`
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
    gradlePluginPortal()
}

val jdkVersion = "22"

// Automatic code formatting before compile
tasks.named("compileJava") {
    dependsOn("spotlessApply")
}

dependencies {
    // JDK21: KEM SPI (Third-Party)
    // https://central.sonatype.com/artifact/org.bouncycastle/bcprov-jdk18on
    // -> JDK API -> Bouncycastle
    implementation("org.bouncycastle:bcprov-jdk18on:1.78.1")
    implementation(project(":lib"))

    // JDK9: Platform Logging (Third-Party)
    // -> JDK API -> SLF4J -> Logback
    runtimeOnly("ch.qos.logback:logback-classic:1.5.6")
    runtimeOnly("org.slf4j:slf4j-api:2.0.13")
    runtimeOnly("org.slf4j:slf4j-jdk-platform-logging:2.0.13")


    // JDK23: JMH (Third-Party) Not required, added for benchmarking
    // https://github.com/openjdk/jmh
    implementation("org.openjdk.jmh:jmh-core:1.37")
    annotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:1.37")
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

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(jdkVersion)
    }
    withJavadocJar()
    withSourcesJar()
}

// Code formatting (./gradlew spotlessApply)
spotless {
    java {
        googleJavaFormat("1.23.0")
            .reflowLongStrings()
    }
}

// Allow GraalVM native AOT compilation
graalvmNative {
    binaries {
        all {
            javaLauncher = javaToolchains.launcherFor {
                // NB: On MacOS ARM ARCH the native-image implementation is not available
                // for the versions of GRAAL_VM Community edition - selecting Oracle
                languageVersion = JavaLanguageVersion.of(jdkVersion)
                vendor = JvmVendorSpec.matching("Oracle")
                // languageVersion = JavaLanguageVersion.of(17)
                // vendor = JvmVendorSpec.GRAAL_VM
            }
        }
    }
}

application {
    // Define the main class for the application.
    mainClass = "org.xxdc.oss.example.App"
    // JDK22: Foreign Function Interface (FFI)
    // Resolves Warning:
    // WARNING: A restricted method in java.lang.foreign.SymbolLookup has been called
    // WARNING: java.lang.foreign.SymbolLookup::libraryLookup has been called by org.xxdc.oss.example.GameBoardNativeImpl in an unnamed module
    // WARNING: Use --enable-native-access=ALL-UNNAMED to avoid a warning for callers in this module
    // WARNING: Restricted methods will be blocked in a future release unless native access is enabled
    applicationDefaultJvmArgs = listOf("--enable-native-access=ALL-UNNAMED", "-XX:+UseZGC")
}

tasks.run.configure {
    // Override the empty stream to allow for interactive runs with gradlew run
    standardInput = System.`in`
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
            artifactId = "tictactoe-app"
            version = "1.0.0-jdk$jdkVersion"
            from(components["java"])
            pom {
                name.set("tictactoe")
                description.set("An Over-Engineered Tic Tac Toe game")
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