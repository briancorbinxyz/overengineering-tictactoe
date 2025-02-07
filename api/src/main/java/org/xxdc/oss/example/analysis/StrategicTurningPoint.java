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

  static boolean moveTakesCenterSquareControl(GameState prevGameState, GameState gameState) {
    if (gameState.board().dimension() % 2 == 0) { // Only odd dimensions can have a center square
      return false;
    }
    int dimension = gameState.board().dimension();
    int centerLocation = dimension * (dimension / 2) + (dimension / 2);
    return prevGameState.board().isValidMove(centerLocation)
      && !gameState.board().isValidMove(centerLocation);
  }

  static Optional<StrategicTurningPoint> from(
      GameState prevGameState, GameState gameState, int moveNumber) {
        if (moveTakesCenterSquareControl(prevGameState, gameState)) {
          return Optional.of(new CenterSquareControl(gameState.lastPlayer(), gameState, moveNumber));
        }
        return Optional.empty();
  }
  
}
