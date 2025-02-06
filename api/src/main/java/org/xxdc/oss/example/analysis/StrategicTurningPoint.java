package org.xxdc.oss.example.analysis;

import java.util.Optional;
import org.xxdc.oss.example.GameState;

/// A strategic turning point in a game of Tic-Tac-Toe.
public sealed interface StrategicTurningPoint {

  /// The player marker for the player who made the strategic move.
  String playerMarker();

  /// The game state at the strategic turning point.
  GameState gameState();

  /// The move number at the strategic turning point.
  int moveNumber();

  /// A strategic turning point where the player has control of the center square.
  public record CenterSquareControl(String playerMarker, GameState gameState, int moveNumber)
      implements StrategicTurningPoint {}

  static Optional<StrategicTurningPoint> from(
      GameState prevGameState, GameState gameState, int moveCounter) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'from'");
  }
  ;
}
