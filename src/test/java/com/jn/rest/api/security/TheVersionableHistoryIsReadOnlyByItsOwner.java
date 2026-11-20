package com.jn.rest.api.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.net.http.HttpResponse;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.jn.entities.JnEntityLoginEmail;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.rest.api.commons.TestVariables;
import com.jn.utils.JnVersionableHistory;
import com.js.entities.JsEntityLoginAnswers;

/**
 * The history of a record of a versionable entity ({@code GET /versionable-history/{email}/{entity}/{id}} on the js,
 * since 2026-11-20) keeps the whole record before each write, so only the owner of the record reads it: a value of its
 * primary key is the e-mail of the session (as the entities keep it, the SHA-1). The answers of the sign up
 * ({@code js_login_answers}, primary key = the e-mail) of the victim are saved twice, so their history has two versions;
 * the attacker, with its own valid session, gets the same 404 of a record with no history.
 */
public class TheVersionableHistoryIsReadOnlyByItsOwner {

	private static final String ATTACKER_TOKEN = SecurityHttp.token();

	private static final String VICTIM_TOKEN = SecurityHttp.token();

	private final TestVariables attacker = new TestVariables();

	private final TestVariables victim = new TestVariables();

	@BeforeClass
	public static void theJsApiMustBeUp() {
		SecurityHttp.load();
		SecurityHttp.theApiMustBeUp(SecurityHttp.JS);
	}

	@Before
	public void bothLogInAndTheVictimAnswersTheSignUpTwice() {
		for (TestVariables user : List.of(this.attacker, this.victim)) {
			JnEntityLoginEmail.ENTITY.save(user.REQUEST_TO_LOGIN);
		}
		this.attacker.saveCurrentSession(ATTACKER_TOKEN);
		this.victim.saveCurrentSession(VICTIM_TOKEN);
		JsEntityLoginAnswers.ENTITY.save(this.victim.ANSWERS_JSON);
		CcpJsonRepresentation changedAnswers = this.victim.ANSWERS_JSON.put(JsEntityLoginAnswers.Fields.channel, "telegram");
		JsEntityLoginAnswers.ENTITY.save(changedAnswers);
	}

	@After
	public void deleteTheAnswersAndTheirHistory() {
		JsEntityLoginAnswers.ENTITY.deleteAnyWhere(this.victim.ANSWERS_JSON);
	}

	private String recordIdOfTheVictim() {
		CcpEntity answers = JsEntityLoginAnswers.ENTITY;
		CcpJsonRepresentation storedAnswers = answers.getHandledJson(this.victim.ANSWERS_JSON);
		String recordId = answers.calculateId(storedAnswers);
		return recordId;
	}

	private HttpResponse<String> read(TestVariables reader, String sessionToken, String recordId) {
		String entityName = JsEntityLoginAnswers.ENTITY.getEntityMetaData().entityName;
		String path = "versionable-history/" + reader.VALID_EMAIL + "/" + entityName + "/" + recordId;
		HttpResponse<String> response = SecurityHttp.send(SecurityHttp.JS, "GET", path, "{}", reader, sessionToken);
		return response;
	}

	@Test
	public void theOwnerReadsEveryVersionOfTheRecord() {
		String recordId = this.recordIdOfTheVictim();

		HttpResponse<String> response = this.read(this.victim, VICTIM_TOKEN, recordId);

		assertEquals(response.body(), 200, response.statusCode());
		CcpJsonRepresentation history = new CcpJsonRepresentation(response.body());
		assertEquals(recordId, history.getAsString(JnJsonCommonsFields.id));
		List<CcpJsonRepresentation> versions = history.getAsJsonList(JnVersionableHistory.Fields.versions);
		assertEquals(response.body(), 2, versions.size());
		CcpJsonRepresentation lastVersion = versions.get(1);
		CcpJsonRepresentation answersBeforeTheLastWrite = lastVersion.getInnerJson(JnJsonCommonsFields.json);
		assertEquals("the last version keeps the record before the change", "linkedin", answersBeforeTheLastWrite.getAsString(JsEntityLoginAnswers.Fields.channel));
	}

	@Test
	public void theAttackerDoesNotReadTheHistoryOfTheVictim() {
		String recordId = this.recordIdOfTheVictim();

		HttpResponse<String> response = this.read(this.attacker, ATTACKER_TOKEN, recordId);

		assertEquals(response.body(), 404, response.statusCode());
		assertFalse(response.body(), response.body().contains("linkedin"));
	}

	@Test
	public void aRecordWithNoHistoryIsNotFound() {
		HttpResponse<String> response = this.read(this.victim, VICTIM_TOKEN, "no-such-record");

		assertEquals(response.body(), 404, response.statusCode());
	}

	@Test
	public void aFakeSessionIsUnauthorized() {
		String recordId = this.recordIdOfTheVictim();

		HttpResponse<String> response = this.read(this.victim, "ABCD1234", recordId);

		assertEquals(response.body(), 401, response.statusCode());
	}
}
