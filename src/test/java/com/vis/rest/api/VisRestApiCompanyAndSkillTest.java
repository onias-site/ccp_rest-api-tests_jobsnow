package com.vis.rest.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;

/**
 * Proves the two public controllers of the vis that take no session: {@code VisRestApiCompany} (the autocomplete of
 * companies) and {@code VisRestApiSkill} (the skills of a free text).
 */
public class VisRestApiCompanyAndSkillTest extends VisRestApiTestTemplate {

	enum JsonFieldNames implements CcpJsonFieldName {
		companies, text, skill
	}

	@Test
	public void aCompanyOutOfTheBaseComesBackWithTheTypedSpelling() {
		HttpResponse<String> response = this.sendWithNoSession(Fields.GET, "companies/search/autocomplete/Zyxwv%20Clinica%20Pet");

		this.assertStatus(200, response);
		CcpJsonRepresentation body = this.bodyOf(response);
		List<String> companies = body.getAsStringList(JsonFieldNames.companies);
		assertEquals(Arrays.asList("Zyxwv Clinica Pet"), companies);
	}

	@Test
	public void theSkillsOfATextAreFound() {
		CcpJsonRepresentation text = CcpOtherConstants.EMPTY_JSON.put(JsonFieldNames.text, "Trabalho com java, spring e sql");

		HttpResponse<String> response = this.send(Fields.POST, "skills/fromText", text, "");

		this.assertStatus(200, response);
		CcpJsonRepresentation body = this.bodyOf(response);
		List<CcpJsonRepresentation> skills = body.getAsJsonList(JsonFieldNames.skill);
		List<String> skillNames = skills.stream().map(skill -> skill.getAsString(JsonFieldNames.skill)).toList();
		assertTrue(skillNames.toString(), skillNames.contains("JAVA"));
		assertTrue(skillNames.toString(), skillNames.contains("SQL"));
	}

	@Test
	public void anEmptyTextHasNoSkills() {
		CcpJsonRepresentation text = CcpOtherConstants.EMPTY_JSON.put(JsonFieldNames.text, "");

		HttpResponse<String> response = this.send(Fields.POST, "skills/fromText", text, "");

		this.assertStatus(200, response);
		CcpJsonRepresentation body = this.bodyOf(response);
		assertTrue(body.toString(), body.isEmpty());
	}

	@Test
	public void theTextIsRequired() {
		HttpResponse<String> response = this.send(Fields.POST, "skills/fromText", CcpOtherConstants.EMPTY_JSON, "");

		this.assertStatus(422, response);
	}
}
