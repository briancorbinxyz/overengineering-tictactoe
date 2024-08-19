package org.example.transport.tcp;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.example.GameBoard;
import org.example.GameState;

public class TcpProtocol {

  public static final String EXIT_CODE = "{}";

  public static final String GAME_STARTED_JSON_FORMAT =
      "{" + "\"version\":1," + "\"message\":\"start\"," + "\"assignedPlayerMarker\":\"%s\"" + "}";

  public static final Pattern GAME_STARTED_JSON_PATTERN =
      Pattern.compile(
          "\\{\\\"version\\\":(\\d+),\\\"message\\\":\\\"([^\\\"]+)\\\",\\\"assignedPlayerMarker\\\":\\\"([^\\\"]+)\\\".*}");

  ///
  /// Next Move Message
  /// e.g.
  /// ```json
  // {"version":1,"message":"nextMove","state":{"playerMarkers":["X","O"],"currentPlayerIndex":1,"board":{"dimension":3,"content":["X","O",null,null,"X",null,null,null,null]}}}
  /// ```
  public static final String NEXT_MOVE_JSON_FORMAT =
      "{" + "\"version\":1," + "\"message\":\"nextMove\"," + "\"state\":%s" + "}";

  public static final Pattern NEXT_MOVE_JSON_PATTERN =
      Pattern.compile(
          "\\{\\\"version\\\":(\\d+),\\\"message\\\":\\\"([^\\\"]+)\\\","
              + "\\\"state\\\":\\{\\\"playerMarkers\\\":\\[(.*)\\],\\\"currentPlayerIndex\\\":(\\d+),"
              + "\\\"board\\\":\\{\\\"dimension\\\":(\\d+),\\\"content\\\":\\[(.*)\\]\\}.*\\}}");

  public static Optional<GameState> fromNextMoveState(String serverMessage) {
    Matcher matcher = TcpProtocol.NEXT_MOVE_JSON_PATTERN.matcher(serverMessage);
    GameState state = null;
    if (matcher.matches()) {
      String[] playerMarkers = matcher.group(3).replaceAll("\"", "").split(",");
      int currentPlayerIndex = Integer.valueOf(matcher.group(4));
      int dimension = Integer.valueOf(matcher.group(5));
      var board = GameBoard.with(dimension);
      String[] rawContent = matcher.group(6).split(",");
      for (int i = 0; i < rawContent.length; i++) {
        if (rawContent[i] != null && !rawContent[i].equals("null")) {
          board = board.withMove(rawContent[i].replaceAll("\"", ""), i);
        }
      }
      state = new GameState(board, List.of(playerMarkers), currentPlayerIndex);
    }
    return Optional.ofNullable(state);
  }

  public static Optional<String> fromGameStartedState(String serverMessage) {
    Matcher matcher = TcpProtocol.GAME_STARTED_JSON_PATTERN.matcher(serverMessage);
    String playerMarker = null;
    if (matcher.matches()) {
      playerMarker = matcher.group(3);
    }
    return Optional.ofNullable(playerMarker);
  }
}
