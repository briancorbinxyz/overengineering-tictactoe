<!--
  Sync Impact Report
  ==================
  Version change: 1.1.0 → 1.2.0 (MINOR — existing principle materially expanded)

  Modified principles:
    - I. Educational Parity with OpenJDK → added MUST requirement:
      any newly introduced JEP MUST be documented in README.md with
      its JEP number, title, and the module(s) demonstrating it.

  Added sections: None

  Removed sections: None

  Templates requiring updates:
    - .specify/templates/plan-template.md        ✅ compatible
    - .specify/templates/spec-template.md         ✅ compatible
    - .specify/templates/tasks-template.md        ✅ compatible

  Follow-up TODOs: None
-->

# Over-Engineering Tic-Tac-Toe Constitution

## Core Principles

### I. Educational Parity with OpenJDK

Every module and feature MUST demonstrate or reference at least one
specific JEP (JDK Enhancement Proposal). Code MUST include comments
or Javadoc explaining *why* a language feature is used, not merely
*what* it does. New features MUST target the current GA JDK version
and use only finalized (non-preview) language features on the main
branch. Preview features are permitted on `jdkXX` branches per
Principle IX. Any newly introduced JEP MUST be documented in
`README.md` with its JEP number, title, and the module(s)
demonstrating it.

### II. Modular Decomposition

The project MUST maintain clean separation across independently
publishable modules: **api** (core game logic), **app** (desktop/CLI),
**native** (Rust FFI), and **tcp-gameserver** (networked play).
Dependencies MUST flow in one direction: app and tcp-gameserver
depend on api; native depends on api. No reverse or circular
dependencies are permitted. Each module MUST be buildable and
testable in isolation.

### III. Sealed Types & Exhaustive Patterns

Domain abstractions MUST use sealed classes or sealed interfaces to
restrict implementations to known types (e.g., `Player`, `BotStrategy`,
`StrategicTurningPoint`). Switch expressions and pattern matching
MUST be exhaustive — default branches are prohibited when the
compiler can guarantee coverage. Records MUST be preferred over
mutable POJOs for value objects.

### IV. Convention-Based Build

All shared build configuration MUST reside in Gradle convention
plugins under `buildSrc/`. Module-level `build.gradle.kts` files
MUST NOT duplicate rules already expressed in convention plugins.
Code formatting MUST be enforced automatically via Spotless
(Google Java Format) with a pre-commit hook. The main branch MUST
NOT enable `--enable-preview` or `--enable-native-access=ALL-UNNAMED`
unless the features used are finalized in the target JDK. The
`jdkXX` branches MAY enable these flags for preview feature work.

### V. Test-Driven Feature Addition

Every new JEP demonstration or feature addition MUST include
corresponding tests using TestNG. Tests MUST exercise real
implementations — mocking is discouraged in favor of integration
tests that run full game flows. Test class names MUST follow the
`*Test.java` convention and mirror the source package structure.

### VI. Security & Cryptography Standards

Cryptographic implementations MUST use standard JCE SPI interfaces
(e.g., KEM API for Kyber-ML-KEM). Raw algorithm usage without a
provider framework is prohibited. Deserialization of untrusted input
MUST apply `ObjectInputFilter` (JEP 415). Network transport MUST
use authenticated encryption (AES-GCM) with proper key derivation
(HKDF).

### VII. Performance as a Feature

The project MUST maintain AOT compilation support (JEP 483/514),
GraalVM Native Image compatibility, and ZGC as the default garbage
collector. Virtual threads MUST be used for I/O-bound concurrency
(e.g., `GameServer`). JMH benchmarks MUST exist for
performance-critical paths. Performance regressions MUST be
investigated before merging.

### VIII. Publishing as a First-Class Concern

All modules MUST be publishable to Maven Central and GitHub Packages.
Artifacts MUST follow semantic versioning with a JDK suffix
(e.g., `3.1.0-jdk25`). Artifact signing MUST use in-memory PGP keys
via CI. Release candidates (`-rc1`, `-rc2`) MUST be used for
validation before final releases.

