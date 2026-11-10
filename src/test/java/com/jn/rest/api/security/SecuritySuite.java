package com.jn.rest.api.security;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.jn.rest.api.commons.ClientErrorStatusTest;
import com.js.rest.api.JsRestApiTelegramWebhookTest;
import com.vis.rest.api.VisRestApiRecruiterResumesTest;
import com.vis.rest.api.VisSessionFilterPathsTest;

/**
 * The security suite of the local APIs (vis on 8081 and js on 8082 must be up; never production): sessions, reading of
 * another user's data, quotas of the routes without session, the webhook of Telegram, the signed links of the recruiters
 * and the client errors that must not leak details. Run with
 * {@code mvn test -pl ccp_rest-api-tests_jobsnow -Dtest=SecuritySuite}.
 */
@RunWith(Suite.class)
@SuiteClasses({
	ACandidateCannotReadTheDataOfAnother.class,
	EveryRouteOfTheUserRefusesARequestWithoutSession.class,
	AnEndedSessionNoLongerOpensAnything.class,
	TheContactUsKeepsItsDailyQuotas.class,
	TheErrorPagesDoNotLeakDetails.class,
	TheApisTalkOnlyToTheOriginsOfTheFrontEnd.class,
	JsRestApiTelegramWebhookTest.class,
	VisRestApiRecruiterResumesTest.class,
	VisSessionFilterPathsTest.class,
	ClientErrorStatusTest.class,
})
public class SecuritySuite {
}
