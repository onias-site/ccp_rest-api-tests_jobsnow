package com.js.business.login;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.function.Supplier;

import org.junit.After;
import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityMetaData;
import com.ccp.flow.CcpErrorFlowDisturb;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.implementations.password.mindrot.CcpMindrotPasswordHandler;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.jn.entities.JnEntityDisposableRecord;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.js.entities.JsEntityLoginSessionValidation;
import com.js.status.login.JsProcessStatusExecuteLogin;

/**
 * {@link JsBusinessRefuseExpiredSession}, run when the session is not in {@code js_login_session_validation}: a session
 * that never existed passes (the flow goes on and counts the attempt), while one whose copy is still in
 * {@code jn_disposable_record} existed and expired, so it is refused with {@code invalidSession} without counting an
 * attempt.
 */
public class JsBusinessRefuseExpiredSessionTest {

	static {
		CcpDependencyInjection.removeAllDependencies();
		CcpDependencyInjection.loadAllDependencies(
				CcpLocalInstances.syncMensageriaListener,
				CcpLocalInstances.bucket,
				CcpLocalInstances.email,
				CcpLocalInstances.instantMessenger,
				CcpElasticSearchQueryExecutor.INSTANCE,
				CcpElasticSearchDbRequest.INSTANCE,
				CcpMindrotPasswordHandler.INSTANCE,
				CcpLocalCacheInstances.mock,
				CcpElasticSerchDbBulk.INSTANCE,
				CcpElasticSearchCrud.INSTANCE,
				CcpGsonJsonHandler.INSTANCE,
				CcpApacheMimeHttp.INSTANCE
		);
	}

	private final CcpJsonRepresentation session = CcpOtherConstants.EMPTY_JSON
			.put(JsEntityLoginSessionValidation.Fields.email, "sessao.expirada." + System.currentTimeMillis() + "@teste.com")
			.put(JsEntityLoginSessionValidation.Fields.token, "TOKEN" + System.nanoTime())
			.put(JsEntityLoginSessionValidation.Fields.ip, "127.0.0.1")
			.put(JsEntityLoginSessionValidation.Fields.userAgent, "Apache-HttpClient/4.5.4 (Java/17.0.9)");

	/** The key of the copy of the session in {@code jn_disposable_record}: the entity and its transformed primary key. */
	private CcpJsonRepresentation copyKey() {
		CcpEntityMetaData metaData = JsEntityLoginSessionValidation.ENTITY.getEntityMetaData();
		CcpJsonRepresentation transformedSession = JsEntityLoginSessionValidation.ENTITY.getHandledJson(this.session);
		Supplier<CcpJsonRepresentation> transformedSessionSupplier = transformedSession.getJsonSupplier();
		CcpJsonRepresentation primaryKey = metaData.getPrimaryKeyValues(transformedSessionSupplier);
		String serializedPrimaryKey = primaryKey.asUgglyJson();
		CcpJsonRepresentation copyKey = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonCommonsFields.entity, metaData.entityName)
				.put(JnJsonCommonsFields.id, serializedPrimaryKey);
		return copyKey;
	}

	@After
	public void deleteTheSession() {
		JsEntityLoginSessionValidation.ENTITY.deleteAnyWhere(this.session);
		JnEntityDisposableRecord.ENTITY.delete(this.copyKey());
	}

	@Test
	public void aSessionThatNeverExistedPasses() {
		assertFalse(JnEntityDisposableRecord.ENTITY.exists(this.copyKey()));

		assertSame(this.session, JsBusinessRefuseExpiredSession.INSTANCE.execute(this.session));
	}

	@Test
	public void aSessionWhoseCopyIsStillRecordedIsRefusedAsExpired() {
		JsEntityLoginSessionValidation.ENTITY.save(this.session);
		assertTrue("saving the session records its copy", JnEntityDisposableRecord.ENTITY.exists(this.copyKey()));

		CcpJsonRepresentation request = this.session.addToItem(CcpEntity.JsonFieldNames._entities, JsEntityLoginSessionValidation.ENTITY, CcpOtherConstants.EMPTY_JSON);
		try {
			JsBusinessRefuseExpiredSession.INSTANCE.execute(request);
			fail("The expired session was accepted");
		} catch (CcpErrorFlowDisturb refusal) {
			assertEquals(JsProcessStatusExecuteLogin.invalidSession, refusal.status);
			assertFalse(refusal.json.toString(), refusal.json.containsAllFields(CcpEntity.JsonFieldNames._entities));
		}
	}
}
