package com.ccp.especifications.db.query;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.query.CcpQueryOptions.CcpQueryAggregations;
import com.ccp.especifications.db.query.CcpQueryOptions.CcpQueryBool;
import com.ccp.especifications.db.query.CcpQueryOptions.CcpQueryBooleanOperator;
import com.ccp.especifications.db.query.CcpQueryOptions.CcpQueryExecutorDecorator;
import com.ccp.especifications.db.query.CcpQueryOptions.CcpQueryFieldRange;
import com.ccp.especifications.db.query.CcpQueryOptions.CcpQueryMust;
import com.ccp.especifications.db.query.CcpQueryOptions.CcpQueryMustNot;
import com.ccp.especifications.db.query.CcpQueryOptions.CcpQueryRange;
import com.ccp.especifications.db.query.CcpQueryOptions.CcpQueryShould;
import com.ccp.especifications.db.query.CcpQueryOptions.CcpQueryShouldNot;
import com.ccp.especifications.db.query.CcpQueryOptions.CcpQuerySimplifiedQuery;
import com.ccp.especifications.db.utils.entity.fields.CcpEntityField;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

/**
 * Cobertura dos aspectos {@code CcpNullParameterAspect} / {@code CcpNullReturnAspect} sobre o
 * builder fluent de queries ({@code CcpQueryComponent} e todos os nós de {@code CcpQueryOptions}).
 */
public class CcpQueryOptionsAopNullTest {

	static {
		CcpDependencyInjection.loadAllDependencies(
				new CcpGsonJsonHandler(),
				new CcpApacheMimeHttp(),
				new CcpElasticSearchDbRequest(),
				new CcpElasticSearchQueryExecutor());
	}

	private static final CcpEntityField FIELD = CcpEntityField.TIMESTAMP;

	private enum SampleFieldName implements CcpJsonFieldName {
		sample
	}

	/**
	 * Subclasse concreta usada apenas para alcançar os métodos declarados diretamente em
	 * {@code CcpQueryBooleanOperator}, que em todos os nós reais aparecem sobrescritos.
	 */
	private static final class BooleanOperatorForTest extends CcpQueryBooleanOperator {

		BooleanOperatorForTest() {
			super(CcpQueryOptions.INSTANCE, "test");
		}

		@SuppressWarnings("unchecked")
		protected <T extends CcpQueryComponent> T getInstanceCopy() {
			return (T) new BooleanOperatorForTest();
		}
	}

	private static BooleanOperatorForTest operator() {
		return new BooleanOperatorForTest();
	}

	private static CcpQueryOptions options() {
		return CcpQueryOptions.INSTANCE;
	}

	private static CcpQueryBool bool() {
		return options().startQuery().startBool();
	}

	private static CcpQueryMust must() {
		return bool().startMust();
	}

	private static CcpQueryMustNot mustNot() {
		return bool().startMustNot();
	}

	private static CcpQueryShould should() {
		return bool().startShould(1);
	}

	private static CcpQueryShouldNot shouldNot() {
		return bool().startShouldNot();
	}

	private static CcpQuerySimplifiedQuery simplified() {
		return options().startSimplifiedQuery();
	}

	private static CcpQueryAggregations aggregations() {
		return options().startAggregations();
	}

	private static CcpQueryRange range() {
		return must().startRange();
	}

	private static CcpQueryFieldRange fieldRange() {
		return range().startFieldRange("f");
	}

	private static CcpQueryExecutorDecorator executor() {
		return options().selectFrom("any_index");
	}

