package com.ccp.implementations.http.apache.mime;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class CustomContentTypeTest {

	@Test
	public void valoresEnumTest() {
		assertNotNull(CustomContentType.TEXT_PLAIN);
		assertNotNull(CustomContentType.TEXT_HTML);
	}

	@Test
	public void valuesTest() {
		CustomContentType[] all = CustomContentType.values();
		assertNotNull(all);
	}

	@Test
	public void valueOfTest() {
		CustomContentType t = CustomContentType.valueOf("TEXT_PLAIN");
		assertNotNull(t);
	}
}
