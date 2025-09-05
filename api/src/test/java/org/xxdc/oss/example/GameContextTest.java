package org.xxdc.oss.example;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

public class GameContextTest {

  @Test
  public void test_record_pattern_deconstruction_in_switch() {
    var ctx = new GameContext.Builder().id("abc-123").build();
    String result = describeContext(ctx);
    assertTrue(result.contains("abc-123"));
  }

  @Test
  public void test_record_pattern_with_guard() {
    var ctx = new GameContext.Builder().id("   ").build();
    String result = validateContext(ctx);
    assertEquals(result, "invalid");

    result = validateContext(new GameContext.Builder().id("ok").build());
    assertEquals(result, "valid");
  }

  private String describeContext(Object o) {
    return switch (o) {
      case GameContext(String id, long createdAt) -> "GameContext[id=%s, createdAt=%d]".formatted(id, createdAt);
      default -> "Unknown";
    };
  }

  private String validateContext(Object o) {
    return switch (o) {
      case GameContext(String id, long createdAt) when id != null && !id.isBlank() -> "valid";
      case GameContext _ -> "invalid";
      default -> "unknown";
    };
  }
}
