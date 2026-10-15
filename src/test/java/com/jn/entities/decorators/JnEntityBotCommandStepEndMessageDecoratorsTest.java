package com.jn.entities.decorators;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.jn.entities.JnEntityBotCommandStepEndMessage;
import com.jn.entities.decorators.EntityDecoratorTestTemplate;

/** jn_bot_command_step_end_message: {@code @CcpEntityCache(3600)}, versionable, transformer and validator. */
public class JnEntityBotCommandStepEndMessageDecoratorsTest extends EntityDecoratorTestTemplate {

	protected CcpEntity entityUnderTest() {
		return JnEntityBotCommandStepEndMessage.ENTITY;
	}

	protected CcpJsonRepresentation validRecord() {
		return this.com(JnEntityBotCommandStepEndMessage.Fields.stepName, "passo" + this.unique)
				.put(JnEntityBotCommandStepEndMessage.Fields.language, "portuguese")
				.put(JnEntityBotCommandStepEndMessage.Fields.message, "fim do passo de teste")
				.put(JnEntityBotCommandStepEndMessage.Fields.instantMessageType, "text");
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
		this.shouldRefuseInvalidRecord(this.validRecord().put(JnEntityBotCommandStepEndMessage.Fields.instantMessageType, "sinal-de-fumaca"));
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
