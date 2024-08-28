package org.xxdc.oss.example.transport;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import org.xxdc.oss.example.Player;
import org.xxdc.oss.example.RemoteMessageHandler;
import org.xxdc.oss.example.SecureMessageHandler;
import org.xxdc.oss.example.transport.tcp.TcpTransportClient;

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
