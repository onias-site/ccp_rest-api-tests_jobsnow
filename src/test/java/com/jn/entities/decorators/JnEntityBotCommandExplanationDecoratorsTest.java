package com.jn.entities.decorators;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.jn.entities.JnEntityBotCommandExplanation;

/** jn_bot_command_explanation: {@code @CcpEntityCache(3600)}, versionable, transformer and validator. */
public class JnEntityBotCommandExplanationDecoratorsTest extends EntityDecoratorTestTemplate {

	protected CcpEntity entityUnderTest() {
		return JnEntityBotCommandExplanation.ENTITY;
	}

	protected CcpJsonRepresentation validRecord() {
		return this.com(JnEntityBotCommandExplanation.Fields.commandName, "/comando" + this.unique)
				.put(JnEntityBotCommandExplanation.Fields.language, "portuguese")
				.put(JnEntityBotCommandExplanation.Fields.message, "explica o comando de teste");
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
		this.shouldRefuseInvalidRecord(this.validRecord().removeFields(JnEntityBotCommandExplanation.Fields.message));
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
