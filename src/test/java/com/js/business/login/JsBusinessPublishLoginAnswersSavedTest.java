package com.js.business.login;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityDecoratorOperationType;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.implementations.password.mindrot.CcpMindrotPasswordHandler;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.jn.entities.JnEntityWriteEventSubscribers;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.js.entities.JsEntityLoginAnswers;

/**
 * {@link JsBusinessPublishLoginAnswersSaved}, run after the save of {@code js_login_answers}: it hands the saved
 * answers to every subscriber recorded for that event in {@code jn_write_event_subscribers} (vis subscribes there to
 * register the recruiter without js importing vis), and does nothing when the event has no subscriber. The test swaps
 * the subscribers of the event for {@link LoginAnswersSavedRecorder} and puts the original record back afterwards.
 */
public class JsBusinessPublishLoginAnswersSavedTest {

	static {
		CcpDependencyInjection.removeAllDependencies();
		CcpDependencyInjection.loadAllDependencies(
				CcpLocalInstances.syncMensageriaListener,
				CcpLocalInstances.bucket,
				CcpLocalInstances.email,
				CcpLocalInstances.instantMessenger,
				CcpElasticSearchQueryExecutor.INSTANCE,
				CcpElasticSearchDbRequest.INSTANCE,
				CcpMindrotPasswordHandler.INSTANCE,
				CcpLocalCacheInstances.mock,
				CcpElasticSerchDbBulk.INSTANCE,
				CcpElasticSearchCrud.INSTANCE,
				CcpGsonJsonHandler.INSTANCE,
				CcpApacheMimeHttp.INSTANCE
		);
	}

	private final CcpJsonRepresentation eventKey = JnEntityWriteEventSubscribers.getEventKey(JsEntityLoginAnswers.ENTITY, CcpEntityDecoratorOperationType.save);

	private final CcpJsonRepresentation savedAnswers = CcpOtherConstants.EMPTY_JSON
			.put(JnJsonCommonsFields.email, "respostas.publicadas@teste.com")
			.put(JsEntityLoginAnswers.Fields.goal, "jobs")
			.put(JsEntityLoginAnswers.Fields.channel, "linkedin");

	/** The subscribers of the event as they were before the test, or an empty json when the event had none. */
	private CcpJsonRepresentation originalSubscribers = CcpOtherConstants.EMPTY_JSON;

	@Before
	public void keepTheOriginalSubscribers() {
		LoginAnswersSavedRecorder.RECEIVED.clear();
		boolean recorded = JnEntityWriteEventSubscribers.ENTITY.exists(this.eventKey);
		if(recorded) {
			this.originalSubscribers = JnEntityWriteEventSubscribers.ENTITY.getOneById(this.eventKey);
		}
	}

	@After
	public void restoreTheOriginalSubscribers() {
		LoginAnswersSavedRecorder.RECEIVED.clear();
		boolean hadNoSubscribers = this.originalSubscribers.isEmpty();
		if(hadNoSubscribers) {
			JnEntityWriteEventSubscribers.ENTITY.delete(this.eventKey);
			return;
		}
		JnEntityWriteEventSubscribers.ENTITY.save(this.originalSubscribers);
	}

	private void subscribe(String... subscribers) {
		CcpJsonRepresentation event = this.eventKey.put(JnEntityWriteEventSubscribers.Fields.subscribers, Arrays.asList(subscribers));
		JnEntityWriteEventSubscribers.ENTITY.save(event);
	}

	@Test
	public void theSavedAnswersReachEachSubscriberOfTheEvent() {
		this.subscribe(LoginAnswersSavedRecorder.class.getName());

		CcpJsonRepresentation answer = new JsBusinessPublishLoginAnswersSaved().execute(this.savedAnswers);

		assertSame(this.savedAnswers, answer);
		assertEquals(1, LoginAnswersSavedRecorder.RECEIVED.size());
		CcpJsonRepresentation received = LoginAnswersSavedRecorder.RECEIVED.get(0);
		assertEquals("respostas.publicadas@teste.com", received.getAsString(JnJsonCommonsFields.email));
		assertEquals("linkedin", received.getAsString(JsEntityLoginAnswers.Fields.channel));
	}

	@Test
	public void withoutSubscribersNothingIsPublished() {
		JnEntityWriteEventSubscribers.ENTITY.delete(this.eventKey);

		CcpJsonRepresentation answer = new JsBusinessPublishLoginAnswersSaved().execute(this.savedAnswers);

		assertSame(this.savedAnswers, answer);
		assertTrue(LoginAnswersSavedRecorder.RECEIVED.isEmpty());
	}
}
