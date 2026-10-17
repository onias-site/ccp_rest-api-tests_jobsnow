package com.jn.entities.decorators;

import com.ccp.especifications.db.utils.entity.decorators.EntityDecoratorTestTemplate;
import java.util.Arrays;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.jn.entities.JnEntityBotCommand;

/** jn_bot_command: {@code @CcpEntityCache(3600)}, versionable (priority 2), transformer and validator. */
public class JnEntityBotCommandDecoratorsTest extends EntityDecoratorTestTemplate {

	protected CcpEntity entityUnderTest() {
		return JnEntityBotCommand.ENTITY;
	}

	protected CcpJsonRepresentation validRecord() {
		return this.com(JnEntityBotCommand.Fields.commandName, "/comando" + this.unique)
				.put(JnEntityBotCommand.Fields.botName, "bot" + this.unique)
				.put(JnEntityBotCommand.Fields.parameterName, Arrays.asList("email"));
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
		this.shouldRefuseInvalidRecord(this.validRecord().put(JnEntityBotCommand.Fields.parameterName, Arrays.asList("")));
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
