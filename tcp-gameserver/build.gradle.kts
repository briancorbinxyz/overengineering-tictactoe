import java.util.*
import com.vanniktech.maven.publish.SonatypeHost

plugins {
    id("buildlogic.java-library-conventions")
    id("com.vanniktech.maven.publish")
    id("signing")
}

group = "org.xxdc.oss.example"

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

// Publishing
mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)
    val signingKey = (findProperty("signingInMemoryKey") ?: findProperty("signing.key")) as String?
    if (signingKey != null) {
        signAllPublications()
    }

    coordinates (
        project.group as String?,
        "tictactoe-tcp-gameserver",
        project.version as String?
    )
    pom {
        name.set("tictactoe-tcp-gameserver")
        description.set("An Over-Engineered Tic Tac Toe Game Server (TCP)")
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
    if (project.hasProperty("useGpg")) {
        useGpgCmd()
    }
    sign(publishing.publications)
}
