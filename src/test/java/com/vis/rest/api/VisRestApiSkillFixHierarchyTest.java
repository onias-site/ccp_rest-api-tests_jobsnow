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
import com.vis.entities.VisEntitySkillFixHierarchyPending;
import com.vis.json.fields.validation.VisSkillFixHierarchyTypes;

/**
 * Proves {@code VisRestApiSkillFixHierarchy} ({@code /resume/{email}/skills/hierarchy}): the session filter guards it,
 * the candidate asks for a fix of the hierarchy, reads it back as pending, cannot ask the same fix twice nor ask another
 * one on the same day, and withdraws it. The review by the support bot is proved in
 * {@code SkillFixHierarchyThroughSupportBotTest}.
 */
public class VisRestApiSkillFixHierarchyTest extends VisRestApiTestTemplate {

	enum JsonFieldNames implements CcpJsonFieldName {
		status, pending
	}

	private static final String HIERARCHY = "/skills/hierarchy";

	private final String run = new CcpStringDecorator("ABCDEFGHIJKLMNOPQRSTUVWXYZ").text().generateToken(8).content.toUpperCase();

	@Before
	public void openTheSession() {
		this.login();
	}

	private String parent(String name) {
		String parent = "TESTE.HTTP.PAI." + name + "." + this.run;
		return parent;
	}

	private CcpJsonRepresentation requestKey(String parent) {
		CcpJsonRepresentation key = CcpOtherConstants.EMPTY_JSON
				.put(VisEntitySkillFixHierarchyPending.Fields.parent, parent)
				.put(VisEntitySkillFixHierarchyPending.Fields.type, VisSkillFixHierarchyTypes.add);
		return key;
	}

	private CcpJsonRepresentation request(String parent) {
		CcpJsonRepresentation key = this.requestKey(parent);
		CcpJsonRepresentation request = key
				.put(VisEntitySkillFixHierarchyPending.Fields.description, "Estas habilidades dependem do pai informado")
				.put(VisEntitySkillFixHierarchyPending.Fields.skill, Arrays.asList("TESTE.HTTP.FILHA." + this.run));
		return request;
	}

	private HttpResponse<String> ask(String parent) {
		CcpJsonRepresentation request = this.request(parent);
		String path = this.resumePath(HIERARCHY);
		HttpResponse<String> response = this.sendWithTheSession(Fields.POST, path, request);
		return response;
	}

	private HttpResponse<String> search(String parent) {
		CcpJsonRepresentation key = this.requestKey(parent);
		String path = this.resumePath(HIERARCHY + "/search");
		HttpResponse<String> response = this.sendWithTheSession(Fields.POST, path, key);
		return response;
	}

	private HttpResponse<String> withdraw(String parent) {
		CcpJsonRepresentation key = this.requestKey(parent);
		String path = this.resumePath(HIERARCHY);
		HttpResponse<String> response = this.sendWithTheSession(Fields.DELETE, path, key);
		return response;
	}

	@Test
	public void theRequestIsReadBackAsPendingAndWithdrawn() {
		String parent = this.parent("PENDENTE");

		this.assertStatus(200, this.ask(parent));

		HttpResponse<String> found = this.search(parent);
		this.assertStatus(200, found);
		CcpJsonRepresentation request = this.bodyOf(found);
		assertEquals(request.toString(), JsonFieldNames.pending.name(), request.getAsString(JsonFieldNames.status));

		this.assertStatus(200, this.withdraw(parent));

		HttpResponse<String> afterWithdrawal = this.search(parent);
		this.assertStatus(200, afterWithdrawal);
		CcpJsonRepresentation nothing = this.bodyOf(afterWithdrawal);
		assertTrue(nothing.toString(), nothing.isEmpty());
	}

	@Test
	public void theSameFixCannotBeAskedTwice() {
		String parent = this.parent("REPETIDO");
		this.assertStatus(200, this.ask(parent));

		this.assertStatus(409, this.ask(parent));

		this.withdraw(parent);
	}

	@Test
	public void aSecondFixOnTheSameDayReachesTheDailyLimit() {
		String firstParent = this.parent("PRIMEIRO");
		String secondParent = this.parent("SEGUNDO");
		this.assertStatus(200, this.ask(firstParent));

		this.assertStatus(429, this.ask(secondParent));

		this.withdraw(firstParent);
	}

	@Test
	public void withdrawingWhatWasNeverAskedIsNotFound() {
		String parent = this.parent("NUNCA.PEDIDO");

		this.assertStatus(404, this.withdraw(parent));
	}

	@Test
	public void aRequestWithNoSkillsIsAValidationError() {
		String parent = this.parent("SEM.FILHAS");
		CcpJsonRepresentation key = this.requestKey(parent);
		String path = this.resumePath(HIERARCHY);

		HttpResponse<String> response = this.sendWithTheSession(Fields.POST, path, key);

		this.assertStatus(422, response);
	}

	@Test
	public void aRequestWithNoSessionIsStoppedByTheSessionValidation() {
		String path = this.resumePath(HIERARCHY);

		HttpResponse<String> response = this.sendWithNoSession(Fields.POST, path);

		this.assertStatus(422, response);
		assertTrue(response.body(), response.body().contains("JsBusinessValidateLogin"));
	}

	@Test
	public void aRequestWithAFakeSessionIsUnauthorized() {
		String parent = this.parent("SESSAO.FALSA");
		CcpJsonRepresentation request = this.request(parent);
		String path = this.resumePath(HIERARCHY);

		HttpResponse<String> response = this.send(Fields.POST, path, request, FAKE_SESSION_TOKEN);

		this.assertStatus(401, response);
	}
}
