package com.vis.rest.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.http.HttpResponse;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpStringDecorator;
import com.vis.entities.VisEntitySkillPending;

/**
 * Proves {@code VisRestApiSkillSuggestion} ({@code /resume/{email}/skills/suggestion}): the session filter guards it,
 * the candidate suggests a skill that the system does not know, reads it back as pending, cannot suggest it twice nor
 * suggest a second skill on the same day, and withdraws it. The review of the suggestion by the support bot is proved in
 * {@code SkillSuggestionThroughSupportBotTest}.
 */
public class VisRestApiSkillSuggestionTest extends VisRestApiTestTemplate {

	enum JsonFieldNames implements CcpJsonFieldName {
		status, pending
	}

	private static final String SUGGESTION = "/skills/suggestion";

	private final String run = new CcpStringDecorator("ABCDEFGHIJKLMNOPQRSTUVWXYZ").text().generateToken(8).content.toUpperCase();

	@Before
	public void openTheSession() {
		this.login();
	}

	private String unknownSkill(String name) {
		String skill = "TESTE HTTP " + name + " " + this.run;
		return skill;
	}

	private CcpJsonRepresentation suggestionKey(String skill) {
		CcpJsonRepresentation key = CcpOtherConstants.EMPTY_JSON.put(VisEntitySkillPending.Fields.skill, skill);
		return key;
	}

	private CcpJsonRepresentation suggestion(String skill) {
		CcpJsonRepresentation key = this.suggestionKey(skill);
		CcpJsonRepresentation suggestion = key
				.put(VisEntitySkillPending.Fields.description, "Habilidade usada no dia a dia e ausente da lista")
				.put(VisEntitySkillPending.Fields.synonym, Arrays.asList());
		return suggestion;
	}

	private HttpResponse<String> suggest(String skill) {
		CcpJsonRepresentation suggestion = this.suggestion(skill);
		String path = this.resumePath(SUGGESTION);
		HttpResponse<String> response = this.sendWithTheSession(Fields.POST, path, suggestion);
		return response;
	}

	private HttpResponse<String> search(String skill) {
		CcpJsonRepresentation key = this.suggestionKey(skill);
		String path = this.resumePath(SUGGESTION + "/search");
		HttpResponse<String> response = this.sendWithTheSession(Fields.POST, path, key);
		return response;
	}

	private HttpResponse<String> withdraw(String skill) {
		CcpJsonRepresentation key = this.suggestionKey(skill);
		String path = this.resumePath(SUGGESTION);
		HttpResponse<String> response = this.sendWithTheSession(Fields.DELETE, path, key);
		return response;
	}

	@Test
	public void theSuggestionIsReadBackAsPendingAndWithdrawn() {
		String skill = this.unknownSkill("PENDENTE");

		this.assertStatus(200, this.suggest(skill));

		HttpResponse<String> found = this.search(skill);
		this.assertStatus(200, found);
		CcpJsonRepresentation suggestion = this.bodyOf(found);
		assertEquals(suggestion.toString(), JsonFieldNames.pending.name(), suggestion.getAsString(JsonFieldNames.status));

		this.assertStatus(200, this.withdraw(skill));

		HttpResponse<String> afterWithdrawal = this.search(skill);
		this.assertStatus(200, afterWithdrawal);
		CcpJsonRepresentation nothing = this.bodyOf(afterWithdrawal);
		assertTrue(nothing.toString(), nothing.isEmpty());
	}

	@Test
	public void theSameSkillCannotBeSuggestedTwice() {
		String skill = this.unknownSkill("REPETIDA");
		this.assertStatus(200, this.suggest(skill));

		this.assertStatus(409, this.suggest(skill));

		this.withdraw(skill);
	}

	@Test
	public void aSecondSkillOnTheSameDayReachesTheDailyLimit() {
		String firstSkill = this.unknownSkill("PRIMEIRA");
		String secondSkill = this.unknownSkill("SEGUNDA");
		this.assertStatus(200, this.suggest(firstSkill));

		this.assertStatus(429, this.suggest(secondSkill));

		this.withdraw(firstSkill);
	}

	@Test
	public void aKnownSkillIsRefused() {
		this.assertStatus(412, this.suggest("JAVA"));
	}

	@Test
	public void withdrawingWhatWasNeverSuggestedIsNotFound() {
		String skill = this.unknownSkill("NUNCA SUGERIDA");

		this.assertStatus(404, this.withdraw(skill));
	}

	@Test
	public void aSuggestionWithNoDescriptionIsAValidationError() {
		String skill = this.unknownSkill("SEM DESCRICAO");
		CcpJsonRepresentation key = this.suggestionKey(skill);
		String path = this.resumePath(SUGGESTION);

		HttpResponse<String> response = this.sendWithTheSession(Fields.POST, path, key);

		this.assertStatus(422, response);
	}

	@Test
	public void aSuggestionWithNoSessionIsStoppedByTheSessionValidation() {
		String path = this.resumePath(SUGGESTION);

		HttpResponse<String> response = this.sendWithNoSession(Fields.POST, path);

		this.assertStatus(422, response);
		assertTrue(response.body(), response.body().contains("JsBusinessValidateLogin"));
	}

	@Test
	public void aSuggestionWithAFakeSessionIsUnauthorized() {
		String skill = this.unknownSkill("SESSAO FALSA");
		CcpJsonRepresentation suggestion = this.suggestion(skill);
		String path = this.resumePath(SUGGESTION);

		HttpResponse<String> response = this.send(Fields.POST, path, suggestion, FAKE_SESSION_TOKEN);

		this.assertStatus(401, response);
	}
}
