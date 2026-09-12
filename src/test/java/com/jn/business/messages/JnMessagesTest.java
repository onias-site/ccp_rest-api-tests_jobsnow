package com.jn.business.messages;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class JnMessagesTest {

	@Test
	public void notifyResendLoginTokenExistsTest() {
		assertNotNull(JnNotifySupportAboutPendingResendLoginToken.class);
	}

	@Test
	public void notifyUnlockLoginTokenExistsTest() {
		assertNotNull(JnNotifySupportAboutPendingResendLoginToken.class);
	}
}
