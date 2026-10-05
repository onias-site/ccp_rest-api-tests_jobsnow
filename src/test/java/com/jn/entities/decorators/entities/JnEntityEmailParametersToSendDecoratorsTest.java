package com.jn.entities.decorators.entities;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.jn.entities.JnEntityEmailParametersToSend;
import com.jn.entities.decorators.EntityDecoratorTestTemplate;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/**
 * jn_email_parameters_to_send: {@code @CcpEntityCache(3600)}, versionable, transformer and validator. The
 * {@code templateId} must be the name of an existing Java class; the one of this very test class is used so that
 * real sending parameters in the local database are not overwritten. The e-mail declares
 * {@code JnJsonTransformersFieldsEntityDoNothing}: it stays in plain text.
 */
public class JnEntityEmailParametersToSendDecoratorsTest extends EntityDecoratorTestTemplate {

	protected CcpEntity entityUnderTest() {
		return JnEntityEmailParametersToSend.ENTITY;
	}

	protected CcpJsonRepresentation validRecord() {
		return this.com(JnJsonCommonsFields.templateId, this.getClass().getName())
				.put(JnJsonCommonsFields.email, this.email)
				.put(JnJsonCommonsFields.sender, "devs.jobsnow@gmail.com")
				.put(JnJsonCommonsFields.subjectType, "teste");
	}

	@Test
	public void chain() {
		this.shouldHaveChain("DecoratorFieldsValidatorEntity", "DecoratorFieldsTransformerEntity", "JnVersionablePurgeEntity", "DecoratorCacheEntity", "JnVersionableEntity", "DefaultImplementationEntity");
	}

	@Test
	public void saveReadAndDelete() {
		this.shouldSaveReadAndDelete();
	}

	@Test
	public void validator() {
		this.shouldRefuseInvalidRecord(this.validRecord().put(JnJsonCommonsFields.templateId, "classe.que.nao.Existe"));
	}

	@Test
	public void transformer() {
		this.shouldStoreEmailInPlainText();
	}

	@Test
	public void cache() {
		this.shouldServeReadFromCacheAndInvalidateOnWrite();
	}

	@Test
	public void versionable() {
		this.shouldRecordHistoryOnEachWrite();
	}

	@Test
	public void historyPurge() {
		this.shouldPurgeHistoryOnDeleteAnyWhere();
	}
}
