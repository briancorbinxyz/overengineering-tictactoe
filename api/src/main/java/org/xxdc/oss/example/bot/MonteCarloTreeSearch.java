package org.xxdc.oss.example.bot;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.xxdc.oss.example.GameState;

/**
 * Implements the Monte Carlo Tree Search (MCTS) algorithm for a game bot strategy. The MCTS
 * algorithm is used to select the best move for the current game state by simulating random game
 * play and backpropagating the results to update the search tree. The algorithm can be configured
 * with a maximum time limit and maximum number of iterations.
 */
public final class MonteCarloTreeSearch implements BotStrategy {

  private static final Logger log = System.getLogger(MonteCarloTreeSearch.class.getName());

  private final GameState initialState;
  private final BotStrategyConfig config;

  private static final double MIN_SCORE = -0.5;
  private static final double MAX_SCORE = 1.0;
  private static final double DRAW_SCORE = 0.0;

  /**
   * Constructs a new instance of the {@link MonteCarloTreeSearch} class with the given initial game
   * state and a default configuration (max time limit of 1 second).
   *
   * @param state the initial game state to use for the Monte Carlo tree search
   */
  public MonteCarloTreeSearch(GameState state) {
    this(state, BotStrategyConfig.newBuilder().maxTimeMillis(TimeUnit.SECONDS, 1).build());
  }

  /**
   * Constructs a new instance of the {@link MonteCarloTreeSearch} class with the given initial game
   * state and configuration.
   *
   * @param state the initial game state to use for the Monte Carlo tree search
   * @param config the configuration settings for the Monte Carlo tree search
   */
  public MonteCarloTreeSearch(GameState state, BotStrategyConfig config) {
    this.initialState = state;
    this.config = config;
  }

  @Override
  public int bestMove() {
    return monteCarloTreeSearch(initialState);
  }

  private int monteCarloTreeSearch(GameState state) {
    MCTSNode root = new MCTSNode(state);
    var startTime = System.currentTimeMillis();

    int iterations = 0;
    while (!config.exceedsMaxTimeMillis(System.currentTimeMillis() - startTime)
        && !config.exceedsMaxIterations(iterations++)) {
      MCTSNode node = treePolicy(root);
      double[] reward = defaultPolicy(node.state);
      backpropagate(node, reward);
    }

    if (log.isLoggable(Level.DEBUG)) {
      log.log(Level.DEBUG, "MCTS: \n" + root);
      log.log(Level.DEBUG, "MCTS (Selected): \n" + bestChild(root).state.lastMove());
    }
    return bestChild(root).state.lastMove();
  }

  /**
   * Selects the best child node of the given node using the UCT (Upper Confidence Bound applied to
   */
  static class MCTSNode {
    GameState state;
    MCTSNode parent;
    List<MCTSNode> children;
    int visits;
    double[] scores;

    /**
     * Constructs a new MCTSNode with the given state and no parent.
     *
     * @param state the state of the game represented by this node
     */
    public MCTSNode(GameState state) {
      this(state, null);
    }

    /**
     * Constructs a new MCTSNode with the given state and parent node.
     *
     * @param state the state of the game represented by this node
     * @param parent the parent node of this node
     */
    public MCTSNode(GameState state, MCTSNode parent) {
      this.state = state;
      this.parent = parent;
      this.children = new ArrayList<>();
      this.visits = 0;
      this.scores = new double[state.playerMarkers().size()];
    }

    /**
     * Returns the child node with the highest UCT value.
     *
     * @return the child node with the highest UCT value
     */
    public MCTSNode select() {
      MCTSNode selected = null;
      double bestValue = Double.NEGATIVE_INFINITY;
      for (MCTSNode child : children) {

        double uctValue =
            child.scores[state.currentPlayerIndex()] / child.visits
                + Math.sqrt(2 * Math.log(visits) / child.visits);
        if (uctValue > bestValue) {
          selected = child;
          bestValue = uctValue;
        }
      }
      return selected;
    }

    /**
     * Expands the node by creating a new child node for each available move.
     *
     * @return
     */
    public boolean isFullyExpanded() {
      return children.size() == state.board().availableMoves().size();
    }

    @Override
    public String toString() {
      return toString(0);
    }

    String toString(int depth) {
      var builder = new StringBuilder();
      builder.append(" ".repeat(depth * 2));
      builder.append(
          parent == null
              ? "Root"
              : state.playerMarkers().get(state.lastPlayerIndex()) + " -> " + state.lastMove());
      builder.append(" (");
      builder.append(visits);
      builder.append(") => ");
      builder.append(
          parent != null && state.lastPlayerHasChain() ? "WINNER" : state.availableMoves());
      builder.append("\n");
      builder.append(" ".repeat(depth * 2));
      builder.append(" (");
      for (int i = 0; i < scores.length; i++) {
        builder.append(state.playerMarkers().get(i));
        builder.append(": ");
        builder.append(scores[i]);
        builder.append(i < scores.length - 1 ? ", " : "");
      }
      builder.append(")");
      for (MCTSNode child : children) {
        builder.append("\n");
        builder.append(child.toString(depth + 1));
      }
      return builder.toString();
    }
  }

  private MCTSNode treePolicy(MCTSNode node) {
    while (!node.state.isTerminal()) {
      if (!node.isFullyExpanded()) {
        return expand(node);
      } else {
        node = node.select();
      }
    }
    return node;
  }

  private MCTSNode expand(MCTSNode node) {
    var untriedMoves = new ArrayList<>(node.state.board().availableMoves());
    untriedMoves.removeAll(
        node.children.stream().map(child -> child.state.lastMove()).collect(Collectors.toList()));

    int move = untriedMoves.get(new Random().nextInt(untriedMoves.size()));
    var newState = node.state.afterPlayerMoves(move);
    var child = new MCTSNode(newState, node);
    node.children.add(child);
    return child;
  }

  private double[] defaultPolicy(GameState state) {
    var tempState = new GameState(state);
    while (!tempState.isTerminal()) {
      var moves = tempState.board().availableMoves();
      int move = moves.get(new Random().nextInt(moves.size()));
      tempState = tempState.afterPlayerMoves(move);
    }
    return defaultReward(tempState);
  }

  private double[] defaultReward(GameState state) {
    var reward = new double[state.playerMarkers().size()];
    int winningPlayerIndex = -1;
    for (int i = 0; i < state.playerMarkers().size(); i++) {
      if (state.board().hasChain(state.playerMarkers().get(i))) {
        winningPlayerIndex = i;
        break;
      }
    }
    for (int i = 0; i < state.playerMarkers().size(); i++) {
      if (i == winningPlayerIndex) {
        reward[i] = MAX_SCORE;
      } else if (winningPlayerIndex != -1) {
        reward[i] = MIN_SCORE;
      } else {
        reward[i] = DRAW_SCORE;
      }
    }
    return reward;
  }

  private void backpropagate(MCTSNode node, double[] reward) {
    while (node != null) {
      node.visits++;
      for (int i = 0; i < initialState.playerMarkers().size(); i++) {
        node.scores[i] += reward[i];
      }
      node = node.parent;
    }
  }

  private MCTSNode bestChild(MCTSNode node) {
    return node.children.stream().max(Comparator.comparingDouble(c -> c.visits)).orElseThrow();
  }
}
