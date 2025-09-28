plugins {
    id("com.vanniktech.maven.publish") version "0.34.0" apply false
    signing
}

tasks.register("checkSigningSetup") {
    group = "verification"
    description = "Verifies that signing-related environment variables are set."

    doLast {
        val requiredEnvVars = listOf(
            "signingInMemoryKey",
            "signingInMemoryKeyId",
            "signingInMemoryKeyPassword"
        )

        val missing = requiredEnvVars.filter { (findProperty(it) as String?).isNullOrBlank() }

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

tasks.register("checkSigningEnvSetup") {
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

