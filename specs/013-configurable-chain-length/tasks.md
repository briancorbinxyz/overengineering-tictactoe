# Tasks: Configurable Winning Chain Length

**Input**: Design documents from `/specs/013-configurable-chain-length/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: Tests are included per constitution Principle V (Test-Driven Feature Addition).

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **api module**: `api/src/main/java/org/xxdc/oss/example/`
- **api tests**: `api/src/test/java/org/xxdc/oss/example/`
- **tcp-gameserver module**: `tcp-gameserver/src/main/java/org/xxdc/oss/example/transport/tcp/`
- **tcp-gameserver tests**: `tcp-gameserver/src/test/java/org/xxdc/oss/example/transport/tcp/`
- **native module**: `native/src/main/java/org/xxdc/oss/example/`
- **native rust**: `native/src/main/rust/src/`
- **native tests**: `native/src/test/java/org/xxdc/oss/example/`

---

## Phase 1: Setup

**Purpose**: No new project structure needed — all changes are to existing files. Verify baseline.

- [x] T001 Run `./gradlew build` to verify all existing tests pass before any changes

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Extend the GameBoard interface and local implementation with chainLength support. All user stories depend on this.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [x] T002 Add `int chainLength()` default method to `api/src/main/java/org/xxdc/oss/example/GameBoard.java` returning `dimension()` as default. Add static factory overload `withDimension(int dimension, int chainLength)` that validates 2 ≤ chainLength ≤ dimension and throws `IllegalArgumentException` on invalid input. Existing `withDimension(int dimension)` delegates to the new overload with `chainLength = dimension`.

- [x] T003 Add `chainLength` as a record component to `api/src/main/java/org/xxdc/oss/example/GameBoardLocalImpl.java`. Update the compact constructor to validate 2 ≤ chainLength ≤ dimension. Update `withMove()` to preserve chainLength when creating a new board instance. Update `asJsonString()` to include `chainLength` in the JSON output. Handle legacy deserialization: if a serialized board from a prior version lacks chainLength, default it to dimension. Update serialVersionUID if needed.

- [x] T004 Generalize `hasChain(String playerMarker)` in `api/src/main/java/org/xxdc/oss/example/GameBoardLocalImpl.java` to detect `chainLength` consecutive markers (not `dimension` consecutive markers). For each row, column, and diagonal: track a running count of consecutive matching cells, reset on mismatch, and return true when count reaches `chainLength`. This replaces the current `chain == dimension` comparisons.

- [x] T005 Add `hasWinnableChain()` method to `api/src/main/java/org/xxdc/oss/example/GameBoard.java` (default method returning `true`) and implement in `api/src/main/java/org/xxdc/oss/example/GameBoardLocalImpl.java`. Scan all possible K-length windows in rows, columns, and diagonals. Return false only when every possible window contains markers from multiple players (no player can complete a chain). Used for early draw detection.

- [x] T006 Update `isTerminal()` in `api/src/main/java/org/xxdc/oss/example/GameState.java` to use `!board.hasWinnableChain()` as an additional terminal condition alongside `!board.hasMovesAvailable()` and win detection. When no winnable chain remains, the game ends as a draw.

- [x] T007 Update `api/src/main/java/org/xxdc/oss/example/Game.java` constructors to accept an optional `chainLength` parameter. Pass it to `GameBoard.withDimension(size, chainLength)`. Existing constructors default chainLength to the board dimension for backward compatibility.

- [x] T008 Write tests in `api/src/test/java/org/xxdc/oss/example/GameBoardTest.java`: (a) 3×3 board with default chainLength=3 detects wins correctly (backward compat), (b) 5×5 board with chainLength=3 detects 3-in-a-row wins across rows, columns, and diagonals, (c) 5×5 board with chainLength=3 does NOT detect 2-in-a-row as a win, (d) board creation with chainLength > dimension throws exception, (e) board creation with chainLength < 2 throws exception, (f) `withMove()` preserves chainLength, (g) `hasWinnableChain()` returns false when no K-length winning window remains, (h) chainLength is preserved identically after multiple consecutive `withMove()` calls (immutability verification for FR-010).

**Checkpoint**: Core board with configurable chain length is functional. All existing 3×3 tests still pass.

---

## Phase 3: User Story 1 — Play with Default Chain Length (Priority: P1) 🎯 MVP

**Goal**: Verify that existing games (no explicit chain length) behave identically.

**Independent Test**: Run a 3×3 bot-vs-bot game without specifying chain length — result must be identical to pre-change behavior.

### Implementation for User Story 1

- [x] T009 [US1] Write backward-compatibility test in `api/src/test/java/org/xxdc/oss/example/GameTest.java`: run a full bot-vs-bot game on a 3×3 board without specifying chain length, verify the game completes with a valid terminal state and correct history length.

- [x] T010 [US1] Write test in `api/src/test/java/org/xxdc/oss/example/GameBoardTest.java`: create a board via `withDimension(3)` (no chainLength arg) and verify `chainLength()` returns 3 (equals dimension).

**Checkpoint**: Default chain length = dimension confirmed. Full backward compatibility.

---

## Phase 4: User Story 2 — Play with Custom Chain Length (Priority: P1)

**Goal**: A 5×5 board with chain length 3 correctly detects 3-in-a-row wins.

**Independent Test**: Play a game on a 5×5 board with chainLength=3, verify 3-in-a-row triggers a win.

### Implementation for User Story 2

- [x] T011 [US2] Write test in `api/src/test/java/org/xxdc/oss/example/GameBoardTest.java`: on a 5×5 board with chainLength=3, place 3 consecutive markers in a row and verify `hasChain()` returns true. Place only 2 and verify false.

- [x] T012 [US2] Write test in `api/src/test/java/org/xxdc/oss/example/GameBoardTest.java`: on a 5×5 board with chainLength=5, verify behavior is identical to default (5-in-a-row required).

- [x] T013 [US2] Write end-to-end test in `api/src/test/java/org/xxdc/oss/example/GameTest.java`: run a bot-vs-bot game on a 5×5 board with chainLength=3, verify the game completes with a valid win (not a draw — wins are very likely with short chains on large boards).

**Checkpoint**: Custom chain length games work correctly.

---

## Phase 5: User Story 3 — Bot AI Adapts to Chain Length (Priority: P1)

**Goal**: Bot strategies correctly identify winning/blocking moves for any chain length.

**Independent Test**: Present a bot with a 5×5 board where a 3-in-a-row win is one move away, verify it takes the winning move.

### Implementation for User Story 3

- [x] T014 [US3] Write test in `api/src/test/java/org/xxdc/oss/example/MinimaxTest.java`: on a 5×5 board with chainLength=3, set up a state where the bot can win with one move (two markers in a row + one empty), verify Minimax selects the winning position.

- [x] T015 [US3] Write test in `api/src/test/java/org/xxdc/oss/example/AlphaBetaTest.java`: same scenario as T014 but using AlphaBeta strategy, verify it selects the winning move and blocks opponent winning moves.

- [x] T016 [P] [US3] Write test in `api/src/test/java/org/xxdc/oss/example/MonteCarloTreeSearchTest.java`: on a 5×5 board with chainLength=3, verify MCTS selects a valid move and recognizes winning opportunities within its time budget.

**Checkpoint**: All bot strategies work correctly with custom chain lengths. No code changes to bot strategies themselves — they delegate to board.hasChain().

---

## Phase 6: User Story 4 — Chain Length Validated on Creation (Priority: P2)

**Goal**: Invalid chain length configurations are rejected before the game starts.

**Independent Test**: Attempt to create boards with invalid chain lengths and verify rejection.

### Implementation for User Story 4

- [x] T017 [US4] Write validation tests in `api/src/test/java/org/xxdc/oss/example/GameBoardTest.java`: (a) chainLength=4 on 3×3 board throws exception, (b) chainLength=0 throws exception, (c) chainLength=-1 throws exception, (d) chainLength=1 throws exception, (e) chainLength=2 on 3×3 board succeeds (valid edge case).

**Checkpoint**: All invalid configurations are properly rejected.

---

## Phase 7: User Story 5 — Chain Length in Networked Play (Priority: P2)

**Goal**: Server transmits chain length in protocol v2 messages, clients apply it correctly.

**Independent Test**: Start a networked game with custom chain length, verify both sides use the same win condition.

### Implementation for User Story 5

- [x] T018 [US5] Update `tcp-gameserver/src/main/java/org/xxdc/oss/example/transport/tcp/TcpProtocol.java`: bump version constant to 2. Add `chainLength` field to `GAME_STARTED_JSON_FORMAT`. Add `chainLength` to the board section of `NEXT_MOVE_JSON_FORMAT`. Update `fromNextMoveState()` parser to extract chainLength from JSON and pass to `GameBoard.withDimension(dimension, chainLength)`. Add backward compatibility: if chainLength is missing in received message, default to dimension.

- [x] T019 [US5] Write tests in `tcp-gameserver/src/test/java/org/xxdc/oss/example/transport/tcp/TcpProtocolTest.java`: (a) v2 start message includes chainLength, (b) v2 nextMove message board section includes chainLength, (c) parsing a v1 message (no chainLength) defaults to dimension, (d) round-trip: serialize and parse a game state with custom chainLength, verify fidelity.

**Checkpoint**: Networked games correctly transmit and apply custom chain lengths.

---

## Phase 8: Native Board Interop

**Goal**: Native board implementation supports configurable chain length identically to local implementation.

**Independent Test**: Run the same chain length tests against native board and verify identical results.

### Implementation

- [x] T020 Update `native/src/main/java/org/xxdc/oss/example/interop/TicTacToeLibrary.java`: add overloaded `newGameBoard(int dimension, int chainLength)` factory method. Update FFI function descriptor to accept two int parameters.

- [x] T021 Update `native/src/main/java/org/xxdc/oss/example/interop/TicTacToeGameBoard.java`: add `chainLength` field, accept it in constructor, pass to native `new_game_board(dimension, chainLength)` call. Update FFI downcall descriptor from single int to two ints. Implement `chainLength()` accessor.

- [x] T022 Update `native/src/main/java/org/xxdc/oss/example/GameBoardNativeImpl.java`: add constructor overload accepting chainLength, pass to `library.newGameBoard(dimension, chainLength)`.

- [x] T023 Update `native/src/main/rust/src/lib.rs`: modify `new_game_board` FFI function to accept a second `chain_length: i32` parameter. Store chain length in native board struct. Update win detection (`has_chain`) to use chain_length instead of dimension.

- [x] T024 Write tests in `native/src/test/java/org/xxdc/oss/example/GameBoardNativeImplTest.java`: (a) native board with chainLength=3 on 5×5 board detects 3-in-a-row, (b) native and local implementations produce identical results for same inputs across multiple dimension/chainLength combinations, (c) default chainLength equals dimension.

**Checkpoint**: Native board is chain-length-aware and matches local implementation behavior.

---

## Phase 9: Polish & Cross-Cutting Concerns

**Purpose**: Analysis updates, serialization compatibility, and final verification.

- [x] T025 Update `api/src/main/java/org/xxdc/oss/example/analysis/StrategicTurningPoint.java`: modify `moveTakesCenterSquareControl()` to account for chain length — if chainLength < dimension, center control is a lower-priority turning point. Add comment explaining the heuristic.

- [x] T026 Write test in `api/src/test/java/org/xxdc/oss/example/analysis/AnalyzersTest.java`: verify turning point detection works correctly on a 5×5 board with chainLength=3.

- [x] T027 [P] Verify serialization backward compatibility: write test that creates a `GameBoardLocalImpl` with chainLength, serializes it, deserializes it, and confirms chainLength is preserved. Also test deserializing a legacy GameBoardLocalImpl (without chainLength field) and verify chainLength defaults to dimension. Document any serialVersionUID changes needed.

- [x] T028 [P] Run `./gradlew build` to verify all tests pass across all modules (api, app, tcp-gameserver, native).

- [x] T029 Update `README.md` per constitution Principle I: if any new JEP is leveraged, document it with JEP number, title, and module. (Note: this feature does not introduce a new JEP, so only update the feature list if applicable.)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — verify baseline
- **Foundational (Phase 2)**: Depends on Phase 1 — BLOCKS all user stories
- **US1 (Phase 3)**: Depends on Phase 2 — backward compatibility verification
- **US2 (Phase 4)**: Depends on Phase 2 — custom chain length games
- **US3 (Phase 5)**: Depends on Phase 2 — bot AI verification
- **US4 (Phase 6)**: Depends on Phase 2 — validation rules
- **US5 (Phase 7)**: Depends on Phase 2 — networked play
- **Native (Phase 8)**: Depends on Phase 2 — can run in parallel with US1-US5
- **Polish (Phase 9)**: Depends on all prior phases

### User Story Dependencies

- **US1 (P1)**: Independent — tests backward compatibility only
- **US2 (P1)**: Independent — tests custom chain length
- **US3 (P1)**: Independent — tests bot correctness (no bot code changes needed)
- **US4 (P2)**: Independent — tests validation rules (already in foundational phase)
- **US5 (P2)**: Independent — tests protocol changes

### Within Each User Story

- Tests written first (per constitution Principle V)
- Core implementation before integration
- Story complete before moving to next priority

### Parallel Opportunities

- After Phase 2, US1-US5 can all proceed in parallel (different test files, no conflicts)
- Phase 8 (native) can run in parallel with US1-US5
- T025 and T027 are independent and can run in parallel in Phase 9

---

## Parallel Example: After Phase 2

```bash
# All of these can run in parallel after foundational phase:
Task: T009-T010  [US1] Backward compatibility tests
Task: T011-T013  [US2] Custom chain length tests
Task: T014-T016  [US3] Bot AI tests
Task: T017       [US4] Validation tests
Task: T018-T019  [US5] Protocol v2 implementation + tests
Task: T020-T024  Native board interop
```

---

## Implementation Strategy

### MVP First (User Story 1 + 2 Only)

1. Complete Phase 1: Verify baseline
2. Complete Phase 2: Add chainLength to GameBoard (CRITICAL)
3. Complete Phase 3: Verify backward compatibility (US1)
4. Complete Phase 4: Verify custom chain length (US2)
5. **STOP and VALIDATE**: 3×3 default games + 5×5 custom chain games both work

### Incremental Delivery

1. Setup + Foundational → Chain-length-aware board
2. Add US1 → Backward compatibility confirmed
3. Add US2 → Custom chain length games work
4. Add US3 → Bot AI verified
5. Add US4 → Validation hardened
6. Add US5 → Networked play updated
7. Add Native → FFI updated
8. Polish → Analysis, serialization, final build

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Bot strategies (Minimax, AlphaBeta, MaxN, Paranoid, MCTS, Random) need NO code changes — they delegate to board.hasChain()
- GameState needs only a minor isTerminal() update for early draw detection
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
