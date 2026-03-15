# Feature Specification: Configurable Winning Chain Length

**Feature Branch**: `013-configurable-chain-length`
**Created**: 2026-03-14
**Status**: Draft
**Input**: User description: "Add configurable winning chain length across the system. The default should be the dimension length."

## Clarifications

### Session 2026-03-14

- Q: Should the protocol version be bumped when chain length is added to the start message? → A: Yes, bump to version 2; old clients reject v2 messages gracefully.
- Q: Should the cross-cutting scope include networked-multiplayer and game-persistence? → A: Yes, add both 007-game-persistence and 008-networked-multiplayer to scope.
- Q: Should the draw condition remain "board full with no winner" regardless of chain length? → A: No, draw = no possible winning chain remains for any player (early draw detection).
- Q: Where does chain length belong as a first-class property? → A: Property of the board (board carries dimension + chain length, does its own win detection).

## Cross-Cutting Scope

This feature modifies behavior across multiple existing features:

- **002-game-board**: Board must accept and enforce a configurable chain length for win detection
- **004-bot-ai-strategies**: AI strategies must account for the configured chain length when evaluating positions
- **005-game-analysis**: Turning point detection must use the configured chain length
- **007-game-persistence**: Serialized game state must include chain length for faithful restoration
- **008-networked-multiplayer**: Protocol must be versioned (v2) to transmit chain length in start message
- **010-native-board-interop**: Native board implementation must support the configurable chain length

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Play with Default Chain Length (Priority: P1)

A player starts a game on an N×N board without specifying a chain length. The system uses the board dimension N as the default winning chain length, preserving backward-compatible behavior.

**Why this priority**: Existing games must continue to work identically — no breaking changes to the default experience.

**Independent Test**: Can be tested by playing a 3×3 game without specifying chain length and verifying 3-in-a-row is still required to win.

**Acceptance Scenarios**:

1. **Given** a 3×3 board with no explicit chain length, **When** a player completes 3 markers in a row, **Then** the system declares a win.
2. **Given** a 4×4 board with no explicit chain length, **When** a player completes 4 markers in a row, **Then** the system declares a win.
3. **Given** a 4×4 board with no explicit chain length, **When** a player has only 3 markers in a row, **Then** the system does not declare a win.

---

### User Story 2 - Play with Custom Chain Length (Priority: P1)

A player starts a game on an N×N board with a custom winning chain length K (where K ≤ N). The system uses K-in-a-row as the win condition instead of the default N-in-a-row.

**Why this priority**: This is the core new capability — enabling game variants like "3-in-a-row on a 5×5 board."

**Independent Test**: Can be tested by configuring a 5×5 board with chain length 3 and verifying 3-in-a-row triggers a win.

**Acceptance Scenarios**:

1. **Given** a 5×5 board with chain length set to 3, **When** a player completes 3 markers in a row, **Then** the system declares a win.
2. **Given** a 5×5 board with chain length set to 3, **When** a player has only 2 markers in a row, **Then** the system does not declare a win.
3. **Given** a 5×5 board with chain length set to 5, **When** a player completes 5 markers in a row, **Then** the system declares a win (equivalent to default behavior).

---

### User Story 3 - Bot AI Adapts to Chain Length (Priority: P1)

Bot players adjust their strategy evaluation based on the configured chain length. A bot playing on a 5×5 board with chain length 3 must recognize and pursue 3-in-a-row opportunities rather than 5-in-a-row.

**Why this priority**: Bots must play correctly under custom chain length — without this, AI opponents are ineffective in variant games.

**Independent Test**: Can be tested by presenting a bot with a game state where a 3-in-a-row win is available on a 5×5 board and verifying it takes the winning move.

**Acceptance Scenarios**:

1. **Given** a 5×5 board with chain length 3 and the bot can win with one move, **When** the bot selects a move using a tree-search strategy, **Then** the bot selects the winning move.
2. **Given** a 5×5 board with chain length 3 and the opponent can win with one move, **When** the bot selects a move, **Then** the bot blocks the opponent's winning move.

---

### User Story 4 - Chain Length Validated on Creation (Priority: P2)

When a game is configured, the system validates that the chain length is within valid bounds. Invalid configurations are rejected before the game starts.

