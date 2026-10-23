package com.ccp.flow;

import java.util.List;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.process.CcpProcessStatusDefault;

/** Main process of {@link FlowRecoveryTest}: records its call and disturbs the flow with NOT_FOUND until the JSON is fixed. */
class FlowRecoveryMainProcess implements CcpBusiness {

	/** Receives the name of each call. */
	private final List<String> calls;

	/** The field that marks the JSON as fixed. */
	private final CcpFieldName fixed;

	FlowRecoveryMainProcess(List<String> calls, CcpFieldName fixed) {
		this.calls = calls;
		this.fixed = fixed;
	}

	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		this.calls.add("main");
		if (json.containsAllFields(this.fixed)) {
			return json;
		}
		throw new CcpErrorFlowDisturb(json, CcpProcessStatusDefault.NOT_FOUND);
	}
}
