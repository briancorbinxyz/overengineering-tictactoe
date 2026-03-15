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
 *
 * <p>Protocol version 2 adds chainLength support: the start message includes the chain length, and
 * the board section of nextMove messages includes a chainLength field. Clients receiving a v1
 * message (no chainLength) default to chainLength = dimension.
 */
public class TcpProtocol {

  private TcpProtocol() {}

  /**
   * A constant representing an empty JSON object, which can be used to indicate an exit or
   * termination condition in the TCP protocol.
   */
  public static final String EXIT_CODE = "{}";

  /**
   * A constant representing the JSON format for a "game started" message (v2), which includes the
   * version, message type, assigned player marker, and chain length.
   */
  public static final String GAME_STARTED_JSON_FORMAT =
      "{"
          + "\"version\":2,"
          + "\"message\":\"start\","
          + "\"assignedPlayerMarker\":\"%s\","
          + "\"chainLength\":%d"
          + "}";

  /**
   * A regular expression pattern that matches a JSON string representing a "game started" message.
   * Supports both v1 (no chainLength) and v2 (with chainLength).
   */
  public static final Pattern GAME_STARTED_JSON_PATTERN =
      Pattern.compile(
          "\\{\\\"version\\\":(\\d+),\\\"message\\\":\\\"([^\\\"]+)\\\","
              + "\\\"assignedPlayerMarker\\\":\\\"([^\\\"]+)\\\"(?:,\\\"chainLength\\\":(\\d+))?.*}");

  /// Next Move Message (v2) — includes chainLength in the board section.
  public static final String NEXT_MOVE_JSON_FORMAT =
      "{" + "\"version\":2," + "\"message\":\"nextMove\"," + "\"state\":%s" + "}";

  public static final Pattern NEXT_MOVE_JSON_PATTERN =
      Pattern.compile(
          "\\{\\\"version\\\":(\\d+),\\\"message\\\":\\\"([^\\\"]+)\\\","
              + "\\\"state\\\":\\{\\\"playerMarkers\\\":\\[(.*)\\],\\\"currentPlayerIndex\\\":(\\d+),"
              + "\\\"board\\\":\\{\\\"dimension\\\":(\\d+)(?:,\\\"chainLength\\\":(\\d+))?"
              + ",\\\"content\\\":\\[(.*)\\]\\}.*\\}}");

  /**
   * Parses a JSON string representing a "next move" message and returns a {@link GameState} object.
   * Supports both v1 (no chainLength — defaults to dimension) and v2 (explicit chainLength).
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
      int chainLength = matcher.group(6) != null ? Integer.parseInt(matcher.group(6)) : dimension;
      var board = GameBoard.withDimension(dimension, chainLength);
      String[] rawContent = matcher.group(7).split(",");
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

  /**
   * Parses the chain length from a "game started" v2 message. Returns the board dimension as
   * default if the message is v1 (no chainLength field).
   *
   * @param serverMessage the JSON "game started" message
   * @param defaultChainLength the default to use if chainLength is absent (typically dimension)
   * @return the parsed chain length, or defaultChainLength if not present
   */
  public static int chainLengthFromStartMessage(String serverMessage, int defaultChainLength) {
    Matcher matcher = TcpProtocol.GAME_STARTED_JSON_PATTERN.matcher(serverMessage);
    if (matcher.matches() && matcher.group(4) != null) {
      return Integer.parseInt(matcher.group(4));
    }
    return defaultChainLength;
  }
}
