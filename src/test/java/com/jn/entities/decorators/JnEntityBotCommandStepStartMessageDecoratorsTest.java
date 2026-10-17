package com.jn.entities.decorators;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.jn.entities.JnEntityBotCommandStepStartMessage;

/** jn_bot_command_step_start_message: {@code @CcpEntityCache(3600)}, versionable, transformer and validator. */
public class JnEntityBotCommandStepStartMessageDecoratorsTest extends EntityDecoratorTestTemplate {

	protected CcpEntity entityUnderTest() {
		return JnEntityBotCommandStepStartMessage.ENTITY;
	}

	protected CcpJsonRepresentation validRecord() {
		return this.com(JnEntityBotCommandStepStartMessage.Fields.stepName, "passo" + this.unique)
				.put(JnEntityBotCommandStepStartMessage.Fields.language, "portuguese")
				.put(JnEntityBotCommandStepStartMessage.Fields.message, "inicio do passo de teste")
				.put(JnEntityBotCommandStepStartMessage.Fields.instantMessageType, "text");
	}

	@Test
	public void chain() {
		this.shouldHaveChain("DecoratorFieldsValidatorEntity", "DecoratorFieldsTransformerEntity", "JnVersionablePurgeEntity", "DecoratorCacheEntity", "JnVersionableEntity", "DefaultImplementationEntity");
	}

	@Test
	public void saveReadAndDelete() {
		this.shouldSaveReadAndDelete();
	}

	@Test
	public void validator() {
		this.shouldRefuseInvalidRecord(this.validRecord().removeFields(JnEntityBotCommandStepStartMessage.Fields.instantMessageType));
	}

	@Test
	public void cache() {
		this.shouldServeReadFromCacheAndInvalidateOnWrite();
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
