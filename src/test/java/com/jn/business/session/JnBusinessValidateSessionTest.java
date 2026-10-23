package com.jn.business.session;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.implementations.password.mindrot.CcpMindrotPasswordHandler;
import com.ccp.json.validations.global.engine.CcpJsonValidationError;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.js.business.login.JsBusinessValidateLogin;

/**
 * The session is validated by the business that js seeds in {@code jn_session_validator}, run by its class name: a
 * request without {@code sessionToken} is refused by the validation of the login validator, proof that the seed was
 * found and run.
 */
public class JnBusinessValidateSessionTest {

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

	enum JsonFieldNames implements CcpJsonFieldName {
		featureName
	}

	@Test
	public void instanceExistsTest() {
		assertNotNull(JnBusinessValidateSession.INSTANCE);
	}

	@Test
	public void theSeededValidatorRefusesARequestWithoutSessionToken() {
		CcpJsonRepresentation request = CcpOtherConstants.EMPTY_JSON.put(JnJsonCommonsFields.email, "nosession@teste.com");
		try {
			JnBusinessValidateSession.INSTANCE.execute(request);
			fail("a request without session token was accepted");
		} catch (CcpJsonValidationError e) {
			String featureName = e.json.getAsString(JsonFieldNames.featureName);
			assertEquals(JsBusinessValidateLogin.class.getName(), featureName);
		}
	}

	@Test(expected = CcpNullParameterException.class)
	public void applyNullTest() {
		JnBusinessValidateSession.INSTANCE.execute(null);
	}
}
