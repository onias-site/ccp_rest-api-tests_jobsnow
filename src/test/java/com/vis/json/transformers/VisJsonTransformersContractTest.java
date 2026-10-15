package com.vis.json.transformers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

/**
 * The contract of the field transformers of the resume, besides the transformation itself (proved by
 * {@code ResumeFieldTransformersTest}): each one is named after the field it transforms, and none can be a primary key,
 * since the value saved is not the value typed.
 */
public class VisJsonTransformersContractTest {

	@Test
	public void theExperienceTransformerIsTheOneOfTheExperienceFieldAndIsNotAPrimaryKey() {
		VisJsonTransformerExperienceStartYear transformer = new VisJsonTransformerExperienceStartYear();
		assertEquals("experience", transformer.name());
		assertFalse(transformer.canBePrimaryKey());
	}

	@Test
	public void theSpokenLanguageTransformerIsTheOneOfTheSpokenLanguageFieldAndIsNotAPrimaryKey() {
		VisJsonTransformerSpokenLanguageWithoutId transformer = new VisJsonTransformerSpokenLanguageWithoutId();
		assertEquals("spokenLanguage", transformer.name());
		assertFalse(transformer.canBePrimaryKey());
	}
}
