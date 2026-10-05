package com.ccp.especifications.db.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.utils.entity.fields.CcpEntityField;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

/**
 * Proves the request options ({@link CcpQueryOptions}), the range conditions ({@link CcpQueryRange},
 * {@link CcpQueryFieldRange}) and the simplified query ({@link CcpQuerySimplifiedQuery}) by the JSON sent to
 * Elasticsearch.
 */
public class RequestOptionsTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	private static final CcpEntityField TITLE = new CcpEntityField("title", false, true, json -> json);

	@Test
	public void aRangeUnderTheSimplifiedQueryHasEveryBoundOfTheField() {
		CcpQueryOptions request = CcpQueryOptions.INSTANCE.startSimplifiedQuery().startRange().startFieldRange("timestamp")
				.greaterThan(10).lessThanEquals(20).endFieldRangeAndBackToRange().endRangeAndBackToSimplifiedQuery()
				.endSimplifiedQueryAndBackToRequest();

		assertEquals("{\"query\":{\"range\":{\"timestamp\":{\"gt\":10.0,\"lte\":20.0}}}}", request.json.asUgglyJson());
	}

	@Test
	public void aRangeUnderABooleanClauseIsOneOfItsConditions() {
		CcpQueryOptions request = CcpQueryOptions.INSTANCE.startQuery().startBool().startMust().startRange()
				.startFieldRange("timestamp").greaterThanEquals(1).lessThan(2).endFieldRangeAndBackToRange().endRangeAndBackToMust()
				.endMustAndBackToBool().endBoolAndBackToQuery().endQueryAndBackToRequest();

		assertEquals("{\"query\":{\"bool\":{\"must\":[{\"range\":{\"timestamp\":{\"gte\":1.0,\"lt\":2.0}}}]}}}", request.json.asUgglyJson());
	}

	@Test
	public void theSimplifiedQueryKeepsOnlyTheLastCondition() {
		CcpQuerySimplifiedQuery query = CcpQueryOptions.INSTANCE.startSimplifiedQuery();
		assertFalse(query.hasChildreen());

		CcpQuerySimplifiedQuery withConditions = query.term(TITLE, "a").match(TITLE, "b");

		assertTrue(withConditions.hasChildreen());
		assertEquals("{\"query\":{\"match\":{\"title\":\"b\"}}}", withConditions.endSimplifiedQueryAndBackToRequest().json.asUgglyJson());
	}

	@Test
	public void theOtherConditionsOfTheSimplifiedQuery() {
		assertEquals("{\"query\":{\"terms\":{\"title\":\"a\"}}}", CcpQueryOptions.INSTANCE.startSimplifiedQuery().terms(TITLE, "a").endSimplifiedQueryAndBackToRequest().json.asUgglyJson());
		assertEquals("{\"query\":{\"prefix\":{\"title\":\"a\"}}}", CcpQueryOptions.INSTANCE.startSimplifiedQuery().prefix(TITLE, "a").endSimplifiedQueryAndBackToRequest().json.asUgglyJson());
		assertEquals("{\"query\":{\"match_phrase\":{\"title\":\"a b\"}}}", CcpQueryOptions.INSTANCE.startSimplifiedQuery().matchPhrase(TITLE, "a b").endSimplifiedQueryAndBackToRequest().json.asUgglyJson());
		assertEquals("{\"query\":{\"exists\":{\"field\":\"title\"}}}", CcpQueryOptions.INSTANCE.startSimplifiedQuery().exists("title").endSimplifiedQueryAndBackToRequest().json.asUgglyJson());
	}

	@Test
	public void theOptionsOfTheRequestComeOutAtTheRoot() {
		CcpQueryOptions request = CcpQueryOptions.INSTANCE.matchAll().addDescSorting("a", "b").addAscSorting("c")
				.setScrollId("s1").setScrollTime("1m").zeroResults().setFrom(5);

		assertEquals("{\"from\":5.0,\"query\":{\"match_all\":{}},\"scroll\":\"1m\",\"scroll_id\":\"s1\",\"size\":0.0,"
				+ "\"sort\":[{\"a\":\"desc\"},{\"b\":\"desc\"},{\"c\":\"asc\"}]}", request.json.asUgglyJson());
	}

	@Test
	public void theRequestBuildersNeverChangeTheSharedInstance() {
		CcpQueryOptions.INSTANCE.setSize(3).matchAll();

		assertTrue(CcpQueryOptions.INSTANCE.json.isEmpty());
	}
}
