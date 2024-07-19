package org.example;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.SequencedMap;
import java.util.Iterator;

public class Players implements Serializable {

    private static final long serialVersionUID = 1L;

    private final SequencedMap<String, Player> players;

    private Iterator<Player> playerIterator;

    public Players() {
        this.players = new LinkedHashMap<String, Player>(2);
    }

    public void tryAddPlayer(Player player) {
        if (!players.containsKey(player.getPlayerMarker())) {
            players.put(player.getPlayerMarker(), player);
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

    public Player nextPlayer() {
        if (playerIterator == null || !playerIterator.hasNext()) {
            playerIterator = players.values().iterator();
        }
        return playerIterator.next();
    }

    public Player getPlayer(String playerMarker) {
        return players.get(playerMarker);
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
