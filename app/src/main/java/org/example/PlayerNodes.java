package org.example;

import java.io.Closeable;
import java.io.Serializable;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.SequencedMap;

public class PlayerNodes implements Serializable {

    private static final Logger log = System.getLogger(PlayerNodes.class.getName());

    private static final long serialVersionUID = 1L;

    private final SequencedMap<String, PlayerNode> players;

    private final List<String> playerMarkers;

    public PlayerNodes() {
        this.players = new LinkedHashMap<String, PlayerNode>(2);
        this.playerMarkers = new ArrayList<String>(2);
    }

    public static PlayerNodes of(PlayerNode... players) {
        var playerList = new PlayerNodes();
        for (PlayerNode p : players) {
            playerList.tryAddPlayer(p);
        }
        return playerList;
    }

    public PlayerNode byIndex(int index) {
        return players.get(playerMarkers.get(index));
    }

    public int nextPlayerIndex(int index) {
        return index + 1 < players.size() ? index + 1 : 0;
    }

    public void render() {
        PlayerPrinter printer = new PlayerPrinter();
        log.log(Level.INFO, "Players: {0}", playerMarkers());
        for (PlayerNode player : players.values()) {
            log.log(Level.INFO, "- {0}", printer.getPlayerIdentifier(player));
        }
    }

    public String playerMarkers() {
        return String.join(", ", players.sequencedKeySet());
    }

    private void tryAddPlayer(PlayerNode player) {
        String playerMarker = player.playerMarker();
        if (!players.containsKey(playerMarker)) {
            players.put(playerMarker, player);
            playerMarkers.add(playerMarker);
        } else {
            throw new RuntimeException(
                    "Unable to add player "
                            + player
                            + " as player with marker '"
                            + playerMarker
                            + "' already exists.");
        }
    }

    public static void closeAll(PlayerNodes players2) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'closeAll'");
    }

	public void close() {
        players.values().forEach((p) -> {
            if (p instanceof AutoCloseable c) {
                try {
                    c.close();
                } catch (Exception e) {
                    log.log(Level.WARNING, "Unable to close player " + p, e);
                }
            }
        });
	}
}
