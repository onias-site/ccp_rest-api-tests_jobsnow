package com.jn.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.bulk.CcpBulkEntityOperationType;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.bulk.CcpBulkScriptedUpdateFields;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.jn.entities.JnEntityGrouper;

/**
 * The scripted updates of the groups of a parent: {@link JnGroupersAddChildItem} adds the child, creating the group
 * with it and with the json of the parent when there is none; {@link JnGroupersRemoveChildItem} removes it, never
 * creating the group.
 */
public class JnGroupersChildItemsTest {

	static {
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE);
	}

	private static final String PARENT_ID = "parent1";

	private final CcpJsonRepresentation parentJson = CcpOtherConstants.EMPTY_JSON.put(new CcpFieldName("hash"), "abc");

	private final CcpJsonRepresentation childIdParams = CcpOtherConstants.EMPTY_JSON.put(new CcpFieldName("childId"), "child1");

	@Test
	public void theAdditionCreatesTheGroupWithTheChildAndTheJsonOfTheParent() {
		JnGroupersAddChildItem addition = new JnGroupersAddChildItem(JnEntityGrouper.ENTITY, "resumeByHash", "vis_resume", "child1",
				this.childIdParams, Map.of(PARENT_ID, this.parentJson));

		CcpBulkItem item = addition.apply(PARENT_ID);

		assertEquals(CcpBulkEntityOperationType.scriptedUpdate, item.operation);
		assertSame(JnEntityGrouper.ENTITY, item.entity);
		assertEquals(JnEntityGrouper.ENTITY.calculateId(JnGroupers.getGroupKey("resumeByHash", "vis_resume", PARENT_ID)), item.id);
		assertEquals(JnGroupers.Scripts.addChild.getValue(), item.json.getAsString(CcpBulkScriptedUpdateFields.script));
		assertFalse(item.json.getAsBoolean(CcpBulkScriptedUpdateFields.scriptedUpsert));
		CcpJsonRepresentation params = item.json.getInnerJson(CcpBulkScriptedUpdateFields.params);
		assertEquals("child1", params.getAsString(new CcpFieldName("childId")));
		assertEquals("abc", params.getInnerJson(JnGroupers.Scripts.parentJson).getAsString(new CcpFieldName("hash")));
		CcpJsonRepresentation newGroup = item.json.getInnerJson(CcpBulkScriptedUpdateFields.upsert);
		assertEquals(List.of("child1"), newGroup.getAsStringList(JnEntityGrouper.Fields.child));
		assertEquals(1, (int) newGroup.getAsIntegerNumber(JnEntityGrouper.Fields.childCount));
		assertEquals(PARENT_ID, newGroup.getAsString(JnEntityGrouper.Fields.parentId));
		assertEquals("abc", newGroup.getInnerJson(JnEntityGrouper.Fields.parentJson).getAsString(new CcpFieldName("hash")));
	}

	@Test
	public void theRemovalNeverCreatesTheGroup() {
		JnGroupersRemoveChildItem removal = new JnGroupersRemoveChildItem(JnEntityGrouper.ENTITY, "resumeByHash", "vis_resume", JnGroupers.Scripts.removeChild, this.childIdParams);

		CcpBulkItem item = removal.apply(PARENT_ID);

		assertEquals(CcpBulkEntityOperationType.scriptedUpdate, item.operation);
		assertEquals(JnGroupers.Scripts.removeChild.getValue(), item.json.getAsString(CcpBulkScriptedUpdateFields.script));
		assertTrue(item.json.getAsBoolean(CcpBulkScriptedUpdateFields.scriptedUpsert));
		assertTrue(item.json.getInnerJson(CcpBulkScriptedUpdateFields.upsert).isEmpty());
		assertEquals("child1", item.json.getInnerJson(CcpBulkScriptedUpdateFields.params).getAsString(new CcpFieldName("childId")));
	}
}
