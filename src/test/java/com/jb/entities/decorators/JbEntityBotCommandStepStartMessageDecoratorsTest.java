package com.jb.entities.decorators;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.jb.entities.JbEntityBotCommandStepStartMessage;
import com.jn.entities.decorators.EntityDecoratorTestTemplate;

/** jb_bot_command_step_start_message: {@code @CcpEntityCache(3600)}, versionável, transformador e validador. */
public class JbEntityBotCommandStepStartMessageDecoratorsTest extends EntityDecoratorTestTemplate {

	protected CcpEntity entityUnderTest() {
		return JbEntityBotCommandStepStartMessage.ENTITY;
	}

	protected CcpJsonRepresentation validRecord() {
		return this.com(JbEntityBotCommandStepStartMessage.Fields.stepName, "passo" + this.unique)
				.put(JbEntityBotCommandStepStartMessage.Fields.language, "portuguese")
				.put(JbEntityBotCommandStepStartMessage.Fields.message, "inicio do passo de teste")
				.put(JbEntityBotCommandStepStartMessage.Fields.instantMessageType, "text");
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
		this.shouldRefuseInvalidRecord(this.validRecord().removeFields(JbEntityBotCommandStepStartMessage.Fields.instantMessageType));
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
