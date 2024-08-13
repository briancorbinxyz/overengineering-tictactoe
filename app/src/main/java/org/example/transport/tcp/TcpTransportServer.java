package org.example.transport.tcp;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.Socket;
import org.example.GameBoard;
import org.example.MessageHandler;
import org.example.RemoteMessageHandler;
import org.example.SecureMessageHandler;
import org.example.transport.TransportConfiguration;
import org.example.transport.TransportException;
import org.example.transport.TransportServer;

public record TcpTransportServer(Socket socket, MessageHandler connection)
        implements TransportServer {

    private static final Logger log = System.getLogger(TcpTransportServer.class.getName());

    public TcpTransportServer(Socket socket) throws IOException {
        this(
                socket,
                new SecureMessageHandler.Client(
                        new RemoteMessageHandler(
                                new ObjectOutputStream(socket.getOutputStream()),
                                new ObjectInputStream(socket.getInputStream()))));
    }

    @Override
    public void initialize(TransportConfiguration configuration) {
        try {
            connection.init();
            connection.sendMessage(
                    String.format(
                            TcpProtocol.GAME_STARTED_JSON_FORMAT, configuration.playerMarker()));
        } catch (IOException e) {
            throw new TransportException(e.getMessage(), e);
        }
        log.log(Level.INFO, "Initialized socket {0} to client for Tic-Tac-Toe.", socket);
    }

    @Override
    public void close() throws Exception {
        connection.sendMessage(TcpProtocol.EXIT_CODE);
        connection.close();
    }

    @Override
    public void sendState(GameBoard board) {
        try {
            connection.sendMessage(
                    String.format(TcpProtocol.NEXT_MOVE_JSON_FORMAT, board.asJsonString()));
        } catch (IOException e) {
            throw new TransportException(e.getMessage(), e);
        }
    }

    @Override
    public int acceptMove(GameBoard board) {
        try {
            var clientMessage = connection.receiveMessage();
            return Integer.parseInt(clientMessage);
        } catch (IOException e) {
            throw new TransportException(e.getMessage(), e);
        }
    }
}
