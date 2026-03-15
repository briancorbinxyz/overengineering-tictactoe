# Implementation Plan: Configurable Winning Chain Length

**Branch**: `013-configurable-chain-length` | **Date**: 2026-03-14 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/013-configurable-chain-length/spec.md`

## Summary

Add a configurable winning chain length (K-in-a-row) to the game board, independent of the board dimension (N×N). The default chain length equals the board dimension for full backward compatibility. This is a cross-cutting change affecting the board interface, native FFI, TCP protocol (v2), game persistence, and strategic analysis. Bot AI strategies require no changes — they delegate win checking to the board.

## Technical Context

**Language/Version**: Java (JDK 25, Azul Zulu) + Rust (native FFI) + Kotlin (build scripts)
**Primary Dependencies**: BouncyCastle (via JCE SPI — unaffected by this change)
**Storage**: Java serialization for game persistence (file-based snapshots)
**Testing**: TestNG 7.5.1
**Target Platform**: JVM (cross-platform) + macOS/Linux native library
**Project Type**: Multi-module library (api, app, tcp-gameserver, native)
**Performance Goals**: Bot move selection within existing time budgets (unbounded depth for tree-search, 2s for MCTS)
**Constraints**: Must not break existing 3×3 game behavior. Must follow sealed types and convention-based build principles.
**Scale/Scope**: Boards up to 5×5 (larger boards supported but not primary target)

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Educational Parity with OpenJDK | ✅ Pass | No new JEP introduced — this feature extends existing patterns |
| II. Modular Decomposition | ✅ Pass | Changes follow existing module boundaries (api, native, tcp-gameserver) |
| III. Sealed Types & Exhaustive Patterns | ✅ Pass | GameBoard interface extended, not replaced. Record patterns preserved. |
| IV. Convention-Based Build | ✅ Pass | No new build plugins or flags needed |
| V. Test-Driven Feature Addition | ✅ Pass | Tests planned for all chain length combinations |
| VI. Security & Cryptography Standards | ✅ Pass | No cryptographic changes |
| VII. Performance as a Feature | ✅ Pass | Early draw detection is a performance improvement. No regressions expected. |
| VIII. Publishing as a First-Class Concern | ✅ Pass | No publishing changes needed |
| IX. Branch Stability & JDK Versioning | ✅ Pass | Uses only finalized JDK 25 features |
| X. Dependency Minimalism | ✅ Pass | No new dependencies |

## Project Structure

### Documentation (this feature)

```text
specs/013-configurable-chain-length/
├── plan.md
├── spec.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   ├── gameboard-api.md
│   └── tcp-protocol-v2.md
├── checklists/
│   └── requirements.md
└── tasks.md              (Phase 2 — /speckit.tasks)
```

### Source Code (affected files)

```text
api/src/main/java/org/xxdc/oss/example/
├── GameBoard.java                    # Add chainLength(), withDimension(int,int), hasWinnableChain()
├── GameBoardLocalImpl.java           # Add chainLength field, generalize hasChain(), add draw detection
├── Game.java                         # Accept chainLength in constructors
└── analysis/
    └── StrategicTurningPoint.java    # Update center-square heuristic

api/src/test/java/org/xxdc/oss/example/
├── GameBoardTest.java                # Chain length win/draw detection tests
├── GameTest.java                     # End-to-end with custom chain length
├── MinimaxTest.java                  # Verify bot correctness with K ≠ N
└── analysis/
    └── AnalyzersTest.java            # Turning points with custom chain length

tcp-gameserver/src/main/java/org/xxdc/oss/example/transport/tcp/
└── TcpProtocol.java                  # Bump to v2, add chainLength field

tcp-gameserver/src/test/java/org/xxdc/oss/example/transport/tcp/
└── TcpProtocolTest.java              # v1/v2 backward compatibility tests

native/src/main/java/org/xxdc/oss/example/
├── GameBoardNativeImpl.java          # Add chainLength constructor
└── interop/
    ├── TicTacToeLibrary.java         # Add chainLength to factory
    └── TicTacToeGameBoard.java       # Update FFI descriptor

native/src/main/rust/src/
└── lib.rs                            # Accept chainLength in new_game_board

native/src/test/java/org/xxdc/oss/example/
└── GameBoardNativeImplTest.java      # Native chain length tests
```

**Structure Decision**: No new modules or directories. All changes modify existing files within the established module structure (api, tcp-gameserver, native).

## Complexity Tracking

> No constitution violations. No complexity justifications needed.

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| (none) | — | — |
