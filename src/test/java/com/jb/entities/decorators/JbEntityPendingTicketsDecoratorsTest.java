package com.jb.entities.decorators;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.jb.entities.JbEntityPendingTickets;
import com.jn.entities.decorators.EntityDecoratorTestTemplate;

/** jb_pending_tickets: transformer and validator, without cache. */
public class JbEntityPendingTicketsDecoratorsTest extends EntityDecoratorTestTemplate {

	protected CcpEntity entityUnderTest() {
		return JbEntityPendingTickets.ENTITY;
	}

	protected CcpJsonRepresentation validRecord() {
		return this.com(JbEntityPendingTickets.Fields.botName, "bot" + this.unique)
				.put(JbEntityPendingTickets.Fields.chatId, this.unique)
				.put(JbEntityPendingTickets.Fields.ticket, "/solveLoginTokenTicket unlockToken " + this.email)
				.put(JbEntityPendingTickets.Fields.timestamp, System.currentTimeMillis());
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
		this.shouldRefuseInvalidRecord(this.validRecord().removeFields(JbEntityPendingTickets.Fields.timestamp));
	}
}
