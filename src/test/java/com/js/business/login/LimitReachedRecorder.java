package com.js.business.login;

import java.util.List;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;

/** Business of the limit of {@link SessionTokenAttemptsBusinessesTest}: keeps each json it runs with and answers it. */
class LimitReachedRecorder implements CcpBusiness {

	/** Receives each json. */
	private final List<CcpJsonRepresentation> received;

	LimitReachedRecorder(List<CcpJsonRepresentation> received) {
		this.received = received;
	}

	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		this.received.add(json);
		return json;
	}
}
