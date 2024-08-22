package org.example.algo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.example.GameState;

public class MonteCarloTreeSearch {

  private final GameState initialState;
  private final Long maxIterationTimeMillis;

  private static final double MIN_SCORE = 0.5;
  private static final double MAX_SCORE = 1.0;
  private static final double DRAW_SCORE = 0.0;

  public MonteCarloTreeSearch(GameState state) {
    this.initialState = state;
    this.maxIterationTimeMillis = TimeUnit.SECONDS.toMillis(1);
  }

  public MonteCarloTreeSearch(GameState state, long maxIterationTime) {
    this.initialState = state;
    this.maxIterationTimeMillis = maxIterationTime;
  }

  public int bestMove() {
    return monteCarloTreeSearch(initialState);
  }

  private int monteCarloTreeSearch(GameState state) {
    MCTSNode root = new MCTSNode(state, null);
    var startTime = System.currentTimeMillis();

    while (System.currentTimeMillis() - startTime < maxIterationTimeMillis) {
      MCTSNode node = treePolicy(root);
      double[] reward = defaultPolicy(node.state);
      backpropagate(node, reward);
    }

    return bestChild(root).state.lastMove();
  }

  static class MCTSNode {
    GameState state;
    MCTSNode parent;
    List<MCTSNode> children;
    int visits;
    double[] scores;

    public MCTSNode(GameState state, MCTSNode parent) {
      this.state = state;
      this.parent = parent;
      this.children = new ArrayList<>();
      this.visits = 0;
      this.scores = new double[state.playerMarkers().size()];
    }

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

    public boolean isFullyExpanded() {
      return children.size() == state.board().availableMoves().size();
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
    List<Integer> untriedMoves = new ArrayList<>(node.state.board().availableMoves());
    untriedMoves.removeAll(
        node.children.stream().map(child -> child.state.lastMove()).collect(Collectors.toList()));

    int move = untriedMoves.get(new Random().nextInt(untriedMoves.size()));
    GameState newState = node.state.withMove(move);
    MCTSNode child = new MCTSNode(newState, node);
    node.children.add(child);
    return child;
  }

  private double[] defaultPolicy(GameState state) {
    GameState tempState = new GameState(state);
    while (!tempState.isTerminal()) {
      List<Integer> moves = tempState.board().availableMoves();
      int move = moves.get(new Random().nextInt(moves.size()));
      tempState = tempState.withMove(move);
    }
    return defaultReward(tempState);
  }

  public double[] defaultReward(GameState state) {
    double[] reward = new double[state.playerMarkers().size()];
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
      } else if (i != -1) {
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
