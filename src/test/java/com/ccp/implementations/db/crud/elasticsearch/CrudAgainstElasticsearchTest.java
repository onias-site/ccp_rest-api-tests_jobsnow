package com.ccp.implementations.db.crud.elasticsearch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.crud.CcpCrud;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.jn.entities.JnEntityAsyncTask;
import com.jn.entities.JnEntityContactUsIgnored;

/**
 * Proves the Elasticsearch CRUD ({@code CcpCrud}) on an index of its own, created before and dropped after the tests:
 * insert and update, read, existence and deletion of a document, plus the request bodies of the multiple get.
 */
public class CrudAgainstElasticsearchTest {

	private static final String INDEX = "ccp_crud_test";

	private static final HttpClient HTTP = HttpClient.newHttpClient();

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler(), new CcpApacheMimeHttp(), new CcpElasticSearchDbRequest(),
				new CcpElasticSearchCrud(), CcpLocalCacheInstances.mock);
	}

	private static void call(String method, String path) throws Exception {
		HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:9200/" + path)).method(method, HttpRequest.BodyPublishers.noBody()).build();
		HTTP.send(request, HttpResponse.BodyHandlers.discarding());
	}

	@BeforeClass
	public static void createTheIndex() throws Exception {
		call("DELETE", INDEX);
		call("PUT", INDEX);
	}

	@AfterClass
	public static void dropTheIndex() throws Exception {
		call("DELETE", INDEX);
	}

	private final CcpCrud crud = CcpDependencyInjection.getDependency(CcpCrud.class);

	private final CcpFieldName name = new CcpFieldName("name");

	@Test
	public void aDocumentIsInsertedThenUpdatedReadAndDeleted() {
		String id = "doc" + System.nanoTime();
		CcpJsonRepresentation first = CcpOtherConstants.EMPTY_JSON.put(this.name, "first");

		CcpJsonRepresentation inserted = this.crud.save(INDEX, first, id);
		assertTrue(inserted.toString(), this.crud.isInsertedDocument(inserted));

		CcpJsonRepresentation updated = this.crud.save(INDEX, first.put(this.name, "second"), id);
		assertFalse(updated.toString(), this.crud.isInsertedDocument(updated));

		assertTrue(this.crud.exists(INDEX, id));
		assertEquals("second", this.crud.getOneById(INDEX, id).getAsString(this.name));

		assertTrue(this.crud.delete(INDEX, id));
		assertFalse(this.crud.exists(INDEX, id));
		assertFalse("deleting what is not there answers false", this.crud.delete(INDEX, id));
	}

	@Test
	public void theOutcomeOfASaveIsReadFromTheBodyOrTheStatus() {
		CcpFieldName result = new CcpFieldName("result");
		assertTrue(this.crud.isInsertedDocument(CcpOtherConstants.EMPTY_JSON.put(result, "created")));
		assertFalse(this.crud.isInsertedDocument(CcpOtherConstants.EMPTY_JSON.put(result, "noop")));
		assertFalse("without body and status the outcome is unknown", this.crud.isInsertedDocument(CcpOtherConstants.EMPTY_JSON));
	}

	@Test
	public void theMultipleGetAsksForEveryIdInEveryEntity() {
		ElasticSearchCrud elasticSearchCrud = new ElasticSearchCrud();

		CcpJsonRepresentation body = elasticSearchCrud.getRequestBodyToMultipleGet(new LinkedHashSet<>(Arrays.asList("1", "2")),
				JnEntityAsyncTask.ENTITY, JnEntityContactUsIgnored.ENTITY);

		List<CcpJsonRepresentation> docs = body.getAsJsonList(new CcpFieldName("docs"));
		assertEquals(4, docs.size());
		assertEquals("jn_async_task", docs.get(0).getAsString(new CcpFieldName("_index")));
		assertEquals("2", docs.get(1).getAsString(new CcpFieldName("_id")));
	}

	@Test
	public void aMultipleGetWithoutAnyCompleteKeyIsUnfeasible() {
		try {
			this.crud.getUnionAllExecutor().unionAll(Arrays.asList(CcpOtherConstants.EMPTY_JSON), JnEntityAsyncTask.ENTITY);
			fail("no record has the primary key of the entity");
		} catch (RuntimeException e) {
			assertTrue(e.getClass().getName(), e.getClass().getName().endsWith("CcpErrorCrudMultiGetSearchUnfeasible"));
		}
	}
}
