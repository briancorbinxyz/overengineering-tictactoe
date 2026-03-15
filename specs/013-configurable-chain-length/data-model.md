# Data Model: Configurable Winning Chain Length

**Branch**: `013-configurable-chain-length` | **Date**: 2026-03-14

## Entity Changes

### GameBoard (modified)

| Attribute | Type | Constraint | Notes |
|-----------|------|------------|-------|
| dimension | int | ≥ 1 | Existing — board side length (N) |
| chainLength | int | 2 ≤ K ≤ dimension | **New** — consecutive markers needed to win. Defaults to dimension |
| content | String[] | length = dimension² | Existing — cell contents |

**Validation rules**:
- chainLength MUST be ≥ 2 (chain of 1 is trivially won on first move)
- chainLength MUST be ≤ dimension (cannot require more cells than a line contains)
- chainLength is immutable after board creation

**State transitions**:
- `withMove(marker, position)` → returns new GameBoard preserving the same chainLength
- `hasChain(marker)` → checks for K consecutive markers in any row, column, or diagonal
- `hasWinnableChain()` → **New** — returns true if any player can still complete a chain of length K

### TcpProtocol (modified)

| Field | Type | Version | Notes |
|-------|------|---------|-------|
| version | int | v1, v2 | **Changed** — bumped to 2 |
| message | String | v1, v2 | Existing — "start", "nextMove", exit |
| assignedPlayerMarker | String | v1, v2 | Existing — in start message |
| chainLength | int | v2 only | **New** — in start message |
| state.board.chainLength | int | v2 only | **New** — in nextMove board section |

**Backward compatibility**:
- v2 client receiving v1 message → chainLength defaults to board dimension
- v1 client receiving v2 message → rejects gracefully (unknown version)

### GameBoardLocalImpl (modified)

| Component | Type | Notes |
|-----------|------|-------|
| dimension | int | Existing record component |
| chainLength | int | **New** record component |
| content | String[] | Existing record component |

**Serialization impact**: Adding a record component changes the serialized form. Legacy deserialization must default chainLength to dimension.

### TicTacToeGameBoard (modified — native interop)

| Field | Type | Notes |
|-------|------|-------|
| chainLength | int | **New** — passed to native library at construction |

**FFI signature change**: `new_game_board(dimension, chainLength)` — two int parameters instead of one.

## Entities NOT Changed

- **GameState** — accesses chainLength transitively via `board.chainLength()`
- **Game** — passes chainLength to board factory; no new stored state beyond constructor parameter
- **BotPlayer / BotStrategy** — chain-length agnostic; delegates to `state.hasChain()`
- **BotStrategyConfig** — no chain-length-specific config needed
- **GamePersistence** — serialization logic unchanged; record serialization handles new field

## Relationships

```
Game ──creates──▶ GameBoard(dimension, chainLength)
                      │
                      ├──▶ GameBoardLocalImpl (Java win detection)
                      └──▶ GameBoardNativeImpl → TicTacToeGameBoard (FFI win detection)
                                                      │
                                                      └──▶ Rust native library

GameState ──references──▶ GameBoard.chainLength()
     │
     ├──▶ BotStrategy.bestMove() → state.hasChain() → board.hasChain()
     ├──▶ Analyzers → StrategicTurningPoint → board.hasChain()
     └──▶ TcpProtocol.toNextMoveState() → board serialization includes chainLength
```
