package org.xxdc.oss.example;

import java.net.InetAddress;
import java.net.UnknownHostException;

/// Provides a utility to generate a player identifier string for a given `PlayerNode`.
public class PlayerPrinter {

  /**
   * Generates a player identifier string for the given `PlayerNode` instance. The identifier
   * includes information about the player type (local or remote), the player marker, the Java
   * version, the operating system, and the host IP address and name.
   *
   * @param player the `PlayerNode` instance to generate the identifier for
   * @return a string representing the player identifier
   */
  public String getPlayerIdentifier(PlayerNode player) {
    String javaVersion = System.getProperty("java.version");
    String osName = System.getProperty("os.name");
    String osVersion = System.getProperty("os.version");

    try {
      InetAddress localHost = InetAddress.getLocalHost();
      String ipAddress = localHost.getHostAddress();
      String hostName = localHost.getHostName();

      return String.format(
          "TicTacToeClient/1.0 [%s] (IP: %s; Host: %s; Java: %s; OS: %s %s)",
          playerToType(player), ipAddress, hostName, javaVersion, osName, osVersion);
    } catch (UnknownHostException _) {
      return String.format(
          "TicTacToeClient/1.0 [%s] (IP: unknown; Host: unknown; Java: %s; OS: %s %s)",
          playerToType(player), javaVersion, osName, osVersion);
    }
  }

  private String playerToType(PlayerNode player) {
    return switch (player) {
      case PlayerNode.Local<?> l ->
          "Local (" + l.playerMarker() + ":" + l.player().getClass().getSimpleName() + ")";
      case PlayerNode.Remote r -> "Remote (" + r.playerMarker() + ")";
    };
  }
}
