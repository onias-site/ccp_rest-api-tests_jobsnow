package com.jb.entities.decorators;

import java.util.Arrays;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.jb.entities.JbEntityBot;
import com.jn.entities.decorators.EntityDecoratorTestTemplate;

/** jb_bot: {@code @CcpEntityCache(3600)}, transformador e validador. */
public class JbEntityBotDecoratorsTest extends EntityDecoratorTestTemplate {

	protected CcpEntity entityUnderTest() {
		return JbEntityBot.ENTITY;
	}

	protected CcpJsonRepresentation validRecord() {
		return this.com(JbEntityBot.Fields.botName, "bot" + this.unique)
				.put(JbEntityBot.Fields.commandName, Arrays.asList("/start"));
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
		this.shouldRefuseInvalidRecord(this.validRecord().put(JbEntityBot.Fields.commandName, Arrays.asList()));
	}

	@Test
	public void cache() {
		this.shouldServeReadFromCacheAndInvalidateOnWrite();
	}
}
