package com.ccp.decorators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;

import org.junit.Test;

public class CcpTimeDecoratorTest {

	@Test
	public void construtorSemArgumentoUsaTempoAtualTest() {
		long antes = System.currentTimeMillis();
		CcpTimeDecorator t = new CcpTimeDecorator();
		long depois = System.currentTimeMillis();
		assertTrue(t.content >= antes);
		assertTrue(t.content <= depois);
	}

	@Test
	public void construtorComLongTest() {
		long tempo = 1_000_000L;
		CcpTimeDecorator t = new CcpTimeDecorator(tempo);
		assertEquals(tempo, (long) t.content);
	}

	@Test
	public void getContentTest() {
		long tempo = System.currentTimeMillis();
		CcpTimeDecorator t = new CcpTimeDecorator(tempo);
		assertEquals(tempo, (long) t.getContent());
	}

	@Test
	public void getMidnightMenorQueAgoreTest() {
		CcpTimeDecorator t = new CcpTimeDecorator();
		long midnight = t.getMidnight();
		assertTrue(midnight <= t.content);
	}

	@Test
	public void getMidnightEhMeiaNoiteTest() {
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
	public void getYearRetornaAnoAtualTest() {
		int anoEsperado = Calendar.getInstance().get(Calendar.YEAR);
		CcpTimeDecorator t = new CcpTimeDecorator();
		assertEquals(anoEsperado, t.getYear());
	}

	@Test
	public void getFormattedDateTimeTest() {
		CcpTimeDecorator t = new CcpTimeDecorator();
		String formatado = t.getFormattedDateTime("yyyy");
		String anoAtual = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
		assertEquals(anoAtual, formatado);
	}

	@Test
	public void getFormattedDateTimeFormatoDiaMesAnoTest() {
		CcpTimeDecorator t = new CcpTimeDecorator();
		String formatado = t.getFormattedDateTime("dd/MM/yyyy");
		assertTrue(formatado.matches("\\d{2}/\\d{2}/\\d{4}"));
	}

	@Test
	public void getBrazilianCalendarNaoNuloTest() {
		CcpTimeDecorator t = new CcpTimeDecorator();
		Calendar cal = t.getBrazilianCalendar();
		assertNotNull(cal);
		assertEquals("America/Sao_Paulo", cal.getTimeZone().getID());
	}

	@Test
	public void sleepPositivoRetornaTrueTest() {
		CcpTimeDecorator t = new CcpTimeDecorator();
		boolean resultado = t.sleep(1);
		assertTrue(resultado);
	}

	@Test
	public void sleepZeroRetornaFalseTest() {
		CcpTimeDecorator t = new CcpTimeDecorator();
		boolean resultado = t.sleep(0);
		assertFalse(resultado);
	}

	@Test
	public void sleepNegativoRetornaFalseTest() {
		CcpTimeDecorator t = new CcpTimeDecorator();
		boolean resultado = t.sleep(-100);
		assertFalse(resultado);
	}
}
