package com.ccp.especifications.db.bulk;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.process.CcpProcessStatusDefault;
import com.vis.entities.VisEntitySkillSynonym;

/** {@link CcpBulkItemThrowNotFound} is the {@code NOT_FOUND} handler of the bulk delete: it throws for the item. */
public class CcpBulkItemThrowNotFoundTest {

	static {
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE);
	}

	@Test
	public void theItemNotFoundRaisesTheError() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(VisEntitySkillSynonym.Fields.skill, "MISSING");
		CcpBulkItem item = new CcpBulkItem(json, CcpBulkEntityOperationType.delete, VisEntitySkillSynonym.ENTITY, "anyId");
		try {
			CcpBulkItemThrowNotFound.INSTANCE.apply(item);
			fail("the item not found was accepted");
		} catch (CcpErrorBulkEntityRecordNotFound expected) {
		}
	}

	@Test
	public void theDeleteOperationUsesItForNotFound() throws Exception {
		java.lang.reflect.Field handlersField = CcpBulkEntityOperationType.class.getDeclaredField("handlers");
		handlersField.setAccessible(true);
		CcpJsonRepresentation handlers = (CcpJsonRepresentation) handlersField.get(CcpBulkEntityOperationType.delete);
		Object handler = handlers.getAsObject(CcpProcessStatusDefault.NOT_FOUND.asJsonFieldName());

		assertSame(CcpBulkItemThrowNotFound.INSTANCE, handler);
	}
}
