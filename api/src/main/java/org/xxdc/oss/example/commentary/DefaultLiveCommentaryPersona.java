package org.xxdc.oss.example.commentary;

import org.xxdc.oss.example.analysis.StrategicTurningPoint;

/**
 * Persona / personality that provides a comment on a strategic turning point in the game.
 */
public class DefaultLiveCommentaryPersona implements CommentaryPersona {

  @Override
  public String comment(StrategicTurningPoint turningPoint) {
    return switch (turningPoint) {
      case StrategicTurningPoint.GameWon _ ->
          "Player %s has won the game!".formatted(turningPoint.playerMarker());
      case StrategicTurningPoint.CenterSquareControl _ ->
          "Player %s has control of the center square.".formatted(turningPoint.playerMarker());
      case StrategicTurningPoint.ImmediateLossPrevention _ ->
          "Player %s has a made move that prevented an potential loss."
              .formatted(turningPoint.playerMarker());
    };
  }
}
