package org.xxdc.oss.example.commentary;

import org.xxdc.oss.example.analysis.StrategicTurningPoint;

public class DefaultAckCommentaryPersona implements CommentaryPersona {

  @Override
  public String comment(StrategicTurningPoint turningPoint) {
    return "Strategic turning point by %s after move %s"
        .formatted(turningPoint.playerMarker(), turningPoint.moveNumber());
  }
}
