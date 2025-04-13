import com.diffplug.gradle.spotless.SpotlessExtension
import org.gradle.api.Project

plugins {
    // Apply the java Plugin to add support for Java.
    java
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/briancorbinxyz/overengineering-tictactoe")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

// Automatic code formatting before compile
tasks.named("compileJava") {
    dependsOn("spotlessApply")
}

// Code formatting (./gradlew spotlessApply)
fun Project.configureSpotless() {
    pluginManager.apply("com.diffplug.spotless")
    
    extensions.configure<SpotlessExtension> {
        java {
            // TODO: Remove once googleJavaFormat understands modern switch expressions
            // Step 'google-java-format' found problem in 'src/main/java/org/xxdc/oss/example/GamePersistence.java':
            // 77:27: error: : or -> expected
            // com.google.googlejavaformat.java.FormatterException: 77:27: error: : or -> expected
            targetExclude(
                "**/GamePersistence.java",
                "**/GameClient.java",
                "**/**Persona.java",
                "**/PlayerPrinter.java",
            )
            googleJavaFormat("1.25.2")
                .reflowLongStrings()
            removeUnusedImports()
        }
        kotlin {
            ktlint()
        }
    }
}
configureSpotless()

dependencies {
    constraints {
        // Define dependency versions as constraints
        implementation("org.apache.commons:commons-text:1.11.0")
    }
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

val projectVersion by extra("2.0.14")

public val jdkVersion = 24
// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(jdkVersion)
        vendor = JvmVendorSpec.ADOPTIUM
    }
}

val isSnapshot = false
version = if (isSnapshot) {
    "$projectVersion-jdk${jdkVersion}-SNAPSHOT"
} else {
    "$projectVersion-jdk${jdkVersion}"
}

// Signing Checks
tasks.register("checkSigningSetup") {
    group = "verification"
    description = "Verifies that signing-related environment variables are set."

    doLast {
        val requiredEnvVars = listOf(
            "ORG_GRADLE_PROJECT_signingInMemoryKey",
            "ORG_GRADLE_PROJECT_signingInMemoryKeyId",
            "ORG_GRADLE_PROJECT_signingInMemoryKeyPassword"
        )

        val missing = requiredEnvVars.filter { System.getenv(it).isNullOrBlank() }

        if (missing.isNotEmpty()) {
            throw GradleException(
                "❌ Missing required environment variables for signing: ${missing.joinToString()}\n" +
                        "Ensure these are set in your environment or CI configuration."
            )
        } else {
            println("✔ All required signing environment variables are set.")
        }
    }
}


