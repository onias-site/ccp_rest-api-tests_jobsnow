package com.jn.entities.decorators.entities;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.jn.entities.JnEntityInstantMessengerBotLocked;
import com.jn.entities.decorators.EntityDecoratorTestTemplate;

/** jn_instant_messenger_bot_locked: {@code @CcpEntityCache(3600)}, transformador e validador. */
public class JnEntityInstantMessengerBotLockedDecoratorsTest extends EntityDecoratorTestTemplate {

	protected CcpEntity entityUnderTest() {
		return JnEntityInstantMessengerBotLocked.ENTITY;
	}

	protected CcpJsonRepresentation validRecord() {
		return this.com(JnEntityInstantMessengerBotLocked.Fields.botName, "bot" + this.unique)
				.put(JnEntityInstantMessengerBotLocked.Fields.chatId, this.unique)
				.put(JnEntityInstantMessengerBotLocked.Fields.subjectType, "bloqueio");
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
		this.shouldRefuseInvalidRecord(this.validRecord().removeFields(JnEntityInstantMessengerBotLocked.Fields.subjectType));
	}

	/** Until 2026-09-27 it copied the rule of JnJsonInstantMessengerFields, which does not declare subjectType. */
	@Test
	public void subjectTypeValidator() {
		String longoDemais = "x".repeat(101);
		this.shouldRefuseInvalidRecord(this.validRecord().put(JnEntityInstantMessengerBotLocked.Fields.subjectType, longoDemais));
	}

	@Test
	public void cache() {
		this.shouldServeReadFromCacheAndInvalidateOnWrite();
	}
}
