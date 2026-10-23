package com.ccp.rest.api.spring.servlet.filters;

import com.ccp.aop.CcpAllowNullReturn;
import com.ccp.aop.CcpAllowNullParameter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

/** Behavior of the fake request of {@link SessionValidationTest}. */
class SessionValidationRequestHandler implements InvocationHandler {

	private final String method;

	private final String contentType;

	private final byte[] bytes;

	private final String url;

	private final Map<String, String> headers;

	SessionValidationRequestHandler(String method, String contentType, byte[] bytes, String url, Map<String, String> headers) {
		this.method = method;
		this.contentType = contentType;
		this.bytes = bytes;
		this.url = url;
		this.headers = headers;
	}

	@CcpAllowNullParameter
	@CcpAllowNullReturn
	public Object invoke(Object proxy, Method m, Object[] args) {
		String name = m.getName();
		if ("getMethod".equals(name)) {
			return this.method;
		}
		if ("getContentType".equals(name)) {
			return this.contentType.isEmpty() ? null : this.contentType;
		}
		if ("getInputStream".equals(name)) {
			return SessionValidationTest.streamOf(this.bytes);
		}
		if ("getRequestURL".equals(name)) {
			return new StringBuffer(this.url);
		}
		if ("getHeader".equals(name)) {
			return this.headers.get(args[0]);
		}
		return null;
	}
}
