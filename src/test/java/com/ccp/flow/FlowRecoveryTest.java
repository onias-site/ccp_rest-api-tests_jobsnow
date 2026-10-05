package com.ccp.flow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.ccp.business.CcpBusiness;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpErrorJsonFieldNotFound;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.process.CcpProcessStatusDefault;

/**
 * Proves how {@link CcpTreeFlow} recovers from a {@link CcpErrorFlowDisturb}: the handlers of the thrown status run in
 * order, a handler that itself disturbs the flow has its JSON merged, the main process runs again with the fixed JSON,
 * and a status without handler (or already handled) ends in {@link CcpErrorJsonFieldNotFound}.
 */
public class FlowRecoveryTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	private static final CcpFieldName fixed = new CcpFieldName("fixed");

	private static final CcpFieldName merged = new CcpFieldName("merged");

	private static final List<String> calls = new ArrayList<>();

	private static final CcpBusiness mainProcess = json -> {
		calls.add("main");
		if (json.containsAllFields(fixed)) {
			return json;
		}
		throw new CcpErrorFlowDisturb(json, CcpProcessStatusDefault.NOT_FOUND);
	};

	private static final CcpBusiness fixer = json -> {
		calls.add("fixer");
		return json.put(fixed, true);
	};

	private static final CcpBusiness disturbingHandler = json -> {
		calls.add("disturbing");
		throw new CcpErrorFlowDisturb(CcpOtherConstants.EMPTY_JSON.put(merged, 1), CcpProcessStatusDefault.CONFLICT);
	};

	@Before
	public void forgetTheCalls() {
		calls.clear();
	}

	@Test
	public void theHandlersFixTheJsonAndTheMainProcessRunsAgain() {
		CcpBusiness next = json -> {
			calls.add("next " + json.containsAllFields(fixed, merged));
			return json;
		};

		CcpJsonRepresentation result = CcpTreeFlow.beginThisStatement()
				.tryToExecuteTheGivenFinalTargetProcess(mainProcess)
				.usingTheGivenJson(CcpOtherConstants.EMPTY_JSON)
				.butIfThisExecutionReturns(CcpProcessStatusDefault.NOT_FOUND)
				.thenExecuteTheGivenProcesses(disturbingHandler, fixer)
				.and().endThisStatement(next);

		assertTrue(result.toString(), result.containsAllFields(fixed, merged));
		assertEquals("[main, disturbing, fixer, main, next true]", calls.toString());
	}

	@Test
	public void aStatusWithoutHandlerEndsInMissingField() {
		try {
			CcpTreeFlow.beginThisStatement()
					.tryToExecuteTheGivenFinalTargetProcess(mainProcess)
					.usingTheGivenJson(CcpOtherConstants.EMPTY_JSON)
					.butIfThisExecutionReturns(CcpProcessStatusDefault.CONFLICT)
					.thenExecuteTheGivenProcesses(fixer)
					.and().endThisStatement();
			fail("NOT_FOUND has no handler");
		} catch (CcpErrorJsonFieldNotFound e) {
			assertEquals("[main]", calls.toString());
		}
	}

	@Test
	public void aStatusIsHandledOnlyOnce() {
		CcpBusiness doesNotFix = json -> {
			calls.add("doesNotFix");
			return json;
		};
		try {
			CcpTreeFlow.beginThisStatement()
					.tryToExecuteTheGivenFinalTargetProcess(mainProcess)
					.usingTheGivenJson(CcpOtherConstants.EMPTY_JSON)
					.butIfThisExecutionReturns(CcpProcessStatusDefault.NOT_FOUND)
					.thenExecuteTheGivenProcesses(doesNotFix)
					.and().endThisStatement();
			fail("the second NOT_FOUND finds its handler already used");
		} catch (CcpErrorJsonFieldNotFound e) {
			assertEquals("[main, doesNotFix, main]", calls.toString());
		}
	}
}
