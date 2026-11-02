package com.ccp.especifications.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

/** Proves that the credentials of a failed call stay out of what outlives it: logs and the database read this error. */
public class CcpHttpSecretMaskTest {

	static {
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE, CcpApacheMimeHttp.INSTANCE);
	}

	private static final String MASK = CcpHttpSecretMask.Fields.MASK.getValue();

	private static CcpJsonRepresentation headers(String name, String value) {
		CcpFieldName headerField = new CcpFieldName(name);
		return CcpOtherConstants.EMPTY_JSON.put(headerField, value);
	}

	@Test
	public void everySecretHeaderIsMaskedInAnyCase() {
		String[] secretHeaders = {"Authorization", "authorization", "AUTHORIZATION", "Proxy-Authorization", "Cookie", "sessionToken", "X-Api-Key"};
		for (String secretHeader : secretHeaders) {
			CcpJsonRepresentation masked = CcpHttpSecretMask.INSTANCE.maskHeaders(headers(secretHeader, "theSecret"));
			String maskedJson = masked.asUgglyJson();

			assertFalse(maskedJson, maskedJson.contains("theSecret"));
			assertTrue(maskedJson, maskedJson.contains(MASK));
		}
	}

	@Test
	public void anOrdinaryHeaderKeepsItsValue() {
		CcpJsonRepresentation masked = CcpHttpSecretMask.INSTANCE.maskHeaders(headers("Accept", "application/json"));

		assertEquals(headers("Accept", "application/json").asUgglyJson(), masked.asUgglyJson());
	}

	@Test
	public void theBotTokenIsMaskedInTheUrl() {
		String maskedUrl = CcpHttpSecretMask.INSTANCE.maskUrl("https://api.telegram.org/bot123:abc/sendMessage");

		assertEquals("https://api.telegram.org/bot***/sendMessage", maskedUrl);
	}

	@Test
	public void theSecretOfTheWebhookIsMaskedInTheBody() {
		String maskedBody = CcpHttpSecretMask.INSTANCE.maskBody("{\"url\":\"https://x\",\"secret_token\" : \"theSecret\",\"max_connections\":1}");

		assertEquals("{\"url\":\"https://x\",\"secret_token\":\"***\",\"max_connections\":1}", maskedBody);
	}

	@Test
	public void anOrdinaryBodyKeepsItsText() {
		String body = "{\"chat_id\":\"1\",\"text\":\"secret_token is just a word here\"}";

		assertEquals(body, CcpHttpSecretMask.INSTANCE.maskBody(body));
	}

	@Test
	public void theErrorKeepsNoCredentialOfTheBody() {
		CcpHttpRequester requester = CcpDependencyInjection.getDependency(CcpHttpRequester.class);
		String body = "{\"secret_token\":\"theSecret\"}";

		CcpErrorHttp error = requester.getHttpError("trace", "https://x", CcpHttpMethods.POST, headers("Accept", "*/*"), body, 400, "{}", Set.of("200"));

		assertFalse(error.getMessage(), error.getMessage().contains("theSecret"));
		assertFalse(error.entity.asUgglyJson(), error.entity.asUgglyJson().contains("theSecret"));
	}

	@Test
	public void theErrorOfAnyStatusKeepsNoCredential() {
		CcpHttpRequester requester = CcpDependencyInjection.getDependency(CcpHttpRequester.class);
		CcpJsonRepresentation headers = headers("Authorization", "Bearer theSecret");
		String url = "https://api.telegram.org/bottheSecret/sendMessage";
		int[] statuses = {302, 401, 429, 500, 600};

		for (int status : statuses) {
			CcpErrorHttp error = requester.getHttpError("trace", url, CcpHttpMethods.POST, headers, "{}", status, "{}", Set.of("200"));
			String message = error.getMessage();
			String details = error.entity.asUgglyJson();

			assertFalse(message, message.contains("theSecret"));
			assertFalse(details, details.contains("theSecret"));
		}
	}
}
