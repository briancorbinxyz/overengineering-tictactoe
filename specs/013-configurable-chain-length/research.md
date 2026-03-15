# Research: Configurable Winning Chain Length

**Branch**: `013-configurable-chain-length` | **Date**: 2026-03-14

## R1: How does current win detection work?

**Decision**: Win detection is hardcoded to board dimension in `GameBoardLocalImpl.hasChain()`.

**Rationale**: The `hasChain()` method iterates rows, columns, and diagonals, comparing a running chain counter against `dimension`. All four detection blocks (rows, columns, main diagonal, anti-diagonal) use `chain == dimension` as the win condition. This must change to `chain == chainLength`.

**Alternatives considered**: None — this is the only code path for win detection in the local implementation.

## R2: Which components need code changes?

**Decision**: Changes required in 8 files across 3 modules. Bot strategies (Minimax, AlphaBeta, MaxN, Paranoid, MCTS) need NO changes — they delegate to `board.hasChain()` which is polymorphic.

**Rationale**: After tracing call chains from `Game` → `GameState` → `GameBoard`:
- `GameState.hasChain()` delegates to `board.hasChain()` — no changes needed
- `GameState.isTerminal()` uses `hasChain()` + `hasMovesAvailable()` — no changes needed
- All bot strategies call `state.hasChain()` — no changes needed
- `Analyzers` and `StrategicTurningPoint` call `board.hasChain()` — minor updates for center-square heuristic only

**Files requiring changes**:
1. `api/.../GameBoard.java` — Add `chainLength()` method, update `withDimension()` factory
2. `api/.../GameBoardLocalImpl.java` — Add `chainLength` field, update `hasChain()` logic, add early draw detection
3. `api/.../Game.java` — Accept chainLength in constructors, pass to board factory
4. `api/.../analysis/StrategicTurningPoint.java` — Update center-square heuristic
5. `tcp-gameserver/.../TcpProtocol.java` — Bump to v2, add chainLength to JSON format
6. `native/.../TicTacToeLibrary.java` — Add chainLength to factory method
7. `native/.../TicTacToeGameBoard.java` — Pass chainLength through FFI
8. `native/.../GameBoardNativeImpl.java` — Add chainLength constructor overload

**Files requiring NO changes**: GameState, Minimax, AlphaBeta, MaxN, Paranoid, MonteCarloTreeSearch, Random, GamePersistence (Java serialization handles new fields), BotStrategyConfig, BotPlayer, HumanPlayer.

## R3: How does the chain detection algorithm need to change?

**Decision**: The current algorithm must be generalized from "full row/column/diagonal match" to "sliding window of length K within a row/column/diagonal."

**Rationale**: Currently, `hasChain()` checks if ALL cells in a row/column/diagonal match. For configurable chain length K < N, it must find any K consecutive matching cells. The algorithm changes from "count matches in a line, compare to dimension" to "track consecutive matches in a line, compare to chainLength." The chain counter must reset on non-matching cells rather than accumulating across the whole line.

**Alternatives considered**:
- Precompute all possible winning positions at board creation time (more memory, faster checks) — rejected for simplicity; the current scanning approach is efficient for small boards.

## R4: How should early draw detection work?

**Decision**: After each move, scan all possible winning lines to check if any player can still complete a chain of length K. If no player can win, declare a draw.

**Rationale**: On an N×N board with chain length K < N, there are more possible winning lines than when K = N. A draw occurs when every possible K-length window in every row, column, and diagonal is blocked (contains markers from both players). This can be checked incrementally but a full scan after each move is acceptable for the board sizes in scope (≤5×5).

**Alternatives considered**:
- Check only when board is >50% full — rejected; early draw detection should be correct at all times.
- Maintain a counter of possible wins per player — more complex, deferred as optimization.

## R5: What is the native FFI impact?

**Decision**: The native Rust library's `new_game_board` function must accept a second integer parameter for chain length. The FFI descriptor must be updated from one `JAVA_INT` to two.

**Rationale**: The current FFI signature is `new_game_board(dimension: i32) -> *mut GameBoard`. It must become `new_game_board(dimension: i32, chain_length: i32) -> *mut GameBoard`. The `has_chain` native function does not need signature changes — it already delegates to internal board state. However, the Rust implementation must store and use chain length internally.

**Alternatives considered**:
- Pass chain length per `has_chain` call instead of at construction — rejected; chain length is immutable and should be set once at board creation per the spec.

## R6: TCP Protocol v2 format

**Decision**: Bump protocol version to 2. Add `chainLength` field to the board section of all messages. Support v1 backward compatibility by defaulting missing chainLength to dimension.

**Rationale**: The start message format changes from:
```json
{"version":1,"message":"start","assignedPlayerMarker":"X"}
```
to:
```json
{"version":2,"message":"start","assignedPlayerMarker":"X","chainLength":3}
```

The nextMove message's board section changes from:
```json
{"dimension":3,"content":["X",null,...]}
```
to:
```json
{"dimension":3,"chainLength":3,"content":["X",null,...]}
```

Clients receiving v2 that don't support it must reject gracefully. Clients receiving v1 (no chainLength) default to dimension.

## R7: Serialization compatibility

**Decision**: Java serialization will handle the new field automatically via the record mechanism. Old saved games without chainLength will fail to deserialize — a migration path using `ObjectInputFilter` or a custom `readObject` is needed to default chainLength to dimension for legacy files.

**Rationale**: `GameBoardLocalImpl` is a record. Adding a new component changes its serialized form. Old files will produce `InvalidClassException`. The serialVersionUID should remain stable if a custom deserialization fallback is provided, or be bumped with a migration utility.

**Alternatives considered**:
- Break backward compatibility for saved games — rejected; spec requires faithful restoration.
