package com.ccp.implementations.db.crud.elasticsearch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.junit.After;
import org.junit.Test;

import com.ccp.business.CcpBusiness;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.dependency.injection.CcpInstanceProvider;
import com.ccp.especifications.db.bulk.CcpBulkOperationResult;
import com.ccp.especifications.db.utils.CcpDbRequester;
import com.ccp.especifications.db.utils.entity.fields.CcpErrorDbUtilsIncorrectEntityFields;
import com.ccp.especifications.http.CcpHttpMethods;
import com.ccp.especifications.http.CcpHttpResponseTransform;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

/**
 * Proves that {@link ElasticSearchCrud#save} gives up a version conflict that does not go away: until 2026-10-06 each 409
 * waited one second and called {@code save} again with no limit, so a persistent conflict was an endless recursion.
 * Elasticsearch is replaced by a requester that answers 409 a given number of times and then 200.
 */
public class SaveVersionConflictTest {

	/** Answers 409 to the first {@code conflicts} calls and 200 afterwards, applying the flows of the save. */
	static class ConflictingRequester implements CcpDbRequester {
		final int conflicts;
		final List<String> urls = new ArrayList<>();

		ConflictingRequester(int conflicts) {
			this.conflicts = conflicts;
		}

		@SuppressWarnings("unchecked")
		public <V> V executeHttpRequest(String trace, String url, CcpHttpMethods method, CcpJsonRepresentation flows, CcpJsonRepresentation body, CcpHttpResponseTransform<V> transformer) {
			this.urls.add(url);
			int status = this.urls.size() <= this.conflicts ? 409 : 200;
			CcpBusiness flow = flows.getAsObject(new CcpFieldName("" + status));
			CcpJsonRepresentation response = CcpOtherConstants.EMPTY_JSON.put(new CcpFieldName("result"), "updated");
			return (V) flow.execute(response);
		}

		public <V> V executeHttpRequest(String trace, String url, CcpHttpMethods method, Integer expectedStatus, CcpJsonRepresentation body, String[] resources, CcpHttpResponseTransform<V> transformer) {
			throw new UnsupportedOperationException();
		}

		public <V> V executeHttpRequest(String trace, String url, CcpHttpMethods method, Integer expectedStatus, String body, CcpJsonRepresentation headers, CcpHttpResponseTransform<V> transformer) {
			throw new UnsupportedOperationException();
		}

		public <V> V executeHttpRequest(String trace, String url, CcpHttpMethods method, Integer expectedStatus, CcpJsonRepresentation body, CcpHttpResponseTransform<V> transformer) {
			throw new UnsupportedOperationException();
		}

		public List<CcpBulkOperationResult> executeDatabaseSetup(String pathToJavaClasses, String hostFolder, String pathToCreateEntityScript, Consumer<CcpErrorDbUtilsIncorrectEntityFields> whenIsIncorrectMapping, Consumer<Throwable> whenOccursAnError) {
			throw new UnsupportedOperationException();
		}

		public CcpJsonRepresentation getConnectionDetails() {
			throw new UnsupportedOperationException();
		}

		public String getFieldNameToEntity() {
			throw new UnsupportedOperationException();
		}

		public String getFieldNameToId() {
			throw new UnsupportedOperationException();
		}

		public CcpDbRequester createTables(String pathToCreateEntityScript, String pathToJavaClasses, String mappingJnEntitiesErrors, String insertErrors) {
			throw new UnsupportedOperationException();
		}
	}

	private ConflictingRequester elasticsearchAnswering409(int conflicts) {
		ConflictingRequester requester = new ConflictingRequester(conflicts);
		CcpInstanceProvider<CcpDbRequester> provider = () -> requester;
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler(), provider);
		return requester;
	}

	/** Gives the other test classes back the real Elasticsearch requester. */
	@After
	public void restoreTheRealRequester() {
		CcpDependencyInjection.loadAllDependencies(new CcpElasticSearchDbRequest());
	}

	private final CcpJsonRepresentation document = CcpOtherConstants.EMPTY_JSON.put(new CcpFieldName("name"), "jobsnow");

	@Test
	public void aConflictThatGoesAwayIsRetriedAndTheSaveSucceeds() {
		ConflictingRequester requester = this.elasticsearchAnswering409(1);

		CcpJsonRepresentation response = new ElasticSearchCrud().save("some_index", this.document, "id1");

		assertEquals(2, requester.urls.size());
		assertEquals("updated", response.getAsString(new CcpFieldName("result")));
	}

	@Test
	public void aPersistentConflictStopsAfterTheLastAttempt() {
		ConflictingRequester requester = this.elasticsearchAnswering409(1000);

		try {
			new ElasticSearchCrud().save("some_index", this.document, "id1");
			fail("a persistent conflict was retried forever");
		} catch (ElasticSearchCrud.CcpErrorElasticSearchVersionConflict e) {
			assertTrue(e.getMessage(), e.getMessage().contains("id1"));
			assertTrue(e.getMessage(), e.getMessage().contains("some_index"));
		}

		assertEquals(ElasticSearchCrud.MAX_SAVE_ATTEMPTS, requester.urls.size());
	}

	@Test
	public void elasticsearchIsAskedToRetryTheConflictItself() {
		ConflictingRequester requester = this.elasticsearchAnswering409(0);

		new ElasticSearchCrud().save("some_index", this.document, "id1");

		assertEquals("/some_index/_update/id1?retry_on_conflict=" + ElasticSearchCrud.MAX_SAVE_ATTEMPTS, requester.urls.get(0));
	}
}
