package com.ccp.especifications.db.query;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.utils.entity.fields.CcpEntityField;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

/**
 * Proves that a simplified query holds a single clause and refuses a second one (finding 16): until 2026-10-07 each new
 * clause silently replaced the previous one, so a query built with two conditions searched by the last only.
 */
public class SimplifiedQuerySingleClauseTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	private static final CcpEntityField NAME = new CcpEntityField("name", false, true, json -> json);

	private static final CcpEntityField CITY = new CcpEntityField("city", false, true, json -> json);

	@Test
	public void aSecondClauseIsRefusedInsteadOfReplacingTheFirst() {
		CcpQuerySimplifiedQuery withOneClause = CcpQueryOptions.INSTANCE.startSimplifiedQuery().term(NAME, "a");
		try {
			withOneClause.term(CITY, "b");
			fail("the first clause would be lost");
		} catch (RuntimeException expected) {
			assertTrue(expected.getMessage(), expected.getMessage().contains("single clause"));
		}
	}

	@Test
	public void aSingleClauseIsKept() {
		CcpQueryOptions request = CcpQueryOptions.INSTANCE.startSimplifiedQuery().term(NAME, "a").endSimplifiedQueryAndBackToRequest();

		assertTrue(request.json.toString(), request.json.toString().contains("name"));
	}
}
