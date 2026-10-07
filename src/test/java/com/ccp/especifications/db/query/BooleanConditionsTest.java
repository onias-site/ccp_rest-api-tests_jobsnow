package com.ccp.especifications.db.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.utils.entity.fields.CcpEntityField;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

/**
 * Proves the conditions that {@link CcpQueryBooleanOperator} adds to a boolean clause, as they come out in the request
 * sent to Elasticsearch: plain and boosted conditions, nested bool blocks, the {@code exists} check, and that a
 * {@code null} value adds nothing.
 */
public class BooleanConditionsTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	private static final CcpEntityField TITLE = new CcpEntityField("title", false, true, json -> json);

	private List<CcpJsonRepresentation> clause(CcpQueryOptions request, String clause) {
		CcpJsonRepresentation bool = request.json.getInnerJsonFromPath(new CcpFieldName("query"), new CcpFieldName("bool"));
		List<CcpJsonRepresentation> conditions = bool.getAsJsonList(new CcpFieldName(clause));
		return conditions;
	}

	private CcpJsonRepresentation path(CcpJsonRepresentation json, String... names) {
		CcpFieldName[] fields = new CcpFieldName[names.length];
		for (int index = 0; index < names.length; index++) {
			fields[index] = new CcpFieldName(names[index]);
		}
		CcpJsonRepresentation inner = json.getInnerJsonFromPath(fields);
		return inner;
	}

	@Test
	public void aRangeGoesBackToTheClauseThatStartedIt() {
		CcpQueryOptions request = CcpQueryOptions.INSTANCE.startQuery().startBool()
				.startShould(1).startRange().startFieldRange("age").greaterThan(18).endFieldRangeAndBackToRange().endRangeAndBackToShould().endShouldAndBackToBool()
				.startMustNot().startRange().startFieldRange("age").greaterThanEquals(90).endFieldRangeAndBackToRange().endRangeAndBackToMustNot().endMustNotAndBackToBool()
				.endBoolAndBackToQuery().endQueryAndBackToRequest();

		assertEquals(18, this.path(this.clause(request, "should").get(0), "range", "age").getAsIntegerNumber(new CcpFieldName("gt")).intValue());
		assertEquals(90, this.path(this.clause(request, "must_not").get(0), "range", "age").getAsIntegerNumber(new CcpFieldName("gte")).intValue());
	}

	@Test
	public void mustConditionsComeOutInTheOrderTheyWereAdded() {
		CcpQueryOptions request = CcpQueryOptions.INSTANCE.startQuery().startBool().startMust()
				.match(TITLE, "java")
				.matchPhrase(TITLE, "java developer")
				.prefix(TITLE, "ja")
				.exists("title")
				.endMustAndBackToBool().endBoolAndBackToQuery().endQueryAndBackToRequest();

		List<CcpJsonRepresentation> must = this.clause(request, "must");

		assertEquals(4, must.size());
		assertEquals("java", this.path(must.get(0), "match").getAsString(new CcpFieldName("title")));
		assertEquals("java developer", this.path(must.get(1), "match_phrase").getAsString(new CcpFieldName("title")));
		assertEquals("ja", this.path(must.get(2), "prefix").getAsString(new CcpFieldName("title")));
		assertEquals("title", this.path(must.get(3), "exists").getAsString(new CcpFieldName("field")));
	}

	@Test
	public void boostedConditionsCarryTheQueryTheBoostAndTheOperator() {
		CcpQueryOptions request = CcpQueryOptions.INSTANCE.startQuery().startBool().startShould(1)
				.match("title", "java", 2.0, "and")
				.matchPhrase("title", "java developer", 1.5)
				.endShouldAndBackToBool().endBoolAndBackToQuery().endQueryAndBackToRequest();

		List<CcpJsonRepresentation> should = this.clause(request, "should");

		CcpJsonRepresentation match = this.path(should.get(0), "match", "title");
		assertEquals("java", match.getAsString(new CcpFieldName("query")));
		assertEquals(2.0, match.getAsDoubleNumber(new CcpFieldName("boost")), 0);
		assertEquals("and", match.getAsString(new CcpFieldName("operator")));

		CcpJsonRepresentation phrase = this.path(should.get(1), "match_phrase", "title");
		assertEquals(1.5, phrase.getAsDoubleNumber(new CcpFieldName("boost")), 0);
		assertFalse("a blank operator is left out", phrase.containsField(new CcpFieldName("operator")));
	}

	@Test
	public void aClauseHasChildrenOnlyAfterACondition() {
		CcpQueryMust must = CcpQueryOptions.INSTANCE.startQuery().startBool().startMust();

		assertFalse(must.hasChildreen());
		assertTrue(must.match(TITLE, "java").hasChildreen());
	}

	@Test(expected = com.ccp.aop.CcpNullParameterException.class)
	public void aNullValueIsRejectedBeforeReachingTheClause() {
		CcpQueryOptions.INSTANCE.startQuery().startBool().startMust().match(TITLE, null);
	}

	@Test
	public void addingAConditionLeavesTheOriginalClauseUntouched() {
		CcpQueryMust must = CcpQueryOptions.INSTANCE.startQuery().startBool().startMust();

		must.match(TITLE, "java");

		assertFalse(must.hasChildreen());
	}

	@Test
	public void aNestedBoolBecomesAConditionOfTheOuterClause() {
		CcpQueryOptions request = CcpQueryOptions.INSTANCE.startQuery().startBool().startMust()
				.startBool().startMust().match(TITLE, "java").endMustAndBackToBool().endBoolAndBackToMust()
				.endMustAndBackToBool().endBoolAndBackToQuery().endQueryAndBackToRequest();

		List<CcpJsonRepresentation> must = this.clause(request, "must");

		assertEquals(1, must.size());
		List<CcpJsonRepresentation> innerMust = this.path(must.get(0), "bool").getAsJsonList(new CcpFieldName("must"));
		assertEquals("java", this.path(innerMust.get(0), "match").getAsString(new CcpFieldName("title")));
	}
}
