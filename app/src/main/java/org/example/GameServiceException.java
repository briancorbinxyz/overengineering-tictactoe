package org.example;

public class GameServiceException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public GameServiceException(String message) {
    super(message);
  }

  public GameServiceException(String message, Throwable cause) {
    super(message, cause);
  }
}
