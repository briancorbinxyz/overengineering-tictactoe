package org.xxdc.oss.example;

import java.io.Serializable;
import java.util.function.ToIntFunction;
import org.xxdc.oss.example.bot.BotStrategy;

/**
 * Represents a bot player in the game. The bot player uses a random number generator to make moves
 * on the game board.
 *
 * @param strategyFunction the function that determines the bot's next move
 */
public record BotPlayer(ToIntFunction<GameState> strategyFunction) implements Player, Serializable {

  private static final long serialVersionUID = 1L;

  /** Constructs a new BotPlayer instance using the default BotStrategy. */
  public BotPlayer() {
    this(BotStrategy.DEFAULT);
  }

  @Override
  public int nextMove(GameState state) {
    return strategyFunction.applyAsInt(state);
  }
}
