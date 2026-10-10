package com.ccp.local.testings.implementations.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.cache.CcpCache;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

/**
 * Locally each process (the REST APIs, the support bot listener) keeps its own cache in memory, while in production the
 * cache is shared. A deletion leaves a mark that every local process sees ({@link CacheDeletionMarks}), so an entry
 * dropped in one process is dropped in all of them, as in production. Until 2026-10-08 the approval of a skill, run by
 * the support bot listener, did not reach the screen until the vis API was restarted.
 *
 * <p>Another process is simulated by the mark itself: it is the only thing a process leaves for the others.
 */
public class CacheDeletionMarksTest {

	@BeforeClass
	public static void loadDependencies() {
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	private final String key = "test.cache.deletion.marks." + System.nanoTime();

	private final CcpCache memory = CcpLocalCacheInstances.map.getInstance();

	@Test
	public void anEntryDeletedByAnotherProcessIsAMiss() {
		this.memory.put(this.key, "value", 3600);

		CacheDeletionMarks.markDeletion(this.key);

		assertNull(this.memory.get(this.key));
	}

	@Test
	public void anEntryStoredAfterTheDeletionIsKept() {
		CacheDeletionMarks.markDeletion(this.key);
		this.waitAMillisecond();

		this.memory.put(this.key, "value", 3600);

		assertEquals("value", this.memory.get(this.key));
	}

	/** The support bot listener uses the cache that stores nothing; its deletions must still reach the REST APIs. */
	@Test
	public void aDeletionInTheCacheThatStoresNothingDropsTheEntryOfTheMemory() {
		this.memory.put(this.key, "value", 3600);

		CcpCache storesNothing = CcpLocalCacheInstances.mock.getInstance();
		storesNothing.delete(this.key);

		assertNull(this.memory.get(this.key));
	}

	@Test
	public void aDeletionInTheMemoryLeavesTheMarkForTheOtherProcesses() {
		long beforeTheDeletion = System.currentTimeMillis();

		this.memory.put(this.key, "value", 3600);
		this.memory.delete(this.key);

		assertNull(this.memory.get(this.key));
		assertTrue(CacheDeletionMarks.wasDeletedAfter(this.key, beforeTheDeletion));
	}

	@Test
	public void aKeyNeverDeletedHasNoMark() {
		assertFalse(CacheDeletionMarks.wasDeletedAfter(this.key, 0L));

		this.memory.put(this.key, "value", 3600);
		assertEquals("value", this.memory.get(this.key));
	}

	private void waitAMillisecond() {
		long start = System.currentTimeMillis();
		while (System.currentTimeMillis() <= start + 1) {
			Thread.onSpinWait();
		}
	}
}
