package com.js.business.login;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Test;

import com.ccp.business.CcpBusiness;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.implementations.password.mindrot.CcpMindrotPasswordHandler;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.js.entities.JsEntityLoginSessionTokenAttempts;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/**
 * The counter of invalid session tokens: {@link JsBusinessIncrementSessionTokenAttempts} saves one attempt more, or runs
 * the business of the limit without saving when the limit is reached; {@link JsBusinessResetSessionTokenAttempts}
 * deletes the counter when there is one. Both answer their input.
 */
public class SessionTokenAttemptsBusinessesTest {

	static {
		CcpDependencyInjection.loadAllDependencies(
				CcpLocalInstances.syncMensageriaListener,
				CcpElasticSearchQueryExecutor.INSTANCE,
				CcpElasticSearchDbRequest.INSTANCE,
				CcpMindrotPasswordHandler.INSTANCE,
				CcpLocalCacheInstances.mock,
				CcpElasticSerchDbBulk.INSTANCE,
				CcpElasticSearchCrud.INSTANCE,
				CcpGsonJsonHandler.INSTANCE,
				CcpApacheMimeHttp.INSTANCE);
	}

	private final CcpJsonRepresentation user = CcpOtherConstants.EMPTY_JSON
			.put(JnJsonCommonsFields.email, "attempts" + System.currentTimeMillis() % 100000 + "@teste.com");

	private final List<CcpJsonRepresentation> limitReached = new ArrayList<>();

	private final CcpBusiness whenTheLimitIsReached = new LimitReachedRecorder(this.limitReached);

	@After
	public void deleteTheCounter() {
		JsEntityLoginSessionTokenAttempts.ENTITY.delete(this.user);
	}

	private CcpJsonRepresentation withCounter(CcpJsonRepresentation counter) {
		return this.user.addToItem(CcpEntity.JsonFieldNames._entities, JsEntityLoginSessionTokenAttempts.ENTITY, counter);
	}

	private int savedAttempts() {
		CcpJsonRepresentation counter = JsEntityLoginSessionTokenAttempts.ENTITY.getOneById(this.user);
		return counter.getAsIntegerNumber(JnJsonCommonsFields.attempts);
	}

	@Test
	public void withoutCounterTheFirstAttemptIsSaved() {
		CcpJsonRepresentation input = this.withCounter(CcpOtherConstants.EMPTY_JSON);

		CcpJsonRepresentation output = new JsBusinessIncrementSessionTokenAttempts(3, this.whenTheLimitIsReached).execute(input);

		assertEquals(input, output);
		assertEquals(1, this.savedAttempts());
		assertTrue(this.limitReached.isEmpty());
	}

	@Test
	public void belowTheLimitTheCounterGoesUpByOne() {
		CcpJsonRepresentation input = this.withCounter(CcpOtherConstants.EMPTY_JSON.put(JnJsonCommonsFields.attempts, 1));

		new JsBusinessIncrementSessionTokenAttempts(3, this.whenTheLimitIsReached).execute(input);

		assertEquals(2, this.savedAttempts());
		assertTrue(this.limitReached.isEmpty());
	}

	@Test
	public void reachingTheLimitRunsItsBusinessAndSavesNothing() {
		CcpJsonRepresentation input = this.withCounter(CcpOtherConstants.EMPTY_JSON.put(JnJsonCommonsFields.attempts, 2));

		CcpJsonRepresentation output = new JsBusinessIncrementSessionTokenAttempts(3, this.whenTheLimitIsReached).execute(input);

		assertEquals(input, output);
		assertEquals(1, this.limitReached.size());
		assertFalse(JsEntityLoginSessionTokenAttempts.ENTITY.exists(this.user));
	}

	@Test
	public void theResetDeletesTheCounter() {
		CcpJsonRepresentation counter = this.user.put(JnJsonCommonsFields.attempts, 2);
		JsEntityLoginSessionTokenAttempts.ENTITY.save(counter);
		CcpJsonRepresentation input = this.withCounter(counter);

		CcpJsonRepresentation output = JsBusinessResetSessionTokenAttempts.INSTANCE.execute(input);

		assertEquals(input, output);
		assertFalse(JsEntityLoginSessionTokenAttempts.ENTITY.exists(this.user));
	}

	@Test
	public void withoutCounterTheResetDoesNothing() {
		CcpJsonRepresentation input = this.withCounter(CcpOtherConstants.EMPTY_JSON);

		CcpJsonRepresentation output = JsBusinessResetSessionTokenAttempts.INSTANCE.execute(input);

		assertEquals(input, output);
		assertFalse(JsEntityLoginSessionTokenAttempts.ENTITY.exists(this.user));
	}
}
