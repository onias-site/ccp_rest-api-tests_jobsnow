package com.jb.entities.decorators;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.jb.entities.JbEntityBotCommandStepExplanation;
import com.jn.entities.decorators.EntityDecoratorTestTemplate;

/** jb_bot_command_step_explanation: {@code @CcpEntityCache(3600)}, transformador e validador. */
public class JbEntityBotCommandStepExplanationDecoratorsTest extends EntityDecoratorTestTemplate {

	protected CcpEntity entityUnderTest() {
		return JbEntityBotCommandStepExplanation.ENTITY;
	}

	protected CcpJsonRepresentation validRecord() {
		return this.com(JbEntityBotCommandStepExplanation.Fields.stepName, "passo" + this.unique)
				.put(JbEntityBotCommandStepExplanation.Fields.language, "portuguese")
				.put(JbEntityBotCommandStepExplanation.Fields.message, "explica o passo de teste");
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
		this.shouldRefuseInvalidRecord(this.validRecord().put(JbEntityBotCommandStepExplanation.Fields.language, "klingon"));
	}

	@Test
	public void cache() {
		this.shouldServeReadFromCacheAndInvalidateOnWrite();
	}
}
