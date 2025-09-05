package org.xxdc.oss.example;

import java.util.Objects;

/// The context of a game.
public record GameContext(String id) {

  /// A builder for creating a game context.
  public static final class Builder {
    private String id;

    /// Sets the ID of the game context.
    public Builder id(String id) {
      this.id = id;
      return this;
    }

    /// Builds the game context.
    public GameContext build() {
      Objects.requireNonNull(id);
      return new GameContext(id);
    }
  }
}
