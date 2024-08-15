package org.example.transport.tcp;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.lang.invoke.MethodHandles;
import java.util.function.ToIntFunction;

import org.example.GameBoard;
import org.example.MessageHandler;
import org.example.Player;
import org.example.transport.TransportException;

public record TcpTransportClient<T extends Player>(
        MessageHandler connection, T player) implements AutoCloseable {

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
            // For now read the board and send a random move
            String serverMessage = connection.receiveMessage();
            log.log(Level.DEBUG, "Received initial message from server: {0}", serverMessage);

            while ((serverMessage = connection.receiveMessage()) != null
                    && !serverMessage.equals(TcpProtocol.EXIT_CODE)) {
                log.log(Level.DEBUG, "Received message from server: {0}", serverMessage);
                var board = TcpProtocol.fromProtocol(serverMessage);
                board.ifPresentOrElse(
                        (GameBoard b) -> {
                            int nextMove = player.nextMove(b);
                            try {
                                connection.sendMessage(String.valueOf(nextMove));
                            } catch (IOException e) {
                                throw new TransportException("IO exception: " + e.getMessage(), e);
                            }
                        },
                        () -> {
                            throw new TransportException("Invalid message from transport");
                        });
            }
        } catch (IOException e) {
            throw new TransportException("IO exception: " + e.getMessage(), e);
        }
    }

    @Override
    public void close() throws Exception {
        connection.close();
    }
}
