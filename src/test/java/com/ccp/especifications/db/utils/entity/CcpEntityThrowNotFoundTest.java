package com.ccp.especifications.db.utils.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.flow.CcpErrorFlowDisturb;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.process.CcpProcessStatusDefault;
import com.vis.entities.VisEntitySkillSynonym;

/** {@link CcpEntityThrowNotFound} answers a record not found with NOT_FOUND, naming the entity. */
public class CcpEntityThrowNotFoundTest {

	static {
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE);
	}

	@Test
	public void theErrorIsNotFoundAndNamesTheEntity() {
		CcpJsonRepresentation context = CcpOtherConstants.EMPTY_JSON.put(VisEntitySkillSynonym.Fields.skill, "MISSING");
		try {
			new CcpEntityThrowNotFound(VisEntitySkillSynonym.ENTITY).execute(context);
			fail("the record not found was accepted");
		} catch (CcpErrorFlowDisturb e) {
			assertSame(CcpProcessStatusDefault.NOT_FOUND, e.status);
			assertEquals("MISSING", e.json.getAsString(VisEntitySkillSynonym.Fields.skill));
			Object entity = e.json.getAsObject(CcpEntity.JsonFieldNames.entity);
			assertSame(VisEntitySkillSynonym.ENTITY, entity);
		}
	}
}
