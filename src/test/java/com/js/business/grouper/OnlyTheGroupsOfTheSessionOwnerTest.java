package com.js.business.grouper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.BeforeClass;
import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.flow.CcpErrorFlowDisturb;
import com.ccp.process.CcpProcessStatusDefault;
import com.jn.business.grouper.JnBusinessGetGroup;
import com.jn.entities.JnEntityLoginEmail;

/**
 * Proves {@link JsBusinessOnlyTheGroupsOfTheSessionOwner}: the group of another user answers 404, and since 2026-11-12
 * the public group of the counter of the forbidden consultancies ({@code vis_virtual_not_allowed_company_counter} /
 * {@code vis_resume}) is served to any logged-in user.
 */
public class OnlyTheGroupsOfTheSessionOwnerTest {

	private static final String SESSION_EMAIL = "dono.da.sessao@teste.com";

	private static final String COUNTER_PARENT_ID = "1cb239585190afdf1cdda1a055a96a1a62a1ba9d";

	@BeforeClass
	public static void loadTheJsonHandler() {
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE);
	}

	private CcpJsonRepresentation askFor(String parentName, String childName, String parentId) {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(JnEntityLoginEmail.Fields.email, SESSION_EMAIL)
				.put(JnBusinessGetGroup.Fields.parentName, parentName)
				.put(JnBusinessGetGroup.Fields.childName, childName)
				.put(JnBusinessGetGroup.Fields.parentId, parentId);
		return json;
	}

	@Test
	public void anyLoggedInUserReadsTheCounterOfTheForbiddenConsultancies() {
		CcpJsonRepresentation asked = this.askFor("vis_virtual_not_allowed_company_counter", "vis_resume", COUNTER_PARENT_ID);
		CcpJsonRepresentation answer = JsBusinessOnlyTheGroupsOfTheSessionOwner.INSTANCE.execute(asked);
		assertEquals(asked, answer);
	}

	@Test
	public void theParentOfThePublicGroupWithAnotherChildIsNotPublic() {
		CcpJsonRepresentation asked = this.askFor("vis_virtual_not_allowed_company_counter", "vis_recruiter", COUNTER_PARENT_ID);
		this.assertNotFound(asked);
	}

	@Test
	public void theLoginHistoryOfAnotherUserStaysHidden() {
		CcpJsonRepresentation asked = this.askFor("jn_login_email", "js_login_session_validation", "id-de-outro-usuario");
		this.assertNotFound(asked);
	}

	private void assertNotFound(CcpJsonRepresentation asked) {
		try {
			JsBusinessOnlyTheGroupsOfTheSessionOwner.INSTANCE.execute(asked);
			fail("a group that is neither of the session owner nor public must answer 404");
		} catch (CcpErrorFlowDisturb expected) {
			assertEquals(CcpProcessStatusDefault.NOT_FOUND, expected.status);
		}
	}
}
