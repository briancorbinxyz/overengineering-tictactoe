# Feature Specification: Human Player Input

**Feature Branch**: `003-human-player-input`
**Created**: 2026-03-14
**Status**: Extracted
**Input**: Reverse-engineered from existing implementation

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Select a Move via Console (Priority: P1)

A human player is prompted to enter a board position during their turn. The system reads the input, validates it against the current board state, and either accepts the move or prompts the player to try again.

**Why this priority**: This is the primary interaction path for human players.

**Independent Test**: Can be tested by providing simulated input and verifying the correct move is returned.

**Acceptance Scenarios**:

1. **Given** it is the human player's turn, **When** the player enters a valid position number, **Then** the move is accepted and play proceeds.
2. **Given** it is the human player's turn, **When** the player enters an invalid or occupied position, **Then** the system displays an error and prompts the player again.
3. **Given** it is the human player's turn, **When** the player enters non-numeric input, **Then** the system handles the error gracefully and re-prompts.

---

### User Story 2 - Multiple Input Sources (Priority: P2)

The system supports reading human player input from different sources — interactive console or a programmatic input stream — to enable both live play and automated testing.

**Why this priority**: Testability and flexibility of input sourcing.

**Independent Test**: Can be tested by providing a programmatic input source with a sequence of moves and verifying the game completes.

**Acceptance Scenarios**:

1. **Given** a human player configured with console input, **When** the player's turn arrives, **Then** the system reads from the interactive console.
2. **Given** a human player configured with a programmatic input source, **When** the player's turn arrives, **Then** the system reads from the provided source.

---

### Edge Cases

- What happens if the input stream is exhausted before the game ends?
- What happens if the player repeatedly enters invalid moves?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST prompt the human player for a move on their turn.
- **FR-002**: System MUST validate input against the current board state before accepting.
- **FR-003**: System MUST re-prompt on invalid input without crashing or ending the game.
- **FR-004**: System MUST support pluggable input sources (console and programmatic).

### Key Entities

- **HumanPlayer**: A player that obtains moves from human input.
- **Input Source**: The mechanism providing move data (console or stream-based).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Valid numeric input within board range is accepted on the first prompt.
- **SC-002**: Invalid input results in re-prompting without game termination.
- **SC-003**: A complete game can be played with a programmatic input source providing all moves.
