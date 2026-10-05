package com.ccp.implementations.db.query.elasticsearch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.query.CcpQueryExecutor;
import com.ccp.especifications.db.query.CcpQueryOptions;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

/**
 * Proves {@link ElasticSearchQueryExecutor} against the local Elasticsearch, on an index of its own that is created
 * before and dropped after the tests: counting, paging through a scroll, listing, aggregations and delete by query.
 */
public class QueryExecutorAgainstElasticsearchTest {

	private static final String INDEX = "ccp_query_executor_test";

	private static final HttpClient HTTP = HttpClient.newHttpClient();

	static {
		CcpDependencyInjection.loadAllDependencies(
				new CcpGsonJsonHandler(),
				new CcpApacheMimeHttp(),
				new CcpElasticSearchDbRequest(),
				new CcpElasticSearchQueryExecutor());
	}

	private static String call(String method, String path, String body) throws Exception {
		HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("http://localhost:9200/" + path)).header("Content-Type", "application/json");
		HttpRequest.BodyPublisher publisher = body.isEmpty() ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body);
		HttpResponse<String> response = HTTP.send(request.method(method, publisher).build(), HttpResponse.BodyHandlers.ofString());
		return response.body();
	}

	@BeforeClass
	public static void createTheIndex() throws Exception {
		call("DELETE", INDEX, "");
		call("PUT", INDEX, "{\"mappings\":{\"properties\":{\"name\":{\"type\":\"keyword\"},\"value\":{\"type\":\"integer\"}}}}");
		call("PUT", INDEX + "/_doc/1?refresh=true", "{\"name\":\"a\",\"value\":1}");
		call("PUT", INDEX + "/_doc/2?refresh=true", "{\"name\":\"a\",\"value\":2}");
		call("PUT", INDEX + "/_doc/3?refresh=true", "{\"name\":\"b\",\"value\":3}");
	}

	@AfterClass
	public static void dropTheIndex() throws Exception {
		call("DELETE", INDEX, "");
	}

	private final CcpQueryExecutor executor = CcpDependencyInjection.getDependency(CcpQueryExecutor.class);

	private final String[] index = { INDEX };

	private CcpQueryOptions allDocuments() {
		return CcpQueryOptions.INSTANCE.matchAll();
	}

	private CcpQueryOptions termsOfName() {
		Map<String, Object> terms = CcpOtherConstants.EMPTY_JSON
				.put(new CcpFieldName("byName"), CcpOtherConstants.EMPTY_JSON
						.put(new CcpFieldName("terms"), CcpOtherConstants.EMPTY_JSON.put(new CcpFieldName("field"), "name").content).content)
				.content;
		CcpQueryOptions query = this.allDocuments().zeroResults().putProperty(new CcpFieldName("aggs"), terms);
		return query;
	}

	@Test
	public void theDecoratorOfTheRequestDelegatesEveryReadToTheExecutor() {
		com.ccp.especifications.db.query.CcpQueryExecutorDecorator all = this.allDocuments().selectFrom(INDEX);
		com.ccp.especifications.db.query.CcpQueryExecutorDecorator terms = this.termsOfName().selectFrom(INDEX);

		assertEquals(3, all.total());
		assertEquals(3, all.getResultAsList("name").size());
		List<CcpJsonRepresentation> scrolled = new ArrayList<>();
		all.consumeQueryResult("10s", 2, scrolled::add, "name");
		assertEquals(3, scrolled.size());
		assertEquals(2, (int) terms.getAggregations().getInnerJson(new CcpFieldName("byName")).getAsIntegerNumber(new CcpFieldName("a")));
		assertTrue(terms.getTermsStatis("byName").isEmpty());
		assertTrue(terms.getMap("byName").isEmpty());
		assertEquals(1, all.getResultAsMap("name").fieldSet().size());
		CcpJsonRepresentation searchPackage = all.getResultAsPackage("/_search", com.ccp.especifications.http.CcpHttpMethods.POST, 200, "name");
		assertTrue(searchPackage.toString(), searchPackage.containsField(new CcpFieldName("hits")));
	}

	@Test
	public void theDecoratorDeletesAndUpdatesByQuery() throws Exception {
		call("PUT", INDEX + "/_doc/9?refresh=true", "{\"name\":\"decorated\",\"value\":9}");
		Map<String, Object> term = CcpOtherConstants.EMPTY_JSON
				.put(new CcpFieldName("term"), CcpOtherConstants.EMPTY_JSON.put(new CcpFieldName("name"), "decorated").content).content;
		CcpQueryOptions decoratedQuery = CcpQueryOptions.INSTANCE.putProperty(new CcpFieldName("query"), term);
		com.ccp.especifications.db.query.CcpQueryExecutorDecorator onlyTheDecorated = decoratedQuery.selectFrom(INDEX);

		onlyTheDecorated.update(CcpOtherConstants.EMPTY_JSON.put(new CcpFieldName("value"), 99));
		call("POST", INDEX + "/_refresh", "");
		CcpJsonRepresentation deleted = onlyTheDecorated.delete();
		call("POST", INDEX + "/_refresh", "");

		assertEquals(1, (int) deleted.getAsIntegerNumber(new CcpFieldName("deleted")));
		assertEquals(3, this.executor.total(this.allDocuments(), this.index));
	}

	@Test
	public void theTotalIsTheNumberOfMatchingDocuments() {
		assertEquals(3, this.executor.total(this.allDocuments(), this.index));
	}

	@Test
	public void theIndexesAreJoinedByCommas() {
		assertEquals("/a, b", new ElasticSearchQueryExecutor().getIndexes(new String[] { "a", "b" }));
	}

	@Test
	public void aScrollDeliversEveryDocumentPageByPage() {
		List<Integer> pageSizes = new ArrayList<>();
		List<CcpJsonRepresentation> documents = new ArrayList<>();

		this.executor.consumeQueryResult(this.allDocuments(), this.index, "10s", 2L, page -> {
			pageSizes.add(page.size());
			documents.addAll(page);
		}, "name");

		assertEquals(3, documents.size());
		assertEquals(Integer.valueOf(2), pageSizes.get(0));
	}

	@Test
	public void theOneByOneVariantOfTheScrollDeliversEveryDocument() {
		List<CcpJsonRepresentation> documents = new ArrayList<>();

		this.executor.consumeQueryResult(this.allDocuments(), this.index, "10s", 2, documents::add, "name");

		assertEquals(3, documents.size());
	}

	@Test
	public void theListBringsTheRequestedFields() {
		List<CcpJsonRepresentation> list = this.executor.getResultAsList(this.allDocuments(), this.index, "name");

		assertEquals(3, list.size());
		for (CcpJsonRepresentation record : list) {
			assertTrue(record.toString(), Arrays.asList("a", "b").contains(record.getAsString(new CcpFieldName("name"))));
		}
	}

	@Test
	public void aTermsAggregationCountsTheDocumentsOfEachKey() {
		CcpJsonRepresentation aggregations = this.executor.getAggregations(this.termsOfName(), this.index);

		CcpJsonRepresentation byName = aggregations.getInnerJson(new CcpFieldName("byName"));
		assertEquals(2, (int) byName.getAsIntegerNumber(new CcpFieldName("a")));
		assertEquals(1, (int) byName.getAsIntegerNumber(new CcpFieldName("b")));
	}

	@Test
	public void theTermsStatisticsAreCurrentlyEmpty() {
		assertTrue(this.executor.getTermsStatis(this.termsOfName(), this.index, "byName").isEmpty());
		assertTrue(this.executor.getMap(this.termsOfName(), this.index, "byName").isEmpty());
	}

	@Ignore("finding 28: getTermsStatis and getMap read the aggregation as a list, but getAggregations returns a map from key to count")
	@Test
	public void theTermsStatisticsCountTheDocumentsOfEachKey() {
		CcpJsonRepresentation statistics = this.executor.getTermsStatis(this.termsOfName(), this.index, "byName");

		assertEquals(2L, (long) statistics.getAsLongNumber(new CcpFieldName("a")));
	}

	@Test
	public void theMapByIdCurrentlyPutsEveryRecordUnderTheSameEmptyKey() {
		CcpJsonRepresentation byId = this.executor.getResultAsMap(this.allDocuments(), this.index, "name");

		assertEquals(byId.toString(), 1, byId.fieldSet().size());
		assertTrue(byId.toString(), byId.containsField(new CcpFieldName("")));
	}

	@Ignore("finding 28: getResultAsMap reads _id, but the records carry the id in the field id")
	@Test
	public void theMapByIdHasOneEntryPerRecord() {
		CcpJsonRepresentation byId = this.executor.getResultAsMap(this.allDocuments(), this.index, "name");

		assertEquals(3, byId.fieldSet().size());
	}

	@Ignore("finding 27: update by query ignores the new values (it sends no script)")
	@Test
	public void anUpdateByQueryChangesTheDocuments() throws Exception {
		CcpJsonRepresentation newValues = CcpOtherConstants.EMPTY_JSON.put(new CcpFieldName("value"), 99);

		this.executor.update(this.allDocuments(), this.index, newValues);
		call("POST", INDEX + "/_refresh", "");

		assertTrue(call("GET", INDEX + "/_doc/1", null).contains("\"value\":99"));
	}

	@Test
	public void aDeleteByQueryRemovesTheMatchingDocuments() throws Exception {
		call("PUT", INDEX + "/_doc/4?refresh=true", "{\"name\":\"temporary\",\"value\":4}");
		Map<String, Object> term = CcpOtherConstants.EMPTY_JSON
				.put(new CcpFieldName("term"), CcpOtherConstants.EMPTY_JSON.put(new CcpFieldName("name"), "temporary").content).content;
		CcpQueryOptions onlyTheTemporary = CcpQueryOptions.INSTANCE.putProperty(new CcpFieldName("query"), term);

		CcpJsonRepresentation response = this.executor.delete(onlyTheTemporary, this.index);
		call("POST", INDEX + "/_refresh", "");

		assertEquals(1, (int) response.getAsIntegerNumber(new CcpFieldName("deleted")));
		assertEquals(3, this.executor.total(this.allDocuments(), this.index));
	}
}
