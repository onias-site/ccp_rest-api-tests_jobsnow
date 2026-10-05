package com.jn.entities.decorators.entities;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.jn.entities.JnEntityVersionable;
import com.jn.entities.decorators.EntityDecoratorTestTemplate;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/** jn_versionable: the history, {@code @CcpEntityOlyReadable}: only the versionable decorator writes to it. */
public class JnEntityVersionableDecoratorsTest extends EntityDecoratorTestTemplate {

	protected CcpEntity entityUnderTest() {
		return JnEntityVersionable.ENTITY;
	}

	protected CcpJsonRepresentation validRecord() {
		return this.com(JnJsonCommonsFields.timestamp, this.unique)
				.put(JnJsonCommonsFields.operation, "create")
				.put(JnJsonCommonsFields.date, "27/09/2026 10:00:00.000")
				.put(JnJsonCommonsFields.entity, "jn_login_email")
				.put(JnJsonCommonsFields.id, "{\"email\":\"" + this.unique + "\"}")
				.put(JnJsonCommonsFields.json, this.com(JnJsonCommonsFields.email, this.email));
	}

	@Test
	public void chain() {
		this.shouldHaveChain("DecoratorFieldsValidatorEntity", "DecoratorReadOnlyEntity", "DecoratorFieldsTransformerEntity", "DefaultImplementationEntity");
	}

	@Test
	public void validator() {
		this.shouldRefuseInvalidRecord(this.validRecord().removeFields(JnJsonCommonsFields.operation));
	}

	@Test
	public void readOnly() {
		this.shouldRefuseWriteBecauseReadOnly();
	}
}
