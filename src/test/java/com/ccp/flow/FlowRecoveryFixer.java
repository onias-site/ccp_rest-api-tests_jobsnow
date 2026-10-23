package com.ccp.flow;

import java.util.List;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;

/** Handler of {@link FlowRecoveryTest}: records its call and marks the JSON as fixed. */
class FlowRecoveryFixer implements CcpBusiness {

	/** Receives the name of each call. */
	private final List<String> calls;

	/** The field that marks the JSON as fixed. */
	private final CcpFieldName fixed;

	FlowRecoveryFixer(List<String> calls, CcpFieldName fixed) {
		this.calls = calls;
		this.fixed = fixed;
	}

	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		this.calls.add("fixer");
		return json.put(this.fixed, true);
	}
}
