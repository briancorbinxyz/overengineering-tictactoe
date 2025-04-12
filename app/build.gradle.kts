import java.io.File

plugins {
    // Apply the application plugin to add support for building a CLI application in Java.
    id("buildlogic.java-application-conventions")
    id("org.graalvm.buildtools.native") version "0.10.2"
    signing
}

dependencies {
    // JDK21: KEM SPI (Third-Party)
    // https://central.sonatype.com/artifact/org.bouncycastle/bcprov-jdk18on
    // -> JDK API -> Bouncycastle
    implementation("org.bouncycastle:bcprov-jdk18on:1.78.1")
    implementation(project(":api"))

    // JDK9: Platform Logging (Third-Party)
    // -> JDK API -> SLF4J -> Logback
    runtimeOnly("ch.qos.logback:logback-classic:1.5.6")
    runtimeOnly("org.slf4j:slf4j-api:2.0.13")
    runtimeOnly("org.slf4j:slf4j-jdk-platform-logging:2.0.13")
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    withJavadocJar()
    withSourcesJar()
}

// Allow GraalVM native AOT compilation
graalvmNative {
    binaries {
        all {
            javaLauncher = javaToolchains.launcherFor {
                // NB: On MacOS ARM ARCH the native-image implementation is not available
                // for the versions of GRAAL_VM Community edition - selecting Oracle
                languageVersion = java.toolchain.languageVersion
                vendor = JvmVendorSpec.matching("Oracle")
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
        maven {
            name = "Sonatype"
            url = uri(
                if (version.toString().endsWith("SNAPSHOT"))
                    "https://s01.oss.sonatype.org/content/repositories/snapshots/"
                else
                    "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
            )
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
            from(components["java"])
            pom {
                name.set("tictactoe")
                description.set("An Over-Engineered Tic Tac Toe Game and Game Server")
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
// Signing
afterEvaluate {
    // Check if we are running any kind of publish task
    val isPublishing = gradle.startParameter.taskNames.any { it.contains("publish", ignoreCase = true) }

    if (isPublishing && project.hasProperty("signing.key")) {
        signing {
            useInMemoryPgpKeys(
                findProperty("signing.keyId") as String?,
                findProperty("signing.key") as String?,
                findProperty("signing.password") as String?
            )
            sign(publishing.publications["maven"])
        }
    }
}
// Install a pre-commit hook to run the Gradle task "spotlessApply" before committing changes.
tasks.register("installGitHook") {
    doLast {
        val hooksDir = file("${rootDir}/.git/hooks")
        val preCommitFile = File(hooksDir, "pre-commit")

        if (!preCommitFile.exists()) {
            preCommitFile.writeText(
                """
                #!/bin/sh
                ./gradlew spotlessApply
                """.trimIndent()
            )
            preCommitFile.setExecutable(true)
            println("Pre-commit hook installed.")
        } else {
            println("Pre-commit hook already exists.")
        }
    }
}
tasks.named("build") {
    dependsOn("installGitHook")
}

// JEP483: Ahead-of-Time Class Loading & Linking
// Print classpath to more easily enable AOT setup
// java --enable-native-access=ALL-UNNAMED --enable-preview -cp "$(gradle -q printClasspath)" org.xxdc.oss.example.App
//
// https://openjdk.org/jeps/483
// 1. Record AOT Configuration
// java -XX:AOTMode=record -XX:AOTConfiguration=app.aotconf --enable-native-access=ALL-UNNAMED --enable-preview  -cp "$(gradle -q printClasspath)" org.xxdc.oss.example.AppTrainer
// 2. Record AOT Cache
// java -XX:AOTMode=create -XX:AOTConfiguration=app.aotconf -XX:AOTCache=app.aot --enable-native-access=ALL-UNNAMED --enable-preview -cp "$(gradle -q printClasspath)"
// 3. Run the app with the AOT Cache
// java -XX:AOTCache=app.aot -cp "$(gradle -q printClasspath)" --enable-native-access=ALL-UNNAMED --enable-preview org.xxdc.oss.example.AppTrainer
// or
// java -XX:AOTCache=app.aot -cp "$(gradle -q printClasspath)" --enable-native-access=ALL-UNNAMED --enable-preview  org.xxdc.oss.example.App
tasks.register("printClasspath") {
    doLast {
        //val runtimeClasspath = configurations.runtimeClasspath.get().asPath
        val runtimeClasspath = sourceSets.main.get().runtimeClasspath.asPath
        println(runtimeClasspath)
    }
}
// NB: AOT cache cannot be created if there are non-empty directories in the classpath
tasks.register("buildClasspath") {
    dependsOn("jar") // Ensure the JAR is built before printing the classpath
    doLast {
        val jarPath = tasks.named("jar").get().outputs.files.singleFile.absolutePath
        val runtimeClasspath = configurations.runtimeClasspath.get().asPath
        println("$jarPath:$runtimeClasspath")
    }
}

// TODO: Disable preview features on the branch when the next JDK is released
val enablePreviewFeatures = true
val standardArgs = listOf(
    "--enable-native-access=ALL-UNNAMED",
    "-XX:+UseZGC"
)

tasks.named<Test>("test") {
    jvmArgs = if (enablePreviewFeatures) {
        listOf("--enable-preview") + standardArgs
    } else {
        standardArgs
    }
}

if (enablePreviewFeatures) {
    tasks.withType<JavaCompile>().configureEach {
        options.compilerArgs.addAll(listOf("--enable-preview"))
    }
    
    tasks.withType<JavaExec>().configureEach {
        jvmArgs("--enable-preview")
    }
    
    tasks.withType<Javadoc>() {
        (options as StandardJavadocDocletOptions).apply {
            addBooleanOption("-enable-preview", true)    
            source = "24"
        }
    }

    application {
        applicationDefaultJvmArgs = listOf("--enable-preview")
    }

}

