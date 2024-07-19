package org.example;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.SequencedMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

public class Players implements Serializable {

    private static final long serialVersionUID = 1L;

    private final SequencedMap<String, Player> players;

    private final List<String> playerMarkers;

    public Players() {
        this.players = new LinkedHashMap<String, Player>(2);
        this.playerMarkers = new ArrayList<String>(2);
    }

    public void tryAddPlayer(Player player) {
        if (!players.containsKey(player.getPlayerMarker())) {
            players.put(player.getPlayerMarker(), player);
            playerMarkers.add(player.getPlayerMarker());
        } else {
            throw new RuntimeException("Unable to add player " + player + " as player with marker '" + player.getPlayerMarker() + "' already exists.");
        }
    }

    public static Players of(Player... players) {
        var playerList = new Players();
        for (Player p : players) {
            playerList.tryAddPlayer(p);
        }
        return playerList;
    }

    public Player byMarker(String playerMarker) {
        return players.get(playerMarker);
    }

    public Player byIndex(int index) {
        return players.get(playerMarkers.get(index));
    }

    public int nextPlayerIndex(int index) {
        return index + 1 < players.size() ? index + 1 : 0;
    }

    public void render() {
        PlayerPrinter printer = new PlayerPrinter();
        System.out.println("Players: " + playerMarkers());
        for (Player player : players.values()) {
            System.out.println("- " + printer.getPlayerIdentifier(player));
        }
    }

    public String playerMarkers() {
        return String.join(", ", players.sequencedKeySet());
    }


}
