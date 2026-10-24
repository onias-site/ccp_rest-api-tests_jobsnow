package com.js.business.login;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;

/**
 * Subscriber of the save of {@code js_login_answers} used by {@link JsBusinessPublishLoginAnswersSavedTest}: the
 * publication runs it by its class name, so what it receives goes to a static list the test reads.
 */
public class LoginAnswersSavedRecorder implements CcpBusiness {

	/** Each json received, in order. */
	static final List<CcpJsonRepresentation> RECEIVED = new CopyOnWriteArrayList<>();

	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		RECEIVED.add(json);
		return json;
	}
}
