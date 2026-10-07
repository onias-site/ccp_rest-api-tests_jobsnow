package com.ccp.implementations.json.gson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Proves that {@link GsonJsonHandler} does not mutate shared state on each serialization (finding 30): until
 * 2026-10-07 every call appended the exclusion strategy once more to a static {@code GsonBuilder}, so the list grew
 * without limit and every later serialization went through all of it. The check reads, by reflection, every static
 * {@code Gson} or {@code GsonBuilder} of the handler, whatever its name, so it runs against the old code too.
 */
public class SerializerReuseTest {

	/** A value with a field of type Class, which the exclusion strategy skips. */
	static class WithClassField {
		String name = "x";
		Class<?> type = String.class;
	}

	private final GsonJsonHandler handler = new GsonJsonHandler();

	private static Object readField(Object owner, Class<?> ownerClass, String fieldName) throws Exception {
		Field field = ownerClass.getDeclaredField(fieldName);
		field.setAccessible(true);
		Object value = field.get(owner);
		return value;
	}

	/** The serialization strategies of the excluder of each static Gson or GsonBuilder of the handler. */
	private static Map<String, Integer> strategiesBySharedSerializer() throws Exception {
		Map<String, Integer> strategiesBySerializer = new HashMap<>();
		for (Field field : GsonJsonHandler.class.getDeclaredFields()) {
			boolean isStatic = Modifier.isStatic(field.getModifiers());
			boolean isSerializer = Gson.class.equals(field.getType()) || GsonBuilder.class.equals(field.getType());
			if (false == isStatic || false == isSerializer) {
				continue;
			}
			field.setAccessible(true);
			Object serializer = field.get(null);
			Object excluder = readField(serializer, field.getType(), "excluder");
			List<?> strategies = (List<?>) readField(excluder, excluder.getClass(), "serializationStrategies");
			strategiesBySerializer.put(field.getName(), strategies.size());
		}
		return strategiesBySerializer;
	}

	@Test
	public void theExclusionStrategyIsNotAddedAgainOnEachSerialization() throws Exception {
		this.handler.toJson(new WithClassField());
		this.handler.asPrettyJson(new WithClassField());
		Map<String, Integer> before = strategiesBySharedSerializer();

		for (int k = 0; k < 50; k++) {
			this.handler.toJson(new WithClassField());
			this.handler.asPrettyJson(new WithClassField());
		}

		assertEquals(before, strategiesBySharedSerializer());
		for (Integer strategies : before.values()) {
			assertTrue("each serializer keeps a single strategy: " + before, strategies <= 1);
		}
	}

	@Test
	public void theCompactOutputStaysCompactAfterAPrettyPrintAndSkipsClassFields() {
		this.handler.asPrettyJson(new WithClassField());

		String json = this.handler.toJson(new WithClassField());

		assertEquals("{\"name\":\"x\"}", json);
	}

	@Test
	public void thePrettyOutputIsIndentedAndSkipsClassFields() {
		String prettyJson = this.handler.asPrettyJson(new WithClassField());

		assertTrue(prettyJson, prettyJson.contains("\n"));
		assertFalse(prettyJson, prettyJson.contains("type"));
	}
}
