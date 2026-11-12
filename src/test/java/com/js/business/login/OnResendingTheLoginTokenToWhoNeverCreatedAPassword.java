package com.js.business.login;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.dependency.injection.CcpInstanceProvider;
import com.ccp.especifications.email.CcpEmailProvider;
import com.ccp.especifications.email.CcpEmailSuppressionList;
import com.ccp.especifications.http.CcpHttpMethods;
import com.ccp.especifications.http.CcpHttpRequestLog;
import com.ccp.flow.CcpErrorFlowDisturb;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.implementations.password.mindrot.CcpMindrotPasswordHandler;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.jn.entities.JnEntityEmailSuppressed;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.utils.JnLanguage;
import com.js.entities.JsEntityLoginPassword;
import com.js.entities.JsEntityLoginTokenRequestResend;
import com.js.services.JsServiceLogin;
import com.js.status.login.JsProcessStatusUnlockLoginToken;
import com.vis.entities.FixedSuppressionsEmailProvider;

/**
 * Proves the daily process that asks support to resend the login token: of the e-mails that got a token in the last 24
 * hours (read from the request log), only the ones neither suppressed by the provider nor with a password get the request;
 * a refusal of the service is counted and does not stop the others. The messaging is synchronous here.
 */
public class OnResendingTheLoginTokenToWhoNeverCreatedAPassword {

	static {
		CcpInstanceProvider<CcpEmailProvider> email = () -> FixedSuppressionsEmailProvider.INSTANCE;
		CcpInstanceProvider<CcpHttpRequestLog> requestLog = () -> FixedHttpRequestLog.INSTANCE;
		CcpDependencyInjection.removeAllDependencies();
		CcpDependencyInjection.loadAllDependencies(
				CcpLocalInstances.syncMensageriaListener,
				CcpLocalInstances.bucket,
				CcpLocalInstances.instantMessenger,
				email,
				requestLog,
				CcpElasticSearchQueryExecutor.INSTANCE,
				CcpElasticSearchDbRequest.INSTANCE,
				CcpMindrotPasswordHandler.INSTANCE,
				CcpLocalCacheInstances.mock,
				CcpElasticSerchDbBulk.INSTANCE,
				CcpElasticSearchCrud.INSTANCE,
				CcpGsonJsonHandler.INSTANCE,
				CcpApacheMimeHttp.INSTANCE
		);
	}

	private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/156.0.0.0";

	private static final String IP = "177.181.5.125";

	private final long now = System.currentTimeMillis();

	private final FixedHttpRequestLog requestLog = FixedHttpRequestLog.INSTANCE;

	private final FixedSuppressionsEmailProvider emailProvider = FixedSuppressionsEmailProvider.INSTANCE;

	@Before
	public void clear() {
		this.requestLog.clear();
		this.emailProvider.clear();
	}

	private String email(String prefix) {
		return prefix + this.now + "@teste.com";
	}

	private CcpJsonRepresentation key(String email) {
		return CcpOtherConstants.EMPTY_JSON.put(JnJsonCommonsFields.email, email);
	}

	private CcpJsonRepresentation loginRequest(String email) {
		return this.key(email)
				.put(JnJsonCommonsFields.userAgent, USER_AGENT)
				.put(JnJsonCommonsFields.ip, IP);
	}

	/** The login e-mail and the token, as the front asks them. */
	private void createTheToken(String email) {
		CcpJsonRepresentation request = this.loginRequest(email);
		try {
			JsServiceLogin.CreateLoginEmail.execute(request);
		} catch (CcpErrorFlowDisturb nextScreen) {
			// a new e-mail ends with the screen of the onboarding answers (201), after saving the e-mail
		}
		CcpJsonRepresentation withLanguage = request.put(JnJsonCommonsFields.language, JnLanguage.portuguese.name());
		JsServiceLogin.CreateLoginToken.execute(withLanguage);
	}

	/** A request of the log, as Cloud Run keeps it: the e-mail in the URL, encoded. */
	private CcpJsonRepresentation logRequest(String encodedEmail) {
		String url = "https://login.onias.site/login/" + encodedEmail + "/token/language/portuguese";
		return CcpOtherConstants.EMPTY_JSON
				.put(CcpHttpRequestLog.Fields.url, url)
				.put(CcpHttpRequestLog.Fields.userAgent, USER_AGENT)
				.put(CcpHttpRequestLog.Fields.ip, IP)
				.put(CcpHttpRequestLog.Fields.timestamp, this.now);
	}

	private CcpJsonRepresentation run() {
		return JsBusinessResendLoginTokenToUsersWithoutPassword.INSTANCE.execute(CcpOtherConstants.EMPTY_JSON);
	}

