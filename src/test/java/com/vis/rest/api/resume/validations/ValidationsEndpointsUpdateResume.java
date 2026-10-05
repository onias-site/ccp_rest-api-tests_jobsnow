package com.vis.rest.api.resume.validations;


import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.http.CcpHttpMethods;
import com.ccp.process.CcpProcessStatusDefault;
import com.vis.commons.VisTestTemplate;

public class ValidationsEndpointsUpdateResume  extends VisTestTemplate{

//	@Test
	public void saveResume() {
		String scenarioName = new Object() {}.getClass().getEnclosingMethod().getName();
		
		String uri = ENDPOINT_URL + "/resume/{email}";
		CcpJsonRepresentation headers = super.getHeaders();
		CcpJsonRepresentation jsonReturnedByTheTest = super.getJsonResponseFromEndpoint(CcpProcessStatusDefault.UPDATED, scenarioName, headers, uri);
		System.out.println(jsonReturnedByTheTest);
	}

	protected CcpHttpMethods getMethod() {
		return CcpHttpMethods.PATCH;
	}

	protected String getUri() {
		// LATER Auto-generated method stub
		return null;
	}
	
}
