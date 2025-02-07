package org.xxdc.oss.example.commentary;

import static org.xxdc.oss.example.analysis.StrategicTurningPoint.*;

import org.xxdc.oss.example.analysis.StrategicTurningPoint;

public class EsportsLiveCommentaryPersona implements CommentaryPersona {

  @Override
  public String comment(StrategicTurningPoint turningPoint) {
    return switch (turningPoint) {
        case GameWon _ -> "Winner, winner, chicken dinner! %s wins the game after move %s!".formatted(turningPoint.playerMarker(), turningPoint.moveNumber());
      case CenterSquareControl _ ->
          "%s seizes the high ground, taking control of the critical center square - textbook tic-tac-toe strategy!"
              .formatted(turningPoint.playerMarker());
      case ImmediateLossPrevention _ ->
          "What a play! %s makes a crucial defensive move that prevents an immediate loss."
              .formatted(turningPoint.playerMarker());
    };
  }
}
