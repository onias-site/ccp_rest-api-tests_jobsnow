package com.jn.entities.decorators.entities;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.jn.entities.JnEntityDisposableRecord;
import com.jn.entities.decorators.EntityDecoratorTestTemplate;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/**
 * jn_disposable_record: copies with a deadline, {@code @CcpEntityOlyReadable}: only the disposable decorator
 * writes to it.
 */
public class JnEntityDisposableRecordDecoratorsTest extends EntityDecoratorTestTemplate {

	protected CcpEntity entityUnderTest() {
		return JnEntityDisposableRecord.ENTITY;
	}

	protected CcpJsonRepresentation validRecord() {
		return this.com(JnJsonCommonsFields.timestamp, System.currentTimeMillis() + 3_600_000L)
				.put(JnEntityDisposableRecord.Fields.format, "dd/MM/yyyy HH")
				.put(JnJsonCommonsFields.entity, "jn_login_email")
				.put(JnJsonCommonsFields.date, "27/09/2026 10:00:00.000")
				.put(JnJsonCommonsFields.json, this.com(JnJsonCommonsFields.email, this.email))
				.put(JnJsonCommonsFields.id, "{\"email\":\"" + this.unique + "\"}")
				.put(JnEntityDisposableRecord.Fields.trueTimestamp, System.currentTimeMillis());
	}

	@Test
	public void chain() {
		this.shouldHaveChain("DecoratorFieldsValidatorEntity", "DecoratorReadOnlyEntity", "DecoratorFieldsTransformerEntity", "DefaultImplementationEntity");
	}

	@Test
	public void validator() {
		this.shouldRefuseInvalidRecord(this.validRecord().removeFields(JnEntityDisposableRecord.Fields.format));
	}

	@Test
	public void readOnly() {
		this.shouldRefuseWriteBecauseReadOnly();
	}
}
