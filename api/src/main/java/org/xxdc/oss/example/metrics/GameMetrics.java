package org.xxdc.oss.example.metrics;

import java.util.Collection;
import java.util.HashMap;
import java.util.stream.Gatherer;
import java.util.stream.Stream;

import org.xxdc.oss.example.GameState;

public class GameMetrics {

  private final Collection<GameState> gameState;

  public GameMetrics(Collection<GameState> gameState) {
    this.gameState = gameState;
  }

  public Stream<GameMetricWinningPath> winningPaths() {
    return gameState.stream()
        .skip(1) // skip initial state
        .gather(
            Gatherer.of(
                HashMap::new, // initializer
                (pathsByPlayer, state) -> { // integrator
                  var player = state.playerMarkers().get(state.lastPlayerIndex());
                  var playerValue = state.lastPlayerIndex() + 1;
                  var currentPaths = pathsByPlayer.computeIfAbsent(player, k -> new HashSet<>());

                  updatePathsWithMove(currentPaths, state.lastMove());
                  currentPaths.addAll(
                      findNewWinningPaths(state.board(), state.lastMove(), playerValue));

                  return new GameMetricWinningPath(
                      state.lastMove(),
                      player,
                      pathsByPlayer.values().stream().mapToInt(Set::size).sum(),
                      Set.copyOf(currentPaths),
                      findCompletedPath(state.board(), currentPaths, playerValue));
                },
                (paths1, paths2) -> { // combiner
                  var combined = new HashMap<>(paths1);
                  paths2.forEach(
                      (player, paths) ->
                          combined.merge(
                              player,
                              paths,
                              (set1, set2) -> {
                                var merged = new HashSet<>(set1);
                                merged.addAll(set2);
                                return merged;
                              }));
                  return combined;
                }));
  }
  private void updatePathsWithMove(Set<List<Integer>> paths, int newMove) {
    var updatedPaths =
        paths.stream()
            .map(
                path -> {
                  var newPath = new ArrayList<>(path);
                  newPath.add(newMove);
                  return newPath;
                })
            .toList();
    paths.addAll(updatedPaths);
  }

  private Set<List<Integer>> findNewWinningPaths(GameBoard board, int position, int playerValue)
  {
    var paths = new HashSet<List<Integer>>();
    // var dimension = board.getDimension();

    // // Check horizontal path
    // var row = position / dimension;
    // var rowPath = IntStream.range(0, dimension).map(col -> row * dimension +
    // col).boxed().toList();
    // paths.add(rowPath);

    // // Check vertical path
    // var col = position % dimension;
    // var colPath = IntStream.range(0, dimension).map(r -> r * dimension +
  col).boxed().toList();
    // paths.add(colPath);

    // // Check diagonals if position is on them
    // if (row == col) {
    //   var diagPath = IntStream.range(0, dimension).map(i -> i * dimension +
  i).boxed().toList();
    //   paths.add(diagPath);
    // }

    // if (row + col == dimension - 1) {
    //   var antiDiagPath = IntStream.range(0, dimension)
    //       .map(i -> i * dimension + (dimension - 1 - i))
    //       .boxed()
    //       .toList();
    //   paths.add(antiDiagPath);
    // }

    return paths;
  }

  private List<Integer> findCompletedPath(
      GameBoard board, Set<List<Integer>> paths, int playerValue) {
    return paths.stream()
        .filter(path -> isPathComplete(board, path, playerValue))
        .findFirst()
        .orElse(List.of());
  }

  private boolean isPathComplete(GameBoard board, List<Integer> path, int playerValue) {
    return true;
    // return path.stream().allMatch(pos -> board.getValueAt(pos) == playerValue);
  }

  public String getWinningPathMetrics() {
    var metrics = new StringBuilder();
    var timestamp = System.currentTimeMillis();

    winningPaths()
        .forEach(
            analysis -> {
              // Total potential winning paths per player
              metrics.append(
                  String.format(
                      "tictactoe_winning_paths_total{player=\"%s\",game_id=\"%s\"} %d %d%n",
                      analysis.playerMarker(),
                      // getGameId(),
                      analysis.potentialWinningPaths().size(),
                      timestamp));

              // Completed winning path counter (0 or 1)
              metrics.append(
                  String.format(
                      "tictactoe_winning_path_completed{player=\"%s\",game_id=\"%s\"} %d %d%n",
                      analysis.playerMarker(),
                      // getGameId(),
                      analysis.completedPath().isEmpty() ? 0 : 1,
                      timestamp));

              // Move counter as a gauge
              metrics.append(
                  String.format(
                      "tictactoe_move_number{player=\"%s\",game_id=\"%s\"} %d %d%n",
                      analysis.playerMarker(),
                      // getGameId(),
                      analysis.moveNumber(),
                      timestamp));
            });

    return metrics.toString();
  }
}
