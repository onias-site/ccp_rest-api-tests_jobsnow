package com.js.entities.decorators.entities;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityExpurgableOptions;
import com.js.entities.JsEntityLoginTokenRequestUnlock;
import com.ccp.especifications.db.utils.entity.decorators.EntityDecoratorTestTemplate;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;

/**
 * js_login_token_request_unlock: request to unlock the token. Asynchronous writing through the queue, notice to
 * the support on insert (pending request) and on delete (fulfilled request), twin of
 * js_login_token_fulfilled_unlock, {@code @CcpEntityCache(3600)}, daily disposable, e-mail in plain text and
 * validator.
 */
public class JsEntityLoginTokenRequestUnlockDecoratorsTest extends EntityDecoratorTestTemplate {

	protected CcpEntity entityUnderTest() {
		return JsEntityLoginTokenRequestUnlock.ENTITY;
	}

	protected CcpJsonRepresentation validRecord() {
		return this.com(JnJsonCommonsFields.email, this.email)
				.put(JnJsonInstantMessengerFields.chatId, 751717896L);
	}

	@Test
	public void chain() {
		this.shouldHaveChain("DecoratorFieldsValidatorEntity", "JnAsyncWriterEntity", "DecoratorFieldsTransformerEntity", "DecoratorWriteSideEffectEntity", "DecoratorWriteSideEffectEntity", "DecoratorTwinEntity", "DecoratorCacheEntity", "JnDisposableEntity", "DefaultImplementationEntity");
	}

	@Test
	public void saveReadAndDelete() {
		this.shouldSaveReadAndDelete();
	}

	@Test
	public void validator() {
		this.shouldRefuseInvalidRecord(this.validRecord().put(JnJsonCommonsFields.email, "isto-nao-e-email"));
	}

	@Test
	public void asyncWrite() {
		this.shouldSaveThroughQueue();
	}

	@Test
	public void supportNoticeOnlyOnInsert() {
		this.shouldNotifyByTelegramOnlyOnInsert();
	}

	@Test
	public void supportNoticeOnFulfillment() {
		this.shouldNotifyByTelegramOnDelete();
	}

	@Test
	public void transformer() {
		this.shouldStoreEmailInPlainText();
	}

	@Test
	public void cache() {
		this.shouldServeReadFromCacheAndInvalidateOnWrite();
	}

	@Test
	public void twin() {
		this.shouldSaveOnMainAndTransferToTwinOnDelete("js_login_token_fulfilled_unlock");
	}

	@Test
	public void redirectToTwin() {
		this.shouldRedirectOnGetOneByIdWhenRecordMovedToTwin();
	}

	@Test
	public void disposable() {
		this.shouldKeepDisposableCopyUntilDeadline(CcpEntityExpurgableOptions.daily);
	}
}
