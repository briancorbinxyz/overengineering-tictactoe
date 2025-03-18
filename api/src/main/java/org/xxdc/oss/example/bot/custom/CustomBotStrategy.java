package org.xxdc.oss.example.bot.custom;

import java.util.List;

/// Custom, extended bot strategy
public interface CustomBotStrategy {

  ///
  /// Returns the best move for the current game state.
  /// @param availableMoves the list of available positions that can be moved to
  /// @return the index of the best move to make
  int bestMove(List<Integer> availableMoves);
}
