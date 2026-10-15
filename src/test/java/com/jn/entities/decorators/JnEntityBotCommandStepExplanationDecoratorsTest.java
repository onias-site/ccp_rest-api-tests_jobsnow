package com.jn.entities.decorators;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.jn.entities.JnEntityBotCommandStepExplanation;
import com.jn.entities.decorators.EntityDecoratorTestTemplate;

/** jn_bot_command_step_explanation: {@code @CcpEntityCache(3600)}, transformador e validador. */
public class JnEntityBotCommandStepExplanationDecoratorsTest extends EntityDecoratorTestTemplate {

	protected CcpEntity entityUnderTest() {
		return JnEntityBotCommandStepExplanation.ENTITY;
	}

	protected CcpJsonRepresentation validRecord() {
		return this.com(JnEntityBotCommandStepExplanation.Fields.stepName, "passo" + this.unique)
				.put(JnEntityBotCommandStepExplanation.Fields.language, "portuguese")
				.put(JnEntityBotCommandStepExplanation.Fields.message, "explica o passo de teste");
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
		this.shouldRefuseInvalidRecord(this.validRecord().put(JnEntityBotCommandStepExplanation.Fields.language, "klingon"));
	}

	@Test
	public void cache() {
		this.shouldServeReadFromCacheAndInvalidateOnWrite();
	}
}
