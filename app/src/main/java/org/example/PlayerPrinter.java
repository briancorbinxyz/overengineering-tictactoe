package org.example;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class PlayerPrinter {

    public String getPlayerIdentifier(Player player) {
        String javaVersion = System.getProperty("java.version");
        String osName = System.getProperty("os.name");
        String osVersion = System.getProperty("os.version");

        try {
            InetAddress localHost = InetAddress.getLocalHost();
            String ipAddress = localHost.getHostAddress();
            String hostName = localHost.getHostName();

            return String.format("TicTacToeClient[%s]/1.0 '%s' (IP: %s; Host: %s; Java: %s; OS: %s %s)",
                    playerToType(player), player.getPlayerMarker(), ipAddress, hostName, javaVersion, osName, osVersion);
        } catch (UnknownHostException e) {
            return String.format("TicTacToeClient[%s]/1.0 '%s' (IP: unknown; Host: unknown; Java: %s; OS: %s %s)",
                playerToType(player), player.getPlayerMarker(), javaVersion, osName, osVersion);
        }
    }

    private String playerToType(Player player) {
        return switch (player.getClass().getSimpleName()) {
            case "HumanPlayer" -> "Human";
            case "BotPlayer" -> "Bot";
            case "LegacyPlayer" -> "Legacy";
            default -> "Unknown";
        };
    }

}
