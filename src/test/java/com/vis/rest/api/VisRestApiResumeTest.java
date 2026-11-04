package com.vis.rest.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.http.HttpResponse;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.vis.entities.VisEntityResume;

/**
 * Proves {@code VisRestApiResume} ({@code /resume/{email}}): through HTTP, with the session of the candidate, the resume
 * is saved, read, deactivated, reactivated and deleted, and the screen "Quem me viu" validates the month. The rules of
 * the resume itself are proved in {@code ResumeServiceTest}.
 */
public class VisRestApiResumeTest extends VisRestApiTestTemplate {

	enum JsonFieldNames implements CcpJsonFieldName {
		vis_resume, vis_inactive_resume, month
	}

	private static final String LANGUAGE = "/language/portuguese";

	@Before
	public void openTheSession() {
		this.login();
	}

	private CcpJsonRepresentation resume() {
		CcpJsonRepresentation resume = CcpOtherConstants.EMPTY_JSON
				.put(VisEntityResume.Fields.ddd, Arrays.asList(11))
				.put(VisEntityResume.Fields.desiredJob, "developer")
				.put(VisEntityResume.Fields.disponibility, 5)
				.put(VisEntityResume.Fields.experience, 4)
				.put(VisEntityResume.Fields.linkedinAddress, "https://www.linkedin.com/in/onias")
				.put(VisEntityResume.Fields.resumeType, 2)
				.put(VisEntityResume.Fields.temporallyJobTime, 0)
				.put(VisEntityResume.Fields.pj, 9_000);
		return resume;
	}

	private CcpJsonRepresentation read() {
		String path = this.resumePath("");
		HttpResponse<String> response = this.sendWithTheSession(Fields.GET, path, CcpOtherConstants.EMPTY_JSON);
		this.assertStatus(200, response);
		CcpJsonRepresentation data = this.bodyOf(response);
		return data;
	}

	private boolean foundIn(CcpJsonRepresentation data, JsonFieldNames entity) {
		CcpJsonRepresentation inTheEntity = data.getInnerJson(entity);
		boolean found = false == inTheEntity.isEmpty();
		return found;
	}

	private void changeStatus() {
		String path = this.resumePath(LANGUAGE + "/status");
		HttpResponse<String> response = this.sendWithTheSession(Fields.DELETE, path, CcpOtherConstants.EMPTY_JSON);
		this.assertStatus(200, response);
	}

	@Test
	public void theResumeGoesThroughItsWholeLife() {
		String savePath = this.resumePath(LANGUAGE);
		CcpJsonRepresentation resume = this.resume();
		this.assertStatus(200, this.sendWithTheSession(Fields.POST, savePath, resume));

		CcpJsonRepresentation active = this.read();
		assertTrue(active.toString(), this.foundIn(active, JsonFieldNames.vis_resume));
		CcpJsonRepresentation storedResume = active.getInnerJson(JsonFieldNames.vis_resume);
		assertEquals("developer", storedResume.getAsString(VisEntityResume.Fields.desiredJob));

		this.changeStatus();
		CcpJsonRepresentation inactive = this.read();
		assertTrue(inactive.toString(), this.foundIn(inactive, JsonFieldNames.vis_inactive_resume));
		assertFalse(inactive.toString(), this.foundIn(inactive, JsonFieldNames.vis_resume));

		this.changeStatus();
		CcpJsonRepresentation reactivated = this.read();
		assertTrue(reactivated.toString(), this.foundIn(reactivated, JsonFieldNames.vis_resume));

		this.assertStatus(200, this.sendWithTheSession(Fields.DELETE, savePath, CcpOtherConstants.EMPTY_JSON));
		CcpJsonRepresentation afterDelete = this.read();
		assertFalse(afterDelete.toString(), this.foundIn(afterDelete, JsonFieldNames.vis_resume));
		assertFalse(afterDelete.toString(), this.foundIn(afterDelete, JsonFieldNames.vis_inactive_resume));
	}

	@Test
	public void theStatusOfAResumeThatDoesNotExistIsNotFound() {
		String path = this.resumePath(LANGUAGE + "/status");

		HttpResponse<String> response = this.sendWithTheSession(Fields.DELETE, path, CcpOtherConstants.EMPTY_JSON);

		this.assertStatus(404, response);
	}

	@Test
	public void anInvalidResumeIsAValidationError() {
		String path = this.resumePath(LANGUAGE);
		CcpJsonRepresentation cutLinkedin = this.resume().put(VisEntityResume.Fields.linkedinAddress, "https://www.linkedin.com/in/andre-");

		HttpResponse<String> response = this.sendWithTheSession(Fields.POST, path, cutLinkedin);

		this.assertStatus(422, response);
	}

	@Test
	public void theViewersOfAMonthComeEvenWhenNobodyViewed() {
		String path = this.resumePath("/viewers/202501");

		HttpResponse<String> response = this.sendWithTheSession(Fields.GET, path, CcpOtherConstants.EMPTY_JSON);

		this.assertStatus(200, response);
		CcpJsonRepresentation body = this.bodyOf(response);
		assertEquals(body.toString(), "202501", body.getAsString(JsonFieldNames.month));
	}

	@Test
	public void theViewersOfAMonthOutOfTheFormatAreAValidationError() {
		String path = this.resumePath("/viewers/2025");

		HttpResponse<String> response = this.sendWithTheSession(Fields.GET, path, CcpOtherConstants.EMPTY_JSON);

		this.assertStatus(422, response);
	}

	@Test
	public void theResumeWithNoSessionIsStoppedByTheSessionValidation() {
		String path = this.resumePath("");

		HttpResponse<String> response = this.sendWithNoSession(Fields.GET, path);

		this.assertStatus(422, response);
		assertTrue(response.body(), response.body().contains("JsBusinessValidateLogin"));
	}

	@Test
	public void theResumeWithAFakeSessionIsUnauthorized() {
		String path = this.resumePath("");

		HttpResponse<String> response = this.send(Fields.GET, path, CcpOtherConstants.EMPTY_JSON, FAKE_SESSION_TOKEN);

		this.assertStatus(401, response);
	}
}
