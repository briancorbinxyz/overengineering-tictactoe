package org.xxdc.oss.example;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Handles duplex (two-way) communication between a client and a server using Java's built-in object
 * serialization. Provides methods to send and receive messages as strings, as well as to send and
 * receive arbitrary Java objects. The handler must be initialized before use.
 */
public class DuplexMessageHandler implements MessageHandler {

  private final ObjectOutputStream out;

  private final ObjectInputStream in;

  private volatile boolean initialized = false;

  /**
   * Constructs a new DuplexMessageHandler instance with the provided ObjectOutputStream and
   * ObjectInputStream.
   *
   * @param out the ObjectOutputStream to use for sending messages
   * @param in the ObjectInputStream to use for receiving messages
   */
  public DuplexMessageHandler(ObjectOutputStream out, ObjectInputStream in) {
    this.out = out;
    this.in = in;
  }

  /**
   * Sends the given message as a byte array over the underlying communication channel.
   *
   * @param message the message to send
   * @throws IOException if an I/O error occurs while sending the message
   */
  @Override
  public void sendMessage(String message) throws IOException {
    checkInitialized();
    sendBytes(message.getBytes());
  }

  /**
   * Receives a message from the underlying communication channel as a byte array and returns it as
   * a string.
   *
   * @return the received message as a string
   * @throws IOException if an I/O error occurs while receiving the message
   */
  @Override
  public String receiveMessage() throws IOException {
    checkInitialized();
    return new String(receiveBytes());
  }

  @Override
  public void close() throws Exception {
    this.in.close();
    this.out.close();
  }

  @Override
  public void init() {
    initialized = true;
  }

  void sendObject(Object object) throws IOException {
    checkInitialized();
    out.writeObject(object);
    out.flush();
  }

  Object receiveObject() throws IOException, ClassNotFoundException {
    checkInitialized();
    return in.readObject();
  }

  void sendBytes(byte[] bytes) throws IOException {
    checkInitialized();
    out.writeInt(bytes.length);
    out.write(bytes);
    out.flush();
  }

  byte[] receiveBytes() throws IOException {
    checkInitialized();
    int sz = in.readInt();
    byte[] data = new byte[sz];
    in.readFully(data);
    return data;
  }

  private void checkInitialized() {
    if (!initialized) {
      throw new IllegalStateException("MessageHandler has not been initialized.");
    }
  }
}
