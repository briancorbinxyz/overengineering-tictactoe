import java.util.*
import java.util.Base64

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
            source = "25"
        }
    }
}

// Publishing
mavenPublishing {
    publishToMavenCentral(automaticRelease = true)
    val skipSigning = ((findProperty("skipSigning") as String?)?.toBoolean() == true)
    val signingKey = (findProperty("signingInMemoryKey") ?: findProperty("signing.key")) as String?
    if (!skipSigning && signingKey != null) {
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
    val skipSigning = ((findProperty("skipSigning") as String?)?.toBoolean() == true)
    if (skipSigning) {
        logger.lifecycle("skipSigning=true; skipping signing for publications.")
        return@signing
    }

    val inMemoryKeyBase64 = findProperty("signingInMemoryKeyBase64") as String?
    val inMemoryKeyPlain = (findProperty("signingInMemoryKey") ?: findProperty("signing.key")) as String?
    val inMemoryKey = when {
        !inMemoryKeyBase64.isNullOrBlank() -> String(Base64.getDecoder().decode(inMemoryKeyBase64), Charsets.UTF_8)
        else -> inMemoryKeyPlain
    }
    val inMemoryKeyId = findProperty("signingInMemoryKeyId") as String?
    val inMemoryKeyPassword = (findProperty("signingInMemoryKeyPassword") ?: findProperty("signing.password")) as String?

    if (project.hasProperty("useGpg")) {
        useGpgCmd()
        sign(publishing.publications)
    } else if (!inMemoryKey.isNullOrBlank()) {
        if (!inMemoryKeyId.isNullOrBlank()) {
            useInMemoryPgpKeys(inMemoryKeyId, inMemoryKey, inMemoryKeyPassword)
            sign(publishing.publications)
        } else {
            throw GradleException("In-memory signing key provided but signingInMemoryKeyId is missing. Provide signingInMemoryKeyId or set -PuseGpg or -PskipSigning=true.")
        }
    } else {
        throw GradleException("Signing is required but not configured. Provide -PuseGpg or in-memory signing properties (signingInMemoryKeyBase64 or signingInMemoryKey/signing.key, signingInMemoryKeyId, and signingInMemoryKeyPassword/signing.password), or set -PskipSigning=true to skip.")
    }
}

// Convenience tasks to print/build classpath similar to :app
// Enables scripts to run with: gradle -q :tcp-gameserver:buildClasspath
tasks.register("printClasspath") {
    doLast {
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
