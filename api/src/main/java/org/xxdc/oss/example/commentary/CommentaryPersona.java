package org.xxdc.oss.example.commentary;

import org.xxdc.oss.example.analysis.StrategicTurningPoint;

/**
 * Persona / personality that provides a comment on a strategic turning point in the game.
 */
public interface CommentaryPersona {
  String comment(StrategicTurningPoint turningPoint);
}