	private int counter(CcpJsonRepresentation result, JsBusinessResendLoginTokenToUsersWithoutPassword.JsonFieldNames field) {
		return result.getAsIntegerNumber(field);
	}

	@Test
	public void onlyWhoIsNeitherSuppressedNorWithAPasswordGetsTheRequest() {
		String withoutPassword = this.email("semsenha+alias");
		String suppressed = this.email("suprimido");
		String withPassword = this.email("comsenha");
		String withoutToken = this.email("semtoken");
		this.createTheToken(withoutPassword);
		this.createTheToken(suppressed);
		this.createTheToken(withPassword);
		JsEntityLoginPassword.ENTITY.save(this.key(withPassword).put(JnJsonCommonsFields.password, "Senha@2026"));
		CcpJsonRepresentation suppression = this.key(suppressed).put(JnEntityEmailSuppressed.Fields.created, Double.valueOf(1700000000L));
		this.emailProvider.lists.put(CcpEmailSuppressionList.bounces, List.of(suppression));
		String encodedWithoutPassword = withoutPassword.replace("+", "%2B");
		this.requestLog.requests.addAll(List.of(
				this.logRequest(encodedWithoutPassword),
				this.logRequest(encodedWithoutPassword),
				this.logRequest(suppressed),
				this.logRequest(withPassword),
				this.logRequest(withoutToken)));

		CcpJsonRepresentation result = this.run();

		assertTrue(JsEntityLoginTokenRequestResend.ENTITY.exists(this.key(withoutPassword)));
		assertFalse(JsEntityLoginTokenRequestResend.ENTITY.exists(this.key(suppressed)));
		assertFalse(JsEntityLoginTokenRequestResend.ENTITY.exists(this.key(withPassword)));
		assertFalse(JsEntityLoginTokenRequestResend.ENTITY.exists(this.key(withoutToken)));
		assertEquals(4, this.counter(result, JsBusinessResendLoginTokenToUsersWithoutPassword.JsonFieldNames.emailsWithToken));
		assertEquals(1, this.counter(result, JsBusinessResendLoginTokenToUsersWithoutPassword.JsonFieldNames.suppressedEmails));
		assertEquals(1, this.counter(result, JsBusinessResendLoginTokenToUsersWithoutPassword.JsonFieldNames.emailsWithPassword));
		assertEquals(1, this.counter(result, JsBusinessResendLoginTokenToUsersWithoutPassword.JsonFieldNames.resendRequested));
		CcpJsonRepresentation refused = result.getInnerJson(JsBusinessResendLoginTokenToUsersWithoutPassword.JsonFieldNames.resendRefused);
		assertEquals(Integer.valueOf(1), refused.getAsIntegerNumber(JsProcessStatusUnlockLoginToken.statusTokenNotExists));
	}

	@Test
	public void runningAgainOnTheSameDayDoesNotRequestTwice() {
		String withoutPassword = this.email("denovo");
		this.createTheToken(withoutPassword);
		this.requestLog.requests.add(this.logRequest(withoutPassword));
		this.run();

		CcpJsonRepresentation second = this.run();

		assertEquals(0, this.counter(second, JsBusinessResendLoginTokenToUsersWithoutPassword.JsonFieldNames.resendRequested));
		CcpJsonRepresentation refused = second.getInnerJson(JsBusinessResendLoginTokenToUsersWithoutPassword.JsonFieldNames.resendRefused);
		assertEquals(Integer.valueOf(1), refused.getAsIntegerNumber(JsProcessStatusUnlockLoginToken.statusAlreadyRequested));
	}

	@Test
	public void requestsOfOtherUrlsOrWithoutTheClientAreLeftOut() {
		CcpJsonRepresentation otherUrl = this.logRequest(this.email("outra")).put(CcpHttpRequestLog.Fields.url, "https://login.onias.site/login/x@teste.com/token");
		CcpJsonRepresentation withoutIp = this.logRequest(this.email("semip")).removeFields(CcpHttpRequestLog.Fields.ip);
		this.requestLog.requests.addAll(List.of(otherUrl, withoutIp));

		CcpJsonRepresentation result = this.run();

		assertEquals(0, this.counter(result, JsBusinessResendLoginTokenToUsersWithoutPassword.JsonFieldNames.emailsWithToken));
	}

	@Test
	public void theLogIsReadForTheCreatedTokensOfTheLast24Hours() {
		this.run();

		List<Object> reading = this.requestLog.readings.get(0);
		assertEquals("jobsnow-js", reading.get(0));
		assertEquals(CcpHttpMethods.POST, reading.get(1));
		assertEquals(200, reading.get(3));
		long startTime = (Long) reading.get(4);
		long distance = System.currentTimeMillis() - startTime;
		assertTrue("distance: " + distance, Math.abs(distance - 86_400_000L) < 60_000);
	}
}
