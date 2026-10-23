package com.vis.business.resume;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;

/** Save of {@link ResumeViewersOfTheMonthTest} that waits for the start signal, so that every thread saves at the same moment. */
class SaveAfterTheStart implements Callable<Boolean> {

	/** The start signal. */
	private final CountDownLatch start;

	/** The entity saved. */
	private final CcpEntity entity;

	/** The record saved. */
	private final CcpJsonRepresentation json;

	SaveAfterTheStart(CountDownLatch start, CcpEntity entity, CcpJsonRepresentation json) {
		this.start = start;
		this.entity = entity;
		this.json = json;
	}

	public Boolean call() throws InterruptedException {
		this.start.await();
		return this.entity.save(this.json);
	}
}
