package com.ccp.flow;

import java.util.List;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;

/** Process after the flow of {@link FlowRecoveryTest}: records whether the JSON came fixed and merged. */
class FlowRecoveryNext implements CcpBusiness {

	/** Receives the name of each call. */
	private final List<String> calls;

	/** The fields the JSON must have. */
	private final CcpFieldName[] expectedFields;

	FlowRecoveryNext(List<String> calls, CcpFieldName... expectedFields) {
		this.calls = calls;
		this.expectedFields = expectedFields;
	}

	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		this.calls.add("next " + json.containsAllFields(this.expectedFields));
		return json;
	}
}
