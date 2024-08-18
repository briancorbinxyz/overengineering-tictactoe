package org.example;

import java.util.List;

public record GameState(GameBoard board, List<String> playerMarkers, int currentPlayerIndex)
        implements JsonSerializable {
    public String currentPlayer() {
        return playerMarkers.get(currentPlayerIndex);
    }

    @Override
    public String asJsonString() {
        StringBuilder json = new StringBuilder();
        json.append("{");
        if (playerMarkers.size() > 0) {
            json.append("\"playerMarkers\":[\"")
                    .append(String.join("\",\"", playerMarkers))
                    .append("\"],");
        } else {
            json.append("\"playerMarkers\":[],");
        }
        json.append("\"currentPlayerIndex\":").append(currentPlayerIndex).append(",");
        json.append("\"board\":").append(board.asJsonString());
        json.append("}");
        return json.toString();
    }
}
