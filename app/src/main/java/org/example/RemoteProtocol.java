package org.example;

import java.util.regex.Pattern;

public class RemoteProtocol {

    public static final String EXIT_CODE = "{}";

    public static final String GAME_STARTED_JSON_FORMAT = "{" +
        "\"version\":1," +
        "\"message\":\"start\"," +
        "\"assignedPlayerMarker\": \"%s\"" +
        "}";

    public static final String NEXT_MOVE_JSON_FORMAT = "{" +
        "\"version\":1," +
        "\"message\":\"nextMove\"," +
        "\"board\":%s" +
        "}";
    
    public static final Pattern NEXT_MOVE_JSON_PATTERN = Pattern.compile("\\{\\\"version\\\":(\\d+),\\\"message\\\":\\\"([^\\\"]+)\\\",\\\"board\\\":\\{\\\"dimension\\\":(\\d+),(.*)\\}.*\\}");
}
