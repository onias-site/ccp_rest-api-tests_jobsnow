package com.ccp.implementations.http.apache.mime;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class CustomContentTypeTest {

	@Test
	public void enumValuesTest() {
		assertNotNull(CustomContentType.TEXT_PLAIN);
		assertNotNull(CustomContentType.TEXT_HTML);
	}

	@Test
	public void valuesTest() {
		CustomContentType[] allContentTypes = CustomContentType.values();
		assertNotNull(allContentTypes);
	}

	@Test
	public void valueOfTest() {
		CustomContentType contentType = CustomContentType.valueOf("TEXT_PLAIN");
		assertNotNull(contentType);
	}
}
