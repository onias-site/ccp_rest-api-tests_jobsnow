package com.vis.business.recruiter.resumes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

/**
 * The signed links of the e-mail of resumes ({@link VisRecruiterLinks}) beyond what {@code ResumesToRecruitersTest}
 * already proves: the token is stable, it changes with each value and with the order of the values, and an empty or
 * altered token never matches. Needs {@code recruiterLinksSecret} in {@code application_properties}.
 */
public class VisRecruiterLinksTest {

	static {
		// the system properties, where the secret is, are parsed as json
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	private static final String RECRUITER = "recrutador@empresa.com.br";

	@Test
	public void theSameValuesAlwaysGiveTheSameToken() {
		assertEquals(VisRecruiterLinks.view.sign(RECRUITER, "resume1"), VisRecruiterLinks.view.sign(RECRUITER, "resume1"));
	}

	@Test
	public void theOrderOfTheValuesIsPartOfTheSignature() {
		assertNotEquals(VisRecruiterLinks.view.sign(RECRUITER, "resume1"), VisRecruiterLinks.view.sign("resume1", RECRUITER));
	}

	@Test
	public void aTokenOfOnePurposeDoesNotOpenTheOther() {
		String unsubscribeToken = VisRecruiterLinks.unsubscribe.sign(RECRUITER);

		assertFalse(VisRecruiterLinks.view.matches(unsubscribeToken, RECRUITER));
		assertTrue(VisRecruiterLinks.unsubscribe.matches(unsubscribeToken, RECRUITER));
	}

	@Test
	public void anEmptyOrAlteredTokenNeverMatches() {
		String token = VisRecruiterLinks.unsubscribe.sign(RECRUITER);
		String altered = token.substring(0, token.length() - 1) + (token.endsWith("0") ? "1" : "0");

		assertFalse(VisRecruiterLinks.unsubscribe.matches("", RECRUITER));
		assertFalse(VisRecruiterLinks.unsubscribe.matches(altered, RECRUITER));
	}

	@Test
	public void theNamesOfThePurposesAreTheValuesOfTheItems() {
		assertEquals("view", VisRecruiterLinks.view.getValue());
		assertEquals("unsubscribe", VisRecruiterLinks.unsubscribe.getValue());
	}
}
