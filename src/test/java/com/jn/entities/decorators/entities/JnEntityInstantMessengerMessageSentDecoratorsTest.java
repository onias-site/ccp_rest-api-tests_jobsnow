package com.jn.entities.decorators.entities;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityExpurgableOptions;
import com.jn.entities.JnEntityInstantMessengerMessageSent;
import com.jn.entities.decorators.EntityDecoratorTestTemplate;

/**
 * jn_instant_messenger_message_sent: {@code @CcpEntityCache(3600)}, descartável por hora, transformador
 * (hash da mensagem, que faz parte da chave primária) e validador. É o que impede repetir a mesma
 * mensagem no mesmo chat dentro da hora.
 */
public class JnEntityInstantMessengerMessageSentDecoratorsTest extends EntityDecoratorTestTemplate {

	private final String message = "mensagem de teste " + this.unique;

	protected CcpEntity entityUnderTest() {
		return JnEntityInstantMessengerMessageSent.ENTITY;
	}

	protected CcpJsonRepresentation validRecord() {
		return this.com(JnEntityInstantMessengerMessageSent.Fields.botName, "bot" + this.unique)
				.put(JnEntityInstantMessengerMessageSent.Fields.chatId, this.unique)
				.put(JnEntityInstantMessengerMessageSent.Fields.instantMessageType, "text")
				.put(JnEntityInstantMessengerMessageSent.Fields.message, this.message);
	}

	@Test
	public void chain() {
		this.shouldHaveChain("DecoratorFieldsValidatorEntity", "DecoratorFieldsTransformerEntity", "DecoratorCacheEntity", "JnDisposableEntity", "DefaultImplementationEntity");
	}

	@Test
	public void saveReadAndDelete() {
		this.shouldSaveReadAndDelete();
	}

	@Test
	public void validator() {
		this.shouldRefuseInvalidRecord(this.validRecord().removeFields(JnEntityInstantMessengerMessageSent.Fields.instantMessageType));
	}

	@Test
	public void transformer() {
		this.shouldStoreTransformed(JnEntityInstantMessengerMessageSent.Fields.message, this.message);
	}

	@Test
	public void cache() {
		this.shouldServeReadFromCacheAndInvalidateOnWrite();
	}

	@Test
	public void disposable() {
		this.shouldKeepDisposableCopyUntilDeadline(CcpEntityExpurgableOptions.hourly);
	}
}
