package com.jb.business.bots.engine;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class JbSupportBotCommandsTest {

	@Test
	public void pendingTicketsExistsTest() {
		assertNotNull(JbSupportBotCommands.pendingTickets);
	}

	@Test
	public void valuesTest() {
		assertNotNull(JbSupportBotCommands.values()); 
	}
 
	@Test
	public void valueOfTest() {
		assertNotNull(JbSupportBotCommands.valueOf("pendingTickets"));
	}
}
