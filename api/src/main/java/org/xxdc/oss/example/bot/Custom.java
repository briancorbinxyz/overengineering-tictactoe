package org.xxdc.oss.example.bot;

import org.xxdc.oss.example.GameState;
import org.xxdc.oss.example.bot.custom.CustomBotStrategy;

/**
 * Implements a custom bot move strategy for the game, delegating to a specific custom
 * implementation.
 */
public final class Custom implements BotStrategy {

  private final CustomBotStrategy delegate;

  private final GameState initialState;

  public Custom(GameState initialState, CustomBotStrategy delegate) {
    this.initialState = initialState;
    this.delegate = delegate;
  }

  @Override
  public int bestMove() {
    return delegate.bestMove(initialState.availableMoves());
  }
}
