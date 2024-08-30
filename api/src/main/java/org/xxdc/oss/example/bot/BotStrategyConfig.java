package org.xxdc.oss.example.bot;

import java.util.concurrent.TimeUnit;

public class BotStrategyConfig {
  private Integer maxIterations;
  private Integer maxDepth;
  private Long maxTimeMillis;

  private static final BotStrategyConfig EMPTY = new BotStrategyConfig(null, null, null);

  private BotStrategyConfig(Integer maxIterations, Integer maxDepth, Long maxTimeMillis) {
    this.maxIterations = maxIterations;
    this.maxDepth = maxDepth;
    this.maxTimeMillis = maxTimeMillis;
  }

  public Integer getMaxIterations() {
    return maxIterations;
  }

  public Integer getMaxDepth() {
    return maxDepth;
  }

  public Long getMaxTimeMillis() {
    return maxTimeMillis;
  }

  public boolean hasMaxIterations() {
    return maxIterations != null;
  }

  public boolean hasMaxDepth() {
    return maxDepth != null;
  }

  public boolean hasMaxTimeMillis() {
    return maxTimeMillis != null;
  }

  public boolean exceedsMaxIterations(int iterations) {
    return hasMaxIterations() && iterations >= maxIterations;
  }

  public boolean exceedsMaxDepth(int depth) {
    return hasMaxDepth() && depth >= maxDepth;
  }

  public boolean exceedsMaxTimeMillis(long timeMillis) {
    return hasMaxTimeMillis() && timeMillis >= maxTimeMillis;
  }

  public static class Builder {
    private Integer maxIterations;
    private Integer maxDepth;
    private Long maxTimeMillis;

    public Builder maxIterations(Integer maxIterations) {
      this.maxIterations = maxIterations;
      return this;
    }

    public Builder maxDepth(Integer maxDepth) {
      this.maxDepth = maxDepth;
      return this;
    }

    public Builder maxTimeMillis(Long maxTimeMillis) {
      this.maxTimeMillis = maxTimeMillis;
      return this;
    }

    public Builder maxTimeMillis(TimeUnit timeUnit, long time) {
      this.maxTimeMillis = timeUnit.toMillis(time);
      return this;
    }

    public BotStrategyConfig build() {
      return new BotStrategyConfig(maxIterations, maxDepth, maxTimeMillis);
    }
  }

  public static BotStrategyConfig.Builder newBuilder() {
    return new BotStrategyConfig.Builder();
  }

  public static BotStrategyConfig empty() {
    return EMPTY;
  }
}
