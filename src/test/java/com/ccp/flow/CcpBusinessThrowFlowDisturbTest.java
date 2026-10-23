package com.ccp.flow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.process.CcpProcessStatusDefault;

/** {@link CcpBusinessThrowFlowDisturb} interrupts the flow with its status, carrying its input. */
public class CcpBusinessThrowFlowDisturbTest {

	static {
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE);
	}

	@Test
	public void theErrorCarriesTheStatusAndTheInput() {
		CcpJsonRepresentation input = CcpOtherConstants.EMPTY_JSON.put(new CcpFieldName("name"), "x");
		try {
			new CcpBusinessThrowFlowDisturb(CcpProcessStatusDefault.CONFLICT).execute(input);
			fail("the flow was not interrupted");
		} catch (CcpErrorFlowDisturb e) {
			assertSame(CcpProcessStatusDefault.CONFLICT, e.status);
			assertEquals(input, e.json);
		}
	}
}
