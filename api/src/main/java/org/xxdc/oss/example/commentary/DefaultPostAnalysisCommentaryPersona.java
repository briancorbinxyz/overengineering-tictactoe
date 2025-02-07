package org.xxdc.oss.example.commentary;

import org.xxdc.oss.example.analysis.StrategicTurningPoint;

public class DefaultPostAnalysisCommentaryPersona implements CommentaryPersona {

  @Override
  public String comment(StrategicTurningPoint turningPoint) {
    return switch (turningPoint) {
      case StrategicTurningPoint.CenterSquareControl _ ->
          "Player %s took control of the center square after move %s."
              .formatted(turningPoint.playerMarker(), turningPoint.moveNumber());
      case StrategicTurningPoint.ImmediateLossPrevention _ ->
          "Player %s made a move that prevented an immediate loss."
              .formatted(turningPoint.playerMarker());
    };
  }
}
