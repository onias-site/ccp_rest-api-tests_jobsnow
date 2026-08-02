package com.jn.utils;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class JnLanguageTest {

	@Test
	public void portugueseExistsTest() {
		assertNotNull(JnLanguage.portuguese);
	}

	@Test
	public void englishExistsTest() {
		assertNotNull(JnLanguage.english);
	}

	@Test
	public void spanishExistsTest() {
		assertNotNull(JnLanguage.spanish);
	}

	@Test
	public void valuesTest() {
		assertNotNull(JnLanguage.values());
	}

	@Test
	public void valueOfTest() {
		assertNotNull(JnLanguage.valueOf("portuguese"));
	}
}
