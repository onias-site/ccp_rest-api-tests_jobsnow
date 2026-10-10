package com.jn.entities.decorators.entities;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.jn.entities.JnEntitySupportCancelledCommand;
import com.jn.entities.decorators.EntityDecoratorTestTemplate;

/** jn_support_cancelled_command: transformer and validator, without cache, as the inbox it cancels. */
public class JnEntitySupportCancelledCommandDecoratorsTest extends EntityDecoratorTestTemplate {

	protected CcpEntity entityUnderTest() {
		return JnEntitySupportCancelledCommand.ENTITY;
	}

	protected CcpJsonRepresentation validRecord() {
		return this.com(JnEntitySupportCancelledCommand.Fields.botName, "bot" + this.unique)
				.put(JnEntitySupportCancelledCommand.Fields.chatId, this.unique)
				.put(JnEntitySupportCancelledCommand.Fields.command, "/reviewSkillSuggestion " + this.email + " PICK BY LIGHT")
				.put(JnEntitySupportCancelledCommand.Fields.timestamp, System.currentTimeMillis());
	}

	@Test
	public void chain() {
		this.shouldHaveChain("DecoratorFieldsValidatorEntity", "DecoratorFieldsTransformerEntity", "DefaultImplementationEntity");
	}

	@Test
	public void saveReadAndDelete() {
		this.shouldSaveReadAndDelete();
	}

	@Test
	public void validator() {
		this.shouldRefuseInvalidRecord(this.validRecord().removeFields(JnEntitySupportCancelledCommand.Fields.timestamp));
	}
}
