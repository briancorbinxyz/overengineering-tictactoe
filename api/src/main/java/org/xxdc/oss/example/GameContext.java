package org.xxdc.oss.example;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/// The context of a game.
public record GameContext(String id, long createdAt, Map<String, String> metadata) {

  /// A builder for creating a game context.
  public static final class Builder {
    private String id;
    private final Map<String, String> metadata = new HashMap<>();

    /// Sets the ID of the game context.
    public Builder id(String id) {
      this.id = id;
      return this;
    }

    /// Adds a single metadata entry.
    public Builder put(String key, String value) {
      this.metadata.put(Objects.requireNonNull(key), Objects.requireNonNullElse(value, ""));
      return this;
    }

    /// Adds all metadata entries from the provided map.
    public Builder putAll(Map<String, String> entries) {
      if (entries != null) this.metadata.putAll(entries);
      return this;
    }

    /// Builds the game context.
    public GameContext build() {
      Objects.requireNonNull(id);
      return new GameContext(id, System.currentTimeMillis(), Map.copyOf(metadata));
    }
  }
}
