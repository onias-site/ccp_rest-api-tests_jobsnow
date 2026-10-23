package com.ccp.flow;

import java.util.List;

import com.ccp.business.CcpBusiness;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.process.CcpProcessStatusDefault;

/** Handler of {@link FlowRecoveryTest}: records its call and disturbs the flow with CONFLICT, carrying a JSON to merge. */
class FlowRecoveryDisturbingHandler implements CcpBusiness {

	/** Receives the name of each call. */
	private final List<String> calls;

	/** The field of the JSON carried by the disturbance. */
	private final CcpFieldName merged;

	FlowRecoveryDisturbingHandler(List<String> calls, CcpFieldName merged) {
		this.calls = calls;
		this.merged = merged;
	}

	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		this.calls.add("disturbing");
		throw new CcpErrorFlowDisturb(CcpOtherConstants.EMPTY_JSON.put(this.merged, 1), CcpProcessStatusDefault.CONFLICT);
	}
}