	// ── CcpQueryComponent ─────────────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void putPropertyNameNullTest() {
		options().putProperty(null, "value");
	}

	@Test(expected = CcpNullParameterException.class)
	public void putPropertyValueNullTest() {
		options().putProperty("name", null);
	}

	// ── CcpQueryOptions ───────────────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void addAscSortingNullTest() {
		options().addAscSorting(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void addDescSortingNullTest() {
		options().addDescSorting((String[]) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void addSortingSortTypeNullTest() {
		options().addSorting(null, "field");
	}

	@Test(expected = CcpNullParameterException.class)
	public void addSortingFieldsNullTest() {
		options().addSorting("asc", (String[]) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void selectFromResourcesNamesNullTest() {
		options().selectFrom((String[]) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void selectFromEntitiesNullTest() {
		options().selectFrom((com.ccp.especifications.db.utils.entity.CcpEntity[]) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void setScrollIdNullTest() {
		options().setScrollId(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void setScrollTimeNullTest() {
		options().setScrollTime(null);
	}

	// ── CcpQueryBooleanOperator (classe base) ─────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void operatorTermFieldNullTest() {
		operator().term((CcpJsonFieldName) null, "value");
	}

	@Test(expected = CcpNullParameterException.class)
	public void operatorTermValueNullTest() {
		operator().term(SampleFieldName.sample, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void operatorTermsFieldNullTest() {
		operator().terms((CcpJsonFieldName) null, "value");
	}

	@Test(expected = CcpNullParameterException.class)
	public void operatorTermsValueNullTest() {
		operator().terms(SampleFieldName.sample, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void operatorPrefixFieldNullTest() {
		operator().prefix((CcpEntityField) null, "value");
	}

	@Test(expected = CcpNullParameterException.class)
	public void operatorPrefixValueNullTest() {
		operator().prefix(FIELD, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void operatorMatchFieldNullTest() {
		operator().match((CcpJsonFieldName) null, "value");
	}

	@Test(expected = CcpNullParameterException.class)
	public void operatorMatchValueNullTest() {
		operator().match(SampleFieldName.sample, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void operatorMatchPhraseFieldNullTest() {
		operator().matchPhrase((CcpEntityField) null, "value");
	}

	@Test(expected = CcpNullParameterException.class)
	public void operatorMatchPhraseValueNullTest() {
		operator().matchPhrase(FIELD, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void operatorMatchBoostFieldNullTest() {
		operator().match((CcpEntityField) null, "value", 1d, "and");
	}

	@Test(expected = CcpNullParameterException.class)
	public void operatorMatchBoostValueNullTest() {
		operator().match(FIELD, null, 1d, "and");
	}

	@Test(expected = CcpNullParameterException.class)
	public void operatorMatchBoostOperatorNullTest() {
		operator().match(FIELD, "value", 1d, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void operatorMatchPhraseBoostFieldNullTest() {
		operator().matchPhrase((CcpEntityField) null, "value", 1d);
	}

	@Test(expected = CcpNullParameterException.class)
	public void operatorMatchPhraseBoostValueNullTest() {
		operator().matchPhrase(FIELD, null, 1d);
	}

	@Test(expected = CcpNullParameterException.class)
	public void operatorExistsNullTest() {
		operator().exists(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void operatorAddConditionFieldNullTest() {
		operator().addCondition(null, "value", "term");
	}

	@Test(expected = CcpNullParameterException.class)
	public void operatorAddConditionValueNullTest() {
		operator().addCondition("field", null, "term");
	}

	@Test(expected = CcpNullParameterException.class)
	public void operatorAddConditionKeyNullTest() {
		operator().addCondition("field", "value", null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void operatorAddConditionBoostFieldNullTest() {
		operator().addCondition(null, "value", "match", 1d, "and");
	}

	@Test(expected = CcpNullParameterException.class)
	public void operatorAddConditionBoostValueNullTest() {
		operator().addCondition("field", null, "match", 1d, "and");
	}

	@Test(expected = CcpNullParameterException.class)
	public void operatorAddConditionBoostKeyNullTest() {
		operator().addCondition("field", "value", null, 1d, "and");
	}

	@Test(expected = CcpNullParameterException.class)
	public void operatorAddConditionBoostOperatorNullTest() {
		operator().addCondition("field", "value", "match", 1d, null);
	}

	// ── CcpQueryMust ──────────────────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void mustMatchPhraseFieldNullTest() {
		must().matchPhrase((CcpEntityField) null, "value");
	}

	@Test(expected = CcpNullParameterException.class)
	public void mustMatchPhraseValueNullTest() {
		must().matchPhrase(FIELD, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void mustPrefixFieldNullTest() {
		must().prefix((CcpEntityField) null, "value");
	}

	@Test(expected = CcpNullParameterException.class)
	public void mustPrefixValueNullTest() {
		must().prefix(FIELD, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void mustTermFieldNullTest() {
		must().term((CcpJsonFieldName) null, "value");
	}

	@Test(expected = CcpNullParameterException.class)
	public void mustTermValueNullTest() {
		must().term(SampleFieldName.sample, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void mustTermsFieldNullTest() {
		must().terms((CcpJsonFieldName) null, "value");
	}

	@Test(expected = CcpNullParameterException.class)
	public void mustTermsValueNullTest() {
		must().terms(SampleFieldName.sample, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void mustExistsNullTest() {
		must().exists(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void mustMatchFieldNullTest() {
		must().match((CcpEntityField) null, "value");
	}

	@Test(expected = CcpNullParameterException.class)
	public void mustMatchValueNullTest() {
		must().match(FIELD, null);
	}

	// ── CcpQueryMustNot ───────────────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void mustNotPrefixFieldNullTest() {
		mustNot().prefix((CcpEntityField) null, "value");
	}

	@Test(expected = CcpNullParameterException.class)
	public void mustNotPrefixValueNullTest() {
		mustNot().prefix(FIELD, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void mustNotMatchPhraseFieldNullTest() {
		mustNot().matchPhrase((CcpEntityField) null, "value");
	}

	@Test(expected = CcpNullParameterException.class)
	public void mustNotMatchPhraseValueNullTest() {
		mustNot().matchPhrase(FIELD, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void mustNotTermFieldNullTest() {
		mustNot().term((CcpEntityField) null, "value");
	}

	@Test(expected = CcpNullParameterException.class)
	public void mustNotTermValueNullTest() {
		mustNot().term(FIELD, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void mustNotExistsNullTest() {
		mustNot().exists(null);
	}

	// ── CcpQueryShould ────────────────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void shouldPrefixFieldNullTest() {
		should().prefix((CcpEntityField) null, "value");
	}

	@Test(expected = CcpNullParameterException.class)
	public void shouldPrefixValueNullTest() {
		should().prefix(FIELD, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void shouldMatchPhrase2FieldNullTest() {
		should().matchPhrase2((CcpEntityField) null, "value");
	}

	@Test(expected = CcpNullParameterException.class)
	public void shouldMatchPhrase2ValueNullTest() {
		should().matchPhrase2(FIELD, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void shouldMatchFieldNullTest() {
		should().match((CcpEntityField) null, "value");
	}

	@Test(expected = CcpNullParameterException.class)
	public void shouldMatchValueNullTest() {
		should().match(FIELD, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void shouldMatchPhraseBoostFieldNullTest() {
		should().matchPhrase((String) null, "value", 1d);
	}

	@Test(expected = CcpNullParameterException.class)
	public void shouldMatchPhraseBoostValueNullTest() {
		should().matchPhrase("field", null, 1d);
	}

	@Test(expected = CcpNullParameterException.class)
	public void shouldMatchBoostFieldNullTest() {
		should().match((String) null, "value", 1d, "and");
	}

	@Test(expected = CcpNullParameterException.class)
	public void shouldMatchBoostValueNullTest() {
		should().match("field", null, 1d, "and");
	}

	@Test(expected = CcpNullParameterException.class)
	public void shouldMatchBoostOperatorNullTest() {
		should().match("field", "value", 1d, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void shouldTermFieldNullTest() {
		should().term((CcpEntityField) null, "value");
	}

	@Test(expected = CcpNullParameterException.class)
	public void shouldTermValueNullTest() {
		should().term(FIELD, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void shouldExistsNullTest() {
		should().exists(null);
	}

	// ── CcpQueryShouldNot ─────────────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void shouldNotPrefixFieldNullTest() {
		shouldNot().prefix((CcpEntityField) null, "value");
	}

	@Test(expected = CcpNullParameterException.class)
	public void shouldNotPrefixValueNullTest() {
		shouldNot().prefix(FIELD, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void shouldNotMatchPhraseFieldNullTest() {
		shouldNot().matchPhrase((CcpEntityField) null, "value");
	}

	@Test(expected = CcpNullParameterException.class)
	public void shouldNotMatchPhraseValueNullTest() {
		shouldNot().matchPhrase(FIELD, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void shouldNotTermFieldNullTest() {
		shouldNot().term((CcpEntityField) null, "value");
	}

	@Test(expected = CcpNullParameterException.class)
	public void shouldNotTermValueNullTest() {
		shouldNot().term(FIELD, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void shouldNotExistsNullTest() {
		shouldNot().exists(null);
	}

	// ── CcpQuerySimplifiedQuery ───────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void simplifiedTermsFieldNullTest() {
		simplified().terms((CcpEntityField) null, "value");
	}

	@Test(expected = CcpNullParameterException.class)
	public void simplifiedTermsValueNullTest() {
		simplified().terms(FIELD, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void simplifiedPrefixFieldNullTest() {
		simplified().prefix((CcpEntityField) null, "value");
	}

	@Test(expected = CcpNullParameterException.class)
	public void simplifiedPrefixValueNullTest() {
		simplified().prefix(FIELD, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void simplifiedMatchPhraseFieldNullTest() {
		simplified().matchPhrase((CcpEntityField) null, "value");
	}

	@Test(expected = CcpNullParameterException.class)
	public void simplifiedMatchPhraseValueNullTest() {
		simplified().matchPhrase(FIELD, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void simplifiedTermFieldNullTest() {
		simplified().term((CcpEntityField) null, "value");
	}

	@Test(expected = CcpNullParameterException.class)
	public void simplifiedTermValueNullTest() {
		simplified().term(FIELD, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void simplifiedMatchFieldNullTest() {
		simplified().match((CcpJsonFieldName) null, "value");
	}

	@Test(expected = CcpNullParameterException.class)
	public void simplifiedMatchValueNullTest() {
		simplified().match(SampleFieldName.sample, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void simplifiedExistsNullTest() {
		simplified().exists(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void simplifiedAddConditionFieldNullTest() {
		simplified().addCondition(null, "value", "term");
	}

	@Test(expected = CcpNullParameterException.class)
	public void simplifiedAddConditionValueNullTest() {
		simplified().addCondition("field", null, "term");
	}

	@Test(expected = CcpNullParameterException.class)
	public void simplifiedAddConditionKeyNullTest() {
		simplified().addCondition("field", "value", null);
	}

	// ── CcpQueryAggregations ──────────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void addMinAggregationNameNullTest() {
		aggregations().addMinAggregation(null, FIELD);
	}

	@Test(expected = CcpNullParameterException.class)
	public void addMinAggregationFieldNullTest() {
		aggregations().addMinAggregation("agg", null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void addMaxAggregationNameNullTest() {
		aggregations().addMaxAggregation(null, FIELD);
	}

	@Test(expected = CcpNullParameterException.class)
	public void addMaxAggregationFieldNullTest() {
		aggregations().addMaxAggregation("agg", null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void addAvgAggregationNameNullTest() {
		aggregations().addAvgAggregation(null, FIELD);
	}

	@Test(expected = CcpNullParameterException.class)
	public void addAvgAggregationFieldNullTest() {
		aggregations().addAvgAggregation("agg", null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void addSumAggregationNameNullTest() {
		aggregations().addSumAggregation(null, FIELD);
	}

	@Test(expected = CcpNullParameterException.class)
	public void addSumAggregationFieldNullTest() {
		aggregations().addSumAggregation("agg", null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void startBucketNameNullTest() {
		aggregations().startBucket(null, FIELD, 10L);
	}

	@Test(expected = CcpNullParameterException.class)
	public void startBucketFieldNullTest() {
		aggregations().startBucket("bucket", null, 10L);
	}

	// ── CcpQueryRange / CcpQueryFieldRange ────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void startFieldRangeNullTest() {
		range().startFieldRange(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void lessThanNullTest() {
		fieldRange().lessThan(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void lessThanEqualsNullTest() {
		fieldRange().lessThanEquals(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void greaterThanNullTest() {
		fieldRange().greaterThan(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void greaterThanEqualsNullTest() {
		fieldRange().greaterThanEquals(null);
	}

	// ── CcpQueryExecutorDecorator ─────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void executorConstrutorQueryNullTest() {
		new CcpQueryExecutorDecorator(null, "any_index");
	}

	@Test(expected = CcpNullParameterException.class)
	public void executorConstrutorResourcesNullTest() {
		new CcpQueryExecutorDecorator(options(), (String[]) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void executorGetResultAsPackageUrlNullTest() {
		executor().getResultAsPackage(null, com.ccp.especifications.http.CcpHttpMethods.POST, 200, "f");
	}

	@Test(expected = CcpNullParameterException.class)
	public void executorGetResultAsPackageMethodNullTest() {
		executor().getResultAsPackage("/_search", null, 200, "f");
	}

	@Test(expected = CcpNullParameterException.class)
	public void executorGetResultAsPackageArrayNullTest() {
		executor().getResultAsPackage("/_search", com.ccp.especifications.http.CcpHttpMethods.POST, 200,
				(String[]) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void executorGetTermsStatisNullTest() {
		executor().getTermsStatis(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void executorUpdateNullTest() {
		executor().update(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void executorConsumeQueryResultScrollTimeNullTest() {
		executor().consumeQueryResult(null, 10, json -> {
		}, "f");
	}

	@Test(expected = CcpNullParameterException.class)
	public void executorConsumeQueryResultConsumerNullTest() {
		executor().consumeQueryResult("1m", 10, null, "f");
	}

	@Test(expected = CcpNullParameterException.class)
	public void executorConsumeQueryResultFieldsNullTest() {
		executor().consumeQueryResult("1m", 10, json -> {
		}, (String[]) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void executorGetResultAsListNullTest() {
		executor().getResultAsList((String[]) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void executorGetResultAsMapNullTest() {
		executor().getResultAsMap(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void executorGetMapNullTest() {
		executor().getMap(null);
	}

	// ── null-return: os nós do builder nunca podem devolver null ──────────────

	@Test
	public void nenhumNoDoBuilderRetornaNullTest() {
		CcpQueryOptions query = options()
				.setSize(1)
				.setFrom(0)
				.addAscSorting("field")
				.startQuery()
				.startBool()
				.startMust()
				.term(SampleFieldName.sample, "value")
				.endMustAndBackToBool()
				.endBoolAndBackToQuery()
				.endQueryAndBackToRequest();
		org.junit.Assert.assertNotNull(query);
		org.junit.Assert.assertNotNull(query.toString());
		org.junit.Assert.assertNotNull(CcpOtherConstants.EMPTY_JSON);
	}
}
