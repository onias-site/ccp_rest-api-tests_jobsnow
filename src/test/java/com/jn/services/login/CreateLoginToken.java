package com.jn.services.login;

import org.junit.Test;

import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.process.CcpProcessStatusDefault;
import com.jn.entities.JnEntityLoginAnswers;
import com.jn.entities.JnEntityLoginEmail;
import com.jn.entities.JnEntityLoginToken;
import com.jn.rest.api.commons.TestVariables;
import com.jn.status.login.JnProcessStatusCreateLoginToken;
import com.jn.utils.JnLanguage;

public class CreateLoginToken extends JnServiceLoginTestTemplate {

	public void invalidEmail() {
		TestVariables testVariables = withInvalidEmail();
		CcpJsonRepresentation jsonWithLanguage = this.withLanguage(testVariables);
		this.execute(jsonWithLanguage, CcpProcessStatusDefault.UNPROCESSABLE_ENTITY);
	}

	@Test
	public void lockedToken() {
		TestVariables testVariables = new TestVariables();
		CcpEntity mirrorEntity = JnEntityLoginToken.ENTITY.getTwinEntity();
		mirrorEntity.save(testVariables.REQUEST_TO_LOGIN);
		CcpJsonRepresentation jsonWithLanguage = this.withLanguage(testVariables);
		this.execute(jsonWithLanguage, JnProcessStatusCreateLoginToken.statusLockedToken);
	}

	@Test
	public void missingToken() {
		TestVariables testVariables = new TestVariables();
		CcpJsonRepresentation jsonWithLanguage = this.withLanguage(testVariables);
		this.execute(jsonWithLanguage, JnProcessStatusCreateLoginToken.statusMissingEmail);
	}

	@Test
	public void happyPath() {
		TestVariables testVariables = new TestVariables();
		JnEntityLoginEmail.ENTITY.save(testVariables.REQUEST_TO_LOGIN);
		JnEntityLoginAnswers.ENTITY.save(testVariables.ANSWERS_JSON);
		CcpJsonRepresentation jsonWithLanguage = this.withLanguage(testVariables);
		this.execute(jsonWithLanguage, JnProcessStatusCreateLoginToken.expectedStatus);
	}

	private com.ccp.decorators.CcpJsonRepresentation withLanguage(TestVariables testVariables) {
		return testVariables.REQUEST_TO_LOGIN.put(new CcpFieldName("language"), JnLanguage.portuguese.name());
	}
}
