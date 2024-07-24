package org.example;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class RemoteMessageHandler implements MessageHandler, AutoCloseable {

    private final ObjectOutputStream out;

	private final ObjectInputStream in;

	private volatile boolean initialized = false;

    public RemoteMessageHandler(ObjectOutputStream out, ObjectInputStream in) {
		this.out = out;
		this.in = in;
	}

	@Override
	public void sendMessage(String message) throws IOException {
        sendBytes(message.getBytes());
	}

	@Override
	public String receiveMessage() throws IOException {
        return new String(receiveBytes());
	}

	void sendObject(Object object) throws IOException {
		if (!initialized) {
			throw new IllegalStateException("MessageHandler has not been initialized.");
		}
        out.writeObject(object);
        out.flush();
	};

	Object receiveObject() throws IOException, ClassNotFoundException {
		if (!initialized) {
			throw new IllegalStateException("MessageHandler has not been initialized.");
		}
        return in.readObject();
	}

	void sendBytes(byte[] bytes) throws IOException {
		if (!initialized) {
			throw new IllegalStateException("MessageHandler has not been initialized.");
		}
        out.writeInt(bytes.length);
        out.write(bytes);
        out.flush();
	}

	byte[] receiveBytes() throws IOException {
		if (!initialized) {
			throw new IllegalStateException("MessageHandler has not been initialized.");
		}
        int sz = in.readInt();
        byte[] data = new byte[sz];
        in.readFully(data);
        return data;
	}

	@Override
	public void close() throws Exception {
        this.in.close();
        this.out.close();
	}

	@Override
	public void init() {
		// nothing to initialize
		initialized = true;
	}
}
