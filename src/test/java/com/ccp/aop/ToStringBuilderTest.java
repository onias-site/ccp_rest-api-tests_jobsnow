package com.ccp.aop;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

/**
 * Proves the rules of {@link CcpToStringBuilder}: the class name for an object without attributes, a JSON of the
 * attributes otherwise, the escaping of texts, the handling of numbers, enums, arrays, collections and maps, the cut of
 * circular references and deep graphs, and the promise of never throwing.
 */
public class ToStringBuilderTest {

	enum Color { RED }

	static class WithoutAttributes {
	}

	static class Scalars {
		final String text = "say \"hi\"\n\tnow\u0001";
		final int count = 3;
		final double notANumber = Double.NaN;
		final boolean active = true;
		final Color color = Color.RED;
		final Object nothing = null;
		final char letter = 'x';
	}

	static class Containers {
		final int[] numbers = {1, 2};
		final Object list = Arrays.asList("a", null);
		final Map<String, Object> map = new LinkedHashMap<>();
		{
			this.map.put("k", 1);
		}
	}

	static class Node {
		Node next;
	}

	static class WithDecimal {
		final java.util.UUID id = java.util.UUID.fromString("00000000-0000-0000-0000-00000000000a");
		final BigDecimal price = new BigDecimal("10.50");
	}

	static class Exploding {
		public String toString() {
			throw new IllegalStateException("boom");
		}
	}

	static class HoldsExploding {
		final Exploding value = new Exploding();
	}

	static class HoldsBrokenContainers {
		final Object list = new AbstractList<Object>() {
			public Object get(int index) {
				throw new IllegalStateException("broken");
			}
			public int size() {
				return 1;
			}
		};
		final Object map = new AbstractMap<Object, Object>() {
			public Set<Map.Entry<Object, Object>> entrySet() {
				throw new IllegalStateException("broken");
			}
		};
	}

	@Test
	public void nullIsTheWordNull() {
		assertEquals("null", CcpToStringBuilder.build(null));
	}

	@Test
	public void anObjectWithoutAttributesIsItsClassName() {
		assertEquals(WithoutAttributes.class.getName(), CcpToStringBuilder.build(new WithoutAttributes()));
	}

	@Test
	public void scalarsBecomeJsonWithEscapedTexts() {
		String json = CcpToStringBuilder.build(new Scalars());

		assertEquals("{\"text\":\"say \\\"hi\\\"\\n\\tnow\\u0001\",\"count\":3,\"notANumber\":\"NaN\",\"active\":true,"
				+ "\"color\":\"RED\",\"nothing\":null,\"letter\":\"x\"}", json);
	}

	@Test
	public void arraysCollectionsAndMapsBecomeJsonArraysAndObjects() {
		String json = CcpToStringBuilder.build(new Containers());

		assertEquals("{\"numbers\":[1,2],\"list\":[\"a\",null],\"map\":{\"k\":1}}", json);
	}

	@Test
	public void aCircularReferenceIsCutOff() {
		Node node = new Node();
		node.next = node;

		String json = CcpToStringBuilder.build(node);

		assertEquals("{\"next\":\"<circular reference: " + Node.class.getName() + ">\"}", json);
	}

	@Test
	public void aDeepGraphIsSummarizedByTheClassName() {
		Node head = new Node();
		Node current = head;
		for (int index = 0; index < 10; index++) {
			current.next = new Node();
			current = current.next;
		}

		String json = CcpToStringBuilder.build(head);

		assertTrue(json, json.contains("\"next\":\"" + Node.class.getName() + "\""));
	}

	@Test
	public void aTypeWithItsOwnToStringIsWrittenAsItsText() {
		assertEquals("{\"id\":\"00000000-0000-0000-0000-00000000000a\",\"price\":10.50}", CcpToStringBuilder.build(new WithDecimal()));
	}

	@Test
	public void aFailureDegradesToTheIdentityOfTheObject() {
		HoldsExploding holder = new HoldsExploding();

		String text = CcpToStringBuilder.build(holder);

		String identity = HoldsExploding.class.getName() + "@" + Integer.toHexString(System.identityHashCode(holder));
		assertEquals(identity, text);
	}

	@Test
	public void aBrokenCollectionOrMapBecomesItsIdentity() {
		HoldsBrokenContainers holder = new HoldsBrokenContainers();

		String json = CcpToStringBuilder.build(holder);

		String listIdentity = holder.list.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(holder.list));
		String mapIdentity = holder.map.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(holder.map));
		assertEquals("{\"list\":\"" + listIdentity + "\",\"map\":\"" + mapIdentity + "\"}", json);
	}
}
