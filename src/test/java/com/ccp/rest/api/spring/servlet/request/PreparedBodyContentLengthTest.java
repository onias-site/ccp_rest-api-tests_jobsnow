package com.ccp.rest.api.spring.servlet.request;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Proves that the wrapper declares the size of the body it gives, not of the original one. Spring reads a {@code String}
 * body with exactly {@code Content-Length} bytes: until 2026-10-09 the original size was kept, so a {@code DELETE
 * /login/{email}/{sessionToken}} carrying {@code {}} had the enriched body cut at 2 bytes and answered 500.
 */
public class PreparedBodyContentLengthTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	private static HttpServletRequest request(String body, String contentType) {
		byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
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
		headers.put("Content-Length", String.valueOf(bytes.length));
		return (HttpServletRequest) Proxy.newProxyInstance(PreparedBodyContentLengthTest.class.getClassLoader(),
				new Class<?>[] { HttpServletRequest.class }, (proxy, m, args) -> {
					switch (m.getName()) {
					case "getContentType":
						return contentType;
					case "getInputStream":
						return servletInputStream;
					case "getRequestURL":
						return new StringBuffer("http://localhost:8080/login/someone@example.com/ABCDEFGH");
					case "getRemoteAddr":
						return "127.0.0.1";
					case "getHeader":
						return headers.get(args[0]);
					case "getHeaders":
						String value = headers.get(args[0]);
						List<String> values = value == null ? List.of() : List.of(value);
						return Collections.enumeration(values);
					case "getContentLength":
						return bytes.length;
					case "getContentLengthLong":
						return (long) bytes.length;
					default:
						return null;
					}
				});
	}

	private static CcpPutSessionValuesRequestWrapper prepared(String body, String contentType) throws IOException {
		HttpServletRequest request = request(body, contentType);
		CcpPutSessionValuesRequestWrapper wrapper = new CcpPutSessionValuesRequestWrapper(request, CcpOtherConstants.DO_NOTHING);
		wrapper.prepareBody();
		return wrapper;
	}

	/** Reads the body the way Spring reads a {@code String}: exactly {@code Content-Length} bytes. */
	private static String readAsSpringDoes(CcpPutSessionValuesRequestWrapper wrapper) throws IOException {
		String contentLength = wrapper.getHeader("Content-Length");
		int size = Integer.parseInt(contentLength);
		byte[] bytes = wrapper.getInputStream().readNBytes(size);
		String body = new String(bytes, StandardCharsets.UTF_8);
		return body;
	}

	@Test
	public void aSmallJsonBodyIsReadWholeAfterTheSessionValuesAreAdded() throws IOException {
		CcpPutSessionValuesRequestWrapper wrapper = prepared("{}", "application/json");
		String body = readAsSpringDoes(wrapper);
		CcpJsonRepresentation json = new CcpJsonRepresentation(body);
		assertEquals("someone@example.com", json.getAsString(CcpPutSessionValuesRequestWrapper.JsonFieldNames.email));
		assertEquals("127.0.0.1", json.getAsString(CcpPutSessionValuesRequestWrapper.JsonFieldNames.ip));
	}

	@Test
	public void theDeclaredSizeIsTheSizeOfTheStreamGiven() throws IOException {
		CcpPutSessionValuesRequestWrapper wrapper = prepared("{\"anyField\":\"anyValue\"}", "application/json");
		byte[] given = wrapper.getInputStream().readAllBytes();
		assertEquals(given.length, wrapper.getContentLength());
		assertEquals(given.length, wrapper.getContentLengthLong());
		String fromHeaders = wrapper.getHeaders("content-length").nextElement();
		assertEquals(String.valueOf(given.length), fromHeaders);
		assertTrue(given.length > "{\"anyField\":\"anyValue\"}".length());
	}

	@Test
	public void aBodyThatIsNotJsonKeepsItsOriginalSize() throws IOException {
		CcpPutSessionValuesRequestWrapper wrapper = prepared("plain text", "text/plain");
		assertEquals("plain text".length(), wrapper.getContentLength());
		assertEquals("plain text", readAsSpringDoes(wrapper));
	}

	@Test
	public void theOtherHeadersComeFromTheRequest() throws IOException {
		CcpPutSessionValuesRequestWrapper wrapper = prepared("{}", "application/json");
		assertEquals("test", wrapper.getHeader("User-Agent"));
		assertEquals("test", wrapper.getHeaders("User-Agent").nextElement());
	}
}
