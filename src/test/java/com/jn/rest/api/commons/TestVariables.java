package com.jn.rest.api.commons;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpStringDecorator;
import com.js.entities.JsEntityLoginAnswers;
import com.js.entities.JsEntityLoginSessionConflict;
import com.js.entities.JsEntityLoginSessionValidation;
import com.js.entities.fields.transformers.JsJsonTransformersFieldEntityTokenHash;
import com.jn.entities.fields.transformers.JnJsonTransformersFieldsEntityDefault;
import com.jn.json.fields.validation.JnJsonCommonsFields;

public class TestVariables {
	public final static String CORRECT_PASSWORD = "Jobsnow1!";
	public final static String WRONG_PASSWORD = "Novasenha1!";
	public final static String INCORRECT_TOKEN_TO_SAVE_PASSWORD = "qualquerCoisa";
	public final static String INVALID_EMAIL = "devs.jobsnowgmail.com";

	
	public CcpJsonRepresentation REQUEST_TO_LOGIN = CcpOtherConstants.EMPTY_JSON
			.put(JnJsonCommonsFields.userAgent, "Apache-HttpClient/4.5.4 (Java/17.0.9)")
			.put(JnJsonCommonsFields.ip, "127.0.0.1")
			.put(JnJsonCommonsFields.password, "Novasenha1!")
			// the transformer only hashes a token it is given (since 2026-10-08)
			.put(JnJsonCommonsFields.originalToken, JnJsonTransformersFieldsEntityDefault.getOriginalToken())
			.getTransformedJson(JnJsonTransformersFieldsEntityDefault.token)
			;
			
	public final String VALID_EMAIL;

	public final CcpJsonRepresentation ANSWERS_JSON;
	


	public TestVariables() {
		this(new CcpStringDecorator("ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toLowerCase()).text().generateToken(8).content + "@teste.com");
	}
	public TestVariables(String email) {
	
		this.REQUEST_TO_LOGIN = this.REQUEST_TO_LOGIN.put(JnJsonCommonsFields.email, email);
		this.VALID_EMAIL = email;
		this.ANSWERS_JSON = this.REQUEST_TO_LOGIN.put(JsEntityLoginAnswers.Fields.goal, "jobs").put(JsEntityLoginAnswers.Fields.channel, "linkedin");
	}

	/**
	 * Saves an open session of this e-mail with the given token, the way a login does: the session itself and the record
	 * that makes it the current session of the e-mail (since 2026-10-11 only the current session is accepted).
	 * @param sessionToken the session token
	 * @return the session as saved (raw, before the transformers)
	 */
	public CcpJsonRepresentation saveCurrentSession(String sessionToken) {
		CcpJsonRepresentation session = this.REQUEST_TO_LOGIN.put(JsEntityLoginSessionValidation.Fields.token, sessionToken);
		JsJsonTransformersFieldEntityTokenHash tokenHash = new JsJsonTransformersFieldEntityTokenHash();
		CcpJsonRepresentation sessionWithTokenHash = tokenHash.execute(session);
		String sessionTokenHash = sessionWithTokenHash.getAsString(JsEntityLoginSessionValidation.Fields.token);
		CcpJsonRepresentation currentSession = this.REQUEST_TO_LOGIN.put(JsEntityLoginSessionConflict.Fields.currentSessionTokenHash, sessionTokenHash);
		JsEntityLoginSessionConflict.ENTITY.save(currentSession);
		JsEntityLoginSessionValidation.ENTITY.save(session);
		return session;
	}
}
