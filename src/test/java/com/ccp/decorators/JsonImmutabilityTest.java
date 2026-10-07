package com.ccp.decorators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

/**
 * Proves the fixes of 2026-10-06 in {@link CcpJsonRepresentation}: every JSON is unmodifiable, the empty one and the one
 * read from a stream included (a {@code content.put} on {@code EMPTY_JSON} used to contaminate every empty JSON of the
 * process); renaming and removing fields keep the order of the fields; and the default of {@code getOrDefault} is only
 * computed when the field is missing.
 */
public class JsonImmutabilityTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	private final CcpFieldName a = new CcpFieldName("a");

	private final CcpFieldName b = new CcpFieldName("b");

	private final CcpFieldName x = new CcpFieldName("x");

	private void assertUnmodifiable(CcpJsonRepresentation json) {
		try {
			json.content.put("intruder", 1);
			fail("the content of the JSON could be changed");
		} catch (UnsupportedOperationException e) {
			// expected
		}
	}

	@Test
	public void theGlobalEmptyJsonCannotBeChanged() {
		this.assertUnmodifiable(CcpOtherConstants.EMPTY_JSON);
		this.assertUnmodifiable(CcpJsonRepresentation.getEmptyJson());
		assertEquals(0, CcpOtherConstants.EMPTY_JSON.content.size());
	}

	@Test
	public void aJsonReadFromAStreamCannotBeChanged() {
		ByteArrayInputStream stream = new ByteArrayInputStream("{\"a\": 1}".getBytes(StandardCharsets.UTF_8));

		CcpJsonRepresentation json = new CcpJsonRepresentation(stream);

		assertEquals(1, json.getAsIntegerNumber(this.a).intValue());
		this.assertUnmodifiable(json);
	}

	@Test
	public void aPropertiesStreamIsReadToo() {
		ByteArrayInputStream stream = new ByteArrayInputStream("a=1\nb=2".getBytes(StandardCharsets.UTF_8));

		CcpJsonRepresentation json = new CcpJsonRepresentation(stream);

		assertEquals("2", json.getAsString(this.b));
		this.assertUnmodifiable(json);
	}

	private CcpJsonRepresentation abc() {
		return CcpOtherConstants.EMPTY_JSON.put(this.a, 1).put(this.b, 2).put(new CcpFieldName("c"), 3);
	}

	@Test
	public void theRenamedFieldKeepsItsPlace() {
		CcpJsonRepresentation renamed = this.abc().renameField(this.b, this.x);

		assertEquals(Arrays.asList("a", "x", "c"), new ArrayList<>(renamed.content.keySet()));
		assertEquals(2, renamed.getAsIntegerNumber(this.x).intValue());
	}

	@Test
	public void renamingOverAnExistingFieldKeepsTheValueOfTheRenamedOne() {
		CcpJsonRepresentation renamed = this.abc().renameField(this.b, this.a);

		assertEquals(Arrays.asList("a", "c"), new ArrayList<>(renamed.content.keySet()));
		assertEquals(2, renamed.getAsIntegerNumber(this.a).intValue());
	}

	@Test
	public void removingFieldsKeepsTheOrderOfTheOthers() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON;
		for (String name : Arrays.asList("z", "y", "x", "w", "v", "u", "t")) {
			json = json.put(new CcpFieldName(name), name);
		}

		CcpJsonRepresentation removed = json.removeFields(new CcpFieldName("x"), new CcpFieldName("u"));

		assertEquals(Arrays.asList("z", "y", "w", "v", "t"), new ArrayList<>(removed.content.keySet()));
	}

	@Test
	public void theDefaultIsOnlyComputedWhenTheFieldIsMissing() {
		AtomicInteger computations = new AtomicInteger();
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(this.a, "present");

		String present = json.getOrDefault(this.a, () -> "default" + computations.incrementAndGet());
		String missing = json.getOrDefault(this.b, () -> "default" + computations.incrementAndGet());

		assertEquals("present", present);
		assertEquals("default1", missing);
		assertEquals(1, computations.get());
	}
}
