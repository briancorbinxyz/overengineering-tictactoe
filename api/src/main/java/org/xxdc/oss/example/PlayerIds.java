package org.xxdc.oss.example;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

/**
 * Manages player IDs for a game system. This class provides thread-safe operations for generating
 * and retrieving player IDs.
 */
public class PlayerIds {

  /** The next available player ID. */
  private volatile int nextId;

  /** VarHandle for atomic operations on nextId. */
  private static final VarHandle NEXT_ID_VH;

  static {
    try {
      NEXT_ID_VH = MethodHandles.lookup().findVarHandle(PlayerIds.class, "nextId", int.class);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  /**
   * Constructs a new PlayerIds instance with the specified initial value.
   *
   * @param initialValue The initial value for the next player ID.
   */
  public PlayerIds(int initialValue) {
    this.nextId = initialValue;
  }

  /**
   * Retrieves the current value of the next player ID without incrementing it.
   *
   * @return The current value of the next player ID.
   */
  public int getNextId() {
    return nextId;
  }

  /**
   * Atomically retrieves the current value of the next player ID and increments it.
   *
   * @return The current value of the next player ID before incrementing.
   */
  public int getNextIdAndIncrement() {
    return (int) NEXT_ID_VH.getAndAdd(this, 1);
  }
}
