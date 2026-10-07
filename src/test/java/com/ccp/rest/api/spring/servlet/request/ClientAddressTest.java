package com.ccp.rest.api.spring.servlet.request;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Proves that the {@code ip} session value is the address of the client (finding 36): until 2026-10-07 it came from the
 * {@code Host} header, so it was the host requested, the same for every user, and a request without that header raised a
 * NullPointerException.
 */
public class ClientAddressTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	/** Keeps the body as it reaches the task. */
	static class CapturingTask implements CcpBusiness {
		CcpJsonRepresentation received;

		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			this.received = json;
			return json;
		}
	}

	private static HttpServletRequest request(String remoteAddress, String host) {
		byte[] bytes = "{}".getBytes(StandardCharsets.UTF_8);
		ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
		ServletInputStream servletInputStream = new ServletInputStream() {
			public int read() {
				return inputStream.read();
			}

			public boolean isFinished() {
				return inputStream.available() == 0;
			}

			public boolean isReady() {
				return true;
			}

			public void setReadListener(ReadListener listener) {
			}
		};
		Map<String, String> headers = new HashMap<>();
		headers.put("User-Agent", "test");
		if (false == host.isEmpty()) {
			headers.put("Host", host);
		}
		return (HttpServletRequest) Proxy.newProxyInstance(ClientAddressTest.class.getClassLoader(),
				new Class<?>[] { HttpServletRequest.class }, (proxy, m, args) -> {
					switch (m.getName()) {
					case "getContentType":
						return "application/json";
					case "getInputStream":
						return servletInputStream;
					case "getRequestURL":
						return new StringBuffer("http://api.example.com/login/someone@example.com/token");
					case "getRemoteAddr":
						return remoteAddress;
					case "getHeader":
						return headers.get(args[0]);
					default:
						return null;
					}
				});
	}

	private String ipSeenByTheTask(String remoteAddress, String host) throws IOException {
		CapturingTask task = new CapturingTask();
		new CcpPutSessionValuesRequestWrapper(request(remoteAddress, host), task).prepareBody();
		String ip = task.received.getAsString(CcpPutSessionValuesRequestWrapper.JsonFieldNames.ip);
		return ip;
	}

	@Test
	public void theIpIsTheAddressOfTheClientNotTheHostRequested() throws IOException {
		assertEquals("203.0.113.7", this.ipSeenByTheTask("203.0.113.7", "api.example.com"));
	}

	@Test
	public void aRequestWithoutHostHeaderStillGetsTheClientAddress() throws IOException {
		assertEquals("203.0.113.7", this.ipSeenByTheTask("203.0.113.7", ""));
	}

	@Test
	public void theIpv6LoopbackBecomesTheIpv4Loopback() throws IOException {
		assertEquals("127.0.0.1", this.ipSeenByTheTask("0:0:0:0:0:0:0:1", "localhost:8080"));
	}

	@Test
	public void anIpv6ClientKeepsItsAddress() throws IOException {
		assertEquals("2001:db8::1", this.ipSeenByTheTask("2001:db8::1", "api.example.com"));
	}
}
