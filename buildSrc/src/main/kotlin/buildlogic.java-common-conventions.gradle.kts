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
            googleJavaFormat("1.23.0")
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

val projectVersion by extra("1.1.0")

public val jdkVersion = 22
// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(jdkVersion)
        vendor = JvmVendorSpec.ADOPTIUM
    }
}
version = "$projectVersion-jdk${jdkVersion}"