package org.xxdc.oss.example.commentary;

import org.xxdc.oss.example.analysis.StrategicTurningPoint;

public class DefaultLiveCommentaryPersona implements CommentaryPersona {

  @Override
  public String comment(StrategicTurningPoint turningPoint) {
    return switch (turningPoint) {
      case StrategicTurningPoint.CenterSquareControl _ ->
          "Player %s has control of the center square.".formatted(turningPoint.playerMarker());
    };
  }
}
