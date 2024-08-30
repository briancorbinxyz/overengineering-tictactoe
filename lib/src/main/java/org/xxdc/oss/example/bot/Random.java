package org.xxdc.oss.example.bot;

import java.security.SecureRandom;
import java.util.random.RandomGenerator;
import org.xxdc.oss.example.GameState;

public final class Random implements BotStrategy {

  private final RandomGenerator random;
  private final GameState state;

  public Random(GameState state) {
    this.state = state;
    this.random = new SecureRandom();
  }

  @Override
  public int bestMove() {
    var availableMoves = state.board().availableMoves();
    return availableMoves.get(random.nextInt(availableMoves.size()));
  }
}
