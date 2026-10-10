package com.ccp.especifications.db.utils.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
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
import com.jn.entities.JnEntityLoginSessionValidation;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/**
 * Proves the entity operations of {@link CcpEntityOperationType} as callbacks, on {@code jn_login_session_validation} (a twin
 * entity whose records expire hourly): save, copy and transfer between the entity and its twin (naming the target by
 * configurator and entity name, or by configurator only), delete and delete everywhere.
 */
public class OperationTypesBehaviorTest {

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

	private final CcpEntity main = JnEntityLoginSessionValidation.ENTITY;

	private final CcpEntity twin = JnEntityLoginSessionValidation.ENTITY.getTwinEntity();

	private final CcpJsonRepresentation record = CcpOtherConstants.EMPTY_JSON.put(JnJsonCommonsFields.email, "operations" + System.nanoTime() + "@jobsnow.com")
			.put(JnEntityLoginSessionValidation.Fields.token, "TOKEN123").put(JnJsonCommonsFields.ip, "127.0.0.1").put(JnJsonCommonsFields.userAgent, "junit");

	@After
	public void removeTheRecord() {
		this.main.deleteAnyWhere(this.record);
	}

	@Test
	public void theSaveCallbackSavesAndTheDeleteCallbackMovesToTheTwin() {
		CcpEntityOperationType.save.getOperationCallback(this.main).execute(this.record);
		assertTrue(this.main.exists(this.record));

		CcpEntityOperationType.delete.getOperationCallback(this.main).execute(this.record);

		assertFalse(this.main.exists(this.record));
		assertTrue("deleting from a twin entity moves the record to the twin", this.twin.exists(this.record));
	}

	@Test
	public void copyingToTheTwinKeepsTheRecordInBothSides() {
		this.main.save(this.record);
		CcpJsonRepresentation withTarget = CcpEntityOperationType.putEntityToTransfer(this.record, this.twin);

		CcpEntityOperationType.copyDataTo.execute(this.main, withTarget);

		assertTrue(this.main.exists(this.record));
		assertTrue(this.twin.exists(this.record));
		CcpJsonRepresentation everywhere = this.main.getOneByIdAnyWhere(this.record);
		assertFalse(everywhere.toString(), everywhere.isEmpty());
	}

	@Test
	public void aTargetNamedOnlyByItsConfiguratorIsTheMainEntity() {
		this.twin.save(this.record);
		CcpJsonRepresentation withTarget = CcpEntityOperationType.putEntityToTransfer(this.record, this.main)
				.removeFields(CcpEntityOperationType.Fields.entityNameToTransfer);

		CcpEntityOperationType.transferDataTo.getTopicHandler(this.twin, com.jn.db.bulk.JnExecuteBulkOperation.INSTANCE, com.jn.utils.JnDeleteKeysFromCache.INSTANCE).execute(withTarget);

		assertTrue(this.main.exists(this.record));
		assertFalse(this.twin.exists(this.record));
	}

	@Test
	public void readingTheMainEntityFindsTheRecordOrRedirectsToTheTwin() {
		this.main.save(this.record);
		assertFalse("the e-mail is stored as its hash", this.main.getOneById(this.record).getAsString(JnJsonCommonsFields.email).isEmpty());

		this.twin.save(this.record);

		try {
			this.main.getOneById(this.record);
			org.junit.Assert.fail("the record is only in the twin");
		} catch (com.ccp.flow.CcpErrorFlowDisturb redirect) {
			assertEquals(com.ccp.process.CcpProcessStatusDefault.REDIRECT, redirect.status);
		}
	}

	@Test
	public void deletingEverywhereLeavesNoSide() {
		this.main.save(this.record);
		this.twin.save(this.record);

		CcpEntityOperationType.deleteAnyWhere.getOperationCallback(this.main).execute(this.record);

		assertFalse(this.main.exists(this.record));
		assertFalse(this.twin.exists(this.record));
	}

	@Test
	public void deletingNeedsNoInputRules() {
		assertFalse(JnEntityLoginSessionValidation.Fields.class.equals(CcpEntityOperationType.delete.getJsonValidationClass(this.main)));
		assertFalse(JnEntityLoginSessionValidation.Fields.class.equals(CcpEntityOperationType.deleteAnyWhere.getJsonValidationClass(this.main)));
		assertEquals(JnEntityLoginSessionValidation.Fields.class, CcpEntityOperationType.save.getJsonValidationClass(this.main));
	}

	/** A business with a public constructor, to be instantiated by reflection. */
	public static class Echo implements CcpBusiness {
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			return json;
		}
	}

	@Test
	public void aBusinessClassIsInstantiatedByReflection() {
		CcpBusiness business = CcpEntityOperationType.instanciateFunction(Echo.class);

		assertTrue(business instanceof Echo);
	}
}
