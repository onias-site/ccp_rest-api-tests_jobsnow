package com.vis.business.recruiter.resumes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.time.Year;
import java.util.List;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.vis.entities.VisEntityResume;

/**
 * 2026-11-24: each row of the tab "Currículos aderentes" brings the raw values by which the screen sorts the list in the
 * order the recruiter chose.
 */
public class ResumeSortValuesTest {

	static {
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE);
	}

	@Test
	public void theClaimsTheYearsTheLanguagesAndTheSkills() {
		int startYear = Year.now().getValue() - 7;
		CcpJsonRepresentation resume = new CcpJsonRepresentation("{\"clt\":5000,\"experience\":" + startYear + ",\"disponibility\":30,\"temporallyJobTime\":6,"
				+ "\"spokenLanguage\":[{\"name\":\"Inglês\",\"level\":2}],"
				+ "\"skill\":[{\"skill\":\"Spring Boot\",\"category\":\"skillsFromText\",\"type\":\"skillsFromResume\",\"word\":\"spring boot\"},"
				+ "{\"skill\":\"Java\",\"category\":\"parent\",\"type\":\"parent\",\"word\":\"Java\"},"
				+ "{\"skill\":\"Go\",\"category\":\"discardedSkills\",\"type\":\"isPieceOfOtherWord\",\"word\":\"go\"}]}");
		CcpJsonRepresentation values = VisBusinessResumeSortValues.INSTANCE.execute(resume);
		assertEquals(5000, values.getAsIntegerNumber(VisEntityResume.Fields.clt).intValue());
		assertFalse("no PJ claim, no PJ value", values.containsAllFields(VisEntityResume.Fields.pj));
		assertEquals(7, values.getAsIntegerNumber(VisEntityResume.Fields.experience).intValue());
		assertEquals(30, values.getAsIntegerNumber(VisEntityResume.Fields.disponibility).intValue());
		assertEquals(6, values.getAsIntegerNumber(VisEntityResume.Fields.temporallyJobTime).intValue());
		assertEquals(2, values.getInnerJson(VisEntityResume.Fields.spokenLanguage).getAsIntegerNumber(() -> "Inglês").intValue());
		assertEquals(List.of("Spring Boot"), values.getAsStringList(VisEntityResume.Fields.skill));
		assertEquals(List.of("Java"), values.getAsStringList(VisBusinessResumeSortValues.Fields.parentSkill));
	}
}
