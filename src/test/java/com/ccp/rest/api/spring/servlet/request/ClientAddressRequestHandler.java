package com.ccp.rest.api.spring.servlet.request;

import com.ccp.aop.CcpAllowNullReturn;
import com.ccp.aop.CcpAllowNullParameter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

import jakarta.servlet.ServletInputStream;

/** Behavior of the fake request of {@link ClientAddressTest}. */
class ClientAddressRequestHandler implements InvocationHandler {

	private final ServletInputStream servletInputStream;

	private final String remoteAddress;

	private final Map<String, String> headers;

	ClientAddressRequestHandler(ServletInputStream servletInputStream, String remoteAddress, Map<String, String> headers) {
		this.servletInputStream = servletInputStream;
		this.remoteAddress = remoteAddress;
		this.headers = headers;
	}

	@CcpAllowNullParameter
	@CcpAllowNullReturn
	public Object invoke(Object proxy, Method m, Object[] args) {
		String name = m.getName();
		if ("getContentType".equals(name)) {
			return "application/json";
		}
		if ("getInputStream".equals(name)) {
			return this.servletInputStream;
		}
		if ("getRequestURL".equals(name)) {
			return new StringBuffer("http://api.example.com/login/someone@example.com/token");
		}
		if ("getRemoteAddr".equals(name)) {
			return this.remoteAddress;
		}
		if ("getHeader".equals(name)) {
			return this.headers.get(args[0]);
		}
		return null;
	}
}
