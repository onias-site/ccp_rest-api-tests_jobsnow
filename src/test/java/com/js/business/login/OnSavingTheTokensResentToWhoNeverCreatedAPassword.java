package com.js.business.login;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpFieldName;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.dependency.injection.CcpInstanceProvider;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.email.CcpEmailProvider;
import com.ccp.especifications.http.CcpHttpRequestLog;
import com.vis.entities.FixedSuppressionsEmailProvider;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.implementations.password.mindrot.CcpMindrotPasswordHandler;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.js.entities.JsEntityLoginPassword;
import com.js.entities.JsEntityLoginTokenRequestResend;
import com.js.entities.JsEntityLoginTokenResentWithoutPassword;

/**
 * Proves the process that runs right after the resending of the login token: every fulfilled resending
 * ({@code js_login_token_fulfilled_resend}) whose e-mail has no password goes, by bulk, to
 * {@code js_login_token_resent_without_password} with the e-mail as its SHA-1 hash; the insertion of the password deletes
 * it. The messaging is synchronous here.
 */
public class OnSavingTheTokensResentToWhoNeverCreatedAPassword {

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

	private final long now = System.currentTimeMillis();

	private final CcpEntity fulfilled = JsEntityLoginTokenRequestResend.ENTITY.getTwinEntity();

	private final CcpEntity withoutPassword = JsEntityLoginTokenResentWithoutPassword.ENTITY;

	private String email(String prefix) {
		return prefix + this.now + "@teste.com";
	}

	private CcpJsonRepresentation key(String email) {
		return CcpOtherConstants.EMPTY_JSON.put(JnJsonCommonsFields.email, email);
	}

	private void savePassword(String email) {
		JsEntityLoginPassword.ENTITY.save(this.key(email).put(JnJsonCommonsFields.password, "Senha@2026"));
	}

	/** The process reads the twin by a search, which sees a document only after the refresh of the index. */
	private CcpJsonRepresentation run() {
		this.post("js_login_token_fulfilled_resend/_refresh", "");
		return JsBusinessSaveTokensResentWithoutPassword.INSTANCE.execute(CcpOtherConstants.EMPTY_JSON);
	}

	private String post(String path, String body) {
		HttpRequest request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:9200/" + path))
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(body))
				.build();
		try {
			HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
			return response.body();
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	/** How many documents of the index have this exact value in the e-mail. */
	private int countByStoredEmail(String value) {
		this.post("js_login_token_resent_without_password/_refresh", "");
		String body = "{\"query\":{\"term\":{\"email\":\"" + value + "\"}}}";
		String response = this.post("js_login_token_resent_without_password/_count", body);
		return new CcpJsonRepresentation(response).getAsIntegerNumber(new CcpFieldName("count"));
	}

	@Test
	public void aFulfilledResendingWithoutPasswordIsSavedWithTheEmailHashed() throws Exception {
		String email = this.email("atendido");
		this.fulfilled.save(this.key(email));

		CcpJsonRepresentation result = this.run();

		assertTrue(this.withoutPassword.exists(this.key(email)));
		assertEquals(0, this.countByStoredEmail(email));
		assertTrue(result.getAsIntegerNumber(JsBusinessSaveTokensResentWithoutPassword.JsonFieldNames.savedWithoutPassword) >= 1);
	}

	@Test
	public void aFulfilledResendingOfWhoHasAPasswordIsLeftOut() {
		String email = this.email("comsenha");
		this.savePassword(email);
		this.fulfilled.save(this.key(email));

		CcpJsonRepresentation result = this.run();

		assertFalse(this.withoutPassword.exists(this.key(email)));
		assertTrue(result.getAsIntegerNumber(JsBusinessSaveTokensResentWithoutPassword.JsonFieldNames.withPassword) >= 1);
	}

	@Test
	public void insertingThePasswordDeletesTheRecord() {
		String email = this.email("criousenha");
		this.fulfilled.save(this.key(email));
		this.run();
		assertTrue(this.withoutPassword.exists(this.key(email)));

		this.savePassword(email);

		assertFalse(this.withoutPassword.exists(this.key(email)));
	}

	@Test
	public void runningTwiceKeepsOneRecord() throws Exception {
		String email = this.email("duasvezes");
		this.fulfilled.save(this.key(email));

		this.run();
		this.run();

		assertTrue(this.withoutPassword.exists(this.key(email)));
	}

	@Test
	public void theDailyProcessRunsTheResendingAndThenThisOne() {
		FixedHttpRequestLog.INSTANCE.clear();
		FixedSuppressionsEmailProvider.INSTANCE.clear();
		String email = this.email("diario");
		this.fulfilled.save(this.key(email));

		this.post("js_login_token_fulfilled_resend/_refresh", "");
		CcpJsonRepresentation result = JsBusinessDailyLoginTokenProcess.INSTANCE.execute(CcpOtherConstants.EMPTY_JSON);

		assertEquals(1, FixedHttpRequestLog.INSTANCE.readings.size());
		assertTrue(result.containsField(JsBusinessResendLoginTokenToUsersWithoutPassword.JsonFieldNames.resendRequested));
		assertTrue(result.containsField(JsBusinessSaveTokensResentWithoutPassword.JsonFieldNames.savedWithoutPassword));
		assertTrue(this.withoutPassword.exists(this.key(email)));
	}
}
