package com.js.business.login;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.dependency.injection.CcpDependencyInjection.CcpErrorDependencyInjectionMissing;
import com.ccp.especifications.db.bulk.CcpBulkExecutor;
import com.ccp.especifications.db.utils.entity.CcpEntityOperationType;
import com.ccp.especifications.mensageria.sender.CcpMensageriaSender;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.implementations.password.mindrot.CcpMindrotPasswordHandler;
import com.ccp.json.fields.validation.CcpJsonCommonsFields;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.js.entities.JsEntityLoginPassword;
import com.js.entities.JsEntityLoginSessionTokenAttempts;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.utils.JnSystemProperties;

/**
 * The session is also validated by the API of vis, which has no bulk since 2026-11-15: the token of no session that
 * reaches the limit of attempts locks the password through the queue of {@code JsEntityLoginPassword} (its delete moves
 * the password to the twin by bulk, in the consumer), instead of deleting it in the thread of the API, where it failed
 * with {@code CcpErrorDependencyInjectionMissing} (2026-11-21).
 */
public class SessionValidationWithoutBulkTest {

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
			.put(JnJsonCommonsFields.email, "nobulk" + System.currentTimeMillis() % 100000 + "@teste.com");

	private final List<String> sentMessages = Collections.synchronizedList(new ArrayList<>());

	@Before
	public void likeTheApiOfVis() {
		// only a password that exists is moved to the twin by bulk
		CcpJsonRepresentation login = this.user.put(JnJsonCommonsFields.password, "Jobsnow@123");
		JsEntityLoginPassword.ENTITY.save(login);
		CcpDependencyInjection.removeDependecy(CcpBulkExecutor.class);
		RecordingMensageriaSender recordingMensageriaSender = new RecordingMensageriaSender(this.sentMessages);
		CcpDependencyInjection.loadAllDependencies(() -> (CcpMensageriaSender) recordingMensageriaSender);
	}

	@After
	public void restoreTheDependencies() {
		CcpDependencyInjection.loadAllDependencies(CcpElasticSerchDbBulk.INSTANCE, CcpLocalInstances.syncMensageriaListener);
		JsEntityLoginSessionTokenAttempts.ENTITY.delete(this.user);
		JsEntityLoginPassword.ENTITY.deleteAnyWhere(this.user);
	}

	@Test
	public void theLastAttemptLocksThePasswordThroughTheQueue() {
		int maxAttempts = JnSystemProperties.INSTANCE.maxAttempts();
		int lastAttemptBeforeTheLimit = maxAttempts - 1;
		CcpJsonRepresentation counter = this.user.put(JnJsonCommonsFields.attempts, lastAttemptBeforeTheLimit);
		JsEntityLoginSessionTokenAttempts.ENTITY.save(counter);
		// the session values that the filter puts in every request
		CcpJsonRepresentation requestWithToken = this.user.put(CcpJsonCommonsFields.sessionToken, "ZZZZZZZZ");
		CcpJsonRepresentation requestWithIp = requestWithToken.put(JnJsonCommonsFields.ip, "127.0.0.1");
		CcpJsonRepresentation request = requestWithIp.put(JnJsonCommonsFields.userAgent, "JUnit");

		try {
			JsBusinessValidateLogin.INSTANCE.execute(request);
			fail("a token of no session was accepted");
		} catch (CcpErrorDependencyInjectionMissing e) {
			fail("the password was locked in the thread of the API: " + e.getMessage());
		} catch (RuntimeException expected) {
			// the session is refused
		}

		String passwordTopic = JsEntityLoginPassword.class.getName();
		String operation = CcpEntityOperationType.delete.name();
		String lockThePassword = passwordTopic + " " + operation;
		List<String> expectedMessages = Arrays.asList(lockThePassword);
		assertEquals(expectedMessages, this.sentMessages);
	}
}
