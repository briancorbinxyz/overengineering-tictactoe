package org.example.transport.tcp;

import java.io.IOException;
import java.util.function.Consumer;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.lang.invoke.MethodHandles;
import org.example.GameBoard;
import org.example.MessageHandler;
import org.example.Player;
import org.example.transport.TransportException;

public record TcpTransportClient<T extends Player>(MessageHandler connection, T player)
        implements AutoCloseable {

    private static final Logger log =
            System.getLogger(MethodHandles.lookup().lookupClass().getName());

    public TcpTransportClient(MessageHandler connection, T player) {
        try {
            this.connection = connection;
            this.player = player;
            this.connection.init();
        } catch (IOException e) {
            throw new TransportException(e);
        }
    }

    public void run() {
        log.log(Level.DEBUG, "Started TCP transport client");
        try {
            String playerMarker = initPlayerMarker();

            String msg;
            while ((msg = connection.receiveMessage()) != null
                    && !msg.equals(TcpProtocol.EXIT_CODE)) {
                log.log(Level.DEBUG, "Received message from server: {0}", msg);
                var board = TcpProtocol.fromNextMoveState(msg);
                board.ifPresentOrElse(
                        makeMove(playerMarker),
                        () -> {
                            throw new TransportException("Invalid message from transport");
                        });
            }  
            handleExit(msg);
        } catch (IOException e) {
            throw new TransportException("IO exception: " + e.getMessage(), e);
        }
    }

    private Consumer<GameBoard> makeMove(String playerMarker) {
        return (GameBoard b) -> {
            int nextMove = player.nextMove(playerMarker, b);
            try {
                connection.sendMessage(String.valueOf(nextMove));
            } catch (IOException e) {
                throw new TransportException("IO exception: " + e.getMessage(), e);
            }
        };
    }

    @Override
    public void close() throws Exception {
        connection.close();
    }

    private void handleExit(String serverMessage) {
        if (serverMessage == null) {
            log.log(Level.DEBUG, "Received null message from server");
        } else {
            log.log(Level.DEBUG, "Received exit code from server");
        }
    }

    private String initPlayerMarker() throws IOException {
        String serverMessage = connection.receiveMessage();
        log.log(Level.DEBUG, "Received initial message from server: {0}", serverMessage);
        String playerMarker = TcpProtocol.fromGameStartedState(serverMessage).orElseThrow(() -> new TransportException("Invalid server message received. Expected game started state."));
        log.log(Level.DEBUG, "Received assigned player marker: '{0}'", playerMarker);
        return playerMarker;
    }

}
