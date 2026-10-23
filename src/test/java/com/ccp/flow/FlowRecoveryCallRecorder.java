package com.ccp.flow;

import java.util.List;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;

/** Handler of {@link FlowRecoveryTest} that fixes nothing: records its call and returns the JSON as it is. */
class FlowRecoveryCallRecorder implements CcpBusiness {

	/** Receives the name of each call. */
	private final List<String> calls;

	/** The name recorded. */
	private final String callName;

	FlowRecoveryCallRecorder(List<String> calls, String callName) {
		this.calls = calls;
		this.callName = callName;
	}

	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		this.calls.add(this.callName);
		return json;
	}
}
