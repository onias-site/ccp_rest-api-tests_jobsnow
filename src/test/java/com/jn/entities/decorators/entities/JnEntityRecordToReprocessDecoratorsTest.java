package com.jn.entities.decorators.entities;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.jn.entities.JnEntityRecordToReprocess;
import com.jn.entities.decorators.EntityDecoratorTestTemplate;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/** jn_record_to_reprocess: {@code @CcpEntityOlyReadable}, transformador e validador. */
public class JnEntityRecordToReprocessDecoratorsTest extends EntityDecoratorTestTemplate {

	protected CcpEntity entityUnderTest() {
		return JnEntityRecordToReprocess.ENTITY;
	}

	protected CcpJsonRepresentation validRecord() {
		return this.com(JnJsonCommonsFields.timestamp, this.unique)
				.put(JnJsonCommonsFields.operation, "save")
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
