package com.vis.business.resume;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.flow.CcpErrorFlowDisturb;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.process.CcpProcessStatusDefault;
import com.vis.entities.VisEntityResume;

/**
 * {@link VisBusinessRefuseResumeNotFound} lets the flow go on when the steps found the resume (something under
 * {@code _entities}) and refuses it as not found, carrying the json of the request, when they did not.
 */
public class VisBusinessRefuseResumeNotFoundTest {

	static {
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE);
	}

	private final CcpJsonRepresentation request = CcpOtherConstants.EMPTY_JSON.put(VisEntityResume.Fields.email, "candidato@jobsnow.com.br");

	@Test
	public void aFoundResumeGoesOn() {
		CcpJsonRepresentation stepsOutput = this.request.addToItem(CcpEntity.JsonFieldNames._entities, VisEntityResume.ENTITY, this.request);

		CcpJsonRepresentation result = new VisBusinessRefuseResumeNotFound(this.request).execute(stepsOutput);

		assertEquals(stepsOutput, result);
	}

	@Test
	public void aMissingResumeIsRefusedAsNotFound() {
		try {
			new VisBusinessRefuseResumeNotFound(this.request).execute(CcpOtherConstants.EMPTY_JSON);
			fail();
		} catch (CcpErrorFlowDisturb e) {
			assertEquals(CcpProcessStatusDefault.NOT_FOUND, e.status);
			assertSame(this.request, e.json);
		}
	}
}
