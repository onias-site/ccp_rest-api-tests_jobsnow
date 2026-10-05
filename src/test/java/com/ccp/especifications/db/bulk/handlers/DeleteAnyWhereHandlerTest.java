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
import com.jn.entities.JnEntityContactUs;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/**
 * Proves {@link CcpEntityBulkHandlerDeleteAnyWhere}: found or not, the record is deleted from the entity and from its
 * twin.
 */
public class DeleteAnyWhereHandlerTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	private final CcpJsonRepresentation record = CcpOtherConstants.EMPTY_JSON.put(JnJsonCommonsFields.email, "handler@jobsnow.com").put(JnJsonCommonsFields.subjectType, "question");

	private Set<String> entitiesOf(List<CcpBulkItem> items) {
		for (CcpBulkItem item : items) {
			assertEquals(CcpBulkEntityOperationType.delete, item.operation);
		}
		Set<String> entities = items.stream().map(item -> item.entity.getEntityMetaData().entityName).collect(Collectors.toSet());
		return entities;
	}

	@Test
	public void foundOrNotTheRecordIsDeletedFromBothSides() {
		CcpEntityBulkHandlerDeleteAnyWhere handler = new CcpEntityBulkHandlerDeleteAnyWhere(JnEntityContactUs.ENTITY);
		String main = JnEntityContactUs.ENTITY.getEntityMetaData().entityName;
		String twin = JnEntityContactUs.ENTITY.getTwinEntity().getEntityMetaData().entityName;

		Set<String> whenFound = this.entitiesOf(handler.whenRecordWasFoundInTheEntitySearch(this.record, this.record));
		Set<String> whenNotFound = this.entitiesOf(handler.whenRecordWasNotFoundInTheEntitySearch(this.record));

		assertTrue(whenFound.toString(), whenFound.contains(main) && whenFound.contains(twin));
		assertEquals(whenFound, whenNotFound);
		assertEquals(JnEntityContactUs.ENTITY, handler.getEntityToSearch());
	}
}
