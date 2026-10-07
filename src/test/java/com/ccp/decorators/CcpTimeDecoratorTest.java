package com.ccp.decorators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.Calendar;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.aop.CcpNullReturnException;

public class CcpTimeDecoratorTest {

	@Test
	public void constructorWithoutArgumentUsesCurrentTimeTest() {
		long before = System.currentTimeMillis();
		CcpTimeDecorator t = new CcpTimeDecorator();
		long after = System.currentTimeMillis();
		assertTrue(t.content >= before);
		assertTrue(t.content <= after);
	}

	@Test
	public void constructorWithLongTest() {
		long time = 1_000_000L;
		CcpTimeDecorator t = new CcpTimeDecorator(time);
		assertEquals(time, (long) t.content);
	}

	@Test
	public void getContentTest() {
		long time = System.currentTimeMillis();
		CcpTimeDecorator t = new CcpTimeDecorator(time);
		assertEquals(time, (long) t.getContent());
	}

	@Test
	public void getMidnightIsBeforeNowTest() {
		CcpTimeDecorator t = new CcpTimeDecorator();
		long midnight = t.getMidnight();
		assertTrue(midnight <= t.content);
	}

	@Test
	public void getMidnightIsMidnightTest() {
		CcpTimeDecorator t = new CcpTimeDecorator();
		long midnight = t.getMidnight();
		Calendar cal = t.getBrazilianCalendar();
		cal.setTimeInMillis(midnight);
		assertEquals(0, cal.get(Calendar.HOUR_OF_DAY));
		assertEquals(0, cal.get(Calendar.MINUTE));
		assertEquals(0, cal.get(Calendar.SECOND));
		assertEquals(0, cal.get(Calendar.MILLISECOND));
	}

	@Test
	public void getSecondsEnlapsedSinceMidnightTest() {
		CcpTimeDecorator t = new CcpTimeDecorator();
		long segundos = t.getSecondsEnlapsedSinceMidnight();
		assertTrue(segundos >= 0);
		assertTrue(segundos < 86400); // menos de 24h em segundos
	}

	@Test
	public void getYearReturnsCurrentYearTest() {
		int expectedYear = Calendar.getInstance().get(Calendar.YEAR);
		CcpTimeDecorator t = new CcpTimeDecorator();
		assertEquals(expectedYear, t.getYear());
	}

	@Test
	public void getFormattedDateTimeTest() {
		CcpTimeDecorator t = new CcpTimeDecorator();
		String formatado = t.getFormattedDateTime("yyyy");
		String currentYear = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
		assertEquals(currentYear, formatado);
	}

	@Test
	public void getFormattedDateTimeDayMonthYearFormatTest() {
		CcpTimeDecorator t = new CcpTimeDecorator();
		String formatado = t.getFormattedDateTime("dd/MM/yyyy");
		assertTrue(formatado.matches("\\d{2}/\\d{2}/\\d{4}"));
	}

	@Test
	public void getBrazilianCalendarNotNullTest() {
		CcpTimeDecorator t = new CcpTimeDecorator();
		Calendar cal = t.getBrazilianCalendar();
		assertNotNull(cal);
		assertEquals("America/Sao_Paulo", cal.getTimeZone().getID());
	}

	@Test
	public void theBrazilianCalendarStartsFromTheWrappedTimestampTest() {
		long aMomentInThePast = 1_000_000_000_000L;
		CcpTimeDecorator t = new CcpTimeDecorator(aMomentInThePast);
		Calendar cal = t.getBrazilianCalendar();
		assertEquals(aMomentInThePast, cal.getTimeInMillis());
	}

	@Test
	public void theMidnightIsTheOneOfTheWrappedDayTest() {
		long aMomentInThePast = 1_000_000_000_000L;
		CcpTimeDecorator t = new CcpTimeDecorator(aMomentInThePast);
		long midnight = t.getMidnight();
		assertTrue(midnight <= aMomentInThePast);
		assertTrue(aMomentInThePast - midnight < 24L * 60 * 60 * 1000);
	}

	@Test
	public void sleepPositiveReturnsTrueTest() {
		CcpTimeDecorator t = new CcpTimeDecorator();
		boolean result = t.sleep(1);
		assertTrue(result);
	}

	@Test
	public void sleepZeroReturnsFalseTest() {
		CcpTimeDecorator t = new CcpTimeDecorator();
		boolean result = t.sleep(0);
		assertFalse(result);
	}

	@Test
	public void sleepNegativeReturnsFalseTest() {
		CcpTimeDecorator t = new CcpTimeDecorator();
		boolean result = t.sleep(-100);
		assertFalse(result);
	}

	// ── null-parameter tests (AOP) ────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void constructorLongNullParamTest() {
		new CcpTimeDecorator((Long) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getFormattedDateTimeNullParamTest() {
		new CcpTimeDecorator().getFormattedDateTime(null);
	}

	// ── null-return tests (AOP) ───────────────────────────────────────────────

	@Test(expected = CcpNullReturnException.class)
	public void getContentNullReturnTest() throws Exception {
		CcpTimeDecorator t = new CcpTimeDecorator(1000L);
		Field f = CcpTimeDecorator.class.getDeclaredField("content");
		f.setAccessible(true);
		f.set(t, null);
		t.getContent();
	}
}
