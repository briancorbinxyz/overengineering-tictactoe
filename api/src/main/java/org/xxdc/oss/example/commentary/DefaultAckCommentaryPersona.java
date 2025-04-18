package org.xxdc.oss.example.commentary;

import org.xxdc.oss.example.analysis.StrategicTurningPoint;

/**
 * Persona / personality that provides an acknowledgement of a strategic turning point in the game.
 */
public class DefaultAckCommentaryPersona implements CommentaryPersona {

  @Override
  public String comment(StrategicTurningPoint turningPoint) {
    return "Strategic turning point by %s after move %s"
        .formatted(turningPoint.playerMarker(), turningPoint.moveNumber());
  }
}
