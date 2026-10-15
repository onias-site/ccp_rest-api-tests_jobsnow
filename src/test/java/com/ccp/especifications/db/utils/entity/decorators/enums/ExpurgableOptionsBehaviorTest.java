package com.ccp.especifications.db.utils.entity.decorators.enums;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.junit.Test;

/**
 * Proves the expiration granularities ({@link CcpEntityExpurgableOptions}): the format of each period, the length of
 * the variable periods (month and year) and the next period after a moment.
 */
public class ExpurgableOptionsBehaviorTest {

	private static final long ONE_DAY = 86_400_000L;

	/** 2024-02-10 12:00 in the local time zone: February of a leap year. */
	private final long leapFebruary = new GregorianCalendar(2024, 1, 10, 12, 0).getTimeInMillis();

	@Test
	public void eachGranularityFormatsItsPeriod() {
		assertEquals("2024", CcpEntityExpurgableOptions.yearly.getFormattedDate(this.leapFebruary));
		assertEquals("202402", CcpEntityExpurgableOptions.monthly.getFormattedDate(this.leapFebruary));
		assertEquals("10022024", CcpEntityExpurgableOptions.daily.getFormattedDate(this.leapFebruary));
		assertEquals("10022024 12", CcpEntityExpurgableOptions.hourly.getFormattedDate(this.leapFebruary));
		assertEquals("10022024 12:00", CcpEntityExpurgableOptions.minute.getFormattedDate(this.leapFebruary));
	}

	@Test
	public void theCurrentPeriodIsFormattedTheSameWay() {
		String now = CcpEntityExpurgableOptions.daily.getFormattedDate();

		assertEquals(CcpEntityExpurgableOptions.daily.getFormattedDate(System.currentTimeMillis()), now);
	}

	@Test
	public void monthsAndYearsHaveTheLengthOfTheirOwnCalendar() {
		assertEquals(29 * ONE_DAY, CcpEntityExpurgableOptions.monthly.getMilliseconds(this.leapFebruary));
		assertEquals(366 * ONE_DAY, CcpEntityExpurgableOptions.yearly.getMilliseconds(this.leapFebruary));
		assertEquals(ONE_DAY, CcpEntityExpurgableOptions.daily.getMilliseconds(this.leapFebruary));
		assertEquals(60_000L, CcpEntityExpurgableOptions.minute.getMilliseconds(this.leapFebruary));
	}

	@Test
	public void theNextPeriodIsOneUnitAfterNow() {
		long before = System.currentTimeMillis();

		long next = CcpEntityExpurgableOptions.daily.getNextTimeStamp();

		assertTrue(next >= before + ONE_DAY);
		assertTrue(next <= System.currentTimeMillis() + ONE_DAY);
		assertTrue(CcpEntityExpurgableOptions.daily.getNextDate().matches("\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}:\\d{2}\\.\\d{3}"));
	}

	/**
	 * Every granularity expires a record exactly one unit after its write, never at the turn of the period: written at
	 * 19:59:17.250, hourly lives until 20:59:17.250 (and not until 20:00).
	 */
	@Test
	public void eachGranularityExpiresOneUnitAfterTheWriteNotAtTheTurnOfThePeriod() {
		TimeZone saoPaulo = TimeZone.getTimeZone("America/Sao_Paulo");
		GregorianCalendar writtenAt = new GregorianCalendar(saoPaulo);
		writtenAt.clear();
		writtenAt.set(2026, Calendar.OCTOBER, 14, 19, 59, 17);
		writtenAt.set(Calendar.MILLISECOND, 250);
		long write = writtenAt.getTimeInMillis();

		assertEquals("14/10/2027 19:59:17.250", CcpEntityExpurgableOptions.yearly.getNextDate(write));
		assertEquals("14/11/2026 19:59:17.250", CcpEntityExpurgableOptions.monthly.getNextDate(write));
		assertEquals("15/10/2026 19:59:17.250", CcpEntityExpurgableOptions.daily.getNextDate(write));
		assertEquals("14/10/2026 20:59:17.250", CcpEntityExpurgableOptions.hourly.getNextDate(write));
		assertEquals("14/10/2026 20:00:17.250", CcpEntityExpurgableOptions.minute.getNextDate(write));
		assertEquals("14/10/2026 19:59:18.250", CcpEntityExpurgableOptions.second.getNextDate(write));
		assertEquals("14/10/2026 19:59:17.251", CcpEntityExpurgableOptions.millisecond.getNextDate(write));

		assertEquals(write + 3_600_000L, (long) CcpEntityExpurgableOptions.hourly.getNextTimeStamp(write));
		assertEquals(write + 60_000L, (long) CcpEntityExpurgableOptions.minute.getNextTimeStamp(write));
		assertEquals(write + 1_000L, (long) CcpEntityExpurgableOptions.second.getNextTimeStamp(write));
		assertEquals(write + 1L, (long) CcpEntityExpurgableOptions.millisecond.getNextTimeStamp(write));
	}

	@Test
	public void theNextPeriodAfterAMomentStartsFromTheMoment() {
		assertEquals(this.leapFebruary + ONE_DAY, (long) CcpEntityExpurgableOptions.daily.getNextTimeStamp(this.leapFebruary));
		assertEquals("11/02/2024 12:00:00.000", CcpEntityExpurgableOptions.daily.getNextDate(this.leapFebruary));
	}
}
