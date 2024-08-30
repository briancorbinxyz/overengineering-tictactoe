package org.xxdc.oss.example;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class RemoteMessageHandler implements MessageHandler {

  private final ObjectOutputStream out;

  private final ObjectInputStream in;

  private volatile boolean initialized = false;

  public RemoteMessageHandler(ObjectOutputStream out, ObjectInputStream in) {
    this.out = out;
    this.in = in;
  }

  @Override
  public void sendMessage(String message) throws IOException {
    checkInitialized();
    sendBytes(message.getBytes());
  }

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
