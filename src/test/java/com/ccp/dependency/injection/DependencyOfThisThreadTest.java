package com.ccp.dependency.injection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

import com.ccp.business.CcpBusiness;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.mensageria.sender.CcpMensageriaSender;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.implementations.mensageria.sender.gcp.pubsub.CcpGcpPubSubMensageriaSender;
import com.ccp.local.testings.implementations.CcpLocalInstances;

/**
 * Proves {@link CcpDependencyInjection#executeWithDependencyOfThisThread} (since 2026-11-05): the business sees the
 * implementation of its thread, the other threads keep the registry, and the thread gets back what it saw before, also
 * when the business throws. It is what lets the consumers of the bot queues publish through Pub/Sub while the others,
 * in the same JVM, keep the synchronous sender.
 */
public class DependencyOfThisThreadTest {

	static {
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE);
	}

	/** Fields of the answers. */
	enum Fields implements CcpJsonFieldName {
		/** What the business saw. */
		seen
	}

	/** An interface that no other test registers. */
	public interface Speaker {
		/** @return a word that identifies the implementation */
		String speak();
	}

	/** The implementation of the registry. */
	public static class RegistrySpeaker implements Speaker, CcpInstanceProvider<Speaker> {
		public String speak() {
			return "registry";
		}
		public Speaker getInstance() {
			return this;
		}
	}

	/** The implementation of the thread. */
	public static class ThreadSpeaker implements Speaker {
		public String speak() {
			return "thread";
		}
	}

	/** Answers what the thread sees. */
	static class WhatTheThreadSees implements CcpBusiness {
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			Speaker speaker = CcpDependencyInjection.getDependency(Speaker.class);
			String word = speaker.speak();
			CcpJsonRepresentation answer = json.put(Fields.seen, word);
			return answer;
		}
	}

	/** Records what another thread sees while the business runs. */
	static class WhatAnotherThreadSees implements CcpBusiness {
		final AtomicReference<String> seenByTheOther = new AtomicReference<>();
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			Thread other = new Thread(() -> this.seenByTheOther.set(CcpDependencyInjection.getDependency(Speaker.class).speak()));
			other.start();
			try {
				other.join();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			return json;
		}
	}

	/** Fails after seeing the implementation of the thread. */
	static class FailingBusiness implements CcpBusiness {
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			throw new IllegalStateException("failed on purpose");
		}
	}

	private String seen(CcpJsonRepresentation answer) {
		String word = answer.getAsString(Fields.seen);
		return word;
	}

	@Test
	public void theBusinessSeesTheImplementationOfItsThreadAndTheRegistryComesBack() {
		CcpDependencyInjection.loadAllDependencies(new RegistrySpeaker());

		CcpJsonRepresentation answer = CcpDependencyInjection.executeWithDependencyOfThisThread(CcpOtherConstants.EMPTY_JSON, new WhatTheThreadSees(), new ThreadSpeaker());

		assertEquals("thread", this.seen(answer));
		CcpJsonRepresentation afterwards = new WhatTheThreadSees().execute(CcpOtherConstants.EMPTY_JSON);
		assertEquals("registry", this.seen(afterwards));
	}

	@Test
	public void anotherThreadKeepsTheRegistryWhileTheBusinessRuns() {
		CcpDependencyInjection.loadAllDependencies(new RegistrySpeaker());
		WhatAnotherThreadSees business = new WhatAnotherThreadSees();

		CcpDependencyInjection.executeWithDependencyOfThisThread(CcpOtherConstants.EMPTY_JSON, business, new ThreadSpeaker());

		assertEquals("registry", business.seenByTheOther.get());
	}

	@Test
	public void theThreadGetsBackWhatItSawAlsoWhenTheBusinessThrows() {
		CcpDependencyInjection.loadAllDependencies(new RegistrySpeaker());
		try {
			CcpDependencyInjection.executeWithDependencyOfThisThread(CcpOtherConstants.EMPTY_JSON, new FailingBusiness(), new ThreadSpeaker());
			fail();
		} catch (IllegalStateException expected) {
		}
		CcpJsonRepresentation afterwards = new WhatTheThreadSees().execute(CcpOtherConstants.EMPTY_JSON);
		assertEquals("registry", this.seen(afterwards));
	}

	/** The consumers of the bot queues: the Pub/Sub sender seen by their thread, the synchronous one in the registry. */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void theSenderOfTheColumnSenderClassIsNotRegistered() {
		CcpDependencyInjection.loadAllDependencies(CcpLocalInstances.syncMensageriaListener);
		CcpMensageriaSender registered = CcpDependencyInjection.getDependency(CcpMensageriaSender.class);
		Class providerClass = CcpGcpPubSubMensageriaSender.class;

		CcpMensageriaSender pubSub = CcpDependencyInjection.getInstance((Class<CcpInstanceProvider<CcpMensageriaSender>>) providerClass);

		assertFalse(pubSub.getClass().equals(registered.getClass()));
		assertTrue(pubSub.getClass().getName().endsWith("GcpPubSubMensageriaSender"));
		assertEquals(registered, CcpDependencyInjection.getDependency(CcpMensageriaSender.class));
	}
}
