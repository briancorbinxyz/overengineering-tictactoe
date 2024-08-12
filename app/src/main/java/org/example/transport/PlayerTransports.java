package org.example.transport;

import java.net.Socket;

import org.example.Player;
import org.example.transport.tcp.TcpClient;

public class PlayerTransports {

    public static TcpClient newSocketClient(Class<? extends Player> player, Socket socket) throws Exception {
        return new TcpClient(socket);
    }

}
