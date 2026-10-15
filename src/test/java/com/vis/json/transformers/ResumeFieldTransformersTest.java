package com.vis.json.transformers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpTimeDecorator;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.vis.entities.VisEntityResume;
import com.vis.entities.VisEntityVirtualHashGrouper;
import com.vis.json.fields.validation.VisJsonCommonsFields;

/**
 * Proves the field transformers of the resume: the years of experience sent by the front end become the year the
 * experience started, and the {@code id} of each spoken language is dropped.
 */
public class ResumeFieldTransformersTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	private final CcpFieldName id = new CcpFieldName("id");
	private final CcpFieldName name = new CcpFieldName("name");
	private final CcpFieldName level = new CcpFieldName("level");

	@Test
	public void yearsOfExperienceBecomeTheYearTheExperienceStarted() {
		CcpJsonRepresentation resume = CcpOtherConstants.EMPTY_JSON.put(VisJsonCommonsFields.experience, 17);

		CcpJsonRepresentation transformed = new VisJsonTransformerExperienceStartYear().execute(resume);

		int currentYear = new CcpTimeDecorator().getYear();
		assertEquals(currentYear - 17, (int) transformed.getAsIntegerNumber(VisJsonCommonsFields.experience));
	}

	private String seniorityOf(CcpJsonRepresentation transformed) {
		CcpJsonRepresentation resume = transformed
				.put(VisEntityResume.Fields.resumeType, 1)
				.put(VisEntityResume.Fields.ddd, Arrays.asList(11));
		List<List<CcpJsonRepresentation>> valuesByField = new VisResumeHashCombinations().getValuesByField(resume);
		// the seniority is the last field of the combinations
		List<CcpJsonRepresentation> seniorities = valuesByField.get(valuesByField.size() - 1);
		assertEquals(1, seniorities.size());
		return seniorities.get(0).getAsString(VisEntityVirtualHashGrouper.Fields.seniority);
	}

	@Test
	public void firstJobStartsTheExperienceInTheCurrentYearAndIsAnIntern() {
		CcpJsonRepresentation resume = CcpOtherConstants.EMPTY_JSON.put(VisJsonCommonsFields.experience, 0);

		CcpJsonRepresentation transformed = new VisJsonTransformerExperienceStartYear().execute(resume);

		int currentYear = new CcpTimeDecorator().getYear();
		assertEquals(currentYear, (int) transformed.getAsIntegerNumber(VisJsonCommonsFields.experience));
		assertEquals(VisSeniorityType.estag.name(), this.seniorityOf(transformed));
	}

	@Test
	public void seniorityIsCalculatedFromTheTransformedExperience() {
		CcpJsonRepresentation resume = CcpOtherConstants.EMPTY_JSON.put(VisJsonCommonsFields.experience, 4);

		CcpJsonRepresentation transformed = new VisJsonTransformerExperienceStartYear().execute(resume);

		assertEquals(VisSeniorityType.pleno.name(), this.seniorityOf(transformed));
	}

	@Test
	public void theLongestExperienceAcceptedByTheValidationIsASpecialist() {
		CcpJsonRepresentation resume = CcpOtherConstants.EMPTY_JSON.put(VisJsonCommonsFields.experience, 70);

		CcpJsonRepresentation transformed = new VisJsonTransformerExperienceStartYear().execute(resume);

		assertEquals(VisSeniorityType.especialista.name(), this.seniorityOf(transformed));
	}

	@Test
	public void resumeWithoutExperienceIsReturnedAsIs() {
		CcpJsonRepresentation resume = CcpOtherConstants.EMPTY_JSON.put(VisJsonCommonsFields.ddd, 11);

		CcpJsonRepresentation transformed = new VisJsonTransformerExperienceStartYear().execute(resume);

		assertSame(resume, transformed);
	}

	@Test
	public void theIdOfEachSpokenLanguageIsDropped() {
		CcpJsonRepresentation english = CcpOtherConstants.EMPTY_JSON.put(this.name, "Inglês").put(this.level, 2).put(this.id, 0);
		CcpJsonRepresentation spanish = CcpOtherConstants.EMPTY_JSON.put(this.name, "Espanhol").put(this.level, 1).put(this.id, 3);
		CcpJsonRepresentation resume = CcpOtherConstants.EMPTY_JSON.put(VisJsonCommonsFields.spokenLanguage, Arrays.asList(english, spanish));

		CcpJsonRepresentation transformed = new VisJsonTransformerSpokenLanguageWithoutId().execute(resume);

		List<CcpJsonRepresentation> spokenLanguages = transformed.getAsJsonList(VisJsonCommonsFields.spokenLanguage);
		assertEquals(2, spokenLanguages.size());
		for (CcpJsonRepresentation spokenLanguage : spokenLanguages) {
			assertFalse(spokenLanguage.containsAllFields(this.id));
		}
		assertEquals("Inglês", spokenLanguages.get(0).getAsString(this.name));
		assertEquals(2, (int) spokenLanguages.get(0).getAsIntegerNumber(this.level));
	}

	@Test
	public void resumeWithoutSpokenLanguageIsReturnedAsIs() {
		CcpJsonRepresentation resume = CcpOtherConstants.EMPTY_JSON.put(VisJsonCommonsFields.ddd, 11);

		CcpJsonRepresentation transformed = new VisJsonTransformerSpokenLanguageWithoutId().execute(resume);

		assertSame(resume, transformed);
	}
}
