package org.xxdc.oss.example.transport.tcp;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.xxdc.oss.example.GameBoard;
import org.xxdc.oss.example.GameState;

/**
 * Provides utility methods for parsing and formatting JSON messages used in the TCP protocol for
 * the game. The protocol defines two main message types: "start" to indicate the game has started,
 * and "nextMove" to communicate the current game state.
 */
public class TcpProtocol {

  private TcpProtocol() {}

  /**
   * A constant representing an empty JSON object, which can be used to indicate an exit or
   * termination condition in the TCP protocol.
   */
  public static final String EXIT_CODE = "{}";

  /**
   * A constant representing the JSON format for a "game started" message, which includes the
   * version, message type, and the assigned player marker.
   */
  public static final String GAME_STARTED_JSON_FORMAT =
      "{" + "\"version\":1," + "\"message\":\"start\"," + "\"assignedPlayerMarker\":\"%s\"" + "}";

  /**
   * A regular expression pattern that matches a JSON string representing a "game started" message.
   * The pattern captures the following groups: 1. The version number as an integer 2. The message
   * type as a string 3. The assigned player marker as a string
   */
  public static final Pattern GAME_STARTED_JSON_PATTERN =
      Pattern.compile(
          "\\{\\\"version\\\":(\\d+),\\\"message\\\":\\\"([^\\\"]+)\\\",\\\"assignedPlayerMarker\\\":\\\"([^\\\"]+)\\\".*}");

  ///
  /// Next Move Message
  /// e.g.
  ///
  /// ```json
  ///
  // {"version":1,"message":"nextMove","state":{"gameId":"abc123","playerMarkers":["X","O"],"currentPlayerIndex":1,"board":{"dimension":3,"content":["X","O",null,null,"X",null,null,null,null]}}}
  /// ```
  public static final String NEXT_MOVE_JSON_FORMAT =
      "{" + "\"version\":1," + "\"message\":\"nextMove\"," + "\"state\":%s" + "}";

  public static final Pattern NEXT_MOVE_JSON_PATTERN =
      Pattern.compile(
          "\\{\\\"version\\\":(\\d+),\\\"message\\\":\\\"([^\\\"]+)\\\","
              + "\\\"state\\\":\\{\\\"playerMarkers\\\":\\[(.*)\\],\\\"currentPlayerIndex\\\":(\\d+),"
              + "\\\"board\\\":\\{\\\"dimension\\\":(\\d+),\\\"content\\\":\\[(.*)\\]\\}.*\\}}");

  /**
   * Parses a JSON string representing a "next move" message and returns a {@link GameState} object
   * containing the parsed information.
   *
   * <p>The JSON string is expected to have the following format (with no whitespace):
   *
   * <p>{ "version": 1, "message": "nextMove", "state": { "gameId": "abc123", "playerMarkers": ["X",
   * "O"], "currentPlayerIndex": 1, "board": { "dimension": 3, "content": ["X", "O", null, null,
   * "X", null, null, null, null] } } }
   *
   * <p>The method extracts the game ID, player markers, the index of the current player, the
   * dimension of the game board, and the content of the game board from the JSON string. It then
   * creates a {@link GameState} object with this information and returns it.
   *
   * @param serverMessage the JSON string representing the "next move" message
   * @return an {@link Optional} containing the parsed {@link GameState} object, or {@link
   *     Optional#empty()} if the input string does not match the expected format
   */
  public static Optional<GameState> fromNextMoveState(String serverMessage) {
    Matcher matcher = TcpProtocol.NEXT_MOVE_JSON_PATTERN.matcher(serverMessage);
    GameState state = null;
    if (matcher.matches()) {
      String[] playerMarkers = matcher.group(3).replace("\"", "").split(",");
      int currentPlayerIndex = Integer.parseInt(matcher.group(4));
      int dimension = Integer.parseInt(matcher.group(5));
      var board = GameBoard.withDimension(dimension);
      String[] rawContent = matcher.group(6).split(",");
      for (int i = 0; i < rawContent.length; i++) {
        if (rawContent[i] != null && !rawContent[i].equals("null")) {
          board = board.withMove(rawContent[i].replace("\"", ""), i);
        }
      }
      state = new GameState(board, List.of(playerMarkers), currentPlayerIndex);
    }
    return Optional.ofNullable(state);
  }

  /**
   * Parses a JSON string representing a "game started" message and returns the player marker
   * assigned to the client.
   *
   * <p>The JSON string is expected to have the following format (with no whitespace):
   *
   * <p>{ "version": 1, "message": "gameStarted", "playerMarker": "X" }
   *
   * <p>The method extracts the player marker from the JSON string and returns it as an {@link
   * Optional}. If the input string does not match the expected format, the method returns {@link
   * Optional#empty()}.
   *
   * @param serverMessage the JSON string representing the "game started" message
   * @return an {@link Optional} containing the player marker, or {@link Optional#empty()} if the
   *     input string does not match the expected format
   */
  public static Optional<String> fromGameStartedState(String serverMessage) {
    Matcher matcher = TcpProtocol.GAME_STARTED_JSON_PATTERN.matcher(serverMessage);
    String playerMarker = null;
    if (matcher.matches()) {
      playerMarker = matcher.group(3);
    }
    return Optional.ofNullable(playerMarker);
  }
}
