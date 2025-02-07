package org.xxdc.oss.example.commentary;

import org.xxdc.oss.example.analysis.StrategicTurningPoint;

public class EsportsLiveCommentaryPersona implements CommentaryPersona {

  @Override
  public String comment(StrategicTurningPoint turningPoint) {
    return switch (turningPoint) {
      case StrategicTurningPoint.CenterSquareControl _ ->
          "%s seizes the high ground, taking control of the critical center square - textbook tic-tac-toe strategy!"
              .formatted(turningPoint.playerMarker());
      case StrategicTurningPoint.ImmediateLossPrevention _ ->
          "What a play! %s makes a crucial defensive move that prevents an immediate loss."
              .formatted(turningPoint.playerMarker());
    };
  }
}
