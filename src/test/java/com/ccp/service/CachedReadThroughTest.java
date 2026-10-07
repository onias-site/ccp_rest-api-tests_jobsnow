package com.ccp.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.ccp.business.CcpBusiness;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.cache.CcpCache;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;

/**
 * Proves the JSON read-through of {@link CcpCache} (computed on a miss, rebuilt from a cached map or parsed from cached
 * text on a hit) and {@link CcpCachedService}, which caches a service by the hash of one input field.
 */
public class CachedReadThroughTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler(), CcpLocalCacheInstances.map);
	}

	private final CcpFieldName counter = new CcpFieldName("counter");

	private final CcpFieldName word = new CcpFieldName("word");

	private final String key = "test.read.through." + System.nanoTime();

	private int computations;

	private final CcpBusiness counting = json -> json.put(this.counter, ++this.computations);

	/** A service that counts its executions in the output. */
	class CountingService implements CcpService {
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			return json.put(CachedReadThroughTest.this.counter, ++CachedReadThroughTest.this.computations);
		}

		public Class<?> getJsonValidationClass() {
			return CountingService.class;
		}

		public String name() {
			return "counting";
		}
	}

	@Test
	public void aMissComputesAndCachesTheMapThatTheHitRebuilds() {
		CcpCache cache = CcpDependencyInjection.getDependency(CcpCache.class);

		CcpJsonRepresentation first = cache.get(this.key, CcpOtherConstants.EMPTY_JSON, this.counting, 60);
		CcpJsonRepresentation second = cache.get(this.key, CcpOtherConstants.EMPTY_JSON, this.counting, 60);

		assertEquals(1, first.getAsIntegerNumber(this.counter).intValue());
		assertEquals(first, second);
		assertEquals(1, this.computations);
	}

	@Test
	public void aCachedTextIsParsedAsJson() {
		CcpCache cache = CcpDependencyInjection.getDependency(CcpCache.class);
		cache.put(this.key, "{\"counter\": 7}", 60);

		CcpJsonRepresentation value = cache.get(this.key, CcpOtherConstants.EMPTY_JSON, this.counting, 60);

		assertEquals(7, value.getAsIntegerNumber(this.counter).intValue());
		assertEquals("the business does not run on a hit", 0, this.computations);
	}

	@Test
	public void theCachedServiceRunsOncePerValueOfTheCachedField() {
		CcpCachedService service = new CcpCachedService(this.word, new CountingService(), 60);
		Map<String, Object> input = new HashMap<>();
		input.put("word", "cached" + System.nanoTime());

		Map<String, Object> first = service.execute(input);
		Map<String, Object> second = service.execute(input);
		input.put("word", "other" + System.nanoTime());
		Map<String, Object> third = service.execute(input);

		assertEquals(1, ((Number) first.get("counter")).intValue());
		assertEquals("on a hit the cached copy comes back, with its integers as decimals", 1.0, ((Number) second.get("counter")).doubleValue(), 0);
		assertEquals(first.get("cacheHash"), second.get("cacheHash"));
		assertEquals(2,((Number) third.get("counter")).intValue());
		assertFalse("the output carries the hash used as key", String.valueOf(first.get("cacheHash")).isEmpty());
		assertFalse(first.get("cacheHash").equals(third.get("cacheHash")));
	}

	/** A service that answers its own name, so a result taken from another service's cache shows up. */
	static class NamingService implements CcpService {
		final String name;

		NamingService(String name) {
			this.name = name;
		}

		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			return json.put(new CcpFieldName("answeredBy"), this.name);
		}

		public Class<?> getJsonValidationClass() {
			return NamingService.class;
		}

		public String name() {
			return this.name;
		}
	}

	/** Until 2026-10-06 the key was only the hash of the value, and the second service got the first one's result. */
	@Test
	public void twoCachedServicesWithTheSameValueDoNotShareTheResult() {
		CcpCachedService login = new CcpCachedService(this.word, new NamingService("login"), 60);
		CcpCachedService profile = new CcpCachedService(this.word, new NamingService("profile"), 60);
		Map<String, Object> input = new HashMap<>();
		input.put("word", "same@value." + System.nanoTime());

		Map<String, Object> fromLogin = login.execute(input);
		Map<String, Object> fromProfile = profile.execute(input);

		assertEquals("login", fromLogin.get("answeredBy"));
		assertEquals("profile", fromProfile.get("answeredBy"));
		assertFalse(fromLogin.get("cacheHash").equals(fromProfile.get("cacheHash")));
	}

	@Test
	public void theSameServiceCachedByTwoFieldsWithTheSameValueKeepsTwoEntries() {
		CcpFieldName otherField = new CcpFieldName("otherWord");
		CountingService counting = new CountingService();
		CcpCachedService byWord = new CcpCachedService(this.word, counting, 60);
		CcpCachedService byOtherWord = new CcpCachedService(otherField, counting, 60);
		String sameValue = "same" + System.nanoTime();
		Map<String, Object> input = new HashMap<>();
		input.put("word", sameValue);
		input.put("otherWord", sameValue);

		byWord.execute(input);
		byOtherWord.execute(input);

		assertEquals("each field has its own entry, so the service ran twice", 2, this.computations);
	}
}
