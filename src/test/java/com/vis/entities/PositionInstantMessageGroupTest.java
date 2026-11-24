package com.vis.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.json.validations.global.engine.CcpJsonValidationError;
import com.ccp.json.validations.global.engine.CcpJsonValidatorEngine;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.vis.business.position.VisBusinessPutPositionInstantMessageGroupDefaultValues;

/**
 * The groups of instant messages of the positions (2026-11-23): one record per seniority, area and ddd, with the chats
 * of the groups and the bot ({@code user} by default).
 */
public class PositionInstantMessageGroupTest {

	static {
		CcpDependencyInjection.loadAllDependencies(
				CcpGsonJsonHandler.INSTANCE,
				CcpElasticSearchDbRequest.INSTANCE,
				CcpElasticSearchCrud.INSTANCE,
				CcpApacheMimeHttp.INSTANCE,
				CcpLocalCacheInstances.mock
		);
	}

	private CcpJsonRepresentation group() {
		return CcpOtherConstants.EMPTY_JSON
				.put(VisEntityPositionInstantMessageGroup.Fields.seniority, "pleno")
				.put(VisEntityPositionInstantMessageGroup.Fields.resumeType, 1)
				.put(VisEntityPositionInstantMessageGroup.Fields.ddd, 11)
				.put(VisEntityPositionInstantMessageGroup.Fields.chatId, List.of(-1001234567890L));
	}

	private boolean accepted(CcpJsonRepresentation group) {
		try {
			CcpJsonValidatorEngine.INSTANCE.validateJson(VisEntityPositionInstantMessageGroup.Fields.class, group, "group");
			return true;
		} catch (CcpJsonValidationError e) {
			return false;
		}
	}

	@Test
	public void theFieldsFollowTheRulesOfThePosition() {
		CcpJsonRepresentation withTheBot = this.group().put(VisEntityPositionInstantMessageGroup.Fields.botType, "user");
		assertTrue("a complete group", this.accepted(withTheBot));
		assertTrue("the support bot", this.accepted(withTheBot.put(VisEntityPositionInstantMessageGroup.Fields.botType, "support")));
		assertFalse("a bot out of the enum", this.accepted(withTheBot.put(VisEntityPositionInstantMessageGroup.Fields.botType, "other")));
		assertFalse("a seniority out of the enum", this.accepted(withTheBot.put(VisEntityPositionInstantMessageGroup.Fields.seniority, "trainee")));
		assertFalse("an area out of the enum", this.accepted(withTheBot.put(VisEntityPositionInstantMessageGroup.Fields.resumeType, 9)));
		assertFalse("a ddd that does not exist", this.accepted(withTheBot.put(VisEntityPositionInstantMessageGroup.Fields.ddd, 23)));
		assertFalse("no chat", this.accepted(withTheBot.put(VisEntityPositionInstantMessageGroup.Fields.chatId, List.of())));
		assertFalse("the chats are required", this.accepted(withTheBot.removeFields(VisEntityPositionInstantMessageGroup.Fields.chatId)));
	}

	@Test
	public void theBotIsTheUserByDefault() {
		CcpJsonRepresentation withDefaults = VisBusinessPutPositionInstantMessageGroupDefaultValues.INSTANCE.execute(this.group());
		assertEquals("user", withDefaults.getAsString(VisEntityPositionInstantMessageGroup.Fields.botType));
		CcpJsonRepresentation support = this.group().put(VisEntityPositionInstantMessageGroup.Fields.botType, "support");
		CcpJsonRepresentation kept = VisBusinessPutPositionInstantMessageGroupDefaultValues.INSTANCE.execute(support);
		assertEquals("support", kept.getAsString(VisEntityPositionInstantMessageGroup.Fields.botType));
	}

	@Test
	public void theSaveKeepsOneRecordPerSeniorityAreaAndDdd() {
		CcpJsonRepresentation group = this.group().put(VisEntityPositionInstantMessageGroup.Fields.seniority, "unknown").put(VisEntityPositionInstantMessageGroup.Fields.ddd, 99);
		try {
			VisEntityPositionInstantMessageGroup.ENTITY.save(group);
			CcpJsonRepresentation saved = VisEntityPositionInstantMessageGroup.ENTITY.getOneById(group);
			assertEquals("the default bot", "user", saved.getAsString(VisEntityPositionInstantMessageGroup.Fields.botType));
			assertEquals(1, saved.getAsObjectList(VisEntityPositionInstantMessageGroup.Fields.chatId).size());
			String otherDdd = VisEntityPositionInstantMessageGroup.ENTITY.calculateId(group.put(VisEntityPositionInstantMessageGroup.Fields.ddd, 98));
			String sameKey = VisEntityPositionInstantMessageGroup.ENTITY.calculateId(group.put(VisEntityPositionInstantMessageGroup.Fields.chatId, List.of(1L)));
			String id = VisEntityPositionInstantMessageGroup.ENTITY.calculateId(group);
			assertFalse(id.equals(otherDdd));
			assertEquals("the chats are not part of the key", id, sameKey);
		} finally {
			VisEntityPositionInstantMessageGroup.ENTITY.delete(group);
		}
	}
}
