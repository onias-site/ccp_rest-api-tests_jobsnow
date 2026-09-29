package com.jn.entities.decorators.entities;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.jn.entities.JnEntityLoginPassword;
import com.jn.entities.decorators.EntityDecoratorTestTemplate;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/**
 * jn_login_password: gêmea de jn_login_password_locked (senha bloqueada), {@code @CcpEntityCache(3600)},
 * versionável, transformador (hash do e-mail e BCrypt da senha) e validador (força da senha).
 */
public class JnEntityLoginPasswordDecoratorsTest extends EntityDecoratorTestTemplate {

	private static final String SENHA = "Senha#12345";

	protected CcpEntity entityUnderTest() {
		return JnEntityLoginPassword.ENTITY;
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
	public void transformadorDaSenha() {
		this.shouldStoreTransformed(JnJsonCommonsFields.password, SENHA);
	}

	@Test
	public void cache() {
		this.shouldServeReadFromCacheAndInvalidateOnWrite();
	}

	@Test
	public void twin() {
		this.shouldAlternateBetweenMainAndTwin("jn_login_password_locked");
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
