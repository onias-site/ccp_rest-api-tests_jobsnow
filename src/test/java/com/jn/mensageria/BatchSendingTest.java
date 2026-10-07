package com.jn.mensageria;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import com.ccp.business.CcpBusiness;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/**
 * Proves the sending of several messages of a task ({@link JnFunctionMensageriaSender#sendToMensageria}) through the
 * synchronous messaging of the tests: each message runs the task once, and a task that declines being recorded as an
 * asynchronous task still runs, as in the single sending (finding 56).
 */
public class BatchSendingTest {

	static {
		CcpDependencyInjection.loadAllDependencies(
				CcpLocalInstances.syncMensageriaListener,
				new CcpElasticSearchQueryExecutor(),
				new CcpElasticSearchDbRequest(),
				CcpLocalCacheInstances.mock,
				new CcpElasticSerchDbBulk(),
				new CcpElasticSearchCrud(),
				new CcpGsonJsonHandler(),
				new CcpApacheMimeHttp());
	}

	static final AtomicInteger RECORDED_RUNS = new AtomicInteger();

	static final AtomicInteger UNRECORDED_RUNS = new AtomicInteger();

	/** A task that may be recorded as an asynchronous task. */
	public static class RecordedTask implements CcpBusiness {
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			RECORDED_RUNS.incrementAndGet();
			return json;
		}
	}

	/** A task that declines being recorded as an asynchronous task. */
	public static class UnrecordedTask implements CcpBusiness {
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			UNRECORDED_RUNS.incrementAndGet();
			return json;
		}
		public boolean canBeSavedAsAsyncTask() {
			return false;
		}
	}

	private CcpJsonRepresentation message(int number) {
		return CcpOtherConstants.EMPTY_JSON.put(JnJsonCommonsFields.message, "message " + number);
	}

	@Test
	public void eachMessageOfTheBatchRunsTheTaskOnce() {
		RECORDED_RUNS.set(0);

		new JnFunctionMensageriaSender(new RecordedTask()).sendToMensageria(Arrays.asList(this.message(1), this.message(2)));
		new JnFunctionMensageriaSender(new RecordedTask()).sendToMensageria(this.message(3));

		assertEquals(3, RECORDED_RUNS.get());
	}

	@Test
	public void aTaskThatDeclinesBeingRecordedStillRunsInABatch() {
		UNRECORDED_RUNS.set(0);

		new JnFunctionMensageriaSender(new UnrecordedTask()).sendToMensageria(this.message(1), this.message(2));

		assertEquals(2, UNRECORDED_RUNS.get());
	}

	@Test
	public void theSingleSendingRunsTheTaskEvenWhenItDeclinesBeingRecorded() {
		UNRECORDED_RUNS.set(0);

		new JnFunctionMensageriaSender(new UnrecordedTask()).execute(this.message(1));

		assertEquals(1, UNRECORDED_RUNS.get());
	}
}
