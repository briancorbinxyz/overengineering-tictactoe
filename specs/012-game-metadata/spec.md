# Feature Specification: Game Metadata

**Feature Branch**: `012-game-metadata`
**Created**: 2026-03-14
**Status**: Extracted
**Input**: Reverse-engineered from existing implementation

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Structured Game State Descriptors (Priority: P1)

The system produces structured metadata describing the current game state, including board dimensions, player information, turn count, and game outcome. This descriptor provides a standardized summary of any game state.

**Why this priority**: Descriptors are the primary mechanism for presenting game state information to external consumers.

**Independent Test**: Can be tested by creating a game state and generating its descriptor, then verifying all fields are populated correctly.

**Acceptance Scenarios**:

1. **Given** a game state in progress, **When** a descriptor is generated, **Then** it includes the board dimension, player markers, current player, and turn number.
2. **Given** a terminal game state (win), **When** a descriptor is generated, **Then** it includes the winning player.
3. **Given** a terminal game state (draw), **When** a descriptor is generated, **Then** it indicates a draw outcome.
4. **Given** an initial game state, **When** a descriptor is generated, **Then** the turn number is 0 and no outcome is reported.

---

### User Story 2 - Temporal Game Descriptors (Priority: P2)

The system produces temporal metadata for a game, capturing when the game was created and providing date-based context.

**Why this priority**: Temporal metadata enables chronological tracking and reporting of game sessions.

**Independent Test**: Can be tested by creating a game context with a known timestamp and verifying the date descriptor matches.

**Acceptance Scenarios**:

1. **Given** a game context with a creation timestamp, **When** a date descriptor is generated, **Then** it reflects the creation date of the game.

---

### User Story 3 - Game Context with Custom Metadata (Priority: P2)

A game session can be enriched with arbitrary key-value metadata at creation time. This metadata is immutable and available throughout the game lifecycle.

**Why this priority**: Enables integration with external systems by attaching custom data to game sessions.

**Independent Test**: Can be tested by creating a game context with custom metadata and verifying it is retrievable.

**Acceptance Scenarios**:

1. **Given** a game context built with custom key-value metadata, **When** the metadata is queried, **Then** all provided key-value pairs are returned.
2. **Given** a game context with no custom metadata, **When** the metadata is queried, **Then** an empty metadata set is returned.
3. **Given** a game context, **When** record pattern matching is applied, **Then** the context's components (ID, timestamp, metadata) can be destructured.

---

### Edge Cases

- What happens if duplicate metadata keys are provided during context construction?
- How does the descriptor handle a board with non-standard dimensions?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST produce structured game state descriptors containing board dimension, player markers, current player, turn count, and outcome.
- **FR-002**: System MUST produce temporal descriptors with game creation date information.
- **FR-003**: Game context MUST support arbitrary key-value metadata attached at creation time.
- **FR-004**: Game context MUST be immutable once created.
- **FR-005**: Game context MUST support structural decomposition (record pattern matching).
- **FR-006**: Descriptors MUST be derivable from any game state (initial, in-progress, or terminal).

### Key Entities

- **GameStateDescriptor**: A structured summary of a game state including board info, players, turn, and outcome.
- **GameDateDescriptor**: Temporal metadata about when the game was created.
- **GameContext**: The immutable record carrying game identity, timestamp, and custom metadata.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A game state descriptor for a won game correctly identifies the winner.
- **SC-002**: A game state descriptor for a draw correctly indicates no winner.
- **SC-003**: Custom metadata attached to a game context is retrievable throughout the game lifecycle.
- **SC-004**: Game context supports pattern-based decomposition into its components.
