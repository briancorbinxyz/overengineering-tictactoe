package org.xxdc.oss.example;

import java.io.IOException;

/**
 * Provides an interface for handling secure message communication. This interface defines methods
 * for initializing the message handler, sending messages, and receiving messages.
 *
 * <p>Implementations of this interface must be thread-safe and handle any necessary
 * synchronization.
 */
public interface MessageHandler extends AutoCloseable {

  /**
   * Initializes the SecureMessageHandler implementation. This method must be called before any
   * other methods can be used.
   *
   * @throws IOException if there is an error during the initialization process
   */
  void init() throws IOException;

  /**
   * Sends a message.
   *
   * @param message the message to send
   * @throws IOException if there is an error sending the message
   */
  void sendMessage(String message) throws IOException;

  /**
   * Receives a message.
   *
   * @return the received message
   * @throws IOException if there is an error receiving the message
   */
  String receiveMessage() throws IOException;
}
