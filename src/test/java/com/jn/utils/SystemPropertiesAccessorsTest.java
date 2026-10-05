package com.jn.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

/**
 * Proves that each typed accessor of {@link JnSystemProperties} reads its own key of the system properties.
 */
public class SystemPropertiesAccessorsTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	private final JnSystemProperties properties = JnSystemProperties.INSTANCE;

	private final CcpJsonRepresentation raw = JnSystemProperties.INSTANCE.systemProperties;

	@Test
	public void eachTextAccessorReadsItsKey() {
		assertEquals(this.raw.getAsString(Fields.databaseAddress), this.properties.databaseAddress());
		assertEquals(this.raw.getAsString(Fields.databaseSecret), this.properties.databaseSecret());
		assertEquals(this.raw.getAsString(Fields.tokenInstantMessengerKey), this.properties.tokenInstantMessengerKey());
		assertEquals(this.raw.getAsString(Fields.urlInstantMessengerKey), this.properties.urlInstantMessengerKey());
		assertEquals(this.raw.getAsString(Fields.tokenEmailKey), this.properties.tokenEmailValue());
		assertEquals(this.raw.getAsString(Fields.urlEmailKey), this.properties.urlEmailValue());
		assertEquals(this.raw.getAsString(Fields.supportLanguage), this.properties.supportLanguage());
	}

	@Test
	public void theDottedKeysAreReadAsTheyAreWritten() {
		assertEquals("database.address", Fields.databaseAddress.getValue());
		assertEquals("database.secret", Fields.databaseSecret.getValue());
	}

	@Test
	public void theOtherAccessorsReadTheirKeys() {
		assertEquals(this.raw.getAsBoolean(Fields.localEnvironment), this.properties.localEnvironment());
		assertEquals(this.raw.getAsStringList(Fields.systems), this.properties.systems());
		Object byName = this.properties.getSystemProperty("supportLanguage");
		Object byField = this.properties.getSystemProperty(Fields.supportLanguage);
		assertEquals(byField, byName);
	}

	@Test
	public void aNestedJsonIsReadByItsPath() {
		CcpJsonRepresentation bots = this.properties.getSystemInnerJson("bots");

		assertEquals(this.raw.getInnerJson(new CcpFieldName("bots")), bots);
	}

	@Test
	public void maxAttemptsIsThreeWhenNotConfigured() {
		boolean configured = this.raw.containsField(Fields.maxAttempts);
		int expected = configured ? this.raw.getAsIntegerNumber(Fields.maxAttempts) : 3;

		assertEquals(expected, this.properties.maxAttempts());
	}
}
