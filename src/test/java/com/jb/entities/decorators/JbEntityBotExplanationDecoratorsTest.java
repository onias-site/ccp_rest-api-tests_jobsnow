package com.jb.entities.decorators;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.jb.entities.JbEntityBotExplanation;
import com.jn.entities.decorators.EntityDecoratorTestTemplate;

/** jb_bot_explanation: {@code @CcpEntityCache(3600)}, transformador e validador. */
public class JbEntityBotExplanationDecoratorsTest extends EntityDecoratorTestTemplate {

	protected CcpEntity entityUnderTest() {
		return JbEntityBotExplanation.ENTITY;
	}

	protected CcpJsonRepresentation validRecord() {
		return this.com(JbEntityBotExplanation.Fields.botName, "bot" + this.unique)
				.put(JbEntityBotExplanation.Fields.language, "portuguese")
				.put(JbEntityBotExplanation.Fields.message, "explica o bot de teste");
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
		this.shouldRefuseInvalidRecord(this.validRecord().removeFields(JbEntityBotExplanation.Fields.message));
	}

	@Test
	public void cache() {
		this.shouldServeReadFromCacheAndInvalidateOnWrite();
	}
}
