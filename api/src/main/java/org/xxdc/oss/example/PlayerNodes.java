package org.xxdc.oss.example;

import java.io.Serializable;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.SequencedMap;

/**
 * Represents a collection of {@link PlayerNode} instances, providing methods to manage and render
 * the players.
 *
 * <p>The {@code PlayerNodes} class maintains a {@link SequencedMap} of {@link PlayerNode}
 * instances, indexed by their unique player marker. It also maintains a list of the player markers
 * in the order they were added.
 *
 * <p>The class provides methods to retrieve players by index, get the next player index, render the
 * player information, and close any {@link AutoCloseable} players.
 */
public class PlayerNodes implements Serializable {

  private static final Logger log = System.getLogger(PlayerNodes.class.getName());

  private static final long serialVersionUID = 1L;

  private final SequencedMap<String, PlayerNode> players;

  private final List<String> playerMarkers;

  /**
   * Constructs a new {@code PlayerNodes} instance with an initial capacity of 2 for both the player
   * map and the player marker list.
   */
  public PlayerNodes() {
    this.players = new LinkedHashMap<String, PlayerNode>(2);
    this.playerMarkers = new ArrayList<String>(2);
  }

  /**
   * Creates a new {@code PlayerNodes} instance and adds the provided {@link PlayerNode} instances
   * to it.
   *
   * @param players the {@link PlayerNode} instances to add to the new {@code PlayerNodes} instance
   * @return a new {@code PlayerNodes} instance containing the provided {@link PlayerNode} instances
   */
  public static PlayerNodes of(PlayerNode... players) {
    var playerList = new PlayerNodes();
    for (PlayerNode p : players) {
      playerList.tryAddPlayer(p);
    }
    return playerList;
  }

  /**
   * Returns the {@link PlayerNode} instance at the specified index in the ordered list of players.
   *
   * @param index the index of the player to retrieve
   * @return the {@link PlayerNode} instance at the specified index
   */
  public PlayerNode byIndex(int index) {
    return players.get(playerMarkers.get(index));
  }

  /**
   * Returns the index of the next player in the ordered list of players. If the current index is
   * the last player, it returns 0 to wrap around to the first player.
   *
   * @param index the current index of the player
   * @return the index of the next player in the ordered list
   */
  public int nextPlayerIndex(int index) {
    return index + 1 < players.size() ? index + 1 : 0;
  }

  /**
   * Renders a list of players, logging their player identifiers. This method is used to display
   * information about the players in the PlayerNodes instance.
   */
  public void render() {
    PlayerPrinter printer = new PlayerPrinter();
    log.log(Level.INFO, "Players: {0} ({1})", playerMarkers(), players.values());
    for (PlayerNode player : players.values()) {
      log.log(Level.INFO, "- {0}", printer.getPlayerIdentifier(player));
    }
  }

  /**
   * Returns a comma-separated string of all the player markers in the PlayerNodes instance.
   *
   * @return a string containing all the player markers
   */
  public String playerMarkers() {
    return String.join(", ", players.sequencedKeySet());
  }

  /**
   * Returns a list of all the player markers in the PlayerNodes instance.
   *
   * @return a list of player markers
   */
  public List<String> playerMarkerList() {
    return new ArrayList<>(playerMarkers);
  }

  private void tryAddPlayer(PlayerNode player) {
    String playerMarker = player.playerMarker();
    if (!players.containsKey(playerMarker)) {
      players.put(playerMarker, player);
      playerMarkers.add(playerMarker);
    } else {
      throw new RuntimeException(
          "Unable to add player "
              + player
              + " as player with marker '"
              + playerMarker
              + "' already exists.");
    }
  }

  public void close() {
    players
        .values()
        .forEach(
            (p) -> {
              if (p instanceof AutoCloseable c) {
                try {
                  c.close();
                } catch (Exception e) {
                  log.log(Level.WARNING, "Unable to close player " + p, e);
                }
              }
            });
  }
}
