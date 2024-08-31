package org.xxdc.oss.example.bot;

import java.util.concurrent.TimeUnit;

/**
 * Represents the configuration for a bot strategy, including limits on the number of iterations,
 * depth, and maximum execution time. The {@link Builder} class can be used to construct instances
 * of this class.
 */
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

  /**
   * Returns the maximum number of iterations allowed for the bot strategy.
   *
   * @return the maximum number of iterations, or null if not set.
   */
  public Integer getMaxIterations() {
    return maxIterations;
  }

  /**
   * Returns the maximum depth allowed for the bot strategy.
   *
   * @return the maximum depth, or null if not set.
   */
  public Integer getMaxDepth() {
    return maxDepth;
  }

  /**
   * Returns the maximum execution time in milliseconds allowed for the bot strategy.
   *
   * @return the maximum execution time in milliseconds, or null if not set.
   */
  public Long getMaxTimeMillis() {
    return maxTimeMillis;
  }

  /**
   * Returns whether the maximum number of iterations has been set for the bot strategy.
   *
   * @return true if the maximum number of iterations has been set, false otherwise.
   */
  public boolean hasMaxIterations() {
    return maxIterations != null;
  }

  /**
   * Returns whether the maximum depth has been set for the bot strategy.
   *
   * @return true if the maximum depth has been set, false otherwise.
   */
  public boolean hasMaxDepth() {
    return maxDepth != null;
  }

  /**
   * Returns whether the maximum execution time in milliseconds has been set for the bot strategy.
   *
   * @return true if the maximum execution time in milliseconds has been set, false otherwise.
   */
  public boolean hasMaxTimeMillis() {
    return maxTimeMillis != null;
  }

  /**
   * Returns whether the number of iterations for the bot strategy exceeds the maximum allowed.
   *
   * @param iterations the current number of iterations
   * @return true if the number of iterations exceeds the maximum, false otherwise
   */
  public boolean exceedsMaxIterations(int iterations) {
    return hasMaxIterations() && iterations >= maxIterations;
  }

  /**
   * Returns whether the current depth exceeds the maximum allowed depth for the bot strategy.
   *
   * @param depth the current depth
   * @return true if the current depth exceeds the maximum allowed depth, false otherwise
   */
  public boolean exceedsMaxDepth(int depth) {
    return hasMaxDepth() && depth >= maxDepth;
  }

  /**
   * Returns whether the current execution time in milliseconds exceeds the maximum allowed time for
   * the bot strategy.
   *
   * @param timeMillis the current execution time in milliseconds
   * @return true if the current execution time exceeds the maximum allowed time, false otherwise
   */
  public boolean exceedsMaxTimeMillis(long timeMillis) {
    return hasMaxTimeMillis() && timeMillis >= maxTimeMillis;
  }

  /**
   * A builder for constructing a {@link BotStrategyConfig} instance.
   *
   * <p>This builder allows setting the maximum number of iterations, maximum depth, and maximum
   * execution time in milliseconds for a bot strategy configuration.
   */
  public static class Builder {
    private Integer maxIterations;
    private Integer maxDepth;
    private Long maxTimeMillis;

    /**
     * Sets the maximum number of iterations for the bot strategy.
     *
     * @param maxIterations the maximum number of iterations
     * @return this builder instance
     */
    public Builder maxIterations(Integer maxIterations) {
      this.maxIterations = maxIterations;
      return this;
    }

    /**
     * Sets the maximum depth for the bot strategy.
     *
     * @param maxDepth the maximum depth
     * @return this builder instance
     */
    public Builder maxDepth(Integer maxDepth) {
      this.maxDepth = maxDepth;
      return this;
    }

    /**
     * Sets the maximum execution time in milliseconds for the bot strategy.
     *
     * @param maxTimeMillis the maximum execution time in milliseconds
     * @return this builder instance
     */
    public Builder maxTimeMillis(Long maxTimeMillis) {
      this.maxTimeMillis = maxTimeMillis;
      return this;
    }

    /**
     * Sets the maximum execution time for the bot strategy.
     *
     * @param timeUnit the time unit of the provided time value
     * @param time the maximum execution time
     * @return this builder instance
     */
    public Builder maxTimeMillis(TimeUnit timeUnit, long time) {
      this.maxTimeMillis = timeUnit.toMillis(time);
      return this;
    }

    /**
     * Builds a {@link BotStrategyConfig} instance with the configured settings.
     *
     * @return the constructed {@link BotStrategyConfig} instance
     */
    public BotStrategyConfig build() {
      return new BotStrategyConfig(maxIterations, maxDepth, maxTimeMillis);
    }
  }

  /**
   * Creates a new {@link BotStrategyConfig.Builder} instance.
   *
   * @return a new {@link BotStrategyConfig.Builder} instance
   */
  public static BotStrategyConfig.Builder newBuilder() {
    return new BotStrategyConfig.Builder();
  }

  /**
   * Returns an empty {@link BotStrategyConfig} instance.
   *
   * @return an empty {@link BotStrategyConfig} instance
   */
  public static BotStrategyConfig empty() {
    return EMPTY;
  }
}
