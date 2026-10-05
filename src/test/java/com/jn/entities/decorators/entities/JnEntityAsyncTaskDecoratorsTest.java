package com.jn.entities.decorators.entities;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.jn.entities.JnEntityAsyncTask;
import com.jn.entities.decorators.EntityDecoratorTestTemplate;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/** jn_async_task: only transformer and validator, the trace of each message published to the queue. */
public class JnEntityAsyncTaskDecoratorsTest extends EntityDecoratorTestTemplate {

	protected CcpEntity entityUnderTest() {
		return JnEntityAsyncTask.ENTITY;
	}

	protected CcpJsonRepresentation validRecord() {
		return this.com(JnEntityAsyncTask.Fields.messageId, "mensagem" + this.unique)
				.put(JnEntityAsyncTask.Fields.started, System.currentTimeMillis())
				.put(JnEntityAsyncTask.Fields.data, "27/09/2026 10:00:00")
				.put(JnEntityAsyncTask.Fields.topic, "topico.de.Teste")
				.put(JnJsonCommonsFields.request, "{}");
	}

	@Test
	public void chain() {
		this.shouldHaveChain("DecoratorFieldsValidatorEntity", "DecoratorFieldsTransformerEntity", "DefaultImplementationEntity");
	}

	@Test
	public void saveReadAndDelete() {
		this.shouldSaveReadAndDelete();
	}

	@Test
	public void validator() {
		this.shouldRefuseInvalidRecord(this.validRecord().removeFields(JnEntityAsyncTask.Fields.topic));
	}
}
