package com.ccp.implementations.cache.gcp.memorystore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.cache.CcpCache;
import com.ccp.especifications.db.utils.CcpDbCallsOfTheThread;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.exceptions.JedisConnectionException;

public class CcpGcpMemoryStoreTest {
	enum Fields implements CcpJsonFieldName {
		name, age, address, city, skills, localhost, DEL, SETEX
	}

	static {
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE, CcpGcpMemoryStore.INSTANCE);
	}

	private FakeMemoryStoreServer server;
	private JedisPooled client;
	private CcpCache cache;

	@Before
	public void startServer() throws IOException {
		this.server = new FakeMemoryStoreServer();
		this.client = new JedisPooled(Fields.localhost.name(), this.server.getPort());
		this.cache = new GcpMemoryStore(this.client);
	}

	@After
	public void stopServer() throws IOException {
		this.client.close();
		this.server.close();
	}

	private static CcpCache getCache() {
		return CcpDependencyInjection.getDependency(CcpCache.class);
	}

	private long countCommands(Fields commandName) {
		long count = this.server.commands.stream().filter(command -> commandName.name().equalsIgnoreCase(command.get(0))).count();
		return count;
	}

	// ── provider ──────────────────────────────────────────────────────────────

	@Test
	public void constructorProviderTest() {
		assertNotNull(CcpGcpMemoryStore.INSTANCE);
	}

	@Test
	public void getInstanceTest() {
		CcpCache instance = CcpGcpMemoryStore.INSTANCE.getInstance();
		assertNotNull(instance);
	}

	// ── reads and writes ──────────────────────────────────────────────────────

	/**
	 * Inside a unit (request or message) each command is counted for the column of the cache hits of the panel of the
	 * requests: reads as hits or misses, in total and per entity of the key, writes and deletes as writes.
	 */
	@Test
	public void insideAUnitTheHitsAndMissesAreCountedPerEntityTest() {
		this.cache.put("records.entity.jn_login.id.1", "value", 60);
		CcpDbCallsOfTheThread.start(CcpDbCallsOfTheThread.newTraceId());
		try {
			this.cache.get("records.entity.jn_login.id.1");
			this.cache.get("records.entity.jn_login.id.2");
			this.cache.get("records.entity.vis_resume.id.3");
			this.cache.get("a1b2c3");
			this.cache.put("records.entity.jn_login.id.2", "value", 60);
			this.cache.deleteAll(Arrays.asList("first", "second"));
		} finally {
			CcpJsonRepresentation summary = CcpDbCallsOfTheThread.finish();
			assertEquals(1, summary.getAsIntegerNumber(CcpDbCallsOfTheThread.Fields.cacheHits).intValue());
			assertEquals(3, summary.getAsIntegerNumber(CcpDbCallsOfTheThread.Fields.cacheMisses).intValue());
			assertEquals(2, summary.getAsIntegerNumber(CcpDbCallsOfTheThread.Fields.cacheWrites).intValue());
			assertEquals(6, summary.getAsIntegerNumber(CcpDbCallsOfTheThread.Fields.cacheCallsCount).intValue());
			assertTrue(summary.getAsDoubleNumber(CcpDbCallsOfTheThread.Fields.cacheClientMs) >= 0);
			CcpJsonRepresentation byEntity = summary.getInnerJson(CcpDbCallsOfTheThread.Fields.cacheByEntity);
			CcpJsonRepresentation login = byEntity.getInnerJson(new CcpFieldName("jn_login"));
			assertEquals(1, login.getAsIntegerNumber(CcpDbCallsOfTheThread.Fields.hits).intValue());
			assertEquals(1, login.getAsIntegerNumber(CcpDbCallsOfTheThread.Fields.misses).intValue());
			CcpJsonRepresentation resume = byEntity.getInnerJson(new CcpFieldName("vis_resume"));
			assertFalse(resume.toString(), resume.containsAllFields(CcpDbCallsOfTheThread.Fields.hits));
			assertEquals(1, resume.getAsIntegerNumber(CcpDbCallsOfTheThread.Fields.misses).intValue());
			CcpJsonRepresentation noEntity = byEntity.getInnerJson(new CcpFieldName(""));
			assertEquals(1, noEntity.getAsIntegerNumber(CcpDbCallsOfTheThread.Fields.misses).intValue());
		}
	}

	/** Outside a unit nothing is counted. */
	@Test
	public void outsideAUnitNothingIsCountedTest() {
		this.cache.get("records.entity.jn_login.id.1");
		CcpJsonRepresentation summary = CcpDbCallsOfTheThread.finish();
		assertEquals(0, summary.getAsIntegerNumber(CcpDbCallsOfTheThread.Fields.cacheCallsCount).intValue());
	}

	@Test
	public void missIsNullTest() {
		assertNull(this.cache.get("missing"));
		assertFalse(this.cache.isPresent("missing"));
	}

	@Test
	public void jsonComesBackAsJsonTest() {
		CcpJsonRepresentation address = CcpOtherConstants.EMPTY_JSON.put(Fields.city, "São Paulo");
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(Fields.name, "João")
				.put(Fields.age, 40)
				.put(Fields.address, address)
				.put(Fields.skills, Arrays.asList("JAVA", "KAFKA"));

		this.cache.put("record", json, 3600);

		CcpJsonRepresentation cached = (CcpJsonRepresentation) this.cache.get("record");
		assertEquals("João", cached.getAsString(Fields.name));
		assertEquals(Integer.valueOf(40), cached.getAsIntegerNumber(Fields.age));
		assertEquals("São Paulo", cached.getInnerJson(Fields.address).getAsString(Fields.city));
		assertEquals(Arrays.asList("JAVA", "KAFKA"), cached.getAsStringList(Fields.skills));
	}

	@Test
	public void mapComesBackAsJsonTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(Fields.name, "Ana");

		this.cache.put("map", json.content, 60);

		Object cached = this.cache.get("map");
		assertTrue(cached instanceof CcpJsonRepresentation);
	}

	@Test
	public void textThatLooksLikeJsonStaysTextTest() {
		this.cache.put("text", "{\"name\":\"Ana\"}", 60);

		assertEquals("{\"name\":\"Ana\"}", this.cache.get("text"));
	}

	@Test
	public void booleanStaysBooleanTest() {
		this.cache.put("flag", true, 60);

		assertEquals(Boolean.TRUE, this.cache.get("flag"));
	}

	@Test
	public void expirationGoesToTheServerTest() {
		this.cache.put("expiring", "value", 3600);

		List<String> setex = this.server.commands.stream().filter(command -> Fields.SETEX.name().equalsIgnoreCase(command.get(0))).findFirst().get();
		assertEquals("3600", setex.get(2));
	}

	@Test
	public void readThroughRunsTheTaskOnlyOnTheMissTest() {
		int[] executions = new int[1];
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(Fields.name, "Ana");

		for (int attempt = 0; attempt < 2; attempt++) {
			CcpJsonRepresentation value = this.cache.get("readThrough", CcpOtherConstants.EMPTY_JSON, x -> { executions[0]++; return json; }, 60);
			assertEquals("Ana", value.getAsString(Fields.name));
		}

		assertEquals(1, executions[0]);
	}

	// ── deletes ───────────────────────────────────────────────────────────────

	@Test
	public void deleteRemovesTheKeyTest() {
		this.cache.put("toDelete", "value", 60);

		this.cache.delete("toDelete");

		assertNull(this.cache.get("toDelete"));
	}

	@Test
	public void deleteAllUsesOneCommandTest() {
		this.cache.put("first", "value", 60).put("second", "value", 60).put("third", "value", 60);

		this.cache.deleteAll(Arrays.asList("first", "second"));

		assertNull(this.cache.get("first"));
		assertNull(this.cache.get("second"));
		assertNotNull(this.cache.get("third"));
		assertEquals(1, this.countCommands(Fields.DEL));
	}

	@Test
	public void deleteAllWithoutKeysSendsNothingTest() {
		this.cache.deleteAll(Collections.emptyList());

		assertEquals(0, this.countCommands(Fields.DEL));
	}

	// ── server down ───────────────────────────────────────────────────────────

	@Test(expected = JedisConnectionException.class)
	public void serverDownIsAnErrorAndNotAMissTest() throws IOException {
		int closedPort;
		try (ServerSocket socket = new ServerSocket(0)) {
			closedPort = socket.getLocalPort();
		}
		try (JedisPooled unreachable = new JedisPooled(Fields.localhost.name(), closedPort)) {
			new GcpMemoryStore(unreachable).get("key");
		}
	}

	// ── null-parameter tests (AOP) — GcpMemoryStore via interface CcpCache ────

	@Test(expected = CcpNullParameterException.class)
	public void getKeyNullTest() {
		getCache().get(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void putKeyNullTest() {
		getCache().put(null, "value", 10);
	}

	@Test(expected = CcpNullParameterException.class)
	public void putValueNullTest() {
		getCache().put("key", null, 10);
	}

	@Test(expected = CcpNullParameterException.class)
	public void deleteKeyNullTest() {
		getCache().delete(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void deleteAllKeysNullTest() {
		getCache().deleteAll(null);
	}
}
