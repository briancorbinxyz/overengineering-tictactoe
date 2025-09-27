import java.util.*
import com.vanniktech.maven.publish.SonatypeHost

plugins {
    id("buildlogic.java-library-conventions")
    // Apply the maven-publish plugin from com.vanniktech for publishing to repositories
    id("com.vanniktech.maven.publish")
    id("signing")
}

group = "org.xxdc.oss.example"

val libPath = "native/src/main/rust/target/debug"
val osName = System.getProperty("os.name").lowercase()
val osArch = System.getProperty("os.arch").lowercase()

val libSuffix = when {
    osName.contains("win") -> "windows-$osArch"
    osName.contains("mac") -> "macos-$osArch"
    osName.contains("nux") -> "linux-$osArch"
    else -> throw GradleException("Unsupported OS")
}

dependencies {
    // Native Library (Rust)

    // JDK9: Platform Logging (Third-Party)
    // -> JDK API -> SLF4J -> Logback
    testRuntimeOnly("ch.qos.logback:logback-classic:1.5.6")
    testRuntimeOnly("org.slf4j:slf4j-api:2.0.13")
    testRuntimeOnly("org.slf4j:slf4j-jdk-platform-logging:2.0.13")

    // JDK23: JMH (Third-Party) Not required, added for benchmarking
    // https://github.com/openjdk/jmh
    testImplementation("org.openjdk.jmh:jmh-core:1.37")
    testAnnotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:1.37")
}

// Run JMH benchmark
// ./gradlew jmh
tasks.register<JavaExec>("jmh") {
    mainClass.set("org.openjdk.jmh.Main")
    classpath = sourceSets["test"].runtimeClasspath
    args = listOf("org.xxdc.oss.example.interop.benchmark.PlayerIdsBenchmark")
}

// TODO: Disable preview features on the branch when the next JDK is released
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
            addStringOption("Xmaxwarns", "1")
            addBooleanOption("-enable-preview", true)
            source = "25"
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
        "tictactoe-api",
        project.version as String?
    )
    pom {
        name.set("tictactoe-api")
        description.set("An Over-Engineered Tic Tac Toe Game API")
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
