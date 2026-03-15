# Feature Specification: Game Commentary

**Feature Branch**: `feature/game-commentary`
**Created**: 2026-03-14
**Status**: Extracted
**Input**: Reverse-engineered from existing implementation

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Live Move-by-Move Commentary (Priority: P1)

As a game progresses, the system provides real-time commentary on each move. The commentary style is determined by a configurable persona.

**Why this priority**: Live commentary is the primary user-facing output during gameplay.

**Independent Test**: Can be tested by playing a game with a live commentary persona and verifying commentary is produced for each move.

**Acceptance Scenarios**:

1. **Given** a game configured with a live commentary persona, **When** a player makes a move, **Then** the persona generates a commentary message for that move.
2. **Given** different commentary personas, **When** the same move is made, **Then** different commentary styles are produced (e.g., esports-style vs. default acknowledgment).

---

### User Story 2 - Post-Game Analysis Commentary (Priority: P2)

After a game completes, a commentary persona reviews the full game history and strategic turning points to produce a summary analysis.

**Why this priority**: Provides a narrative summary of the completed game.

**Independent Test**: Can be tested by providing a completed game history with turning points and verifying a post-analysis commentary is generated.

**Acceptance Scenarios**:

1. **Given** a completed game with identified turning points, **When** the post-analysis persona is invoked, **Then** a narrative summary covering key moments is produced.
2. **Given** a completed game with no turning points, **When** the post-analysis persona is invoked, **Then** a basic game summary is produced.

---

### User Story 3 - Multiple Commentary Personas (Priority: P2)

The system supports multiple distinct commentary personas, each with its own tone and style. Personas are interchangeable.

**Why this priority**: Demonstrates extensibility and allows varied user experiences.

**Independent Test**: Can be tested by substituting different personas into the same game and verifying different output styles.

**Acceptance Scenarios**:

1. **Given** the default acknowledgment persona, **When** commentary is generated, **Then** it produces simple move acknowledgments.
2. **Given** the esports live persona, **When** commentary is generated, **Then** it produces energetic, play-by-play style commentary.
3. **Given** the esports post-analysis persona, **When** analysis commentary is generated, **Then** it produces a detailed breakdown of strategic moments.

---

### Edge Cases

- What happens if no commentary persona is configured?
- How does the commentary handle a game that ends in a single move?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST support live commentary generation for each move during gameplay.
- **FR-002**: System MUST support post-game analysis commentary based on game history and turning points.
- **FR-003**: Commentary personas MUST be interchangeable without modifying game logic.
- **FR-004**: System MUST provide at least two live commentary personas (default acknowledgment and esports-style).
- **FR-005**: System MUST provide at least two post-analysis commentary personas (default and esports-style).

### Key Entities

- **CommentaryPersona**: The contract for generating commentary, with separate interfaces for live and post-analysis modes.
- **Persona Variant**: A specific commentary style (e.g., default, esports).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Every move in a commentary-enabled game produces at least one commentary output.
- **SC-002**: Different personas produce observably different commentary for the same game events.
- **SC-003**: Post-analysis commentary references at least one strategic turning point when turning points are present.
