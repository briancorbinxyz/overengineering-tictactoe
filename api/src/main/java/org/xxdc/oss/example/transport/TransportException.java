package org.xxdc.oss.example.transport;

/**
 * Represents an exception that occurred during transport-related operations. This exception is a
 * subclass of {@link RuntimeException} and can be used to indicate transport-related errors that
 * occur during the execution of the application.
 */
public class TransportException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /**
   * Constructs a new {@link TransportException} with the specified detail message.
   *
   * @param message the detail message (which is saved for later retrieval by the {@link
   *     #getMessage()} method).
   */
  public TransportException(String message) {
    super(message);
  }

  /**
   * Constructs a new {@link TransportException} with the specified detail message and cause.
   *
   * @param message the detail message (which is saved for later retrieval by the {@link
   *     #getMessage()} method).
   * @param cause the cause (which is saved for later retrieval by the {@link #getCause()} method).
   *     (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
   */
  public TransportException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructs a new {@link TransportException} with the specified cause.
   *
   * @param cause the cause (which is saved for later retrieval by the {@link #getCause()} method).
   *     (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
   */
  public TransportException(Throwable cause) {
    super(cause);
  }
}
