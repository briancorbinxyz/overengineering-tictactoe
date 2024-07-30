package org.example;

import java.util.Arrays;
import java.util.stream.Collectors;

public class JsonPrinter {

    public String asJson(GameBoard board) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"dimension\":").append(board.getDimension()).append(",");
        json.append("\"content\":")
                .append(
                        Arrays.stream(board.getContent())
                                .map(m -> m == null ? "null" : "\"" + m + "\"")
                                .collect(Collectors.joining(",", "[", "]")));
        json.append("}");
        return json.toString();
    }
}
