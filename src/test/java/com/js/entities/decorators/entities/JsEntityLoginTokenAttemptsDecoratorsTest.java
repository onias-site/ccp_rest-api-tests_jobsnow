package com.js.entities.decorators.entities;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.js.entities.JsEntityLoginTokenAttempts;
import com.ccp.especifications.db.utils.entity.decorators.EntityDecoratorTestTemplate;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/** js_login_token_attempts: {@code @CcpEntityCache(3600)}, transformador e validador. */
public class JsEntityLoginTokenAttemptsDecoratorsTest extends EntityDecoratorTestTemplate {

	protected CcpEntity entityUnderTest() {
		return JsEntityLoginTokenAttempts.ENTITY;
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
		this.shouldRefuseInvalidRecord(this.validRecord().put(JnJsonCommonsFields.attempts, -3));
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
