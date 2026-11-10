package com.jn.rest.api.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.http.HttpResponse;

import org.junit.BeforeClass;
import org.junit.Test;

import com.jn.rest.api.commons.TestVariables;

/**
 * The contact us takes no session ({@code POST /contact-us/{email}} on the js, 8082), so its only brake against a flood
 * is the daily quota by group of ticket kinds ({@code JnEntityTicketDailyLimit}): the second ticket of the same group on
 * the same day answers 429, through the API, the way a script would send it.
 */
public class TheContactUsKeepsItsDailyQuotas {

	private final TestVariables user = new TestVariables();

	@BeforeClass
	public static void theApiMustBeUp() {
		SecurityHttp.load();
		SecurityHttp.theApiMustBeUp(SecurityHttp.JS);
	}

	private int open(String type, int sequence) {
		String body = "{\"type\":\"" + type + "\",\"title\":\"Teste de cota " + sequence + "\",\"description\":\"Texto do teste de seguranca numero " + sequence + "\"}";
		String path = "contact-us/" + this.user.VALID_EMAIL;
		HttpResponse<String> response = SecurityHttp.send(SecurityHttp.JS, "POST", path, body, this.user, "");
		return response.statusCode();
	}

	@Test
	public void theSecondSuggestionOfTheDayIsRefused() {
		int first = this.open("makeSuggestion", 1);
		int second = this.open("makeCompliment", 2);

		assertTrue("the first ticket was refused with " + first, first < 300);
		assertEquals("the second ticket of the shared group", 429, second);
	}

	@Test
	public void theSecondDoubtOfTheDayIsRefused() {
		int first = this.open("clarifyDoubts", 1);
		int second = this.open("clarifyDoubts", 2);

		assertTrue("the first doubt was refused with " + first, first < 300);
		assertEquals("the second doubt of the day", 429, second);
	}

	@Test
	public void aBurstOfTicketsDoesNotPassTheQuota() {
		int accepted = 0;
		for (int sequence = 0; sequence < 10; sequence++) {
			int status = this.open("makeCriticism", sequence);
			if (status >= 300) {
				continue;
			}
			accepted++;
		}

		assertEquals("criticisms accepted out of 10 in a row", 1, accepted);
	}
}
