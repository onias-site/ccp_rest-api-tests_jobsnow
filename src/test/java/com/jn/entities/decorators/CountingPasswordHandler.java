package com.jn.entities.decorators;

import java.util.concurrent.atomic.AtomicInteger;

import com.ccp.especifications.password.CcpPasswordHandler;
import com.ccp.implementations.password.mindrot.CcpMindrotPasswordHandler;

/**
 * The real BCrypt handler, counting how many hashes it computes: each one costs about 250 ms, so a test can tell which
 * operations pay it.
 */
class CountingPasswordHandler implements CcpPasswordHandler {

	/** The hashes computed since the last {@link #reset()}. */
	final AtomicInteger hashes = new AtomicInteger();

	private final CcpPasswordHandler bcrypt = new CcpMindrotPasswordHandler().getInstance();

	public boolean matches(String password, String hash) {
		boolean matches = this.bcrypt.matches(password, hash);
		return matches;
	}

	public String getHash(String password) {
		this.hashes.incrementAndGet();
		String hash = this.bcrypt.getHash(password);
		return hash;
	}

	void reset() {
		this.hashes.set(0);
	}
}
