package com.js.business.bots;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class JsSupportBotCommandsTest {

	@Test
	public void solveLoginTokenTicketExistsTest() {
		assertNotNull(JsSupportBotCommands.solveLoginTokenTicket);
	}

	@Test
	public void valueOfTest() {
		assertEquals(JsSupportBotCommands.solveLoginTokenTicket, JsSupportBotCommands.valueOf("solveLoginTokenTicket"));
	}

	@Test
	public void portugueseNameTest() {
		assertNotNull(JsCommandNamesInPortuguese.solveLoginTokenTicket.getValue());
	}
}
