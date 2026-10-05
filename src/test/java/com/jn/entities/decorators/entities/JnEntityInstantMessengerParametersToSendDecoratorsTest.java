package com.jn.entities.decorators.entities;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.jn.entities.JnEntityInstantMessengerParametersToSend;
import com.jn.entities.decorators.EntityDecoratorTestTemplate;

/**
 * jn_instant_messenger_parameters_to_send: {@code @CcpEntityCache(3600)}, versionable, transformer and
 * validator.
 */
public class JnEntityInstantMessengerParametersToSendDecoratorsTest extends EntityDecoratorTestTemplate {

	protected CcpEntity entityUnderTest() {
		return JnEntityInstantMessengerParametersToSend.ENTITY;
	}

	protected CcpJsonRepresentation validRecord() {
		return this.com(JnEntityInstantMessengerParametersToSend.Fields.botName, "bot" + this.unique)
				.put(JnEntityInstantMessengerParametersToSend.Fields.templateId, this.getClass().getName())
				.put(JnEntityInstantMessengerParametersToSend.Fields.chatId, 751717896L)
				.put(JnEntityInstantMessengerParametersToSend.Fields.instantMessageType, "text");
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
		this.shouldRefuseInvalidRecord(this.validRecord().put(JnEntityInstantMessengerParametersToSend.Fields.instantMessageType, "pombo-correio"));
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
