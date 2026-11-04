package com.vis.rest.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.http.HttpResponse;
import java.util.Arrays;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.vis.business.recruiter.resumes.VisRecruiterLinks;
import com.vis.business.recruiter.resumes.VisRecruiterResumesFields;
import com.vis.entities.VisEntityResume;
import com.vis.entities.VisEntityViewedResume;
import com.vis.services.VisServiceResume;

/**
 * Proves {@code VisRestApiRecruiterResumes}: the links of the e-mail of resumes ({@code /recruiter-email-link/*}, no
 * session, signed by {@link VisRecruiterLinks}) refuse a link signed for something else and open, name and unsubscribe
 * with a good one; the screens of the recruiter ({@code /recruiter/{email}/*}) are guarded by the session and validate
 * the month. The content of the e-mail and of the screens is proved in {@code ResumesToRecruitersTest}.
 */
public class VisRestApiRecruiterResumesTest extends VisRestApiTestTemplate {

	private static final String LINK = "recruiter-email-link/";

	private String recruiter() {
		String recruiter = this.testVariables.VALID_EMAIL;
		return recruiter;
	}

	private CcpJsonRepresentation viewLink(String resumeId, String token) {
		String recruiter = this.recruiter();
		CcpJsonRepresentation link = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonCommonsFields.email, recruiter)
				.put(VisRecruiterResumesFields.resumeId, resumeId)
				.put(VisRecruiterResumesFields.token, token);
		return link;
	}

	private CcpJsonRepresentation signedViewLink(String resumeId) {
		String recruiter = this.recruiter();
		String token = VisRecruiterLinks.view.sign(recruiter, resumeId);
		CcpJsonRepresentation link = this.viewLink(resumeId, token);
		return link;
	}

	private HttpResponse<String> post(String route, CcpJsonRepresentation body) {
		HttpResponse<String> response = this.send(Fields.POST, LINK + route, body, "");
		return response;
	}

	private String recruiterPath(String suffix) {
		String path = "recruiter/" + this.recruiter() + suffix;
		return path;
	}

	/** Saves an active resume of a random candidate and returns its id, the one that goes in the link of the e-mail. */
	private CcpJsonRepresentation saveAResume() {
		String candidate = "candidato." + this.recruiter();
		CcpJsonRepresentation key = CcpOtherConstants.EMPTY_JSON.put(VisEntityResume.Fields.email, candidate);
		CcpJsonRepresentation resume = key
				.put(VisEntityResume.Fields.ddd, Arrays.asList(11))
				.put(VisEntityResume.Fields.desiredJob, "developer")
				.put(VisEntityResume.Fields.disponibility, 5)
				.put(VisEntityResume.Fields.experience, 4)
				.put(VisEntityResume.Fields.linkedinAddress, "https://www.linkedin.com/in/candidato-do-link")
				.put(VisEntityResume.Fields.resumeType, 2)
				.put(VisEntityResume.Fields.temporallyJobTime, 0)
				.put(VisEntityResume.Fields.pj, 9_000);
		VisServiceResume.Save.execute(resume);
		return key;
	}

	@Test
	public void aSignedLinkOpensTheResumeAndKeepsItsAlias() {
		CcpJsonRepresentation candidate = this.saveAResume();
		// the id comes from the resume as stored (e-mail already hashed), as the e-mail of resumes does
		CcpJsonRepresentation storedResume = VisEntityResume.ENTITY.getOneById(candidate);
		String resumeId = VisEntityResume.ENTITY.calculateId(storedResume);
		try {
			CcpJsonRepresentation link = this.signedViewLink(resumeId);

			HttpResponse<String> opened = this.post("view-resume", link);
			this.assertStatus(200, opened);
			CcpJsonRepresentation view = this.bodyOf(opened);
			assertEquals(view.toString(), "https://www.linkedin.com/in/candidato-do-link", view.getAsString(VisEntityResume.Fields.linkedinAddress));

			CcpJsonRepresentation linkWithAlias = link.put(VisEntityViewedResume.Fields.alias, "Desenvolvedor do link");
			this.assertStatus(200, this.post("alias", linkWithAlias));

			HttpResponse<String> reopened = this.post("view-resume", link);
			this.assertStatus(200, reopened);
			CcpJsonRepresentation viewWithAlias = this.bodyOf(reopened);
			assertEquals(viewWithAlias.toString(), "Desenvolvedor do link", viewWithAlias.getAsString(VisEntityViewedResume.Fields.alias));
		} finally {
			VisServiceResume.Delete.execute(candidate);
		}
	}

	@Test
	public void aSignedLinkToAResumeNoLongerActiveIsNotFound() {
		CcpJsonRepresentation link = this.signedViewLink("curriculoquenaoexiste");

		this.assertStatus(404, this.post("view-resume", link));
	}

	@Test
	public void aLinkSignedForAnotherResumeIsUnauthorized() {
		String token = VisRecruiterLinks.view.sign(this.recruiter(), "outrocurriculo");
		CcpJsonRepresentation link = this.viewLink("curriculo", token);

		this.assertStatus(401, this.post("view-resume", link));
	}

	@Test
	public void theAliasOfALinkNotSignedIsUnauthorized() {
		CcpJsonRepresentation link = this.viewLink("curriculo", "tokenfalso").put(VisEntityViewedResume.Fields.alias, "Apelido valido");

		this.assertStatus(401, this.post("alias", link));
	}

	@Test
	public void theLinkWithNoFieldsIsAValidationError() {
		this.assertStatus(422, this.post("view-resume", CcpOtherConstants.EMPTY_JSON));
	}

	@Test
	public void theUnsubscribeWithASignedLinkIsAcceptedEvenTwice() {
		String recruiter = this.recruiter();
		String token = VisRecruiterLinks.unsubscribe.sign(recruiter);
		CcpJsonRepresentation link = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonCommonsFields.email, recruiter)
				.put(VisRecruiterResumesFields.token, token);

		this.assertStatus(200, this.post("unsubscribe", link));
		this.assertStatus(200, this.post("unsubscribe", link));
	}

	@Test
	public void theUnsubscribeWithALinkNotSignedIsUnauthorized() {
		CcpJsonRepresentation link = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonCommonsFields.email, this.recruiter())
				.put(VisRecruiterResumesFields.token, "tokenfalso");

		this.assertStatus(401, this.post("unsubscribe", link));
	}

	@Test
	public void theOneClickUnsubscribeTakesTheLinkInTheQuery() {
		String recruiter = this.recruiter();
		String token = VisRecruiterLinks.unsubscribe.sign(recruiter);
		String query = "one-click-unsubscribe?email=" + recruiter + "&token=";

		this.assertStatus(200, this.post(query + token, CcpOtherConstants.EMPTY_JSON));
		this.assertStatus(401, this.post(query + "tokenfalso", CcpOtherConstants.EMPTY_JSON));
	}

	@Test
	public void theOneClickUnsubscribeWithNoTokenIsABadRequest() {
		String route = "one-click-unsubscribe?email=" + this.recruiter();

		this.assertStatus(400, this.post(route, CcpOtherConstants.EMPTY_JSON));
	}

	@Test
	public void aRecruiterWithNoEmailOfResumesHasNoResumesOfTheWeek() {
		this.login();
		String path = this.recruiterPath("/resumes-of-the-week");

		HttpResponse<String> response = this.sendWithTheSession(Fields.GET, path, CcpOtherConstants.EMPTY_JSON);

		this.assertStatus(404, response);
	}

	@Test
	public void theViewedResumesOfAMonthComeEvenWhenNoneWasViewed() {
		this.login();
		String path = this.recruiterPath("/viewed-resumes/202501");

		HttpResponse<String> response = this.sendWithTheSession(Fields.GET, path, CcpOtherConstants.EMPTY_JSON);

		this.assertStatus(200, response);
	}

	@Test
	public void theViewedResumesOfAMonthOutOfTheFormatAreAValidationError() {
		this.login();
		String path = this.recruiterPath("/viewed-resumes/2025");

		HttpResponse<String> response = this.sendWithTheSession(Fields.GET, path, CcpOtherConstants.EMPTY_JSON);

		this.assertStatus(422, response);
	}

	@Test
	public void theDetailsOfAResumeNotViewedInTheMonthAreNotFound() {
		this.login();
		String path = this.recruiterPath("/viewed-resumes/202501/details");
		CcpJsonRepresentation body = CcpOtherConstants.EMPTY_JSON.put(VisEntityViewedResume.Fields.linkedin, "https://www.linkedin.com/in/ninguem-viu");

		HttpResponse<String> response = this.sendWithTheSession(Fields.POST, path, body);

		this.assertStatus(404, response);
	}

	@Test
	public void theScreensOfTheRecruiterWithNoSessionAreStoppedByTheSessionValidation() {
		String path = this.recruiterPath("/resumes-of-the-week");

		HttpResponse<String> response = this.sendWithNoSession(Fields.GET, path);

		this.assertStatus(422, response);
		assertTrue(response.body(), response.body().contains("JsBusinessValidateLogin"));
	}
}
