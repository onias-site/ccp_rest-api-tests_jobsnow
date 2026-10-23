package com.ccp.rest.api.spring.exceptions.handler;

import com.ccp.aop.CcpAllowNullReturn;
import com.ccp.aop.CcpAllowNullParameter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

/** Behavior of the fake response of {@link CcpRestApiExceptionHandlerSpringTest}: keeps the status set; every call answers null. */
class StatusRecordingResponseHandler implements InvocationHandler {

	/** Receives the status set. */
	private final AtomicInteger status;

	StatusRecordingResponseHandler(AtomicInteger status) {
		this.status = status;
	}

	@CcpAllowNullParameter
	@CcpAllowNullReturn
	public Object invoke(Object proxy, Method m, Object[] args) {
		if ("setStatus".equals(m.getName())) {
			this.status.set((Integer) args[0]);
		}
		return null;
	}
}
