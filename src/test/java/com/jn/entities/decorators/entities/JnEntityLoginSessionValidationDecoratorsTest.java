package com.jn.entities.decorators.entities;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.crud.CcpCrud;
import com.ccp.especifications.db.crud.CcpSelectUnionAll;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityExpurgableOptions;
import com.jn.entities.JnEntityLoginSessionValidation;
import com.ccp.especifications.db.utils.entity.decorators.ElasticsearchLocal;
import com.ccp.especifications.db.utils.entity.decorators.EntityDecoratorTestTemplate;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.utils.JnDeleteKeysFromCache;

/**
 * jn_login_session_validation: twin of jn_login_session_terminated (ended session),
 * {@code @CcpEntityCache(3600)}, hourly disposable, transformer (hash of the token, which is the primary key)
 * and validator.
 */
public class JnEntityLoginSessionValidationDecoratorsTest extends EntityDecoratorTestTemplate {

	private final String token = "TOKEN" + this.unique;

	protected CcpEntity entityUnderTest() {
		return JnEntityLoginSessionValidation.ENTITY;
	}

	protected CcpJsonRepresentation validRecord() {
		return this.com(JnJsonCommonsFields.email, this.email)
				.put(JnEntityLoginSessionValidation.Fields.token, this.token)
				.put(JnJsonCommonsFields.ip, "127.0.0.1")
				.put(JnJsonCommonsFields.userAgent, "junit");
	}

	@Test
	public void chain() {
		this.shouldHaveChain("DecoratorFieldsValidatorEntity", "DecoratorFieldsTransformerEntity", "DecoratorTwinEntity", "DecoratorCacheEntity", "JnDisposableEntity", "DefaultImplementationEntity");
	}

	@Test
	public void saveReadAndDelete() {
		this.shouldSaveReadAndDelete();
	}

	@Test
	public void validator() {
		this.shouldRefuseInvalidRecord(this.validRecord().put(JnJsonCommonsFields.ip, "1.1"));
	}

	@Test
	public void emailTransformer() {
		this.shouldStoreEmailAsHash();
	}

	@Test
	public void tokenTransformer() {
		this.shouldStoreTransformed(JnEntityLoginSessionValidation.Fields.token, this.token);
	}

	@Test
	public void cache() {
		this.shouldServeReadFromCacheAndInvalidateOnWrite();
	}

	@Test
	public void twin() {
		this.shouldAlternateBetweenMainAndTwin("jn_login_session_terminated");
	}

	@Test
	public void redirectToTwin() {
		this.shouldRedirectOnGetOneByIdWhenRecordMovedToTwin();
	}

	@Test
	public void disposable() {
		this.shouldKeepDisposableCopyUntilDeadline(CcpEntityExpurgableOptions.hourly);
	}

	/**
	 * The session validation ({@code JnBusinessValidateLogin}) does not ask {@code exists}: it reads the session through
	 * a union all ({@code CcpGetEntityId}). When the hour changes the document of the period is no longer the current one,
	 * and only the disposable copy keeps the session alive until its deadline; without it every session ended at the turn
	 * of the hour, and each request after that counted a wrong session token attempt until the password was locked (423).
	 */
	@Test
	public void unionAllFindsTheSessionByTheCopyAfterTheHourChanges() {
		CcpEntity entityUnderTest = this.entityUnderTest();
		CcpJsonRepresentation record = this.validRecord();
		entityUnderTest.save(record);

		// the turn of the hour: the document of the period is gone, the copy is still valid
		ElasticsearchLocal.delete(this.name(), this.idInTheDatabase(record));
		this.clearCache(record);

		CcpCrud crud = CcpDependencyInjection.getDependency(CcpCrud.class);
		CcpJsonRepresentation[] parametersToSearch = new CcpJsonRepresentation[] { record };
		CcpSelectUnionAll unionAll = crud.unionAll(parametersToSearch, JnDeleteKeysFromCache.INSTANCE, entityUnderTest);
		assertTrue("the valid copy did not keep the session in the union all", entityUnderTest.isPresentInThisUnionAll(unionAll, record));

		// past the deadline of the copy the session is gone
		long now = System.currentTimeMillis();
		CcpJsonRepresentation expiredTimestamp = CcpOtherConstants.EMPTY_JSON.put(JnJsonCommonsFields.timestamp, now - 1000);
		ElasticsearchLocal.update("jn_disposable_record", this.disposableCopyId(record), expiredTimestamp);
		this.clearCache(record);
		CcpSelectUnionAll unionAllAfterTheDeadline = crud.unionAll(parametersToSearch, JnDeleteKeysFromCache.INSTANCE, entityUnderTest);
		assertFalse("the expired copy still keeps the session in the union all", entityUnderTest.isPresentInThisUnionAll(unionAllAfterTheDeadline, record));
	}
}
