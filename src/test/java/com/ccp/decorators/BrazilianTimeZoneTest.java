package com.ccp.decorators;

import static org.junit.Assert.assertEquals;

import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Proves that the year and the formatting of {@link CcpTimeDecorator} are in America/Sao_Paulo whatever the time zone of
 * the JVM (finding 9): until 2026-10-07 they used the time zone of the JVM, so on a server in UTC the night of December 31
 * in Brazil already counted as the next year, while the midnight was Brazilian.
 */
public class BrazilianTimeZoneTest {

	/** 2026-01-01T01:30:00Z, which is 2025-12-31 22:30 in Brazil. */
	private static final long NEW_YEAR_IN_UTC_STILL_DECEMBER_IN_BRAZIL = 1767231000000L;

	private TimeZone timeZoneOfTheJvm;

	@Before
	public void runTheJvmInUtc() {
		this.timeZoneOfTheJvm = TimeZone.getDefault();
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}

	@After
	public void restoreTheTimeZoneOfTheJvm() {
		TimeZone.setDefault(this.timeZoneOfTheJvm);
	}

	@Test
	public void theYearIsTheBrazilianOneEvenWhenTheJvmIsInUtc() {
		CcpTimeDecorator time = new CcpTimeDecorator(NEW_YEAR_IN_UTC_STILL_DECEMBER_IN_BRAZIL);

		assertEquals(2025, time.getYear());
	}

	@Test
	public void theFormattingIsTheBrazilianOneEvenWhenTheJvmIsInUtc() {
		CcpTimeDecorator time = new CcpTimeDecorator(NEW_YEAR_IN_UTC_STILL_DECEMBER_IN_BRAZIL);

		assertEquals("2025-12-31 22:30", time.getFormattedDateTime("yyyy-MM-dd HH:mm"));
	}
}
