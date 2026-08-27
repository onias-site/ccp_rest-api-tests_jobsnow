package com.jn.business.messages;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class JnMessagesTest {

	@Test
	public void construtorTest() {
		assertNotNull(new JnMessages());
	}

	@Test
	public void notifyResendLoginTokenExistsTest() {
		assertNotNull(JnMessages.NotifySupportAboutPendingResendLoginToken.class);
	}

	@Test
	public void notifyUnlockLoginTokenExistsTest() {
		assertNotNull(JnMessages.NotifySupportAboutPendingLockedLoginToken.class);
	}
}
