package org.example;

import java.io.Serializable;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import org.example.transport.Transport;
import org.example.transport.TransportConfiguration;

public final class RemotePlayer<T extends Player> implements Player, Serializable, AutoCloseable {

    static final Logger log = System.getLogger(RemotePlayer.class.getName());

    private static final long serialVersionUID = 1L;

    private final String playerMarker;

    private final transient Transport transport;

    public String playerMarker() {
        return playerMarker;
    }

    public RemotePlayer(String playerMarker, Transport transport) throws Exception {
        this.playerMarker = playerMarker;
        this.transport = transport;
        this.transport.initialize(new TransportConfiguration(playerMarker));
    }

    public int nextMove(GameBoard board) {
        int location = -1;
        do {
            try {
                transport.sendState(board);
                // After receiving the game board the player should send a move
                location = transport.acceptMove(board);
            } catch (NumberFormatException e) {
                log.log(Level.TRACE, "Invalid move from client: {0}", e.getMessage(), e);
            }
        } while (!board.isValidMove(location));
        return location;
    }

    @Override
    public void close() throws Exception {
        transport.close();
    }
}
