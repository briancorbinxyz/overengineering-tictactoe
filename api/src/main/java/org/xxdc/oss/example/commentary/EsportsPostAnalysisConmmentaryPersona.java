package org.xxdc.oss.example.commentary;

import org.xxdc.oss.example.analysis.StrategicTurningPoint;

public class EsportsPostAnalysisConmmentaryPersona implements CommentaryPersona {

  @Override
  public String comment(StrategicTurningPoint turningPoint) {
    return switch (turningPoint) {
      case StrategicTurningPoint.CenterSquareControl _ ->
          "After the %s move %s seized the high ground - in textbook tic-tac-toe strategy by taking control of the critical center square."
              .formatted(withOrdinal(turningPoint.moveNumber()), turningPoint.playerMarker());
    case StrategicTurningPoint.ImmediateLossPrevention _ ->
    "With the %s move of the game %s made a critical defensive play that prevented an immediate loss. Clutch!".formatted(
            withOrdinal(turningPoint.moveNumber()), turningPoint.playerMarker());
    };
  }

  public String withOrdinal(int number) {
    String suffix =
        switch (number % 10) {
          case 1 -> (number == 11) ? "th" : "st";
          case 2 -> (number == 12) ? "th" : "nd";
          case 3 -> (number == 13) ? "th" : "rd";
          default -> "th";
        };
    return number + suffix;
  }
}
