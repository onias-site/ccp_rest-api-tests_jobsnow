package com.jn.rest.api.security;

import static org.junit.Assert.assertTrue;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.jn.rest.api.commons.TestVariables;

/**
 * Every route that reads or writes data of a user refuses the request with no session and with a forged one. The list
 * is every mapping of the controllers of the vis (8081) and of the js (8082) under the session filter, plus the root of
 * the resume; a new route under {@code /resume}, {@code /recruiter}, {@code /recruiters}, {@code /login},
 * {@code /grouper} or {@code /contact-us} must be added here.
 */
public class EveryRouteOfTheUserRefusesARequestWithoutSession {

	private static final String FORGED_TOKEN = SecurityHttp.token();

	private final TestVariables user = new TestVariables();

	@BeforeClass
	public static void theApisMustBeUp() {
		SecurityHttp.load();
		SecurityHttp.theApiMustBeUp(SecurityHttp.JS);
		SecurityHttp.theApiMustBeUp(SecurityHttp.VIS);
	}

	private String[][] routes() {
		String email = this.user.VALID_EMAIL;
		return new String[][] {
			{"8081", "GET", "resume/" + email},
			{"8081", "GET", "resume/" + email + "/viewers/202611"},
			{"8081", "POST", "resume/" + email + "/language/portuguese"},
			{"8081", "DELETE", "resume/" + email + "/language/portuguese"},
			{"8081", "DELETE", "resume/" + email + "/language/portuguese/status"},
			{"8081", "POST", "resume/" + email + "/skills/hierarchy"},
			{"8081", "DELETE", "resume/" + email + "/skills/hierarchy"},
			{"8081", "POST", "resume/" + email + "/skills/hierarchy/search"},
			{"8081", "POST", "resume/" + email + "/skills/suggestion"},
			{"8081", "DELETE", "resume/" + email + "/skills/suggestion"},
			{"8081", "POST", "resume/" + email + "/skills/suggestion/search"},
			{"8081", "GET", "recruiter/" + email + "/resumes-of-the-week"},
			{"8081", "GET", "recruiter/" + email + "/viewed-resumes/202611"},
			{"8081", "POST", "recruiter/" + email + "/viewed-resumes/202611/details"},
			{"8082", "GET", "grouper/" + email + "/jn_login_email/js_login_session_validation/x"},
			{"8082", "GET", "grouper/" + email + "/jn_login_email/js_login_session_validation/x/bothSides"},
			{"8082", "POST", "contact-us/" + email},
		};
	}

	private List<String> acceptedWith(String sessionToken) {
		List<String> accepted = new ArrayList<>();
		for (String[] route : this.routes()) {
			int port = Integer.parseInt(route[0]);
			HttpResponse<String> response = SecurityHttp.send(port, route[1], route[2], "{}", this.user, sessionToken);
			int status = response.statusCode();
			boolean refused = SecurityHttp.refusedForTheSession(status);
			if (refused) {
				continue;
			}
			accepted.add(route[1] + " " + route[2] + " -> " + status);
		}
		return accepted;
	}

	@Test
	public void noRouteAnswersWithoutTheSessionHeader() {
		List<String> accepted = this.acceptedWith("");

		assertTrue("routes that did not refuse for the session: " + accepted, accepted.isEmpty());
	}

	@Test
	public void noRouteAnswersWithAForgedSession() {
		List<String> accepted = this.acceptedWith(FORGED_TOKEN);

		assertTrue("routes that did not refuse the forged session: " + accepted, accepted.isEmpty());
	}
}
