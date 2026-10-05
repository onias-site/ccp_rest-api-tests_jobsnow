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
 * jn_login_token: the most decorated entity of jn. Asynchronous writing through the queue, {@code beforeSave}
 * operation ({@code JnBusinessPrepareLoginTokenBeforeSave}), e-mail with the token to the user only on insert,
 * twin of jn_login_token_locked (locked token), {@code @CcpEntityCache(86400)}, monthly disposable, transformer
 * (hash of the e-mail, token generated and kept as BCrypt) and validator.
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
	public void emailWithTokenOnlyOnInsert() {
		this.shouldSendEmailOnlyOnInsert();
	}

	@Test
	public void emailTransformer() {
		this.shouldStoreEmailAsHash();
	}

	/** The token is generated on the save and only reaches the database as BCrypt. */
	@Test
	public void tokenTransformer() {
		CcpJsonRepresentation record = this.validRecord();
		this.entityUnderTest().save(record);
		CcpJsonRepresentation stored = this.asStored(record);
		String token = stored.getAsString(JnEntityLoginToken.Fields.token);
		assertTrue("the token of jn_login_token was not stored as BCrypt: " + token, token.startsWith("$2"));
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
