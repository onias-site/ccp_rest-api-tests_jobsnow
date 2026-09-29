package com.jb.entities.decorators;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.jb.entities.JbEntityBotCommandExplanation;
import com.jn.entities.decorators.EntityDecoratorTestTemplate;

/** jb_bot_command_explanation: {@code @CcpEntityCache(3600)}, versionável, transformador e validador. */
public class JbEntityBotCommandExplanationDecoratorsTest extends EntityDecoratorTestTemplate {

	protected CcpEntity entityUnderTest() {
		return JbEntityBotCommandExplanation.ENTITY;
	}

	protected CcpJsonRepresentation validRecord() {
		return this.com(JbEntityBotCommandExplanation.Fields.commandName, "/comando" + this.unique)
				.put(JbEntityBotCommandExplanation.Fields.language, "portuguese")
				.put(JbEntityBotCommandExplanation.Fields.message, "explica o comando de teste");
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
		this.shouldRefuseInvalidRecord(this.validRecord().removeFields(JbEntityBotCommandExplanation.Fields.message));
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
