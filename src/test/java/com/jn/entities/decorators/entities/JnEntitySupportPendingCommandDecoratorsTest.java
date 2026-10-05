package com.jn.entities.decorators.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.jn.entities.JnEntitySupportPendingCommand;
import com.jn.entities.decorators.EntityDecoratorTestTemplate;

/** jn_support_pending_command: transformer and validator, without cache; and the rules of what a command is. */
public class JnEntitySupportPendingCommandDecoratorsTest extends EntityDecoratorTestTemplate {

	protected CcpEntity entityUnderTest() {
		return JnEntitySupportPendingCommand.ENTITY;
	}

	protected CcpJsonRepresentation validRecord() {
		return this.com(JnEntitySupportPendingCommand.Fields.botName, "bot" + this.unique)
				.put(JnEntitySupportPendingCommand.Fields.chatId, this.unique)
				.put(JnEntitySupportPendingCommand.Fields.command, "/fixSkillHierarchy JVM add " + this.email)
				.put(JnEntitySupportPendingCommand.Fields.timestamp, System.currentTimeMillis());
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
		this.shouldRefuseInvalidRecord(this.validRecord().removeFields(JnEntitySupportPendingCommand.Fields.timestamp));
	}

	@Test
	public void onlyTextStartingWithSlashIsACommand() {
		assertTrue(JnEntitySupportPendingCommand.isCommand("/solveLoginTokenTicket unlockToken a@b.com"));
		assertTrue(JnEntitySupportPendingCommand.isCommand("  /pendingTickets"));
		assertFalse(JnEntitySupportPendingCommand.isCommand("Ao endereço a@b.com, envie a seguinte mensagem"));
	}

	@Test
	public void normalizedCommandHasSingleSpacesAndNoBorders() {
		assertEquals("/solveLoginTokenTicket unlockToken a@b.com", JnEntitySupportPendingCommand.normalize(" /solveLoginTokenTicket  unlockToken a@b.com \n"));
	}
}
