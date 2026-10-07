package com.jn.entities.decorators.engine;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.dependency.injection.CcpInstanceProvider;
import com.ccp.especifications.db.query.CcpQueryExecutor;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/**
 * Proves that the purge of the history of a versionable record runs again while Elasticsearch skips rows changed
 * during the deletion ({@code version_conflicts}), instead of failing: until 2026-10-06 one concurrent write aborted the
 * {@code _delete_by_query} with 409. Elasticsearch is replaced by an executor that answers with the given responses.
 */
public class VersionablePurgeConflictsTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	/** Gives the other test classes back the real query executor. */
	@After
	public void restoreTheRealExecutor() {
		CcpDependencyInjection.loadAllDependencies(new CcpElasticSearchQueryExecutor());
	}

	/** Answers each delete with the next response and counts the deletes. */
	private AtomicInteger elasticsearchAnswering(String... responses) {
		LinkedList<String> pending = new LinkedList<>(Arrays.asList(responses));
		AtomicInteger deletes = new AtomicInteger();
		CcpQueryExecutor executor = (CcpQueryExecutor) Proxy.newProxyInstance(this.getClass().getClassLoader(),
				new Class<?>[] { CcpQueryExecutor.class }, (proxy, method, args) -> {
					if ("delete".equals(method.getName())) {
						deletes.incrementAndGet();
						return new CcpJsonRepresentation(pending.removeFirst());
					}
					throw new UnsupportedOperationException(method.getName());
				});
		CcpInstanceProvider<CcpQueryExecutor> provider = () -> executor;
		CcpDependencyInjection.loadAllDependencies(provider);
		return deletes;
	}

	private CcpJsonRepresentation purgeRequest() {
		return CcpOtherConstants.EMPTY_JSON
				.put(JnBusinessDeleteVersionableRecords.JsonFieldNames.entitiesToDelete, Arrays.asList("jn_some_entity"))
				.put(JnJsonCommonsFields.id, "{\"email\":\"someone@example.com\"}");
	}

	private long deletedBy(CcpJsonRepresentation result) {
		return result.getAsLongNumber(JnBusinessDeleteVersionableRecords.JsonFieldNames.deleted);
	}

	@Test
	public void rowsSkippedByAConflictAreDeletedInTheNextRound() {
		AtomicInteger deletes = this.elasticsearchAnswering(
				"{\"deleted\": 2, \"version_conflicts\": 1}",
				"{\"deleted\": 1, \"version_conflicts\": 0}");

		CcpJsonRepresentation result = JnBusinessDeleteVersionableRecords.INSTANCE.execute(this.purgeRequest());

		assertEquals(2, deletes.get());
		assertEquals(3, this.deletedBy(result));
	}

	@Test
	public void withoutConflictsThereIsASingleRound() {
		AtomicInteger deletes = this.elasticsearchAnswering("{\"deleted\": 4, \"version_conflicts\": 0}");

		CcpJsonRepresentation result = JnBusinessDeleteVersionableRecords.INSTANCE.execute(this.purgeRequest());

		assertEquals(1, deletes.get());
		assertEquals(4, this.deletedBy(result));
	}

	@Test
	public void persistentConflictsStopAfterTheLastRound() {
		AtomicInteger deletes = this.elasticsearchAnswering(
				"{\"deleted\": 0, \"version_conflicts\": 1}",
				"{\"deleted\": 0, \"version_conflicts\": 1}",
				"{\"deleted\": 0, \"version_conflicts\": 1}");

		JnBusinessDeleteVersionableRecords.INSTANCE.execute(this.purgeRequest());

		assertEquals(JnBusinessDeleteVersionableRecords.MAX_PURGE_ROUNDS, deletes.get());
	}
}
