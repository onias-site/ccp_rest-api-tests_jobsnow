package com.ccp.hash;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

import com.ccp.decorators.CcpStringDecorator;

/**
 * Proves that hashes computed at the same time by several threads are right. The SHA-1 is the id of every entity record;
 * until 2026-10-06 one {@code MessageDigest} per algorithm was shared by every thread, and concurrent requests could mix
 * their bytes and produce wrong ids.
 */
public class HashConcurrencyTest {

	private static final int THREADS = 16;

	private static final int HASHES_PER_THREAD = 3000;

	/** The SHA-1 computed with a digest of its own, as the reference. */
	private static BigInteger expectedSha1(String text) throws Exception {
		MessageDigest digest = MessageDigest.getInstance("SHA1");
		byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
		return new BigInteger(hash);
	}

	@Test(timeout = 120_000)
	public void hashesComputedAtTheSameTimeAreAllRight() throws Exception {
		ExecutorService executor = Executors.newFixedThreadPool(THREADS);
		try {
			List<Future<Integer>> wrongHashesPerThread = new ArrayList<>();

			for (int thread = 0; thread < THREADS; thread++) {
				int threadNumber = thread;
				Callable<Integer> hashing = () -> {
					int wrongHashes = 0;
					for (int k = 0; k < HASHES_PER_THREAD; k++) {
						String text = "thread " + threadNumber + " record " + k + " " + "x".repeat(k % 50);
						BigInteger computed = new CcpStringDecorator(text).hash().asBigInteger(CcpHashAlgorithm.SHA1);
						boolean isWrong = false == expectedSha1(text).equals(computed);
						if (isWrong) {
							wrongHashes++;
						}
					}
					return wrongHashes;
				};
				wrongHashesPerThread.add(executor.submit(hashing));
			}

			int wrongHashes = 0;
			for (Future<Integer> future : wrongHashesPerThread) {
				wrongHashes += future.get();
			}
			assertEquals("hashes corrupted by another thread", 0, wrongHashes);
		} finally {
			executor.shutdownNow();
		}
	}

	@Test
	public void eachThreadHasItsOwnDigest() throws Exception {
		MessageDigest fromThisThread = CcpHashAlgorithm.SHA1.getMessageDigest();
		AtomicReference<MessageDigest> fromAnotherThread = new AtomicReference<>();

		Thread another = new Thread(() -> fromAnotherThread.set(CcpHashAlgorithm.SHA1.getMessageDigest()));
		another.start();
		another.join();

		assertNotSame(fromThisThread, fromAnotherThread.get());
		assertSame("a thread reuses its own digest", fromThisThread, CcpHashAlgorithm.SHA1.getMessageDigest());
	}

	/** A digest left in the middle of a hash (bytes given, digest not finished) does not leak into the next hash. */
	@Test
	public void aDigestLeftHalfwayDoesNotSpoilTheNextHash() throws Exception {
		CcpHashAlgorithm.SHA1.getMessageDigest().update("leftover".getBytes(StandardCharsets.UTF_8));

		BigInteger computed = new CcpStringDecorator("jobsnow").hash().asBigInteger(CcpHashAlgorithm.SHA1);

		assertEquals(expectedSha1("jobsnow"), computed);
	}
}
