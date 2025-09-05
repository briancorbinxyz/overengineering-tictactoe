![Over-Engineering TicTacToe](oe-tictactoe.png)

Over-Engineering Tic-Tac-Toe
---

Tic-Tac-Toe in Java deliberately over-engineered to apply features of Java introduced over time.

Developed to pair with the ongoing blog post: [Road to JDK 25 - Over-Engineering Tic-Tac-Toe](https://sympatheticengineering.com/Library/03-Resources/Road-to-JDK-25---Over-Engineering-Tic-Tac-Toe!) also serialized to Medium @ [Road to JDK 25 - Over-Engineering Tic-Tac-Toe On Medium](https://briancorbinxyz.medium.com/list/road-to-jdk-25-d0f656f66a8f)

---

### Features

https://openjdk.org/projects/jdk/25/

- **JEP512**:   Compact Source Files and Instance Main Methods
  - See: [AppLite.java](app/src/main/java/org/xxdc/oss/example/AppLite.java) — compact source file with a top-level `main`.
  - How to run: [run_lite.sh](app/scripts/run_lite.sh)
- **JEP506**:   Scoped Values (See: [Game.java](api/src/main/java/org/xxdc/oss/example/Game.java), [GameTest.java](api/src/test/java/org/xxdc/oss/example/GameTest.java))
  - Uses `ScopedValue` to bind a per-play `GameContext` (see: [GameContext.java](api/src/main/java/org/xxdc/oss/example/GameContext.java)) with `id` and `createdAt`, scoped strictly to `play()` execution; verified in tests.

https://openjdk.org/projects/jdk/24/

- **JEP496**:   Quantum-Resistant Module-Lattice-Based Key Encapsulation Mechanism (See: [KyberKEMSpi.java](tcp-gameserver/src/main/java/org/xxdc/oss/example/security/KyberKEMSpi.java), [SecureConnectionTest.java](tcp-gameserver/src/test/java/org/xxdc/oss/example/security/SecureConnectionTest.java))
  - Implements ML-KEM via JCE SPI and uses BouncyCastle PQC; tests establish secure client/server handshake.
- **JEP485**:   Stream Gatherers (See: [Analyzers.java](api/src/main/java/org/xxdc/oss/example/analysis/Analyzers.java), [StrategicTurningPoint.java](api/src/main/java/org/xxdc/oss/example/analysis/StrategicTurningPoint.java))
  - Custom `Gatherer` discovers strategic turning points while streaming `GameState` history.
- **JEP483**:   Ahead-of-Time Class Loading & Linking (See: [app/scripts/](app/scripts/), e.g., [1_aot_record.sh](app/scripts/1_aot_record.sh), [2_aot_create.sh](app/scripts/2_aot_create.sh), [3a_aot_run.sh](app/scripts/3a_aot_run.sh))
  - Scripts record, generate, and run with AOT class data to improve startup.
- **JEP484**:   Class-File API (See: [NaiveFifoGenerator.java](api/src/main/java/org/xxdc/oss/example/bot/custom/NaiveFifoGenerator.java))
  - Uses the `java.lang.classfile` API to generate a simple bot class at runtime.

https://openjdk.org/projects/jdk/23/

- **JEP467**:   Markdown Documentation Comments	(See: [Player.java](api/src/main/java/org/xxdc/oss/example/Player.java))
  - Uses Markdown-friendly documentation with `@snippet` to embed example usage.
- **JEP474**:   ZGC: Generational Mode by Default
  - Runtime/flag-level feature; no code changes required here.
- **JEP471**:   Deprecate the Memory-Access Methods in sun.misc.Unsafe for Removal
  - Not used; project favors standard APIs like FFM and `Cleaner`.

https://openjdk.org/projects/jdk/22/

- **JEP454**:	Foreign Function & Memory API (See: [TicTacToeLibrary.java](native/src/main/java/org/xxdc/oss/example/interop/TicTacToeLibrary.java), [TicTacToeGameBoard.java](native/src/main/java/org/xxdc/oss/example/interop/TicTacToeGameBoard.java))
  - Calls native functions via `Linker`/`SymbolLookup`, uses `MemorySegment` and upcall stubs.
- **JEP456**:	Unnamed Variables & Patterns (See: [PlayerPrinter.java](api/src/main/java/org/xxdc/oss/example/PlayerPrinter.java), [GameClient.java](tcp-gameserver/src/main/java/org/xxdc/oss/example/GameClient.java))
  - Demonstrates unnamed catch variables: e.g., `catch (UnknownHostException _)` and `catch (ConnectException _)`.

https://openjdk.org/projects/jdk/21/

- **JEP431**:	Sequenced Collections (See: `history()` in [Game.java](api/src/main/java/org/xxdc/oss/example/Game.java))
  - Uses `SequencedCollection<GameState>` for ordered game history.
- **JEP439**:	Generational ZGC
  - Runtime/flag-level feature; no code changes required here.
- **JEP440**:	Record Patterns (See: [GameContextTest.java](api/src/test/java/org/xxdc/oss/example/GameContextTest.java))
  - Demonstrates record pattern deconstruction and guarded cases in a `switch` over `GameContext`.
- **JEP441**:	Pattern Matching for switch (See: `handleException(...)` in [GameServer.java](tcp-gameserver/src/main/java/org/xxdc/oss/example/GameServer.java))
  - Uses a `switch` with type patterns to handle root causes (e.g., `SocketTimeoutException`).
- **JEP444**:	Virtual Threads (See: [GameServer.java](tcp-gameserver/src/main/java/org/xxdc/oss/example/GameServer.java))
  - Handles many concurrent games using `Thread.ofVirtual()` and `Executors.newThreadPerTaskExecutor`.
- **JEP452**:	Key Encapsulation Mechanism API (See: [KyberKEMSpi.java](tcp-gameserver/src/main/java/org/xxdc/oss/example/security/KyberKEMSpi.java))
  - Implements the JCE KEM SPI to integrate ML-KEM with the standard crypto API.

https://openjdk.org/projects/jdk/20/

- No new features

https://openjdk.org/projects/jdk/19/

- No new features

https://openjdk.org/projects/jdk/18/

- **JEP400**:	UTF-8 by Default
  - Source files and runtime default charset assume UTF-8; no special handling required.
- **JEP408**:	Simple Web Server
  - Not used in this project; server components use TCP sockets.
- **JEP413**:	Code Snippets in Java API Documentation (See: [Player.java](api/src/main/java/org/xxdc/oss/example/Player.java))
  - Uses `@snippet` to embed example usage directly in Javadoc.
- **JEP421**:	Deprecate Finalization for Removal (See: [TicTacToeGameBoard.java](native/src/main/java/org/xxdc/oss/example/interop/TicTacToeGameBoard.java))
  - Avoids finalization in favor of `Cleaner` for native resource management.

https://openjdk.org/projects/jdk/17/

- **JEP421**:	Deprecate Finalization for Removal
  - Uses `Cleaner` over finalization for native resource management.
- **JEP409**:	Sealed Classes (See: [StrategicTurningPoint.java](api/src/main/java/org/xxdc/oss/example/analysis/StrategicTurningPoint.java))
  - `StrategicTurningPoint` is a sealed interface with record implementations.
- **JEP410**:	Remove the Experimental AOT and JIT Compiler
  - Alternative: Use GraalVM Native Image via Gradle (See: [app/build.gradle.kts](app/build.gradle.kts) — `org.graalvm.buildtools.native`)
    - Example tasks: `./gradlew :app:nativeCompile`, `./gradlew :app:nativeRun`
- **JEP415**:	Context-Specific Deserialization Filters (See: [GamePersistence.java](api/src/main/java/org/xxdc/oss/example/GamePersistence.java))
  - Applies an `ObjectInputFilter` before deserialization and uses guarded checks.

---

### Algorithms

The following algorithms are used by the AI BOT in this project - for a detailed discussion see [Road to JDK 25 - An Algorithmic Interlude](https://briancorbinxyz.medium.com/over-engineering-tic-tac-toe-an-algorithmic-interlude-8af3aa13173a):

- [Random](https://en.wikipedia.org/wiki/Randomness) See: [Random.java](api/src/main/java/org/xxdc/oss/example/bot/Random.java)
- [Minimax](https://en.wikipedia.org/wiki/Minimax) See: [Minimax.java](api/src/main/java/org/xxdc/oss/example/bot/Minimax.java)
- [Minimax w. Alpha-Beta](https://en.wikipedia.org/wiki/Alpha-beta_pruning) See: [AlphaBeta.java](api/src/main/java/org/xxdc/oss/example/bot/AlphaBeta.java)
- [MaxN](https://en.wikipedia.org/wiki/Maxn_algorithm) See: [MaxN.java](api/src/main/java/org/xxdc/oss/example/bot/MaxN.java)
- [Paranoid](https://en.wikipedia.org/wiki/Paranoid_AI) See: [Paranoid.java](api/src/main/java/org/xxdc/oss/example/bot/Paranoid.java)
- [Monte Carlo Tree Search](https://en.wikipedia.org/wiki/Monte_Carlo_method) See [MonteCarloTreeSearch.java](api/src/main/java/org/xxdc/oss/example/bot/MonteCarloTreeSearch.java)


---

### Quick Start

- To run the single game application, use the following command: `./gradlew run`

- If you don't have Java installed on your system you can install it first with [SDKMAN](https://sdkman.io/) to build with a JDK 24 toolchain:

```bash
curl -s "https://get.sdkman.io" | bash

sdk install java 24-tem

./gradlew run
```

#### Native Image (GraalVM)

- Requires JDK with GraalVM Native Image tooling. This project is configured with the Gradle plugin `org.graalvm.buildtools.native` in `app/build.gradle.kts`.
- Build and run the native image:

```bash
./gradlew :app:nativeCompile
./gradlew :app:nativeRun
```

#### Simple Web Server (JEP 408)

- Serve the project directory (e.g., to host `index.html`) using JDK’s built-in `jwebserver`:

```bash
app/scripts/serve.sh 8000 .
# then open http://localhost:8000
```

---

### Related

- [Over-Engineering Tic-Tac-Toe - CLI](https://github.com/briancorbinxyz/overengineering-tictactoe-cli) Run overengineered tic-tac-toe from the command line
