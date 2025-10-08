package com.jn.rest.api.commons;

import com.ccp.constantes.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpStringDecorator;
import com.jn.entities.JnEntityLoginAnswers;
import com.jn.entities.JnEntityLoginToken;

public class VariaveisParaTeste {
	public final static String CORRECT_PASSWORD = "Jobsnow1!";
	public final static String WRONG_PASSWORD = "Novasenha1!";
	public final static String INCORRECT_TOKEN_TO_SAVE_PASSWORD = "qualquerCoisa";
	public final static String INVALID_EMAIL = "devs.jobsnowgmail.com";

	
	public CcpJsonRepresentation REQUEST_TO_LOGIN = CcpOtherConstants.EMPTY_JSON
			.put(JnEntityLoginToken.Fields.userAgent, "Apache-HttpClient/4.5.4 (Java/17.0.9)")
			.put(JnEntityLoginToken.Fields.ip, "127.0.0.1")
			;
			
	public final String VALID_EMAIL;

	public final CcpJsonRepresentation ANSWERS_JSON;
	


	public VariaveisParaTeste() {
		this(new CcpStringDecorator("ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toLowerCase()).text().generateToken(8).content + "@teste.com");
	}
	public VariaveisParaTeste(String email) {
	
		this.REQUEST_TO_LOGIN = this.REQUEST_TO_LOGIN.put(JnEntityLoginAnswers.Fields.email, email);
		this.VALID_EMAIL = email;
		this.ANSWERS_JSON = this.REQUEST_TO_LOGIN.put(JnEntityLoginAnswers.Fields.goal, "jobs").put(JnEntityLoginAnswers.Fields.channel, "linkedin");
	}	
}
