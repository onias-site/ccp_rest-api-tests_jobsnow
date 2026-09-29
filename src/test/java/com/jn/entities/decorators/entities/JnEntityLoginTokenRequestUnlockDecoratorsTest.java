package com.jn.entities.decorators.entities;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityExpurgableOptions;
import com.jn.entities.JnEntityLoginTokenRequestUnlock;
import com.jn.entities.decorators.EntityDecoratorTestTemplate;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;

/**
 * jn_login_token_request_unlock: pedido de desbloqueio de token. Escrita assíncrona pela fila, aviso
 * ao suporte na inclusão (pedido pendente) e na exclusão (pedido atendido), gêmea de
 * jn_login_token_fulfilled_unlock, {@code @CcpEntityCache(3600)}, descartável diário, e-mail em claro
 * e validador.
 */
public class JnEntityLoginTokenRequestUnlockDecoratorsTest extends EntityDecoratorTestTemplate {

	protected CcpEntity entityUnderTest() {
		return JnEntityLoginTokenRequestUnlock.ENTITY;
	}

	protected CcpJsonRepresentation validRecord() {
		return this.com(JnJsonCommonsFields.email, this.email)
				.put(JnJsonInstantMessengerFields.chatId, 751717896L);
	}

	@Test
	public void chain() {
		this.shouldHaveChain("DecoratorFieldsValidatorEntity", "JnAsyncWriterEntity", "JnSendMessageToUserEntityBeforeWrite", "DecoratorFieldsTransformerEntity", "JnSendMessageToUserEntityAfterWrite", "DecoratorTwinEntity", "DecoratorCacheEntity", "JnDisposableEntity", "DefaultImplementationEntity");
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
		this.shouldSaveOnMainAndTransferToTwinOnDelete("jn_login_token_fulfilled_unlock");
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
