package com.js.business.login;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.ccp.business.CcpBusiness;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.flow.CcpErrorFlowDisturb;
import com.ccp.hash.CcpHashAlgorithm;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.process.CcpProcessStatus;
import com.js.entities.JsEntityLoginSessionConflict;
import com.js.entities.JsEntityLoginSessionValidation;
import com.js.status.login.JsProcessStatusExecuteLogin;
import com.js.status.login.JsProcessStatusExecuteLogout;

/**
 * The two businesses that compare the token of the request with the current session of the e-mail
 * ({@code currentSessionTokenHash} of {@link JsEntityLoginSessionConflict}, found under {@code _entities}):
 * {@link JsBusinessCheckCurrentSession} lets only the current session through (a session replaced by a newer login is
 * refused, with the status of the flow that uses it); {@link JsBusinessRefuseSessionOfAnotherClient} does the opposite,
 * refusing the right token when it is sent by another client.
 */
public class SessionTokenComparisonBusinessesTest {

	static {
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE);
	}

	private static final String TOKEN = "TOKEN-DA-SESSAO-ATUAL";

	private static String sha1(String text) {
		return new CcpStringDecorator(text).hash().asString(CcpHashAlgorithm.SHA1);
	}

	/** The request with its token and, under {@code _entities}, the current session of the e-mail. */
	private CcpJsonRepresentation request(String currentSessionTokenHash) {
		CcpJsonRepresentation conflict = CcpOtherConstants.EMPTY_JSON.put(JsEntityLoginSessionConflict.Fields.currentSessionTokenHash, currentSessionTokenHash);
		CcpJsonRepresentation request = CcpOtherConstants.EMPTY_JSON
				.put(JsEntityLoginSessionValidation.Fields.email, "sessao.atual@teste.com")
				.put(JsEntityLoginSessionValidation.Fields.token, TOKEN)
				.addToItem(CcpEntity.JsonFieldNames._entities, JsEntityLoginSessionConflict.ENTITY, conflict);
		return request;
	}

	/** Runs the business expecting a refusal and answers the error. */
	private CcpErrorFlowDisturb refusalOf(CcpBusiness business, CcpJsonRepresentation request) {
		try {
			business.execute(request);
		} catch (CcpErrorFlowDisturb refusal) {
			return refusal;
		}
		fail("The request was accepted");
		throw new IllegalStateException();
	}

	private void assertRefused(CcpBusiness business, CcpJsonRepresentation request, CcpProcessStatus expectedStatus) {
		CcpErrorFlowDisturb refusal = this.refusalOf(business, request);
		assertEquals(expectedStatus, refusal.status);
		// the records read by the flow do not leak into the answer of the refusal
		assertFalse(refusal.json.toString(), refusal.json.containsAllFields(CcpEntity.JsonFieldNames._entities));
	}

	@Test
	public void theCurrentSessionPassesTheCheck() {
		CcpJsonRepresentation request = this.request(sha1(TOKEN));

		assertSame(request, JsBusinessCheckCurrentSession.TO_VALIDATE_LOGIN.execute(request));
		assertSame(request, JsBusinessCheckCurrentSession.TO_EXECUTE_LOGOUT.execute(request));
	}

	@Test
	public void aReplacedSessionIsRefusedWithTheStatusOfTheFlow() {
		CcpJsonRepresentation request = this.request(sha1("TOKEN-DE-UM-LOGIN-MAIS-NOVO"));

		this.assertRefused(JsBusinessCheckCurrentSession.TO_VALIDATE_LOGIN, request, JsProcessStatusExecuteLogin.invalidSession);
		this.assertRefused(JsBusinessCheckCurrentSession.TO_EXECUTE_LOGOUT, request, JsProcessStatusExecuteLogout.missingLogin);
	}

	@Test
	public void withoutACurrentSessionTheCheckRefuses() {
		CcpJsonRepresentation request = CcpOtherConstants.EMPTY_JSON
				.put(JsEntityLoginSessionValidation.Fields.email, "sem.sessao@teste.com")
				.put(JsEntityLoginSessionValidation.Fields.token, TOKEN);

		this.assertRefused(JsBusinessCheckCurrentSession.TO_VALIDATE_LOGIN, request, JsProcessStatusExecuteLogin.invalidSession);
	}

	@Test
	public void aTokenOfNoCurrentSessionIsNotTheSessionOfAnotherClient() {
		CcpJsonRepresentation request = this.request(sha1("TOKEN-DE-OUTRA-SESSAO"));

		assertSame(request, JsBusinessRefuseSessionOfAnotherClient.INSTANCE.execute(request));
	}

	@Test
	public void theCurrentTokenSentByAnotherClientIsRefused() {
		CcpJsonRepresentation request = this.request(sha1(TOKEN));

		this.assertRefused(JsBusinessRefuseSessionOfAnotherClient.INSTANCE, request, JsProcessStatusExecuteLogin.invalidSession);
	}
}
