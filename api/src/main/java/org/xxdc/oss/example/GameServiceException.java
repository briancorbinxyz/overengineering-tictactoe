package org.xxdc.oss.example;

import java.util.Objects;

/** Represents an exception that can occur when using the game service. */
public class GameServiceException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /**
   * Constructs a new {@link GameServiceException} with the specified error message.
   *
   * @param message the error message describing the exception
   */
  public GameServiceException(String message) {
    super(message);
  }

  /**
   * Constructs a new {@link GameServiceException} with the specified error message and cause.
   *
   * @param message the error message describing the exception
   * @param cause the underlying cause of the exception
   */
  public GameServiceException(String message, Throwable cause) {
    Objects.requireNonNull(cause);
    super(message, cause);
  }
}
