package com.ccp.rest.api.spring.servlet.request;

import com.ccp.aop.CcpAllowNullReturn;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.ccp.aop.CcpAllowNullParameter;

import jakarta.servlet.ServletInputStream;

/** Behavior of the fake request of {@link PreparedBodyContentLengthTest}. */
class PreparedBodyRequestHandler implements InvocationHandler {

	private final String contentType;

	private final ServletInputStream servletInputStream;

	private final Map<String, String> headers;

	private final int contentLength;

	@CcpAllowNullParameter
	PreparedBodyRequestHandler(String contentType, ServletInputStream servletInputStream, Map<String, String> headers, int contentLength) {
		this.contentType = contentType;
		this.servletInputStream = servletInputStream;
		this.headers = headers;
		this.contentLength = contentLength;
	}

	@CcpAllowNullParameter
	@CcpAllowNullReturn
	public Object invoke(Object proxy, Method m, Object[] args) {
		String name = m.getName();
		if ("getContentType".equals(name)) {
			return this.contentType;
		}
		if ("getInputStream".equals(name)) {
			return this.servletInputStream;
		}
		if ("getRequestURL".equals(name)) {
			return new StringBuffer("http://localhost:8082/login/someone@example.com/ABCDEFGH");
		}
		if ("getRemoteAddr".equals(name)) {
			return "127.0.0.1";
		}
		if ("getHeader".equals(name)) {
			return this.headers.get(args[0]);
		}
		if ("getHeaders".equals(name)) {
			String value = this.headers.get(args[0]);
			List<String> values = value == null ? List.of() : List.of(value);
			return Collections.enumeration(values);
		}
		if ("getContentLength".equals(name)) {
			return this.contentLength;
		}
		if ("getContentLengthLong".equals(name)) {
			return (long) this.contentLength;
		}
		return null;
	}
}
