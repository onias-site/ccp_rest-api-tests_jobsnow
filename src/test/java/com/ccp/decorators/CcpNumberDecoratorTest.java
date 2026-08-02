package com.ccp.decorators;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;

public class CcpNumberDecoratorTest {

	@Test
	public void greaterThanTest() {
		CcpNumberDecorator n = new CcpNumberDecorator("10");
		assertTrue(n.greaterThan(9d));
		assertFalse(n.greaterThan(10d));
		assertFalse(n.greaterThan(11d));
	}

	@Test
	public void equalsOrGreaterThanTest() {
		CcpNumberDecorator n = new CcpNumberDecorator("5");
		assertTrue(n.equalsOrGreaterThan(5d));
		assertTrue(n.equalsOrGreaterThan(4d));
		assertFalse(n.equalsOrGreaterThan(6d));
	}

	@Test
	public void lessThanTest() {
		CcpNumberDecorator n = new CcpNumberDecorator("3");
		assertTrue(n.lessThan(4d));
		assertFalse(n.lessThan(3d));
		assertFalse(n.lessThan(2d));
	}

	@Test
	public void equalsOrLessThanTest() {
		CcpNumberDecorator n = new CcpNumberDecorator("7");
		assertTrue(n.equalsOrLessThan(7d));
		assertTrue(n.equalsOrLessThan(8d));
		assertFalse(n.equalsOrLessThan(6d));
	}

	@Test
	public void equalsToTest() {
		CcpNumberDecorator n = new CcpNumberDecorator("42");
		assertTrue(n.equalsTo(42d));
		assertFalse(n.equalsTo(43d));
	}

	@Test
	public void belongsToRestrictedValuesVarargsTest() {
		CcpNumberDecorator n = new CcpNumberDecorator("2");
		assertTrue(n.belongsToRestrictedValues(1d, 2d, 3d));
		assertFalse(n.belongsToRestrictedValues(10d, 20d, 30d));
	}

	@Test
	public void belongsToRestrictedValuesCollectionTest() {
		CcpNumberDecorator n = new CcpNumberDecorator("99");
		List<Double> lista = Arrays.asList(99d, 100d, 101d);
		assertTrue(n.belongsToRestrictedValues(lista));
		List<Double> semOValor = Arrays.asList(1d, 2d, 3d);
		assertFalse(n.belongsToRestrictedValues(semOValor));
	}

	@Test
	public void getContentTest() {
		CcpNumberDecorator n = new CcpNumberDecorator("3.14");
		assertTrue(n.getContent() == 3.14d);
	}

	@Test
	public void toStringTest() {
		CcpNumberDecorator n = new CcpNumberDecorator("8");
		assertTrue(n.toString().contains("8"));
	}

	// ── null-parameter tests (AOP) ────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void construtorNullParamTest() {
		new CcpNumberDecorator((String) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void greaterThanNullParamTest() {
		new CcpNumberDecorator("1").greaterThan(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void equalsOrGreaterThanNullParamTest() {
		new CcpNumberDecorator("1").equalsOrGreaterThan(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void lessThanNullParamTest() {
		new CcpNumberDecorator("1").lessThan(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void equalsOrLessThanNullParamTest() {
		new CcpNumberDecorator("1").equalsOrLessThan(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void equalsToNullParamTest() {
		new CcpNumberDecorator("1").equalsTo(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void belongsToRestrictedValuesVarargsNullParamTest() {
		new CcpNumberDecorator("1").belongsToRestrictedValues((Double[]) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void belongsToRestrictedValuesCollectionNullParamTest() {
		new CcpNumberDecorator("1").belongsToRestrictedValues((List<Double>) null);
	}

	// ── null-return tests (AOP) ───────────────────────────────────────────────
	// Nota: getContent() e toString() nunca podem retornar null (content é primitivo double).
	// Todos os outros métodos públicos retornam boolean primitivo, não sujeito ao aspecto.
}
