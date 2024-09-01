package org.xxdc.oss.example.transport.tcp;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import org.xxdc.oss.example.Player;
import org.xxdc.oss.example.transport.DuplexMessageHandler;
import org.xxdc.oss.example.transport.SecureDuplexMessageHandler;

/** A utility class for creating transport clients and servers. */
public class TcpTransports {

  private TcpTransports() {}

  /**
   * Creates a new TCP transport client for the given player and socket.
   *
   * @param <P> the type of player
   * @param player the player instance
   * @param socket the socket to use for the transport
   * @return a new TCP transport client
   * @throws IOException if an I/O error occurs
   */
  public static <P extends Player> TcpTransportClient<P> newTcpTransportClient(
      P player, Socket socket) throws IOException {
    return new TcpTransportClient<P>(
        new SecureDuplexMessageHandler.Client(
            new DuplexMessageHandler(
                new ObjectOutputStream(socket.getOutputStream()),
                new ObjectInputStream(socket.getInputStream()))),
        player);
  }
}
