package com.ccp.implementations.cache.gcp.memorystore;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Local server that speaks enough of the protocol of Memorystore for Valkey for the tests of {@code GcpMemoryStore}. It
 * proves the commands the client sends, not the behavior of a real server: expiration, for instance, is only recorded.
 */
class FakeMemoryStoreServer implements Runnable, Closeable {

	/** The socket accepting clients on a free port. */
	private final ServerSocket serverSocket;

	/** The stored values, by key. */
	final Map<String, String> values = new ConcurrentHashMap<>();

	/** Every command received, in order. */
	final List<List<String>> commands = new CopyOnWriteArrayList<>();

	FakeMemoryStoreServer() throws IOException {
		this.serverSocket = new ServerSocket(0);
		Thread acceptingThread = new Thread(this);
		acceptingThread.setDaemon(true);
		acceptingThread.start();
	}

	/**
	 * Returns the port the server listens on.
	 * @return the port
	 */
	int getPort() {
		int port = this.serverSocket.getLocalPort();
		return port;
	}

	/** Accepts clients until the server is closed. */
	public void run() {
		try {
			while (true) {
				Socket socket = this.serverSocket.accept();
				FakeMemoryStoreConnection connection = new FakeMemoryStoreConnection(socket, this.values, this.commands);
				Thread connectionThread = new Thread(connection);
				connectionThread.setDaemon(true);
				connectionThread.start();
			}
		} catch (IOException e) {
			// the server was closed: nothing else to accept
		}
	}

	/** Stops accepting clients. */
	public void close() throws IOException {
		this.serverSocket.close();
	}
}
