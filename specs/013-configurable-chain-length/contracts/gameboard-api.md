# Contract: GameBoard API

**Scope**: Public interface for game board operations

## New Methods

### `chainLength()`
Returns the number of consecutive markers required to win.

- **Returns**: int (2 ≤ result ≤ dimension)
- **Default**: equals `dimension()` when not explicitly set

### `withDimension(int dimension, int chainLength)` (factory)
Creates a new empty board with the specified dimension and chain length.

- **Parameters**:
  - `dimension` — board side length (N), must be ≥ 1
  - `chainLength` — consecutive markers to win (K), must satisfy 2 ≤ K ≤ N
- **Returns**: GameBoard instance
- **Throws**: if chainLength is out of bounds

### `hasWinnableChain()` (new)
Returns true if at least one player can still complete a chain of length K.

- **Returns**: boolean — false triggers early draw detection
- **Behavior**: Scans all possible K-length windows in rows, columns, and diagonals. Returns false only when every possible window is blocked (contains markers from multiple players).

## Modified Methods

### `withDimension(int dimension)` (existing factory)
Backward-compatible — creates a board with `chainLength = dimension`.

### `hasChain(String playerMarker)` (existing)
Now checks for `chainLength` consecutive markers (not `dimension` consecutive markers).

### `withMove(String playerMarker, int location)` (existing)
Returns a new board preserving the original's `chainLength`.

## JSON Serialization Format

### Current (v1)
```json
{"dimension": 3, "content": ["X", null, "O", ...]}
```

### New (v2)
```json
{"dimension": 3, "chainLength": 3, "content": ["X", null, "O", ...]}
```

Deserialization of v1 format (missing chainLength) defaults chainLength to dimension.
