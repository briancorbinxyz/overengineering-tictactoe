package org.xxdc.oss.example.analysis;

import java.util.Optional;
import org.xxdc.oss.example.GameState;

/// A strategic turning point in a game of Tic-Tac-Toe.
public sealed interface StrategicTurningPoint {

  static enum PriorityLevel {
    HIGH,
    MEDIUM,
    LOW
  }

  /// The player marker for the player who made the strategic move.
  String playerMarker();

  /// The game state at the strategic turning point.
  GameState gameState();

  /// The move number at the strategic turning point.
  int moveNumber();

  /// The priority level of the strategic turning point.
  PriorityLevel priorityLevel();

  /// A strategic turning point where the player has control of the center square.
  ///
  /// @param playerMarker the player marker for the player who made the strategic move
  /// @param gameState the game state at the strategic turning point
  /// @param moveNumber the move number at the strategic turning point
  public record CenterSquareControl(String playerMarker, GameState gameState, int moveNumber)
      implements StrategicTurningPoint {
    @Override
    public PriorityLevel priorityLevel() {
      return PriorityLevel.MEDIUM;
    }
  }

  public record ImmediateLossPrevention(String playerMarker, GameState gameState, int moveNumber)
      implements StrategicTurningPoint {
    @Override
    public PriorityLevel priorityLevel() {
      return PriorityLevel.HIGH;
    }
  }

  /// Check for a strategic turning point where the player has control of the center square.
  /// @param prevGameState the game state before the strategic move
  /// @param gameState the game state after the strategic move
  /// @param moveNumber the move number at the strategic turning point
  /// @return an optional strategic turning point
  static boolean moveTakesCenterSquareControl(GameState prevGameState, GameState gameState) {
    // Only boards with odd dimensions can have a center square
    if (gameState.board().dimension() % 2 == 0) {
      return false;
    }
    int dimension = gameState.board().dimension();
    int centerLocation = dimension * (dimension / 2) + (dimension / 2);
    return prevGameState.board().isValidMove(centerLocation)
        && !gameState.board().isValidMove(centerLocation);
  }

  /// Check for a strategic turning point where the player is about to lose.
  /// @param prevGameState the game state before the strategic move
  /// @param gameState the game state after the strategic move
  /// @param moveNumber the move number at the strategic turning point
  /// @return an optional strategic turning point
  static boolean movePreventedImmediateLoss(GameState prevGameState, GameState gameState) {
    String lastPlayer = gameState.lastPlayer();
    int lastMove = gameState.lastMove();
    if (gameState.availableMoves().isEmpty()) { // Game is over
      return false;
    }
    return prevGameState.playerMarkers().stream()
        .filter(player -> !player.equals(lastPlayer))
        .anyMatch(
            opponent ->
                prevGameState
                    .board()
                    .withMove(opponent, lastMove)
                    .hasChain(
                        opponent)); // TODO: this is not quite right if there were not enough moves
    // left until this player's turn
  }

  static Optional<StrategicTurningPoint> from(
      GameState prevGameState, GameState gameState, int moveNumber) {
    if (moveTakesCenterSquareControl(prevGameState, gameState)) {
      return Optional.of(new CenterSquareControl(gameState.lastPlayer(), gameState, moveNumber));
    }
    if (movePreventedImmediateLoss(prevGameState, gameState)) {
      return Optional.of(
          new ImmediateLossPrevention(gameState.lastPlayer(), gameState, moveNumber));
    }
    return Optional.empty();
  }
}