### IX. Branch Stability & JDK Versioning

The **main** branch MUST NOT require a JDK version that is not
generally available (GA). The main branch MUST NOT enable or depend
on any JEP features that are still in preview status. Feature work
targeting a future or pre-GA JDK MUST be developed on a `jdkXX`
branch (e.g., `jdk25`, `jdk26`). These `jdkXX` branches MAY use
preview features, but new features added on a `jdkXX` branch MUST
be based on new JEP features (or other enhancements) introduced in
that specific JDK version — not arbitrary preview features from
earlier versions. A `jdkXX` branch MUST only be merged to main once
all features it depends on have reached final/GA status in the
target JDK.

### X. Dependency Minimalism

External dependencies MUST be avoided unless they are referenced in
application code exclusively through JDK SPIs (e.g., JCE providers,
`java.util.spi` interfaces, `System.LoggerFinder`). Libraries that
require direct import of third-party packages in production source
code are prohibited. Test-only dependencies (e.g., TestNG, Logback
for SLF4J bridge) are exempt from this rule. Build-time-only
dependencies (e.g., Spotless, Gradle plugins) are also exempt.

## Technology & Build Standards

- **Language**: Java (current GA: JDK 25, Azul Zulu distribution)
- **Secondary Languages**: Kotlin (build scripts), Rust (native FFI)
- **Build System**: Gradle 8.7+ with Kotlin DSL
- **Test Framework**: TestNG 7.5.1
- **Code Formatting**: Spotless + Google Java Format 1.27.0
- **Logging**: `System.getLogger()` with SLF4J bridge (no direct
  Logback dependency in production source)
- **CI/CD**: GitHub Actions (build on push/PR to main; publish on
  release)
- **Commit Convention**: Conventional Commits —
  `<type>(<scope>): <subject>` where type is one of feat, fix, chore,
  docs, refactor, build, test; scope is one of api, app, native,
  tcp-gameserver, build, ci

## Development Workflow & Quality Gates

- **Pre-commit Hook**: `./gradlew spotlessApply` MUST run
  automatically before every commit via the `installGitHook` Gradle
  task.
- **CI Gate**: All tests MUST pass on GitHub Actions before a PR can
  be merged to main.
- **PR Reviews**: All changes to main MUST go through a pull request.
- **Preview Features**: `--enable-preview` MUST only be enabled on
  `jdkXX` branches. The main branch MUST compile and test without
  preview flags.
- **Branch Strategy**: `jdkXX` branches (e.g., `jdk25`, `jdk26`)
  for preview/pre-GA JDK work; merged to `main` only after all
  dependent features reach GA. Other feature branches (e.g.,
  `security`) merged via PR to `main`.
- **Commit Quality**: Commit messages MUST follow Conventional Commits
  format. The body SHOULD explain *why* the change was made. Breaking
  changes MUST use `feat!:` prefix or `BREAKING CHANGE:` footer.

## Governance

This constitution is the authoritative source for project principles
and standards. All pull requests and code reviews MUST verify
compliance with these principles. Amendments to this constitution
require:

1. A pull request with a clear rationale for the change.
2. An updated version number following semantic versioning:
   - **MAJOR**: Principle removals or incompatible redefinitions.
   - **MINOR**: New principles added or existing ones materially
     expanded.
   - **PATCH**: Clarifications, wording fixes, non-semantic edits.
3. Update of the `Last Amended` date.
4. A sync impact review of dependent templates
   (plan-template, spec-template, tasks-template).

Complexity beyond what these principles prescribe MUST be justified
in the plan's Complexity Tracking table. Refer to `CLAUDE.md` for
runtime development guidance.

**Version**: 1.2.0 | **Ratified**: 2026-03-14 | **Last Amended**: 2026-03-14
