# System Feature Map — Over-Engineering Tic-Tac-Toe

**Generated**: 2026-03-14
**Repository Coverage**: 100%

---

## System Feature Map

### Candidate Capabilities (Phase 1)

| Capability | Source Modules | Evidence |
|---|---|---|
| Game orchestration & lifecycle | api | Game.java, GameTest.java |
| Game state transitions | api | GameState.java, GameStateTest.java |
| Board representation & win detection | api, native | GameBoard.java, GameBoardLocalImpl.java, GameBoardTest.java |
| Human player console input | api | HumanPlayer.java |
| Bot AI move selection (6 algorithms) | api | bot/*.java, MinimaxTest.java, AlphaBetaTest.java, etc. |
| Strategic turning point analysis | api | analysis/Analyzers.java, AnalyzersTest.java |
| Live & post-game commentary | api | commentary/*.java |
| Game state serialization/persistence | api | GamePersistence.java |
| TCP client-server multiplayer | tcp-gameserver | GameServer.java, GameClient.java, TcpProtocol.java |
| Encrypted transport (post-quantum) | tcp-gameserver | SecureDuplexMessageHandler.java, KyberKEMSpi.java |
| Native board via foreign function interface | native | TicTacToeLibrary.java, GameBoardNativeImpl.java |
| Runtime bot code generation | api | NaiveFifoGenerator.java, NaiveFifoGeneratorTest.java |
| Game context & metadata descriptors | api | GameContext.java, GameStateDescriptor.java, GameDateDescriptor.java |

---

## Capability → Feature Mapping (Phase 2)

| Feature | Capabilities Grouped | Short Name |
|---|---|---|
| Game Lifecycle Management | Game orchestration, state transitions, history | game-lifecycle |
| Game Board | Board representation, move validation, win detection | game-board |
| Human Player Input | Console input, move validation, retry on invalid | human-player-input |
| Bot AI Strategies | Random, Minimax, AlphaBeta, MaxN, Paranoid, MCTS | bot-ai-strategies |
| Game Analysis | Strategic turning point identification | game-analysis |
| Game Commentary | Live commentary, post-analysis commentary, personas | game-commentary |
| Game Persistence | Serialization, deserialization filters, file snapshots | game-persistence |
| Networked Multiplayer | TCP server, client, protocol, virtual threads | networked-multiplayer |
| Secure Transport | Post-quantum key exchange, AES-GCM encryption, HKDF | secure-transport |
| Native Board Interop | Foreign function calls to native library, fallback | native-board-interop |
| Runtime Bot Generation | Dynamic strategy generation via class-file API | runtime-bot-generation |
| Game Metadata | Game context, state descriptors, date descriptors | game-metadata |

---

## Feature Inventory (Phase 3)

| # | Feature | Short Name | Evidence | Confidence |
|---|---|---|---|---|
| 1 | Game Lifecycle Management | game-lifecycle | Game.java, GameState.java, GameTest.java, GameplayTest.java | High |
| 2 | Game Board | game-board | GameBoard.java, GameBoardLocalImpl.java, GameBoardTest.java | High |
| 3 | Human Player Input | human-player-input | HumanPlayer.java | High |
| 4 | Bot AI Strategies | bot-ai-strategies | bot/Random.java, bot/Minimax.java, bot/AlphaBeta.java, bot/MaxN.java, bot/Paranoid.java, bot/MonteCarloTreeSearch.java, MinimaxTest.java, AlphaBetaTest.java, MaxNTest.java, ParanoidTest.java, MonteCarloTreeSearchTest.java | High |
| 5 | Game Analysis | game-analysis | analysis/Analyzers.java, analysis/StrategicTurningPoint.java, AnalyzersTest.java | High |
| 6 | Game Commentary | game-commentary | commentary/CommentaryPersona.java, commentary/*Persona.java | High |
| 7 | Game Persistence | game-persistence | GamePersistence.java | High |
| 8 | Networked Multiplayer | networked-multiplayer | GameServer.java, GameClient.java, TcpProtocol.java, TcpTransportTest.java, GameServerPerformanceTest.java | High |
| 9 | Secure Transport | secure-transport | SecureDuplexMessageHandler.java, KyberKEMSpi.java, SecureConnectionTest.java, SecureKyberTest.java | High |
| 10 | Native Board Interop | native-board-interop | TicTacToeLibrary.java, TicTacToeGameBoard.java, GameBoardNativeImpl.java, TicTacToeLibraryTest.java | High |
| 11 | Runtime Bot Generation | runtime-bot-generation | NaiveFifoGenerator.java, NaiveFifoGeneratorTest.java | High |
| 12 | Game Metadata | game-metadata | GameContext.java, GameStateDescriptor.java, GameDateDescriptor.java, GameContextTest.java, GameStateDescriptorTest.java | High |

---

## Feature Extraction Task List (Phase 4)

- [x] game-lifecycle
- [x] game-board
- [x] human-player-input
- [x] bot-ai-strategies
- [x] game-analysis
- [x] game-commentary
- [x] game-persistence
- [x] networked-multiplayer
- [x] secure-transport
- [x] native-board-interop
- [x] runtime-bot-generation
- [x] game-metadata

---

## Repository Coverage

- [x] api/src/main/java/org/xxdc/oss/example/ (core)
- [x] api/src/main/java/org/xxdc/oss/example/bot/ (AI strategies)
- [x] api/src/main/java/org/xxdc/oss/example/bot/custom/ (code generation)
- [x] api/src/main/java/org/xxdc/oss/example/analysis/ (analysis)
- [x] api/src/main/java/org/xxdc/oss/example/commentary/ (commentary)
- [x] api/src/main/java/org/xxdc/oss/example/transport/ (transport interfaces)
- [x] api/src/test/java/ (all test suites)
- [x] app/src/main/java/ (application entry points)
- [x] tcp-gameserver/src/main/java/ (server, client, protocol, security)
- [x] tcp-gameserver/src/test/java/ (server tests)
- [x] native/src/main/java/ (FFI interop)
- [x] native/src/main/rust/ (native library)
- [x] native/src/test/java/ (native tests)

**Coverage: 100%**
