package org.example;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.SequencedSet;

public class PlayerList implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<Player> players;

    private final SequencedSet<String> playerMarkers;

    private int playerIndex;

    public PlayerList() {
        this.players = new ArrayList<Player>(2);
        this.playerMarkers = new LinkedHashSet<String>(2);
        this.playerIndex = 0;
    }

    public void tryAddPlayer(Player player) {
        if (!playerMarkers.contains(player.getPlayerMarker())) {
            players.add(player);
            playerMarkers.add(player.getPlayerMarker());
        } else {
            throw new RuntimeException("Unable to add player " + player + " as player with marker '" + player.getPlayerMarker() + "' already exists.");
        }
    }

    public Player playerAt(int index) {
        return players.get(index);
    }

    public static PlayerList of(Player... players) {
        var playerList = new PlayerList();
        for (Player p : players) {
            playerList.tryAddPlayer(p);
        }
        return playerList;
    }

    public Player nextPlayer() {
        int currentIdx = playerIndex;
        playerIndex = playerIndex + 1;
        if (playerIndex >= players.size()) {
            playerIndex = 0;
        }
        return players.get(currentIdx);
    }

    public void render() {
        PlayerPrinter printer = new PlayerPrinter();
        System.out.println("Players: " + playerMarkers());
        for (Player player : players) {
            System.out.println("- " + printer.getPlayerIdentifier(player));
        }
    }

    public String playerMarkers() {
        return String.join(", ", playerMarkers);
    }

}
