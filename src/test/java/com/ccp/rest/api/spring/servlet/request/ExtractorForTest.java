package com.ccp.rest.api.spring.servlet.request;

final class ExtractorForTest implements CcpJsonExtractorFromHttpServletRequest {
	public final static ExtractorForTest INSTANCE = new ExtractorForTest();

	private ExtractorForTest() {}

}
