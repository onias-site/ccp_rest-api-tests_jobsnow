package com.jn.entities.decorators.entities;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityExpurgableOptions;
import com.jn.entities.JnEntityLoginSessionValidation;
import com.jn.entities.decorators.EntityDecoratorTestTemplate;
import com.jn.json.fields.validation.JnJsonCommonsFields;

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
}
