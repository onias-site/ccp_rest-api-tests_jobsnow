package com.jn.entities.decorators.entities;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.jn.entities.JnEntityInstantMessengerTemplateMessage;
import com.jn.entities.decorators.EntityDecoratorTestTemplate;

/** jn_instant_messenger_template_message: {@code @CcpEntityCache(3600)}, versionável, transformador e validador. */
public class JnEntityInstantMessengerTemplateMessageDecoratorsTest extends EntityDecoratorTestTemplate {

	protected CcpEntity entityUnderTest() {
		return JnEntityInstantMessengerTemplateMessage.ENTITY;
	}

	protected CcpJsonRepresentation validRecord() {
		return this.com(JnEntityInstantMessengerTemplateMessage.Fields.templateId, this.getClass().getName())
				.put(JnEntityInstantMessengerTemplateMessage.Fields.language, "portuguese")
				.put(JnEntityInstantMessengerTemplateMessage.Fields.message, "mensagem de teste");
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
		this.shouldRefuseInvalidRecord(this.validRecord().removeFields(JnEntityInstantMessengerTemplateMessage.Fields.message));
	}

	/** Até 2026-09-27 copiava a regra de JnJsonInstantMessengerFields, que não declara templateId. */
	@Test
	public void validadorDoTemplateId() {
		this.shouldRefuseInvalidRecord(this.validRecord().put(JnEntityInstantMessengerTemplateMessage.Fields.templateId, "classe.que.nao.Existe"));
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
