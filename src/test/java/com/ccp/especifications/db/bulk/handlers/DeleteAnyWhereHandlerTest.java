package com.ccp.especifications.db.bulk.handlers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.bulk.CcpBulkEntityOperationType;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.js.entities.JsEntityLoginSessionValidation;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/**
 * Proves {@link CcpEntityBulkHandlerDeleteAnyWhere}: found or not, the record is deleted from the entity and from its
 * twin.
 */
public class DeleteAnyWhereHandlerTest {

	static {
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE);
	}

	private final CcpJsonRepresentation record = CcpOtherConstants.EMPTY_JSON.put(JnJsonCommonsFields.email, "handler@jobsnow.com").put(JsEntityLoginSessionValidation.Fields.token, "TOKEN123").put(JnJsonCommonsFields.ip, "127.0.0.1").put(JnJsonCommonsFields.userAgent, "junit");

	private Set<String> entitiesOf(List<CcpBulkItem> items) {
		for (CcpBulkItem item : items) {
			assertEquals(CcpBulkEntityOperationType.delete, item.operation);
		}
		Set<String> entities = items.stream().map(item -> item.entity.getEntityMetaData().entityName).collect(Collectors.toSet());
		return entities;
	}

	@Test
	public void foundOrNotTheRecordIsDeletedFromBothSides() {
		CcpEntityBulkHandlerDeleteAnyWhere handler = new CcpEntityBulkHandlerDeleteAnyWhere(JsEntityLoginSessionValidation.ENTITY);
		String main = JsEntityLoginSessionValidation.ENTITY.getEntityMetaData().entityName;
		String twin = JsEntityLoginSessionValidation.ENTITY.getTwinEntity().getEntityMetaData().entityName;

		Set<String> whenFound = this.entitiesOf(handler.whenRecordWasFoundInTheEntitySearch(this.record, this.record));
		Set<String> whenNotFound = this.entitiesOf(handler.whenRecordWasNotFoundInTheEntitySearch(this.record));

		assertTrue(whenFound.toString(), whenFound.contains(main) && whenFound.contains(twin));
		assertEquals(whenFound, whenNotFound);
		assertEquals(JsEntityLoginSessionValidation.ENTITY, handler.getEntityToSearch());
	}
}
