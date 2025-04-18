package org.xxdc.oss.example.analysis;

import java.util.Optional;
import org.xxdc.oss.example.GameState;

/// A strategic turning point in a game of Tic-Tac-Toe.
public sealed interface StrategicTurningPoint {

  /// Priority/severity level of the strategic turning point.
  enum PriorityLevel {
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

  /// A strategic turning point where the player has prevented an immediate loss.
  /// @param playerMarker the player marker for the player who made the strategic move
  /// @param gameState the game state at the strategic turning point
  /// @param moveNumber the move number at the strategic turning point
  public record ImmediateLossPrevention(String playerMarker, GameState gameState, int moveNumber)
      implements StrategicTurningPoint {
    @Override
    public PriorityLevel priorityLevel() {
      return PriorityLevel.HIGH;
    }
  }

  /// A strategic turning point where the player has won the game.
  /// @param playerMarker the player marker for the player who made the strategic move
  /// @param gameState the game state at the strategic turning point
  /// @param moveNumber the move number at the strategic turning point
  public record GameWon(String playerMarker, GameState gameState, int moveNumber)
      implements StrategicTurningPoint {
    @Override
    public PriorityLevel priorityLevel() {
      return PriorityLevel.HIGH;
    }
  }

  /// Check for a strategic turning point where the player has control of the center square.
  /// @param prevGameState the game state before the strategic move
  /// @param gameState the game state after the strategic move
  /// @return true if the strategic turning point is a center square control
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
  /// @return true if the player is about to lose, false otherwise
  static boolean movePreventedImmediateLoss(GameState prevGameState, GameState gameState) {
    var lastPlayer = gameState.lastPlayer();
    int lastMove = gameState.lastMove();
    if (gameState.availableMoves().isEmpty()) { // Game is over
      return false;
    }
    return prevGameState.playerMarkers().stream()
        .filter(player -> !player.equals(lastPlayer))
        .anyMatch(
            opponent -> prevGameState.board().withMove(opponent, lastMove).hasChain(opponent));
    // TODO: this is not quite right if there were not enough moves
    // left until the opponent player's turn
  }

  /// Check for a strategic turning point where the player has won the game.
  /// @param gameState the game state after the strategic move
  /// return true if the player has won the game, false otherwise
  static boolean moveWinsGame(GameState gameState) {
    return gameState.lastPlayerHasChain();
  }

  static Optional<StrategicTurningPoint> from(
      GameState prevGameState, GameState gameState, int moveNumber) {
    if (moveWinsGame(gameState)) {
      return Optional.of(new GameWon(gameState.lastPlayer(), gameState, moveNumber));
    }
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
