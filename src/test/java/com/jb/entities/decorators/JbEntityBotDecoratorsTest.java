package com.jb.entities.decorators;

import java.util.Arrays;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.jb.entities.JbEntityBot;
import com.ccp.especifications.db.utils.entity.decorators.EntityDecoratorTestTemplate;

/** jb_bot: {@code @CcpEntityCache(3600)}, transformador e validador. */
public class JbEntityBotDecoratorsTest extends EntityDecoratorTestTemplate {

	protected CcpEntity entityUnderTest() {
		return JbEntityBot.ENTITY;
	}

	protected CcpJsonRepresentation validRecord() {
		return this.com(JbEntityBot.Fields.botName, "bot" + this.unique);
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
		// since 2026-10-15 the bot has only its name (the commands are found by botName in jn_bot_command): a name that
		// is not a text is the invalid record
		this.shouldRefuseInvalidRecord(this.validRecord().put(JbEntityBot.Fields.botName, Arrays.asList("bot")));
	}

	@Test
	public void cache() {
		this.shouldServeReadFromCacheAndInvalidateOnWrite();
	}
}
