package com.ccp.especifications.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.jn.entities.JnEntityAsyncTask;

/**
 * Proves {@link CcpCacheDecorator} over the in-memory cache: the key of an entity record, the composed keys, reading
 * through the cache, defaults, exceptions and deletion.
 */
public class CacheDecoratorBehaviorTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler(), CcpLocalCacheInstances.map);
	}

	private final String base = "test.cache." + System.nanoTime();

	@Test
	public void theKeyOfARecordNamesTheEntityAndTheId() {
		CcpCacheDecorator cache = new CcpCacheDecorator(JnEntityAsyncTask.ENTITY, "abc");

		assertEquals("records.entity.jn_async_task.id.abc", cache.key);
		assertEquals(cache.key, cache.toString());
	}

	@Test
	public void incrementingTheKeyAppendsTheNameAndTheValue() {
		CcpCacheDecorator cache = new CcpCacheDecorator(this.base).incrementKey("page", 2);

		assertEquals(this.base + ".page.2", cache.key);
	}

	@Test
	public void incrementingByKeysUsesOnlyTheNamedFieldsOfTheJson() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(new CcpFieldName("page"), 2).put(new CcpFieldName("ignored"), 9);

		CcpCacheDecorator cache = new CcpCacheDecorator(this.base).incrementKeys(json, "page");

		assertEquals(this.base + ".page.2", cache.key);
	}

	@Test
	public void aValueIsComputedOnceAndThenReadFromTheCache() {
		AtomicInteger computations = new AtomicInteger();
		CcpCacheDecorator cache = new CcpCacheDecorator(this.base);

		String first = cache.get(json -> "value " + computations.incrementAndGet(), 60);
		String second = cache.get(json -> "value " + computations.incrementAndGet(), 60);

		assertEquals("value 1", first);
		assertEquals("value 1", second);
		assertEquals(1, computations.get());
		assertTrue(cache.isPresentInTheCache());
	}

	@Test
	public void anAbsentKeyGivesTheDefaultOrTheException() {
		CcpCacheDecorator cache = new CcpCacheDecorator(this.base + ".absent");

		assertEquals("fallback", cache.getOrDefault("fallback"));
		IllegalStateException expected = new IllegalStateException("absent");
		try {
			cache.getOrThrowException(expected);
			org.junit.Assert.fail("an absent key must throw");
		} catch (IllegalStateException e) {
			assertEquals(expected, e);
		}
	}

	@Test
	public void aStoredValueCanBeReadAndDeleted() {
		CcpCacheDecorator cache = new CcpCacheDecorator(this.base + ".stored").put("kept", 60);

		assertEquals("kept", cache.getOrDefault("fallback"));

		cache.delete();

		assertFalse(cache.isPresentInTheCache());
	}

	@Test
	public void deletingSeveralKeysAtOnceRemovesThemAll() {
		CcpCacheDecorator first = new CcpCacheDecorator(this.base + ".a").put(1, 60);
		CcpCacheDecorator second = new CcpCacheDecorator(this.base + ".b").put(2, 60);

		CcpCacheDecorator.deleteAll(new ArrayList<>());
		assertTrue(first.isPresentInTheCache());

		CcpCacheDecorator.deleteAll(Arrays.asList(first.key, second.key));

		assertFalse(first.isPresentInTheCache());
		assertFalse(second.isPresentInTheCache());
	}
}
