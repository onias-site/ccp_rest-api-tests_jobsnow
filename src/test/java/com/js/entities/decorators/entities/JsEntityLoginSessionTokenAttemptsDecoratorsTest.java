package com.js.entities.decorators.entities;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.js.entities.JsEntityLoginSessionTokenAttempts;
import com.ccp.especifications.db.utils.entity.decorators.EntityDecoratorTestTemplate;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/** js_login_session_token_attempts: {@code @CcpEntityCache(3600)}, transformador e validador. */
public class JsEntityLoginSessionTokenAttemptsDecoratorsTest extends EntityDecoratorTestTemplate {

	protected CcpEntity entityUnderTest() {
		return JsEntityLoginSessionTokenAttempts.ENTITY;
	}

	protected CcpJsonRepresentation validRecord() {
		return this.com(JnJsonCommonsFields.email, this.email)
				.put(JnJsonCommonsFields.attempts, 1);
	}

	@Test
	public void chain() {
		this.shouldHaveChain("DecoratorFieldsValidatorEntity", "DecoratorFieldsTransformerEntity", "DecoratorCacheEntity", "DefaultImplementationEntity");
	}

	@Test
	public void saveReadAndDelete() {
		this.shouldSaveReadAndDelete();
	}

	@Test
	public void validator() {
		this.shouldRefuseInvalidRecord(this.validRecord().removeFields(JnJsonCommonsFields.attempts));
	}

	@Test
	public void transformer() {
		this.shouldStoreEmailAsHash();
	}

	@Test
	public void cache() {
		this.shouldServeReadFromCacheAndInvalidateOnWrite();
	}
}
