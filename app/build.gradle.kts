plugins {
    // Apply the application plugin to add support for building a CLI application in Java.
    application
    id("org.graalvm.buildtools.native") version "0.10.2"
    id("com.diffplug.spotless") version "7.0.0.BETA1"
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    // JDK21: KEM SPI (Third-Party)
    // https://central.sonatype.com/artifact/org.bouncycastle/bcprov-jdk18on
    // -> JDK API -> Bouncycastle
    implementation("org.bouncycastle:bcprov-jdk18on:1.78.1")

    // JDK9: Platform Logging (Third-Party)
    // -> JDK API -> SLF4J -> Logback
    implementation("ch.qos.logback:logback-classic:1.5.6")
    implementation("org.slf4j:slf4j-api:2.0.13")
    implementation("org.slf4j:slf4j-jdk-platform-logging:2.0.13")
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
        languageVersion = JavaLanguageVersion.of(21)
    }
}

// Code formatting (./gradlew spotlessApply)
spotless {
    java {
        googleJavaFormat().reflowLongStrings().aosp()
    }
}

// Allow GraalVM native AOT compilation
graalvmNative {
    binaries {
        all {
            javaLauncher = javaToolchains.launcherFor {
                // NB: On MacOS ARM ARCH the native-image implementation is not available
                // for the versions of GRAAL_VM Community edition - selecting Oracle
                languageVersion = JavaLanguageVersion.of(22)
                vendor = JvmVendorSpec.matching("Oracle")
                // languageVersion = JavaLanguageVersion.of(17)
                // vendor = JvmVendorSpec.GRAAL_VM
            }
        }
    }
}

application {
    // Define the main class for the application.
    mainClass = "org.example.App"
}

tasks.run.configure {
    // Override the empty stream to allow for interactive runs with gradlew run
    standardInput = System.`in`
}
