package org.example;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.random.RandomGenerator;
import java.util.regex.Matcher;

/**
 * Represents a bot player in the game. The bot player uses a random number generator to make moves
 * on the game board.
 */
public final class RemoteBotPlayer implements Player, Serializable, AutoCloseable {

    private static final Logger log = System.getLogger(RemoteBotPlayer.class.getName());

    private static final long serialVersionUID = 1L;

    private final String playerMarker;

    private final transient JsonPrinter json = new JsonPrinter();

    private transient MessageHandler connection;

    public String getPlayerMarker() {
        return playerMarker;
    }
    ;

    public RemoteBotPlayer(String playerMarker, Socket socket) throws Exception {
        this.playerMarker = playerMarker;
        initConnection(playerMarker, socket);
        log.log(Level.INFO, "Server connecting client to socket {0} for Tic-Tac-Toe.", socket);
    }

    private void initConnection(String playerMarker, Socket socket) throws IOException {
        this.connection =
                new SecureMessageHandler.Server(
                        new RemoteMessageHandler(
                                new ObjectOutputStream(socket.getOutputStream()),
                                new ObjectInputStream(socket.getInputStream())));
        this.connection.init();
        this.connection.sendMessage(
                String.format(RemoteProtocol.GAME_STARTED_JSON_FORMAT, playerMarker));
    }

    public int nextMove(GameBoard board) {
        int location = -1;
        do {
            try {
                connection.sendMessage(
                        String.format(RemoteProtocol.NEXT_MOVE_JSON_FORMAT, json.asJson(board)));
                String clientMessage = connection.receiveMessage();
                location = Integer.parseInt(clientMessage);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        } while (!board.isValidMove(location));
        return location;
    }

    @Override
    public void close() throws Exception {
        connection.sendMessage(RemoteProtocol.EXIT_CODE);
        connection.close();
    }
    ;

    public static record Client(MessageHandler connection, RandomGenerator randomGenerator)
            implements Serializable, AutoCloseable {

        public Client(Socket socket) throws Exception {
            this(
                    new SecureMessageHandler.Client(
                            new RemoteMessageHandler(
                                    new ObjectOutputStream(socket.getOutputStream()),
                                    new ObjectInputStream(socket.getInputStream()))),
                    new SecureRandom());
            this.connection.init();
        }

        public void connectAndPlay(Socket socket) {
            Matcher matcher;
            try {
                // For now read the board and send a random move
                String serverMessage;
                while ((serverMessage = connection.receiveMessage()) != null
                        && !serverMessage.equals(RemoteProtocol.EXIT_CODE)) {
                    // LOG.log(Level.INFO, "DEBUG: " + serverMessage);
                    matcher = RemoteProtocol.NEXT_MOVE_JSON_PATTERN.matcher(serverMessage);
                    if (matcher.matches()) {
                        int dimension = Integer.valueOf(matcher.group(3));
                        int nextMove = randomGenerator.nextInt(dimension * dimension);
                        connection.sendMessage(String.valueOf(nextMove));
                    }
                    // LOG.log(Level.INFO, this + " sending move: " + nextMove);
                }
                ;
            } catch (IOException e) {
                log.log(Level.INFO, "IO exception: {0}", e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }

        @Override
        public void close() throws Exception {
            connection.close();
        }
    }
}
