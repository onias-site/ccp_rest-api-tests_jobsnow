package com.js.entities.decorators.entities;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.js.entities.JsEntityLoginPassword;
import com.ccp.especifications.db.utils.entity.decorators.EntityDecoratorTestTemplate;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/**
 * js_login_password: twin of js_login_password_locked (locked password), {@code @CcpEntityCache(3600)},
 * versionable, transformer (hash of the e-mail and BCrypt of the password) and validator (password strength).
 */
public class JsEntityLoginPasswordDecoratorsTest extends EntityDecoratorTestTemplate {

	private static final String SENHA = "Senha#12345";

	protected CcpEntity entityUnderTest() {
		return JsEntityLoginPassword.ENTITY;
	}

	protected CcpJsonRepresentation validRecord() {
		return this.com(JnJsonCommonsFields.email, this.email)
				.put(JnJsonCommonsFields.password, SENHA);
	}

	@Test
	public void chain() {
		this.shouldHaveChain("DecoratorFieldsValidatorEntity", "DecoratorFieldsTransformerEntity", "JnVersionablePurgeEntity", "DecoratorTwinEntity", "DecoratorCacheEntity", "JnVersionableEntity", "DefaultImplementationEntity");
	}

	@Test
	public void saveReadAndDelete() {
		this.shouldSaveReadAndDelete();
	}

	@Test
	public void validator() {
		this.shouldRefuseInvalidRecord(this.validRecord().put(JnJsonCommonsFields.password, "fraca"));
	}

	@Test
	public void emailTransformer() {
		this.shouldStoreEmailAsHash();
	}

	@Test
	public void passwordTransformer() {
		this.shouldStoreTransformed(JnJsonCommonsFields.password, SENHA);
	}

	@Test
	public void cache() {
		this.shouldServeReadFromCacheAndInvalidateOnWrite();
	}

	@Test
	public void twin() {
		this.shouldAlternateBetweenMainAndTwin("js_login_password_locked");
	}

	@Test
	public void redirectToTwin() {
		this.shouldRedirectOnGetOneByIdWhenRecordMovedToTwin();
	}

	@Test
	public void versionable() {
		this.shouldRecordHistoryOnEachWrite();
	}

	@Test
	public void historyPurge() {
		this.shouldPurgeHistoryOnDeleteAnyWhere();
	}
}
