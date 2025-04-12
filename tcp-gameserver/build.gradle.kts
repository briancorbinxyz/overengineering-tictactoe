import java.util.*
import org.gradle.api.Action

plugins {
    id("buildlogic.java-library-conventions")
    signing
}

dependencies {
    // JDK21: KEM SPI (Third-Party)
    // https://central.sonatype.com/artifact/org.bouncycastle/bcprov-jdk18on
    // -> JDK API -> Bouncycastle
    implementation("org.bouncycastle:bcprov-jdk18on:1.78.1")

    // JDK9: Platform Logging (Third-Party)
    // -> JDK API -> SLF4J -> Logback
    testRuntimeOnly("ch.qos.logback:logback-classic:1.5.6")
    testRuntimeOnly("org.slf4j:slf4j-api:2.0.13")
    testRuntimeOnly("org.slf4j:slf4j-jdk-platform-logging:2.0.13")

    implementation(project(":api"))
}

java {
    withJavadocJar()
    withSourcesJar()
}

tasks.named<Test>("test") {
    // JDK22: Foreign Function Interface (FFI)
    // Resolves Warning:
    // WARNING: A restricted method in java.lang.foreign.SymbolLookup has been called
    // WARNING: java.lang.foreign.SymbolLookup::libraryLookup has been called by org.xxdc.oss.example.GameBoardNativeImpl in an unnamed module
    // WARNING: Use --enable-native-access=ALL-UNNAMED to avoid a warning for callers in this module
    // WARNING: Restricted methods will be blocked in a future release unless native access is enabled
    jvmArgs = listOf("--enable-native-access=ALL-UNNAMED", "-XX:+UseZGC")
}

val enablePreviewFeatures = true

val collectorArgs = listOf(
    "-XX:+UseZGC"
)
val standardArgs = listOf(
    "--enable-native-access=ALL-UNNAMED",
) + collectorArgs

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
}
// https://docs.gradle.org/current/userguide/publishing_maven.html
publishing {
    repositories {
        // Publish to GitHub Packages
        // https://docs.github.com/en/actions/use-cases-and-examples/publishing-packages/publishing-java-packages-with-gradle
        val targetRepo: String? = findProperty("repo") as String?
        if (targetRepo == null || targetRepo == "Sonatype") {
            maven {
                name = "Sonatype"
                url = uri(
                    if (version.toString().endsWith("SNAPSHOT"))
                        //"https://s01.oss.sonatype.org/content/repositories/snapshots/"
                        "https://central.sonatype.com/repository/maven-snapshots/"
                    else
                       // "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
                        "https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2"
                )
                credentials {
                    username = project.findProperty("sonatype.user") as String? ?: System.getenv("SONATYPE_USER")
                    password = project.findProperty("sonatype.key") as String? ?: System.getenv("SONATYPE_TOKEN")
                }
            }
        }
        if (targetRepo == null || targetRepo == "GitHubPackages") {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/briancorbinxyz/overengineering-tictactoe")
                credentials {
                    username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
                    password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
                }
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            groupId = "org.xxdc.oss.example"
            artifactId = "tictactoe-tcp-gameserver"
            from(components["java"])
            pom {
                name.set("tictactoe")
                description.set("An Over-Engineered Tic Tac Toe Game API")
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

fun decodeKey(raw: String): String =
    if (raw.contains("-----BEGIN PGP PRIVATE KEY BLOCK-----")) {
        raw
    } else {
        String(Base64.getDecoder().decode(raw))
    }

val rawSigningKey = System.getenv("SIGNING_KEY") ?: findProperty("signing.key") as String?
val signingKey = rawSigningKey?.let(::decodeKey)

val signingPassword = System.getenv("SIGNING_PASSWORD") ?: findProperty("signing.password") as String?
val signingKeyId = System.getenv("SIGNING_KEY_ID") ?: findProperty("signing.keyId") as String?

val isPublishing = gradle.startParameter.taskNames.any { it.contains("publish", ignoreCase = true) }

val shouldSign = signingKey != null && signingPassword != null

logger.lifecycle("🔐 Signing check:")
logger.lifecycle("  • isPublishing: $isPublishing")
logger.lifecycle("  • signingKeyId: ${signingKeyId != null}")
logger.lifecycle("  • signingKey present: ${signingKey != null}")
logger.lifecycle("  • signingPassword present: ${signingPassword != null}")
logger.lifecycle("  • shouldSign: $shouldSign")
signing {
    setRequired({ isPublishing && shouldSign })
    useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
    sign(configurations.runtimeElements.get())
}