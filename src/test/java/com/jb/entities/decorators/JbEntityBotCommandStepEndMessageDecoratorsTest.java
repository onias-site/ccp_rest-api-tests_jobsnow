package com.jb.entities.decorators;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.jb.entities.JbEntityBotCommandStepEndMessage;
import com.jn.entities.decorators.EntityDecoratorTestTemplate;

/** jb_bot_command_step_end_message: {@code @CcpEntityCache(3600)}, versionável, transformador e validador. */
public class JbEntityBotCommandStepEndMessageDecoratorsTest extends EntityDecoratorTestTemplate {

	protected CcpEntity entityUnderTest() {
		return JbEntityBotCommandStepEndMessage.ENTITY;
	}

	protected CcpJsonRepresentation validRecord() {
		return this.com(JbEntityBotCommandStepEndMessage.Fields.stepName, "passo" + this.unique)
				.put(JbEntityBotCommandStepEndMessage.Fields.language, "portuguese")
				.put(JbEntityBotCommandStepEndMessage.Fields.message, "fim do passo de teste")
				.put(JbEntityBotCommandStepEndMessage.Fields.instantMessageType, "text");
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
		this.shouldRefuseInvalidRecord(this.validRecord().put(JbEntityBotCommandStepEndMessage.Fields.instantMessageType, "sinal-de-fumaca"));
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