**Why this priority**: Prevents invalid game configurations that could cause unexpected behavior.

**Independent Test**: Can be tested by attempting to create a board with an invalid chain length and verifying rejection.

**Acceptance Scenarios**:

1. **Given** a 3×3 board, **When** chain length is set to 4 (greater than dimension), **Then** the configuration is rejected.
2. **Given** a 5×5 board, **When** chain length is set to 0 or negative, **Then** the configuration is rejected.
3. **Given** a 5×5 board, **When** chain length is set to 1, **Then** the configuration is rejected (trivial game — first move always wins).

---

### User Story 5 - Chain Length in Networked Play (Priority: P2)

When a game is played over the network, the server communicates the configured chain length to clients as part of the game start message so both sides apply the same win condition.

**Why this priority**: Ensures consistent game rules in distributed play.

**Independent Test**: Can be tested by starting a networked game with a custom chain length and verifying both client and server agree on the win condition.

**Acceptance Scenarios**:

1. **Given** a server configured with a 5×5 board and chain length 3, **When** clients connect, **Then** the start message includes the chain length.
2. **Given** a client that receives a start message with chain length 3, **When** the client evaluates win conditions locally, **Then** it uses chain length 3.

---

### Edge Cases

- What happens when chain length equals 2 on a 3×3 board? (Valid but unusual — very short games.)
- How does the native board implementation handle chain lengths different from the board dimension?
- Does game analysis correctly identify turning points when chain length differs from dimension?
- How is chain length serialized and deserialized for game persistence?
- On a 5×5 board with chain length 3, how does early draw detection perform when many possible chains remain?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST accept an optional chain length parameter when creating a game board.
- **FR-002**: When no chain length is specified, the system MUST default to the board dimension N (preserving existing behavior).
- **FR-003**: Win detection MUST use the configured chain length K — a player wins by completing K markers in a consecutive row, column, or diagonal.
- **FR-004**: Chain length MUST be validated: 2 ≤ K ≤ N (where N is the board dimension). Invalid values MUST be rejected at configuration time.
- **FR-005**: All bot AI strategies MUST evaluate positions using the configured chain length, not the board dimension.
- **FR-006**: Game analysis MUST detect turning points based on the configured chain length.
- **FR-007**: The native board implementation MUST support configurable chain length with identical behavior to the default implementation.
- **FR-008**: The network protocol MUST bump to version 2 and transmit the chain length as part of the game start message. Clients receiving a v2 message that they do not support MUST reject it gracefully. Clients receiving a v1 message (no chain length) MUST default chain length to the board dimension.
- **FR-009**: Game state serialization MUST include the chain length so persisted games can be restored with the correct win condition.
- **FR-010**: The chain length MUST be immutable once the game has started.
- **FR-011**: The system MUST declare a draw when no possible winning chain remains for any player, rather than waiting for the board to fill completely. This enables early draw detection on larger boards with shorter chain lengths.

### Key Entities

- **GameBoard**: Extended to carry a chain length parameter alongside its dimension. Chain length is a first-class property of the board — win detection and draw detection are performed by the board using its own chain length. Game state accesses chain length transitively through the board.
- **ChainLength**: The number of consecutive markers (K) required to win. Constrained to 2 ≤ K ≤ board dimension. Defaults to board dimension when not specified.

## Assumptions

- A chain length of 2 is valid but produces very short games — this is acceptable as a user-chosen game variant.
- The minimum valid chain length is 2 (chain length of 1 is rejected as trivial).
- Chain length does not change mid-game.
- All existing games (without explicit chain length) continue to behave identically due to the default.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A 3×3 game with no explicit chain length produces identical outcomes to the current system (full backward compatibility).
- **SC-002**: A 5×5 game with chain length 3 correctly detects wins for 3-in-a-row across rows, columns, and diagonals.
- **SC-003**: Bot strategies select winning/blocking moves correctly for any valid chain length on any valid board size.
- **SC-004**: Invalid chain length configurations (K > N, K < 2) are rejected before the game starts.
- **SC-005**: Networked games with custom chain lengths complete successfully with both sides applying the same win condition.
- **SC-006**: Native and default board implementations produce identical results for all valid chain length and board dimension combinations.
