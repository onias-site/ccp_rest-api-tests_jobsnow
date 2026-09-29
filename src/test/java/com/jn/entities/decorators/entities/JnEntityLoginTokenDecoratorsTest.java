package com.jn.entities.decorators.entities;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityExpurgableOptions;
import com.jn.entities.JnEntityLoginToken;
import com.jn.entities.decorators.EntityDecoratorTestTemplate;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/**
 * jn_login_token: a entidade mais decorada do jn. Escrita assíncrona pela fila, operação
 * {@code beforeSave} ({@code JnBusinessPrepareLoginTokenBeforeSave}), e-mail com o token ao usuário só
 * na inclusão, gêmea de jn_login_token_locked (token bloqueado), {@code @CcpEntityCache(86400)},
 * descartável mensal, transformador (hash do e-mail, token gerado e guardado em BCrypt) e validador.
 */
public class JnEntityLoginTokenDecoratorsTest extends EntityDecoratorTestTemplate {

	protected CcpEntity entityUnderTest() {
		return JnEntityLoginToken.ENTITY;
	}

	protected CcpJsonRepresentation validRecord() {
		return this.com(JnJsonCommonsFields.email, this.email);
	}

	@Test
	public void chain() {
		this.shouldHaveChain("DecoratorFieldsValidatorEntity", "JnAsyncWriterEntity", "JnSendMessageToUserEntityBeforeWrite", "DecoratorBeforeOperationsWriterEntity", "DecoratorFieldsTransformerEntity", "JnSendMessageToUserEntityAfterWrite", "DecoratorAfterOperationsWriterEntity", "DecoratorTwinEntity", "DecoratorCacheEntity", "JnDisposableEntity", "DefaultImplementationEntity");
	}

	@Test
	public void saveReadAndDelete() {
		this.shouldSaveReadAndDelete();
	}

	@Test
	public void validator() {
		this.shouldRefuseInvalidRecord(this.com(JnJsonCommonsFields.email, "isto-nao-e-email"));
	}

	@Test
	public void asyncWrite() {
		this.shouldSaveThroughQueue();
	}

	@Test
	public void emailComTokenSoNaInclusao() {
		this.shouldSendEmailOnlyOnInsert();
	}

	@Test
	public void emailTransformer() {
		this.shouldStoreEmailAsHash();
	}

	/** O token é gerado na gravação e só chega ao banco como BCrypt. */
	@Test
	public void tokenTransformer() {
		CcpJsonRepresentation registro = this.validRecord();
		this.entityUnderTest().save(registro);
		CcpJsonRepresentation gravado = this.asStored(registro);
		String token = gravado.getAsString(JnEntityLoginToken.Fields.token);
		assertTrue("token de jn_login_token nao foi gravado como BCrypt: " + token, token.startsWith("$2"));
	}

	@Test
	public void cache() {
		this.shouldServeReadFromCacheAndInvalidateOnWrite();
	}

	@Test
	public void twin() {
		this.shouldSaveOnMainAndTransferToTwinOnDelete("jn_login_token_locked");
	}

	@Test
	public void redirectToTwin() {
		this.shouldRedirectOnGetOneByIdWhenRecordMovedToTwin();
	}

	@Test
	public void disposable() {
		this.shouldKeepDisposableCopyUntilDeadline(CcpEntityExpurgableOptions.monthly);
	}
}
