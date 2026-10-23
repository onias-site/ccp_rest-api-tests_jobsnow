package com.ccp.rest.api.spring.servlet.filters;

import com.ccp.aop.CcpAllowNullReturn;
import com.ccp.aop.CcpAllowNullParameter;
import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import com.ccp.rest.api.spring.servlet.filters.SessionValidationTest.Outcome;

/** Behavior of the fake response of {@link SessionValidationTest}: keeps the status and the body in the outcome. */
class SessionValidationResponseHandler implements InvocationHandler {

	private final Outcome outcome;

	private final PrintWriter writer;

	SessionValidationResponseHandler(Outcome outcome, PrintWriter writer) {
		this.outcome = outcome;
		this.writer = writer;
	}

	@CcpAllowNullParameter
	@CcpAllowNullReturn
	public Object invoke(Object proxy, Method m, Object[] args) {
		String name = m.getName();
		if ("setStatus".equals(name)) {
			this.outcome.status.set((Integer) args[0]);
			return null;
		}
		if ("getStatus".equals(name)) {
			return this.outcome.status.get();
		}
		if ("getWriter".equals(name)) {
			return this.writer;
		}
		return null;
	}
}
