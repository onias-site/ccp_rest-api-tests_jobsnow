package com.vis.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.vis.json.fields.validation.VisJsonCommonsFields;
import com.vis.json.transformers.VisRecruiterDomainInitialsCombinations;
import com.vis.json.transformers.VisSkillInitialsCombinations;

/**
 * The pieces of the seeds and of the groups of the vis entities: {@link VisRecruiterDomainInitialsCombinations},
 * {@link VisEntityVirtualRecruiterDomainInitialsGrouper#getCompanyName}, {@link VisLegacyRecruiterEmailCollector},
 * {@link VisSkillWithResumesCount} and {@link VisSkillInitialsCombinations}.
 */
public class VisSeedHelpersTest {

	static {
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE);
	}

	private static CcpJsonRepresentation legacyRecruiter(String email) {
		return CcpOtherConstants.EMPTY_JSON.put(VisJsonCommonsFields.id, email);
	}

	@Test
	public void aProfessionalRecruiterIsInTheGroupOfTheFirstThreeLettersOfItsDomain() {
		CcpJsonRepresentation recruiter = CcpOtherConstants.EMPTY_JSON
				.put(VisEntityRecruiter.Fields.domain, "jobsnow.com.br")
				.put(VisEntityRecruiter.Fields.professional, true);

		List<CcpJsonRepresentation> combinations = VisRecruiterDomainInitialsCombinations.INSTANCE.apply(recruiter);

		assertEquals(1, combinations.size());
		assertEquals("JOB", combinations.get(0).getAsString(VisEntityVirtualRecruiterDomainInitialsGrouper.Fields.firstThreeInitials));
	}

	@Test
	public void aNonProfessionalRecruiterIsInNoGroup() {
		CcpJsonRepresentation recruiter = CcpOtherConstants.EMPTY_JSON
				.put(VisEntityRecruiter.Fields.domain, "gmail.com")
				.put(VisEntityRecruiter.Fields.professional, false);

		List<CcpJsonRepresentation> combinations = VisRecruiterDomainInitialsCombinations.INSTANCE.apply(recruiter);

		assertTrue(combinations.isEmpty());
	}

	@Test
	public void theCompanyIsTheFirstPartOfTheDomainCapitalized() {
		assertEquals("Jobsnow", VisEntityVirtualRecruiterDomainInitialsGrouper.getCompanyName("jobsnow.com.br"));
		assertEquals("Rh", VisEntityVirtualRecruiterDomainInitialsGrouper.getCompanyName("rh.empresa.com"));
	}

	@Test
	public void theEmailOfEachLegacyRecruiterIsKeptOnce() {
		Set<String> legacyEmails = new LinkedHashSet<>();
		VisLegacyRecruiterEmailCollector collector = new VisLegacyRecruiterEmailCollector(legacyEmails);

		collector.accept(legacyRecruiter("ana@jobsnow.com"));
		collector.accept(legacyRecruiter("bia@jobsnow.com"));
		collector.accept(legacyRecruiter("ana@jobsnow.com"));

		assertEquals(List.of("ana@jobsnow.com", "bia@jobsnow.com"), new ArrayList<>(legacyEmails));
	}

	@Test
	public void theSkillGetsTheCountOfTheResumesOfItAndOfItsSynonyms() {
		CcpJsonRepresentation skill = new CcpJsonRepresentation("{\"skill\":\"JAVA\",\"synonym\":[{\"skill\":\"J2SE\"}]}");
		List<String> lines = List.of("JAVA = 10", "J2SE = 2", "PYTHON = 7");

		CcpJsonRepresentation skillWithCount = new VisSkillWithResumesCount(lines).apply(skill);

		assertEquals(12, (int) skillWithCount.getAsIntegerNumber(VisJsonCommonsFields.resumesCount));
		assertEquals("JAVA", skillWithCount.getAsString(VisJsonCommonsFields.skill));
	}

	@Test
	public void theSkillIsInTheGroupOfTheInitialsOfEachOfItsWords() {
		CcpJsonRepresentation skill = CcpOtherConstants.EMPTY_JSON
				.put(VisEntitySkill.Fields.skill, "JAVA")
				.put(VisEntitySkill.Fields.synonym, List.of("j2se", "JAKARTA"))
				.put(VisEntitySkill.Fields.parent, List.of("PROGRAMMING LANGUAGE", "C"));

		List<CcpJsonRepresentation> combinations = VisSkillInitialsCombinations.INSTANCE.apply(skill);

		// JAVA and JAKARTA share JA, the synonym in lower case goes to J2 and the parent C is too short to have a group
		List<String> initials = new ArrayList<>();
		for (CcpJsonRepresentation combination : combinations) {
			String firstTwoInitials = combination.getAsString(VisEntityVirtualSkillInitialsGrouper.Fields.firstTwoInitials);
			initials.add(firstTwoInitials);
		}
		assertEquals(List.of("JA", "J2", "PR"), initials);
	}
}
