package org.example.transport.tcp;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.invoke.MethodHandles;
import java.util.function.ToIntFunction;
import org.example.GameBoard;
import org.example.MessageHandler;
import org.example.Player;
import org.example.transport.TransportException;

public record TcpTransportClient<T extends Player>(
        MessageHandler connection, ToIntFunction<GameBoard> moveSelector) implements AutoCloseable {

    private static final Logger log =
            System.getLogger(MethodHandles.lookup().lookupClass().getName());

    public TcpTransportClient(MessageHandler connection, ToIntFunction<GameBoard> moveSelector) {
        try {
            this.connection = connection;
            this.moveSelector = moveSelector;
            this.connection.init();
        } catch (IOException e) {
            throw new TransportException(e);
        }
    }

    public void run() {
        try {
            // For now read the board and send a random move
            String serverMessage;
            while ((serverMessage = connection.receiveMessage()) != null
                    && !serverMessage.equals(TcpProtocol.EXIT_CODE)) {
                var board = TcpProtocol.fromProtocol(serverMessage);
                board.ifPresentOrElse(
                        (GameBoard b) -> {
                            int nextMove = moveSelector.applyAsInt(b);
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
