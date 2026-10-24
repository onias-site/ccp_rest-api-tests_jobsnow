package com.js.db.bulk.handlers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.bulk.CcpBulkEntityOperationType;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.jn.entities.JnEntityGrouper;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.js.entities.JsEntityLoginSessionValidation;

/**
 * {@link JsBulkHandlerRegisterLogout}, the bulk of the logout: a session found moves to the twin
 * ({@code js_login_session_validation} → its twin) and leaves its logout ({@code userAgent}, {@code ip},
 * {@code logoutDate}) in the history of the e-mail in {@code jn_inactive_grouper}; a session not found writes nothing,
 * not even the history. The items are only produced here, nothing is written ({@code LoginHistoryTest} writes them).
 */
public class JsBulkHandlerRegisterLogoutTest {

	static {
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE);
	}

	private final CcpJsonRepresentation session = CcpOtherConstants.EMPTY_JSON
			.put(JsEntityLoginSessionValidation.Fields.email, "logout.registrado@teste.com")
			.put(JsEntityLoginSessionValidation.Fields.token, "TOKEN-DO-LOGOUT")
			.put(JnJsonCommonsFields.ip, "127.0.0.1")
			.put(JnJsonCommonsFields.userAgent, "Apache-HttpClient/4.5.4 (Java/17.0.9)");

	private static String nameOf(CcpEntity entity) {
		return entity.getEntityMetaData().entityName;
	}

	/** Each item as "entity operation", in order. */
	private static List<String> describe(List<CcpBulkItem> items) {
		List<String> descriptions = items.stream().map(item -> nameOf(item.entity) + " " + item.operation).collect(Collectors.toList());
		return descriptions;
	}

	@Test
	public void searchesTheSessions() {
		assertSame(JsEntityLoginSessionValidation.ENTITY, JsBulkHandlerRegisterLogout.INSTANCE.getEntityToSearch());
	}

	@Test
	public void aSessionFoundGoesToTheTwinAndItsLogoutToTheInactiveHistory() {
		List<CcpBulkItem> items = JsBulkHandlerRegisterLogout.INSTANCE.whenRecordWasFoundInTheEntitySearch(this.session, this.session);

		CcpEntity sessions = JsEntityLoginSessionValidation.ENTITY;
		CcpEntity inactiveGrouper = JnEntityGrouper.ENTITY.getTwinEntity();
		List<String> descriptions = describe(items);
		assertTrue(descriptions.toString(), descriptions.contains(nameOf(sessions.getTwinEntity()) + " " + CcpBulkEntityOperationType.create));
		assertTrue(descriptions.toString(), descriptions.contains(nameOf(sessions) + " " + CcpBulkEntityOperationType.delete));

		List<CcpBulkItem> historyItems = items.stream().filter(item -> nameOf(item.entity).equals(nameOf(inactiveGrouper))).collect(Collectors.toList());
		assertEquals(descriptions.toString(), 1, historyItems.size());
		// the logout only enters the inactive side of the history; the active side keeps the logins
		assertTrue(descriptions.toString(), descriptions.stream().noneMatch(description -> description.startsWith(nameOf(JnEntityGrouper.ENTITY) + " ")));
	}

	@Test
	public void aSessionNotFoundWritesNothing() {
		List<CcpBulkItem> items = JsBulkHandlerRegisterLogout.INSTANCE.whenRecordWasNotFoundInTheEntitySearch(this.session);

		assertTrue(describe(items).toString(), items.isEmpty());
	}
}
