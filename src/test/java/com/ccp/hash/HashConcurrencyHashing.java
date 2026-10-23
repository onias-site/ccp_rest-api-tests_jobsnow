package com.ccp.hash;

import java.math.BigInteger;
import java.util.concurrent.Callable;

import com.ccp.decorators.CcpStringDecorator;

/** Work of one thread of {@link HashConcurrencyTest}: computes its hashes and counts the wrong ones. */
class HashConcurrencyHashing implements Callable<Integer> {

	/** The number of the thread, part of every text hashed. */
	private final int threadNumber;

	/** How many hashes the thread computes. */
	private final int hashesPerThread;

	HashConcurrencyHashing(int threadNumber, int hashesPerThread) {
		this.threadNumber = threadNumber;
		this.hashesPerThread = hashesPerThread;
	}

	public Integer call() throws Exception {
		int wrongHashes = 0;
		for (int k = 0; k < this.hashesPerThread; k++) {
			String text = "thread " + this.threadNumber + " record " + k + " " + "x".repeat(k % 50);
			BigInteger computed = new CcpStringDecorator(text).hash().asBigInteger(CcpHashAlgorithm.SHA1);
			boolean isWrong = false == HashConcurrencyTest.expectedSha1(text).equals(computed);
			if (isWrong) {
				wrongHashes++;
			}
		}
		return wrongHashes;
	}
}
