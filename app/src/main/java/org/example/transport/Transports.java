package org.example.transport;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.SecureRandom;
import org.example.Player;
import org.example.RemoteMessageHandler;
import org.example.SecureMessageHandler;
import org.example.transport.tcp.TcpTransportClient;

public class Transports {

    public static <P extends Player> TcpTransportClient<P> newTcpTransportClient(
            Class<P> clazz, Socket socket) throws Exception {
        return new TcpTransportClient<P>(
                new SecureMessageHandler.Client(
                        new RemoteMessageHandler(
                                new ObjectOutputStream(socket.getOutputStream()),
                                new ObjectInputStream(socket.getInputStream()))),
                (board) -> new SecureRandom().nextInt(board.dimension() * board.dimension()));
        // TODO: hardcoded as a bot for now
    }
}
