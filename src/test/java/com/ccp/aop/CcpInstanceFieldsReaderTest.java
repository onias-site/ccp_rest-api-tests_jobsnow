package com.ccp.aop;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

/** {@link CcpInstanceFieldsReader} lists the instance fields of a class and of its superclasses, accessible, without the static ones. */
public class CcpInstanceFieldsReaderTest {

	static class Parent {
		@SuppressWarnings("unused")
		private final int parentField = 1;
		static int parentStatic;
	}

	static class Child extends Parent {
		@SuppressWarnings("unused")
		private final String childField = "x";
		static String childStatic;
	}

	private static List<String> names(List<Field> fields) {
		return fields.stream().map(Field::getName).collect(Collectors.toList());
	}

	@Test
	public void theFieldsOfTheClassComeBeforeTheOnesOfItsSuperclass() {
		List<Field> fields = CcpInstanceFieldsReader.INSTANCE.apply(Child.class);

		assertEquals(List.of("childField", "parentField"), names(fields));
	}

	@Test
	public void theStaticFieldsAreLeftOut() {
		List<String> names = names(CcpInstanceFieldsReader.INSTANCE.apply(Child.class));

		assertTrue(names.toString(), false == names.contains("childStatic") && false == names.contains("parentStatic"));
	}

	@Test
	public void theFieldsComeAccessible() throws Exception {
		List<Field> fields = CcpInstanceFieldsReader.INSTANCE.apply(Child.class);

		assertEquals("x", fields.get(0).get(new Child()));
	}

	@Test
	public void objectHasNoFields() {
		assertTrue(CcpInstanceFieldsReader.INSTANCE.apply(Object.class).isEmpty());
	}
}
