package org.xxdc.oss.example;

/** Thrown when an invalid move is attempted in the game. */
public class InvalidMoveException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  /** Constructs a new {@link InvalidMoveException} with no detail message. */
  public InvalidMoveException() {
    super();
  }

  /**
   * Constructs a new {@link InvalidMoveException} with the specified detail message.
   *
   * @param message the detail message (which is saved for later retrieval by the {@link
   *     #getMessage()} method)
   */
  public InvalidMoveException(String message) {
    super(message);
  }
}
