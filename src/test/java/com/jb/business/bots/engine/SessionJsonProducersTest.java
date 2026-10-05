package com.jb.business.bots.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.jb.entities.JbEntityBotCommandStepSession;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/**
 * Proves the transformations of the session JSON of the bots ({@link JsonProducers}) and its conditions
 * ({@link JsonConditions}): the session fields stay at the root and every other value goes into {@code json}, which is
 * read back as JSON when it holds one and wrapped otherwise.
 */
public class SessionJsonProducersTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	private final CcpFieldName answer = new CcpFieldName("answer");

	@Test
	public void theSessionKeepsItsFieldsAndPutsEveryOtherValueInTheJsonField() {
		CcpJsonRepresentation newJson = CcpOtherConstants.EMPTY_JSON
				.put(JbEntityBotCommandStepSession.Fields.chatId, 1L)
				.put(JbEntityBotCommandStepSession.Fields.stepName, "step")
				.put(this.answer, "yes");

		CcpJsonRepresentation session = JsonProducers.sessionValuesProducer.execute(newJson);

		assertEquals("step", session.getAsString(JbEntityBotCommandStepSession.Fields.stepName));
		assertFalse(session.containsField(this.answer));
		CcpJsonRepresentation inner = session.getInnerJson(JnJsonCommonsFields.json);
		assertEquals("yes", inner.getAsString(this.answer));
	}

	@Test
	public void aJsonFieldHoldingAJsonIsReadBackAsJson() {
		CcpJsonRepresentation session = CcpOtherConstants.EMPTY_JSON.put(JnJsonCommonsFields.json, "{\"answer\":\"yes\"}");

		assertTrue(JsonConditions.IfFieldExists.test(session));
		assertTrue(JsonConditions.thisFieldIsValidJson.test(session));
		CcpJsonRepresentation handled = JsonProducers.handleInnerJson.execute(session);

		assertEquals("yes", handled.getAsString(this.answer));
	}

	@Test
	public void aJsonFieldHoldingATextIsWrapped() {
		CcpJsonRepresentation session = CcpOtherConstants.EMPTY_JSON.put(JnJsonCommonsFields.json, "plain text");

		assertFalse(JsonConditions.thisFieldIsValidJson.test(session));
		CcpJsonRepresentation handled = JsonProducers.handleInnerJson.execute(session);

		assertEquals("plain text", handled.getAsString(JnJsonCommonsFields.json));
		assertEquals("plain text", JsonProducers.createInnerJson.execute(session).getAsString(JnJsonCommonsFields.json));
	}

	@Test
	public void withoutTheJsonFieldTheConditionFails() {
		assertFalse(JsonConditions.IfFieldExists.test(CcpOtherConstants.EMPTY_JSON));
	}
}
