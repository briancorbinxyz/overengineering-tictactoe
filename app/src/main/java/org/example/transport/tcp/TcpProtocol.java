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
                    + "\"assignedPlayerMarker\": \"%s\""
                    + "}";

    public static final String NEXT_MOVE_JSON_FORMAT =
            "{" + "\"version\":1," + "\"message\":\"nextMove\"," + "\"board\":%s" + "}";

    public static final Pattern NEXT_MOVE_JSON_PATTERN =
            Pattern.compile(
                    "\\{\\\"version\\\":(\\d+),\\\"message\\\":\\\"([^\\\"]+)\\\",\\\"board\\\":\\{\\\"dimension\\\":(\\d+),(.*)\\}.*\\}");

    public static Optional<GameBoard> fromProtocol(String serverMessage) {
        Matcher matcher = TcpProtocol.NEXT_MOVE_JSON_PATTERN.matcher(serverMessage);
        GameBoard board = null;
        if (matcher.matches()) {
            int dimension = Integer.valueOf(matcher.group(3));
            board = new GameBoardDefaultImpl(dimension);
        }
        return Optional.ofNullable(board);
    }
}
