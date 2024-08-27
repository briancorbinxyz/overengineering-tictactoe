package org.example.transport;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import org.example.Player;
import org.example.RemoteMessageHandler;
import org.example.SecureMessageHandler;
import org.example.transport.tcp.TcpTransportClient;

public class Transports {

  public static <P extends Player> TcpTransportClient<P> newTcpTransportClient(
      P player, Socket socket) throws IOException {
    return new TcpTransportClient<P>(
        new SecureMessageHandler.Client(
            new RemoteMessageHandler(
                new ObjectOutputStream(socket.getOutputStream()),
                new ObjectInputStream(socket.getInputStream()))),
        player);
  }
}
