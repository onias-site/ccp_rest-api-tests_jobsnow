package com.js.entities;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.dependency.injection.CcpInstanceProvider;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityMetaData;
import com.ccp.especifications.db.utils.entity.decorators.engine.SilentInstantMessenger;
import com.ccp.especifications.email.CcpEmailProvider;
import com.ccp.especifications.instant.messenger.CcpInstantMessenger;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.implementations.password.mindrot.CcpMindrotPasswordHandler;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.jn.utils.JnGroupers;
import com.vis.entities.RecordingEmailSender;

/**
 * 2026-11-14: the answers of the sign-up questionnaire are counted in {@code jn_grouper} by goal and by channel, the
 * current state: the insertion adds 1 to its goal and to its channel, a save that changes the channel moves the count,
 * the delete subtracts it.
 */
public class LoginAnswersCounterTest {
	static {
		CcpInstanceProvider<CcpInstantMessenger> telegram = () -> SilentInstantMessenger.INSTANCE;
		CcpInstanceProvider<CcpEmailProvider> email = () -> RecordingEmailSender.INSTANCE;
		CcpDependencyInjection.removeAllDependencies();
		CcpDependencyInjection.loadAllDependencies(
				CcpLocalInstances.syncMensageriaListener,
				CcpLocalInstances.bucket,
				CcpElasticSearchQueryExecutor.INSTANCE,
				CcpElasticSearchDbRequest.INSTANCE,
				CcpMindrotPasswordHandler.INSTANCE,
				CcpLocalCacheInstances.mock,
				CcpElasticSerchDbBulk.INSTANCE,
				CcpElasticSearchCrud.INSTANCE,
				CcpGsonJsonHandler.INSTANCE,
				CcpApacheMimeHttp.INSTANCE,
				telegram,
				email
		);
	}

	private final String email = "loginAnswersCounter" + System.currentTimeMillis() + "@jobsnow.com";

	private CcpJsonRepresentation answers(String goal, String channel) {
		return CcpOtherConstants.EMPTY_JSON
				.put(JsEntityLoginAnswers.Fields.email, this.email)
				.put(JsEntityLoginAnswers.Fields.goal, goal)
				.put(JsEntityLoginAnswers.Fields.channel, channel);
	}

	private static long countOf(CcpEntity parentEntity, CcpJsonRepresentation combination, String key) {
		CcpEntityMetaData parentDetails = parentEntity.getEntityMetaData();
		CcpEntityMetaData childDetails = JsEntityLoginAnswers.ENTITY.getEntityMetaData();
		String parentId = parentEntity.calculateId(combination);
		CcpJsonRepresentation groupKey = JnGroupers.getGroupKey(parentDetails.entityName, childDetails.entityName, parentId);
		List<CcpJsonRepresentation> groupKeys = Arrays.asList(groupKey);
		List<List<String>> childrenOfEachGroup = JnGroupers.getChildrenOfEachGroup(groupKeys);
		List<String> children = childrenOfEachGroup.get(0);
		if (children.isEmpty()) {
			return 0L;
		}
		CcpJsonRepresentation counters = new CcpJsonRepresentation(children.get(0));
		CcpFieldName keyField = new CcpFieldName(key);
		return counters.containsAllFields(keyField) ? counters.getAsLongNumber(keyField) : 0L;
	}

	private static long goals(String goal) {
		return countOf(JsEntityVirtualLoginGoalCounter.ENTITY, JsEntityVirtualLoginGoalCounter.getCombination(), goal);
	}

	private static long channels(String channel) {
		return countOf(JsEntityVirtualLoginChannelCounter.ENTITY, JsEntityVirtualLoginChannelCounter.getCombination(), channel);
	}

	@After
	public void deleteTheAnswers() {
		JsEntityLoginAnswers.ENTITY.delete(this.answers("jobs", "friends"));
	}

	@Test
	public void theAnswersAreCountedByGoalAndByChannel() {
		long jobsBefore = goals("jobs");
		long telegramBefore = channels("telegram");
		long friendsBefore = channels("friends");

		JsEntityLoginAnswers.ENTITY.save(this.answers("jobs", "telegram"));
		assertEquals("the goal of the answers", jobsBefore + 1, goals("jobs"));
		assertEquals("the channel of the answers", telegramBefore + 1, channels("telegram"));

		JsEntityLoginAnswers.ENTITY.save(this.answers("jobs", "friends"));
		assertEquals("a save again is not a new answer", jobsBefore + 1, goals("jobs"));
		assertEquals("the answers left the channel", telegramBefore, channels("telegram"));
		assertEquals("and went to the new one", friendsBefore + 1, channels("friends"));

		JsEntityLoginAnswers.ENTITY.delete(this.answers("jobs", "friends"));
		assertEquals(jobsBefore, goals("jobs"));
		assertEquals(friendsBefore, channels("friends"));
	}
}
