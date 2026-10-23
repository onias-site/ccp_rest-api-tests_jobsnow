package com.ccp.rest.api.spring.servlet.filters;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.ccp.rest.api.spring.servlet.filters.SessionValidationTest.Outcome;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

/** The chain (the controller) of {@link SessionValidationTest}: records that the request reached it and, optionally, the body it read. */
class SessionValidationController implements FilterChain {

	private final Outcome outcome;

	private final boolean controllerReadsBody;

	SessionValidationController(Outcome outcome, boolean controllerReadsBody) {
		this.outcome = outcome;
		this.controllerReadsBody = controllerReadsBody;
	}

	public void doFilter(ServletRequest req, ServletResponse res) {
		this.outcome.reachedController = true;
		if (this.controllerReadsBody == false) {
			return;
		}
		try {
			byte[] read = req.getInputStream().readAllBytes();
			this.outcome.bodySeenByController.set(new String(read, StandardCharsets.UTF_8));
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
}
