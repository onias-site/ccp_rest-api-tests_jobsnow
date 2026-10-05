package com.ccp.especifications.db.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Ignore;
import org.junit.Test;

import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.utils.entity.fields.CcpEntityField;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

/**
 * Proves the clauses of {@link CcpQueryBool} (filter, should with its minimum, must not and should not), the nesting of
 * bool blocks inside each of them, and that Elasticsearch refuses the {@code should_not} clause.
 */
public class BoolClausesTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler(), new CcpApacheMimeHttp(), new CcpElasticSearchDbRequest(),
				new CcpElasticSearchQueryExecutor());
	}

	private static final CcpEntityField TITLE = new CcpEntityField("title", false, true, json -> json);

	@Test
	public void eachClauseComesOutUnderTheBool() {
		CcpQueryOptions request = CcpQueryOptions.INSTANCE.startQuery().startBool()
				.startFilter().startBool().startMust().term(TITLE, "f").endMustAndBackToBool().endBoolAndBackToFilter().endFilterAndBackToBool()
				.startShould(1).match(TITLE, "s").endShouldAndBackToBool()
				.startMustNot().term(TITLE, "mn").endMustNotAndBackToBool()
				.endBoolAndBackToQuery().endQueryAndBackToRequest();

		assertEquals("{\"query\":{\"bool\":{\"filter\":{\"bool\":{\"must\":[{\"term\":{\"title\":\"f\"}}]}},\"minimum_should_match\":\"1\","
				+ "\"should\":[{\"match\":{\"title\":\"s\"}}],\"must_not\":[{\"term\":{\"title\":\"mn\"}}]}}}", request.json.asUgglyJson());
	}

	@Test
	public void aBoolNestsInsideEveryClause() {
		CcpQueryOptions request = CcpQueryOptions.INSTANCE.startQuery().startBool()
				.startShould(1).startBool().startMust().term(TITLE, "x").endMustAndBackToBool().endBoolAndBackToShould().endShouldAndBackToBool()
				.startMustNot().startBool().startMust().term(TITLE, "y").endMustAndBackToBool().endBoolAndBackToMustNot().endMustNotAndBackToBool()
				.endBoolAndBackToQuery().endQueryAndBackToRequest();

		assertEquals("{\"query\":{\"bool\":{\"minimum_should_match\":\"1\",\"should\":[{\"bool\":{\"must\":[{\"term\":{\"title\":\"x\"}}]}}],"
				+ "\"must_not\":[{\"bool\":{\"must\":[{\"term\":{\"title\":\"y\"}}]}}]}}}", request.json.asUgglyJson());
	}

	private CcpQueryOptions withShouldNot() {
		CcpQueryOptions request = CcpQueryOptions.INSTANCE.startQuery().startBool()
				.startShouldNot().startBool().startMust().term(TITLE, "w").endMustAndBackToBool().endBoolAndBackToShouldNot().endShouldNotAndBackToBool()
				.endBoolAndBackToQuery().endQueryAndBackToRequest();
		return request;
	}

	@Test
	public void theShouldNotClauseIsCurrentlyRefusedByElasticsearch() {
		assertTrue(this.withShouldNot().json.asUgglyJson().contains("\"should_not\""));
		CcpQueryExecutor executor = CcpDependencyInjection.getDependency(CcpQueryExecutor.class);
		try {
			executor.total(this.withShouldNot(), new String[] { "jn_async_task" });
			fail("Elasticsearch has no should_not clause");
		} catch (RuntimeException expected) {
			// finding 16
		}
	}

	@Ignore("finding 16: should not must become a must_not inside a should, Elasticsearch has no should_not")
	@Test
	public void theShouldNotClauseIsAcceptedByElasticsearch() {
		CcpQueryExecutor executor = CcpDependencyInjection.getDependency(CcpQueryExecutor.class);

		executor.total(this.withShouldNot(), new String[] { "jn_async_task" });
	}
}
