package com.ccp.implementations.db.bulk.elasticsearch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.bulk.CcpBulkEntityOperationType;
import com.ccp.especifications.db.bulk.CcpBulkExecutor;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.jn.entities.JnEntityAsyncTask;
import com.jn.entities.JnEntityContactUsIgnored;

/**
 * Proves how the Elasticsearch bulk pairs each item with its line of the answer ({@link ElasticSearchBulkOperationResult}),
 * the identity and text of the request lines ({@link BulkItem}) and the reset of the executor.
 */
public class BulkResultsTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler(), new CcpApacheMimeHttp(), new CcpElasticSearchDbRequest(), new CcpElasticSerchDbBulk());
	}

	private final CcpBulkItem item = new CcpBulkItem(CcpOtherConstants.EMPTY_JSON.put(JnEntityAsyncTask.Fields.topic, "t"),
			CcpBulkEntityOperationType.create, JnEntityAsyncTask.ENTITY, "abc");

	private CcpJsonRepresentation line(String index, String id, int status) {
		CcpJsonRepresentation details = CcpOtherConstants.EMPTY_JSON
				.put(new CcpFieldName("_index"), index)
				.put(new CcpFieldName("_id"), id)
				.put(new CcpFieldName("status"), status);
		return CcpOtherConstants.EMPTY_JSON.put(new CcpFieldName("create"), details.content);
	}

	@Test
	public void eachItemFindsItsLineByIdAndEntity() {
		List<CcpJsonRepresentation> answer = Arrays.asList(this.line("jn_contact_us_ignored", "abc", 409), this.line("jn_async_task", "abc", 201));

		ElasticSearchBulkOperationResult result = new ElasticSearchBulkOperationResult(this.item, answer);

		assertEquals(201, result.status());
		assertEquals(this.item, result.getBulkItem());
		assertTrue(result.getErrorDetails().isEmpty());
		assertTrue(result.toString(), result.toString().contains("201"));
	}

	@Test
	public void anItemWithoutItsLineIsAnError() {
		for (List<CcpJsonRepresentation> answer : Arrays.asList(
				Arrays.asList(this.line("jn_async_task", "other", 201)),
				Arrays.asList(this.line("jn_contact_us_ignored", "abc", 201)))) {
			try {
				new ElasticSearchBulkOperationResult(this.item, answer);
				fail("the answer has no line for the item");
			} catch (RuntimeException e) {
				assertTrue(e.getClass().getName(), e.getClass().getName().endsWith("CcpErrorBulkItemNotFound"));
			}
		}
	}

	@Test
	public void theRequestLinesAreTheSameWhenEntityAndIdAreTheSame() {
		BulkItem line = new BulkItem(this.item);
		BulkItem sameDocument = new BulkItem(new CcpBulkItem(this.item, CcpBulkEntityOperationType.delete));
		BulkItem otherEntity = new BulkItem(new CcpBulkItem(this.item.json, CcpBulkEntityOperationType.create, JnEntityContactUsIgnored.ENTITY, "abc"));

		assertEquals(line, sameDocument);
		assertEquals(line.hashCode(), sameDocument.hashCode());
		assertNotEquals(line, otherEntity);
		assertNotEquals(line, "not a line");
		assertTrue(line.toString(), line.toString().contains("id=abc") && line.toString().contains("entity=jn_async_task"));
	}

	@Test
	public void anEmptyOrClearedExecutorSendsNothing() {
		CcpBulkExecutor executor = CcpDependencyInjection.getDependency(CcpBulkExecutor.class);

		assertTrue(executor.getBulkOperationResult().isEmpty());
		assertTrue(executor.addRecords(new ArrayList<>(Arrays.asList(this.item))).clearRecords().getBulkOperationResult().isEmpty());
	}
}
