package org.example.transport;

public class TransportException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public TransportException(String message) {
    super(message);
  }

  public TransportException(String message, Throwable cause) {
    super(message, cause);
  }

  public TransportException(Throwable cause) {
    super(cause);
  }
}
