package org.example;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class IOConnection implements RemoteConnection, AutoCloseable {

    private final ObjectOutputStream out;

	private final ObjectInputStream in;

    public IOConnection(ObjectOutputStream out, ObjectInputStream in) {
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

	private void sendBytes(byte[] bytes) throws IOException {
        out.writeInt(bytes.length);
        out.write(bytes);
        out.flush();
	}

	private byte[] receiveBytes() throws IOException {
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
}
