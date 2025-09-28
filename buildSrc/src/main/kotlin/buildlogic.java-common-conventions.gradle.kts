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
                "**/AppLite.java",
                "**/AppLiteHttp.java",
                "**/GamePersistence.java",
                "**/GameClient.java",
                "**/**Persona.java",
                "**/PlayerPrinter.java",
                "**/GameContextTest.java",
            )
            googleJavaFormat("1.27.0")
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

val projectVersion by extra("3.0.1")

val jdkVersion = 25
// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(jdkVersion)
        // Disable vendor check, enabling use of early access JDKs
        vendor = JvmVendorSpec.AZUL
    }
}

val isSnapshot = false
version = if (isSnapshot) {
    "$projectVersion-jdk${jdkVersion}-SNAPSHOT"
} else {
    "$projectVersion-jdk${jdkVersion}"
}
