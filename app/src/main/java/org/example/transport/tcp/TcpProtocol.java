package org.example.transport.tcp;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.example.GameBoard;
import org.example.GameBoardDefaultImpl;

public class TcpProtocol {

    public static final String EXIT_CODE = "{}";

    public static final String GAME_STARTED_JSON_FORMAT =
            "{"
                    + "\"version\":1,"
                    + "\"message\":\"start\","
                    + "\"assignedPlayerMarker\":\"%s\""
                    + "}";

    public static final Pattern GAME_STARTED_JSON_PATTERN =
            Pattern.compile(
                    "\\{\\\"version\\\":(\\d+),\\\"message\\\":\\\"([^\\\"]+)\\\",\\\"assignedPlayerMarker\\\":\\\"([^\\\"]+)\\\".*}");

    ///
    /// Next Move Message
    /// e.g.
    /// ```json
    ///
    // {"version":1,"message":"nextMove","board":{"dimension":3,"content":["X",null,"O",null,"X",null,"X",null,"O"]}}
    /// ```
    public static final String NEXT_MOVE_JSON_FORMAT =
            "{" + "\"version\":1," + "\"message\":\"nextMove\"," + "\"board\":%s" + "}";

    public static final Pattern NEXT_MOVE_JSON_PATTERN =
            Pattern.compile(
                    "\\{\\\"version\\\":(\\d+),\\\"message\\\":\\\"([^\\\"]+)\\\",\\\"board\\\":\\{\\\"dimension\\\":(\\d+),\\\"content\\\":\\[(.*)\\]\\}.*\\}");

    public static Optional<GameBoard> fromNextMoveState(String serverMessage) {
        Matcher matcher = TcpProtocol.NEXT_MOVE_JSON_PATTERN.matcher(serverMessage);
        GameBoard board = null;
        if (matcher.matches()) {
            int dimension = Integer.valueOf(matcher.group(3));
            board = new GameBoardDefaultImpl(dimension);
            String[] rawContent = matcher.group(4).split(",");
            for (int i = 0; i < rawContent.length; i++) {
                if (rawContent[i] != null && !rawContent[i].equals("null")) {
                    board = board.withMove(rawContent[i].replaceAll("\"", ""), i);
                }
            }
        }
        return Optional.ofNullable(board);
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
