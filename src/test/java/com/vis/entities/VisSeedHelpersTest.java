package com.vis.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.vis.json.fields.validation.VisJsonCommonsFields;

/**
 * The pieces of the seeds of the vis entities: {@link VisCompanyNameGrouperByInitials},
 * {@link VisLegacyRecruiterEmailCollector}, {@link VisSkillWithResumesCount} and {@link VisSkillsGroupOfTheInitials}.
 */
public class VisSeedHelpersTest {

	static {
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE);
	}

	private static CcpJsonRepresentation legacyRecruiter(String email) {
		return CcpOtherConstants.EMPTY_JSON.put(VisJsonCommonsFields.id, email);
	}

	@Test
	public void theCompanyOfTheDomainIsGroupedByItsFirstThreeLetters() {
		Map<String, Set<String>> groupedCompanies = new LinkedHashMap<>();
		VisCompanyNameGrouperByInitials grouper = new VisCompanyNameGrouperByInitials(groupedCompanies);

		grouper.accept(legacyRecruiter("ana@jobsnow.com"));
		grouper.accept(legacyRecruiter("bia@jobsbrasil.com.br"));
		grouper.accept(legacyRecruiter("caio@gmail.com"));
		grouper.accept(legacyRecruiter("davi@jobsnow.com"));

		assertEquals(List.of("Jobsnow", "Jobsbrasil"), new ArrayList<>(groupedCompanies.get("JOB")));
		assertEquals(List.of("Gmail"), new ArrayList<>(groupedCompanies.get("GMA")));
	}

	@Test
	public void aTextThatIsNotAnEmailOrAShortCompanyIsLeftOut() {
		Map<String, Set<String>> groupedCompanies = new LinkedHashMap<>();
		VisCompanyNameGrouperByInitials grouper = new VisCompanyNameGrouperByInitials(groupedCompanies);

		grouper.accept(legacyRecruiter("not an email"));
		grouper.accept(legacyRecruiter("ana@ab.com"));

		assertTrue(groupedCompanies.isEmpty());
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
	public void theGroupOfTheInitialsCarriesItsSkills() {
		CcpJsonRepresentation skillJava = CcpOtherConstants.EMPTY_JSON.put(VisJsonCommonsFields.skill, "JAVA");
		CcpJsonRepresentation groupedSkills = CcpOtherConstants.EMPTY_JSON.put(new CcpFieldName("JA"), List.of(skillJava));

		CcpJsonRepresentation group = new VisSkillsGroupOfTheInitials(groupedSkills).apply("JA");

		assertEquals("JA", group.getAsString(VisEntityGroupPositionsBySkills.Fields.firstTwoInitials));
		List<CcpJsonRepresentation> skills = group.getAsJsonList(VisJsonCommonsFields.skill);
		assertEquals(1, skills.size());
		assertEquals("JAVA", skills.get(0).getAsString(VisJsonCommonsFields.skill));
	}
}
