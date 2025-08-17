package com.vis.rest.api.resume.validations;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.http.CcpHttpMethods;
import com.ccp.process.CcpProcessStatusDefault;
import com.vis.commons.VisTemplateDeTestes;

public class ValidationsEndpointsUpdateResume  extends VisTemplateDeTestes{

	@Test
	public void saveResume() {
		String scenarioName = new Object() {}.getClass().getEnclosingMethod().getName();
		
		String uri = ENDPOINT_URL + "/resume/{email}";
		CcpJsonRepresentation headers = super.getHeaders();
		CcpJsonRepresentation jsonDeRetornoDoTeste = super.getJsonResponseFromEndpoint(CcpProcessStatusDefault.UPDATED, scenarioName, headers, uri);
		System.out.println(jsonDeRetornoDoTeste);
	}

	protected CcpHttpMethods getMethod() {
		return CcpHttpMethods.PATCH;
	}

	protected String getUri() {
		// FIXME Auto-generated method stub
		return null;
	}
	
}
