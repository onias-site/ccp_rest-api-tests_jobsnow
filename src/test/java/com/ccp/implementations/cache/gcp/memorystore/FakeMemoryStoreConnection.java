package com.ccp.implementations.cache.gcp.memorystore;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.ccp.decorators.CcpJsonFieldName;

/**
 * One client of {@link FakeMemoryStoreServer}: reads the commands in the protocol of the server (RESP) and answers
 * SETEX, GET and DEL over a map; any other command, such as the CLIENT SETINFO of the handshake, gets an OK.
 */
class FakeMemoryStoreConnection implements Runnable {
	enum Fields implements CcpJsonFieldName {
		SETEX,
		GET,
		DEL,
		OK_REPLY {
			public String getValue() {
				return "+OK\r\n";
			}
		},
		NIL_REPLY {
			public String getValue() {
				return "$-1\r\n";
			}
		},
		BULK_REPLY {
			public String getValue() {
				return "$%d\r\n%s\r\n";
			}
		},
		INTEGER_REPLY {
			public String getValue() {
				return ":%d\r\n";
			}
		},
	}

	/** The connected client. */
	private final Socket socket;

	/** The stored values, by key. */
	private final Map<String, String> values;

	/** Every command received, in order. */
	private final List<List<String>> commands;

	FakeMemoryStoreConnection(Socket socket, Map<String, String> values, List<List<String>> commands) {
		this.socket = socket;
		this.values = values;
		this.commands = commands;
	}

	/** Answers commands until the client disconnects. */
	public void run() {
		try (InputStream input = new BufferedInputStream(this.socket.getInputStream());
				OutputStream output = this.socket.getOutputStream()) {
			while (true) {
				List<String> command = this.readCommand(input);
				this.commands.add(command);
				String reply = this.execute(command);
				byte[] replyBytes = reply.getBytes(StandardCharsets.UTF_8);
				output.write(replyBytes);
				output.flush();
			}
		} catch (IOException e) {
			// the client disconnected: nothing else to answer
		}
	}

	/**
	 * Reads one command: an array of bulk strings.
	 * @param input the stream of the client
	 * @return the command name and its arguments
	 * @throws IOException when the client disconnects
	 */
	private List<String> readCommand(InputStream input) throws IOException {
		String header = this.readLine(input);
		String argumentsCountText = header.substring(1);
		int argumentsCount = Integer.parseInt(argumentsCountText);
		List<String> command = new ArrayList<>();
		for (int index = 0; index < argumentsCount; index++) {
			String lengthLine = this.readLine(input);
			String lengthText = lengthLine.substring(1);
			int length = Integer.parseInt(lengthText);
			byte[] argumentBytes = input.readNBytes(length);
			String argument = new String(argumentBytes, StandardCharsets.UTF_8);
			command.add(argument);
			this.readLine(input);
		}
		return command;
	}

	/**
	 * Reads up to the end of the line.
	 * @param input the stream of the client
	 * @return the line, without its terminator
	 * @throws IOException when the client disconnects
	 */
	private String readLine(InputStream input) throws IOException {
		ByteArrayOutputStream line = new ByteArrayOutputStream();
		int current = input.read();
		while (current != '\r') {
			if (current < 0) {
				throw new EOFException();
			}
			line.write(current);
			current = input.read();
		}
		input.read();
		String text = line.toString(StandardCharsets.UTF_8);
		return text;
	}

	/**
	 * Runs the command over the map.
	 * @param command the command name and its arguments
	 * @return the reply in the protocol of the server
	 */
	private String execute(List<String> command) {
		String commandName = command.get(0);

		if (Fields.SETEX.name().equalsIgnoreCase(commandName)) {
			this.values.put(command.get(1), command.get(3));
			return Fields.OK_REPLY.getValue();
		}

		if (Fields.GET.name().equalsIgnoreCase(commandName)) {
			return this.read(command.get(1));
		}

		if (Fields.DEL.name().equalsIgnoreCase(commandName)) {
			return this.delete(command);
		}

		return Fields.OK_REPLY.getValue();
	}

	/**
	 * Reads a key.
	 * @param key the key
	 * @return the value as a bulk string, or the nil reply
	 */
	private String read(String key) {
		boolean missing = false == this.values.containsKey(key);

		if (missing) {
			return Fields.NIL_REPLY.getValue();
		}

		String value = this.values.get(key);
		byte[] valueBytes = value.getBytes(StandardCharsets.UTF_8);
		String template = Fields.BULK_REPLY.getValue();
		String reply = String.format(template, valueBytes.length, value);
		return reply;
	}

	/**
	 * Deletes the keys of the command.
	 * @param command the command name and the keys
	 * @return how many keys existed, as an integer reply
	 */
	private String delete(List<String> command) {
		int deleted = 0;
		for (String key : command.subList(1, command.size())) {
			boolean existed = this.values.containsKey(key);
			this.values.remove(key);
			deleted += existed ? 1 : 0;
		}
		String template = Fields.INTEGER_REPLY.getValue();
		String reply = String.format(template, deleted);
		return reply;
	}
}
