package com.ccp.especifications.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Base64;
import java.util.List;

import org.junit.Test;

import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

/**
 * Proves {@link CcpHttpResponse} and the transformations of {@link CcpHttpResponseType}: the status ranges, the body
 * read as text, JSON, list or Base64, and the text form of the response.
 */
public class HttpResponseBehaviorTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	private CcpHttpResponse response(String body, int status) {
		return new CcpHttpResponse(body, status, "curl http://localhost");
	}

	@Test
	public void theStatusTellsTheRangeOfTheAnswer() {
		assertTrue(this.response("", 200).isSuccess());
		assertTrue(this.response("", 299).isSuccess());
		assertFalse(this.response("", 300).isSuccess());
		assertTrue(this.response("", 404).isClientError());
		assertFalse(this.response("", 500).isClientError());
		assertTrue(this.response("", 503).isServerError());
		assertFalse(this.response("", 199).isSuccess());
	}

	@Test
	public void theBodyIsReadInEachFormat() {
		CcpHttpResponse response = this.response("[1,2]", 200);

		assertEquals("[1,2]", CcpHttpResponseType.string.transform(response));
		assertEquals(2, CcpHttpResponseType.listObject.transform(response).size());
		assertEquals("[1,2]", new String(CcpHttpResponseType.byteArray.transform(response)));
		assertEquals("[1,2]", new String(Base64.getDecoder().decode(CcpHttpResponseType.base64.transform(response))));
		CcpJsonRepresentation json = CcpHttpResponseType.singleRecord.transform(this.response("{\"a\":1}", 200));
		assertEquals(1, (int) json.getAsIntegerNumber(new CcpFieldName("a")));
	}

	@Test
	public void theTextFormHasTheStatusAndTheBody() {
		String text = this.response("hello", 418).toString();

		assertTrue(text, text.contains("418"));
		assertTrue(text, text.contains("hello"));
	}

	/** Finding 23: until 2026-10-07 the items were the maps of Gson and reading one raised ClassCastException. */
	@Test
	public void aListOfRecordsHoldsJsons() {
		List<CcpJsonRepresentation> records = CcpHttpResponseType.listRecord.transform(this.response("[{\"a\":1},{\"a\":2}]", 200));

		assertEquals(2, records.size());
		assertEquals(1, (int) records.get(0).getAsIntegerNumber(new CcpFieldName("a")));
		assertEquals(2, (int) records.get(1).getAsIntegerNumber(new CcpFieldName("a")));
	}

	@Test
	public void aBlankBodyIsAnEmptyListOfRecords() {
		assertTrue(CcpHttpResponseType.listRecord.transform(this.response("  ", 200)).isEmpty());
		assertTrue(CcpHttpResponseType.listRecord.transform(this.response("[]", 200)).isEmpty());
	}

	@Test
	public void aListWithAnItemThatIsNotAnObjectIsRefusedWhereItIsRead() {
		this.assertNotListOfRecords("[{\"a\":1}, 2]");
	}

	@Test
	public void aBodyThatIsNotAListIsRefusedWhereItIsRead() {
		this.assertNotListOfRecords("{\"a\":1}");
	}

	private void assertNotListOfRecords(String body) {
		try {
			CcpHttpResponseType.listRecord.transform(this.response(body, 200));
			fail("the body " + body + " is not a list of records");
		} catch (CcpHttpResponse.CcpErrorHttpResponseIsNotListOfRecords expected) {
			assertTrue(expected.getMessage(), expected.getMessage().contains(body));
		}
	}
}
