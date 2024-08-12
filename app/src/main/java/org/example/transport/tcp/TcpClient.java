package org.example.transport.tcp;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.System.Logger;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.Optional;
import java.util.random.RandomGenerator;
import java.util.regex.Matcher;

import org.example.GameBoard;
import org.example.GameBoardDefaultImpl;
import org.example.MessageHandler;
import org.example.RemoteMessageHandler;
import org.example.SecureMessageHandler;
import org.example.transport.TransportException;

public record TcpClient(MessageHandler connection, RandomGenerator randomGenerator)
        implements Serializable, AutoCloseable {

    private static final Logger log = System.getLogger(TcpClient.class.getName());

    public TcpClient(Socket socket) throws Exception {
        this(new SecureMessageHandler.Client(
                        new RemoteMessageHandler(
                                new ObjectOutputStream(socket.getOutputStream()),
                                new ObjectInputStream(socket.getInputStream()))),
                new SecureRandom());
        this.connection.init();
    }

    public void connectAndPlay(Socket socket) {
        try {
            // For now read the board and send a random move
            String serverMessage;
            while ((serverMessage = connection.receiveMessage()) != null
                    && !serverMessage.equals(TcpProtocol.EXIT_CODE)) {
                var board = fromProtocol(serverMessage);
                board.ifPresentOrElse((GameBoard b) -> {
                    int nextMove = randomGenerator.nextInt(b.dimension() * b.dimension());
                    try {
                        connection.sendMessage(String.valueOf(nextMove));
                    } catch (IOException e) {
                        throw new TransportException("IO exception: " + e.getMessage(), e);
                    }
                }, () -> {throw new TransportException("Invalid message from transport");});
            }
        } catch (IOException e) {
            throw new TransportException("IO exception: " + e.getMessage(), e);
        }
    }

    private Optional<GameBoard> fromProtocol(String serverMessage) {
        Matcher matcher = TcpProtocol.NEXT_MOVE_JSON_PATTERN.matcher(serverMessage);
        GameBoard board = null;
        if (matcher.matches()) {
            int dimension = Integer.valueOf(matcher.group(3));
            board = new GameBoardDefaultImpl(dimension);
        }
        return Optional.ofNullable(board);
    }

    @Override
    public void close() throws Exception {
        connection.close();
    }
}