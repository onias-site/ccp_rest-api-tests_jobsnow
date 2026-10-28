package com.js.rest.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.implementations.password.mindrot.CcpMindrotPasswordHandler;
import com.ccp.json.fields.validation.CcpJsonCommonsFields;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.jn.entities.JnEntityGrouper;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.rest.api.commons.TestVariables;
import com.jn.utils.JnGroupers;
import com.jn.entities.JnEntityLoginEmail;

/**
 * Proves the endpoint of the groups of the js ({@code GET /grouper/{email}/{parentName}/{childName}/{parentId}}, port
 * 8082, which must be up): the session is validated, the group comes by its primary key on the side asked (mainEntity by
 * default, or twinEntity), an unknown group answers 404 and an unknown side 422; {@code .../bothSides} brings both sides
 * at once, each one under the name of its entity.
 */
public class GrouperEndpointTest {

	enum JsonFieldNames implements CcpJsonFieldName {
		User_Agent {
			public String getValue() {
				return "User-Agent";
			}
		},
		bothSides_path {
			public String getValue() {
				return "/bothSides";
			}
		},
		filhoAtivo, filhoInativo, inexistente, mainEntity, twinEntity, inactive
	}

	static {
		CcpDependencyInjection.removeAllDependencies();
		CcpDependencyInjection.loadAllDependencies(
				CcpLocalInstances.syncMensageriaListener,
				CcpLocalInstances.bucket,
				CcpLocalInstances.email,
				CcpLocalInstances.instantMessenger,
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

	private static final String PARENT_NAME = "zz_test_parent";

	private static final String CHILD_NAME = "zz_test_child";

	private static final String SESSION_TOKEN = "12345678";

	private static final String JS_API_HOST = "localhost";

	private static final int JS_API_PORT = 8082;

	private final String parentId = "grupoapi" + System.currentTimeMillis();

	private final TestVariables testVariables = new TestVariables();

	private CcpJsonRepresentation groupKey() {
		return JnGroupers.getGroupKey(PARENT_NAME, CHILD_NAME, this.parentId);
	}

	private void addChild(boolean twinSide, JsonFieldNames childId) {
		CcpJsonRepresentation parentJson = CcpOtherConstants.EMPTY_JSON.put(JnEntityGrouper.Fields.parentId, this.parentId);
		Map<String, CcpJsonRepresentation> parents = Map.of(this.parentId, parentJson);
		String childIdText = childId.getValue();
		JnGroupers.addChild(JnGroupers.getGrouper(twinSide), PARENT_NAME, CHILD_NAME, childIdText, parents);
	}

	/**
	 * Stops the class with a clear message when the js API is down: until 2026-10-27 every test failed with a bare
	 * {@code SoftException} of the connection refused.
	 */
	@BeforeClass
	public static void theJsApiMustBeUp() {
		try (Socket socket = new Socket()) {
			InetSocketAddress jsApi = new InetSocketAddress(JS_API_HOST, JS_API_PORT);
			socket.connect(jsApi, 2_000);
		} catch (IOException e) {
			throw new AssertionError("The js API is down on port " + JS_API_PORT + ": start JsRestApiSpringStarter before running " + GrouperEndpointTest.class.getSimpleName(), e);
		}
	}

	@Before
	public void createTheGroupOnBothSidesAndALogin() {
		this.addChild(false, JsonFieldNames.filhoAtivo);
		this.addChild(true, JsonFieldNames.filhoInativo);
		JnEntityLoginEmail.ENTITY.save(this.testVariables.REQUEST_TO_LOGIN);
		this.testVariables.saveCurrentSession(SESSION_TOKEN);
	}

	@After
	public void deleteTheGroup() {
		JnEntityGrouper.ENTITY.deleteAnyWhere(this.groupKey());
	}

	private HttpResponse<String> get(String email, String sessionToken, String parentId, String query) throws Exception {
		URI uri = URI.create("http://" + JS_API_HOST + ":" + JS_API_PORT + "/grouper/" + email + "/" + PARENT_NAME + "/" + CHILD_NAME + "/" + parentId + query);
		// the session key holds the user agent, so the request repeats the one saved with the session
		String userAgent = this.testVariables.REQUEST_TO_LOGIN.getAsString(JnJsonCommonsFields.userAgent);
		String sessionTokenHeader = CcpJsonCommonsFields.sessionToken.getValue();
		String userAgentHeader = JsonFieldNames.User_Agent.getValue();
		HttpRequest request = HttpRequest.newBuilder(uri)
				.header(sessionTokenHeader, sessionToken)
				.header(userAgentHeader, userAgent)
				.GET()
				.build();
		HttpClient client = HttpClient.newHttpClient();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		return response;
	}

	private HttpResponse<String> getWithTheSession(String parentId, String query) throws Exception {
		HttpResponse<String> response = this.get(this.testVariables.VALID_EMAIL, SESSION_TOKEN, parentId, query);
		return response;
	}

	private List<String> getChildren(HttpResponse<String> response) {
		String body = response.body();
		CcpJsonRepresentation group = new CcpJsonRepresentation(body);
		List<String> children = group.getAsStringList(JnEntityGrouper.Fields.child);
		return children;
	}

	@Test
	public void theActiveGroupComesByDefault() throws Exception {
		HttpResponse<String> response = this.getWithTheSession(this.parentId, "");

		assertEquals(response.body(), 200, response.statusCode());
		List<String> children = this.getChildren(response);
		assertEquals(Arrays.asList(JsonFieldNames.filhoAtivo.getValue()), children);
	}

	@Test
	public void theActiveSideComesWhenAsked() throws Exception {
		HttpResponse<String> response = this.getWithTheSession(this.parentId, "?side=" + JsonFieldNames.mainEntity.getValue());

		assertEquals(response.body(), 200, response.statusCode());
		List<String> children = this.getChildren(response);
		assertEquals(Arrays.asList(JsonFieldNames.filhoAtivo.getValue()), children);
	}

	@Test
	public void theInactiveSideComesWhenAsked() throws Exception {
		HttpResponse<String> response = this.getWithTheSession(this.parentId, "?side=" + JsonFieldNames.twinEntity.getValue());

		assertEquals(response.body(), 200, response.statusCode());
		List<String> children = this.getChildren(response);
		assertEquals(Arrays.asList(JsonFieldNames.filhoInativo.getValue()), children);
	}

	@Test
	public void bothSidesComeTogether() throws Exception {
		HttpResponse<String> response = this.getWithTheSession(this.parentId, JsonFieldNames.bothSides_path.getValue());

		assertEquals(response.body(), 200, response.statusCode());
		String body = response.body();
		CcpJsonRepresentation groupOnBothSides = new CcpJsonRepresentation(body);
		CcpJsonRepresentation activeGroup = groupOnBothSides.getInnerJson(JnEntityGrouper.ENTITY);
		CcpJsonRepresentation inactiveGroup = groupOnBothSides.getInnerJson(JnEntityGrouper.ENTITY.getTwinEntity());
		List<String> activeChildren = activeGroup.getAsStringList(JnEntityGrouper.Fields.child);
		List<String> inactiveChildren = inactiveGroup.getAsStringList(JnEntityGrouper.Fields.child);
		assertEquals(Arrays.asList(JsonFieldNames.filhoAtivo.getValue()), activeChildren);
		assertEquals(Arrays.asList(JsonFieldNames.filhoInativo.getValue()), inactiveChildren);
	}

	@Test
	public void anUnknownGroupOnBothSidesIsNotFound() throws Exception {
		String unknownParentId = this.parentId + JsonFieldNames.inexistente.getValue();

		HttpResponse<String> response = this.getWithTheSession(unknownParentId, JsonFieldNames.bothSides_path.getValue());

		assertEquals(response.body(), 404, response.statusCode());
	}

	@Test
	public void bothSidesWithAFakeSessionIsUnauthorized() throws Exception {
		HttpResponse<String> response = this.get(this.testVariables.VALID_EMAIL, "ABCD1234", this.parentId, JsonFieldNames.bothSides_path.getValue());

		assertEquals(response.body(), 401, response.statusCode());
	}

	@Test
	public void anUnknownGroupIsNotFound() throws Exception {
		String unknownParentId = this.parentId + JsonFieldNames.inexistente.getValue();

		HttpResponse<String> response = this.getWithTheSession(unknownParentId, "");

		assertEquals(response.body(), 404, response.statusCode());
	}

	@Test
	public void anUnknownSideIsAValidationError() throws Exception {
		HttpResponse<String> response = this.getWithTheSession(this.parentId, "?side=" + JsonFieldNames.inactive.getValue());

		assertEquals(response.body(), 422, response.statusCode());
	}

	@Test
	public void aGroupWithNoSessionIsStoppedByTheSessionValidation() throws Exception {
		HttpResponse<String> response = this.get(this.testVariables.VALID_EMAIL, "", this.parentId, "");

		assertEquals(response.body(), 422, response.statusCode());
		assertTrue(response.body(), response.body().contains("JsBusinessValidateLogin"));
	}

	@Test
	public void aGroupWithAFakeSessionIsUnauthorized() throws Exception {
		HttpResponse<String> response = this.get(this.testVariables.VALID_EMAIL, "ABCD1234", this.parentId, "");

		assertEquals(response.body(), 401, response.statusCode());
	}

	@Test
	public void aGroupWithAnInvalidEmailIsABadRequest() throws Exception {
		HttpResponse<String> response = this.get(TestVariables.INVALID_EMAIL, SESSION_TOKEN, this.parentId, "");

		assertEquals(response.body(), 400, response.statusCode());
	}
}
