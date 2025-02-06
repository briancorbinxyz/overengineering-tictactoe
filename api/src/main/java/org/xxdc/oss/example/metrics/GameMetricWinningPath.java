package org.xxdc.oss.example.metrics;

import java.util.List;
import java.util.Set;

public record GameMetricWinningPath(
    int position,
    String playerMarker,
    int moveNumber,
    Set<List<Integer>> potentialWinningPaths,
    List<Integer> completedPath) {

  @Override
  public String toString() {
    return "Move %d: Player '%s' at %d has %d winning paths %s"
        .formatted(
            moveNumber,
            playerMarker,
            position,
            potentialWinningPaths.size(),
            completedPath.isEmpty() ? "" : "WINNER: " + completedPath);
  }
}
