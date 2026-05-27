package com.ccp.decorators;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

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
}
