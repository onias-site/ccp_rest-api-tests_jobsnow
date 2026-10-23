package com.js.db.bulk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.hash.CcpHashAlgorithm;
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
import com.jn.db.bulk.JnExecuteBulkOperation;
import com.jn.entities.JnEntityGrouper;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.rest.api.commons.TestVariables;
import com.jn.utils.JnDeleteKeysFromCache;
import com.jn.utils.JnGroupers;
import com.js.business.login.JsBusinessExecuteLogin;
import com.js.business.login.JsBusinessExecuteLogout;
import com.js.entities.JsEntityLoginEmail;
import com.js.entities.JsEntityLoginSessionValidation;

/**
 * Proves the history of the logins in the group of the e-mail ({@code JsBulkItemsLoginHistory}): the login adds
 * {@code {userAgent, ip, loginDate}} to the group of {@code jn_grouper}, the logout adds {@code {userAgent, ip,
 * logoutDate}} to the group of {@code jn_inactive_grouper}, both written by the bulk of the login and of the logout, and
 * each side keeps only the last 100.
 */
public class LoginHistoryTest {

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

	private static final String DATE_PATTERN = "\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}:\\d{2}";

	private final TestVariables testVariables = new TestVariables();

	private final String sessionToken = "TOKEN" + System.currentTimeMillis();

	private static String sha1(String text) {
		return new CcpStringDecorator(text).hash().asString(CcpHashAlgorithm.SHA1);
	}

	/** The parent id of the group of the e-mail: the id in {@code js_login_email} of the e-mail already transformed. */
	private String parentId() {
		String emailHash = sha1(this.testVariables.VALID_EMAIL);
		CcpJsonRepresentation parentJson = CcpOtherConstants.EMPTY_JSON.put(JsEntityLoginEmail.Fields.email, emailHash);
		String parentId = JsEntityLoginEmail.ENTITY.calculateId(parentJson);
		return parentId;
	}

	private String parentName() {
		return JsEntityLoginEmail.ENTITY.getEntityMetaData().entityName;
	}

	private String childName() {
		return JsEntityLoginSessionValidation.ENTITY.getEntityMetaData().entityName;
	}

	private List<CcpJsonRepresentation> history(boolean twinSide) {
		List<String> children = JnGroupers.getChildren(twinSide, this.parentName(), this.childName(), this.parentId());
		List<CcpJsonRepresentation> history = new ArrayList<>();
		for (String child : children) {
			history.add(new CcpJsonRepresentation(child));
		}
		return history;
	}

	@After
	public void deleteTheHistory() {
		CcpJsonRepresentation groupKey = JnGroupers.getGroupKey(this.parentName(), this.childName(), this.parentId());
		JnEntityGrouper.ENTITY.deleteAnyWhere(groupKey);
	}

	private void login() {
		CcpJsonRepresentation login = this.testVariables.REQUEST_TO_LOGIN.put(CcpJsonCommonsFields.sessionToken, this.sessionToken);
		JsBusinessExecuteLogin.INSTANCE.execute(login);
	}

	private void logout() {
		CcpJsonRepresentation logout = this.testVariables.REQUEST_TO_LOGIN.put(JsEntityLoginSessionValidation.Fields.token, this.sessionToken);
		JsBusinessExecuteLogout.INSTANCE.execute(logout);
	}

	@Test
	public void theLoginAddsUserAgentIpAndLoginDateToTheActiveGroup() {
		this.login();

		List<CcpJsonRepresentation> history = this.history(false);
		assertEquals(1, history.size());
		CcpJsonRepresentation login = history.get(0);
		assertEquals(3, login.fieldSet().size());
		assertEquals(this.testVariables.REQUEST_TO_LOGIN.getAsString(JnJsonCommonsFields.userAgent), login.getAsString(JnJsonCommonsFields.userAgent));
		assertEquals(this.testVariables.REQUEST_TO_LOGIN.getAsString(JnJsonCommonsFields.ip), login.getAsString(JnJsonCommonsFields.ip));
		String loginDate = login.getAsString(JsBulkItemsLoginHistory.Fields.loginDate);
		assertTrue(loginDate, loginDate.matches(DATE_PATTERN));
		assertTrue(this.history(true).isEmpty());
	}

	@Test
	public void theLogoutAddsUserAgentIpAndLogoutDateToTheInactiveGroup() {
		this.login();
		this.logout();

		List<CcpJsonRepresentation> history = this.history(true);
		assertEquals(1, history.size());
		CcpJsonRepresentation logout = history.get(0);
		assertEquals(3, logout.fieldSet().size());
		assertEquals(this.testVariables.REQUEST_TO_LOGIN.getAsString(JnJsonCommonsFields.userAgent), logout.getAsString(JnJsonCommonsFields.userAgent));
		assertEquals(this.testVariables.REQUEST_TO_LOGIN.getAsString(JnJsonCommonsFields.ip), logout.getAsString(JnJsonCommonsFields.ip));
		String logoutDate = logout.getAsString(JsBulkItemsLoginHistory.Fields.logoutDate);
		assertTrue(logoutDate, logoutDate.matches(DATE_PATTERN));
		assertEquals(1, this.history(false).size());
	}

	@Test
	public void eachLoginIsKeptEvenFromTheSameClient() {
		this.login();
		this.login();

		assertEquals(2, this.history(false).size());
	}

	@Test
	public void eachSideKeepsOnlyTheLast100() {
		String emailHash = sha1(this.testVariables.VALID_EMAIL);
		// one bulk per login, as in the login itself: a bulk keeps a single item per document
		for (int login = 0; login < 105; login++) {
			CcpJsonRepresentation session = CcpOtherConstants.EMPTY_JSON
					.put(JnJsonCommonsFields.email, emailHash)
					.put(JnJsonCommonsFields.ip, "127.0.0.1")
					.put(JnJsonCommonsFields.userAgent, "agent" + login);
			List<CcpBulkItem> loginItems = JsBulkItemsLoginHistory.LOGIN.apply(session);
			JnExecuteBulkOperation.INSTANCE.executeBulk(loginItems, JnDeleteKeysFromCache.INSTANCE);
		}

		List<CcpJsonRepresentation> history = this.history(false);
		assertEquals(JsBulkItemsLoginHistory.MAX_CHILDREN, history.size());
		assertEquals("agent5", history.get(0).getAsString(JnJsonCommonsFields.userAgent));
		assertEquals("agent104", history.get(99).getAsString(JnJsonCommonsFields.userAgent));
		CcpJsonRepresentation group = JnGroupers.getGroup(false, this.parentName(), this.childName(), this.parentId());
		assertEquals(Integer.valueOf(100), group.getAsIntegerNumber(JnEntityGrouper.Fields.childCount));
	}
}
