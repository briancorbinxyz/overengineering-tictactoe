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
import org.example.transport.Transport;
import org.example.transport.TransportConfiguration;
import org.example.transport.TransportException;

public record TcpTransport(Socket socket, MessageHandler connection) implements Transport {

    private static final Logger log = System.getLogger(TcpTransport.class.getName());

    public TcpTransport(Socket socket) throws IOException {
        this(socket, new SecureMessageHandler.Client(new RemoteMessageHandler(
                new ObjectOutputStream(socket.getOutputStream()),
                new ObjectInputStream(socket.getInputStream()))));
    }

    @Override
    public void initialize(TransportConfiguration configuration) throws Exception {
        connection.init();
        connection.sendMessage(String.format(TcpProtocol.GAME_STARTED_JSON_FORMAT, configuration.playerMarker()));
        log.log(Level.INFO, "Initialized client to socket {0} for Tic-Tac-Toe.", socket);    
    }

    @Override
    public void close() throws Exception {
        connection.sendMessage(TcpProtocol.EXIT_CODE);
        connection.close();
    }

    @Override
    public void sendState(GameBoard board) {
        try {
            connection.sendMessage(String.format(TcpProtocol.NEXT_MOVE_JSON_FORMAT, board.asJsonString()));
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
