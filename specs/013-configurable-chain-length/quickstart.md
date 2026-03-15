# Quickstart: Configurable Winning Chain Length

## What this feature does

Allows games to use a winning chain length (K-in-a-row) that differs from the board dimension (N×N). For example, play 3-in-a-row on a 5×5 board. The default chain length equals the board dimension, so existing games are unaffected.

## Key changes

1. **GameBoard** gains a `chainLength` property. Win detection uses `chainLength` instead of `dimension`.
2. **Early draw detection** ends games when no winning chain is possible for any player.
3. **TCP Protocol** bumps to v2 to transmit chain length in start messages.
4. **Native board** FFI accepts chain length as a second parameter.

## Implementation order

1. Core: Add chainLength to GameBoard interface and GameBoardLocalImpl
2. Validation: Enforce 2 ≤ K ≤ N at board creation
3. Win detection: Generalize hasChain() to find K consecutive markers
4. Draw detection: Add hasWinnableChain() for early draw
5. Game class: Accept chainLength parameter, pass to board factory
6. Native: Update FFI signatures and Rust library
7. Protocol: Bump to v2, add chainLength to messages
8. Analysis: Update center-square heuristic for variable chain lengths
9. Tests: Cover all combinations of dimension × chainLength

## What does NOT change

- Bot strategies (Minimax, AlphaBeta, etc.) — they delegate to board.hasChain()
- GameState — delegates to board
- Human player input — unaffected
- Commentary system — unaffected
- Game metadata descriptors — unaffected
