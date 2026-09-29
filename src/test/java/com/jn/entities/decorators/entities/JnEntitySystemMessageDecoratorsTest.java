package com.jn.entities.decorators.entities;

import java.util.Arrays;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.jn.entities.JnEntitySystemMessage;
import com.jn.entities.decorators.EntityDecoratorTestTemplate;

/** jn_system_message: {@code @CcpEntityCache(3600)}, transformador e validador. */
public class JnEntitySystemMessageDecoratorsTest extends EntityDecoratorTestTemplate {

	protected CcpEntity entityUnderTest() {
		return JnEntitySystemMessage.ENTITY;
	}

	protected CcpJsonRepresentation validRecord() {
		return this.com(JnEntitySystemMessage.Fields.systemMessageName, "mensagem" + this.unique)
				.put(JnEntitySystemMessage.Fields.language, "portuguese")
				.put(JnEntitySystemMessage.Fields.message, Arrays.asList("mensagem de sistema de teste"));
	}

	@Test
	public void chain() {
		this.shouldHaveChain("DecoratorFieldsValidatorEntity", "DecoratorFieldsTransformerEntity", "DecoratorCacheEntity", "DefaultImplementationEntity");
	}

	@Test
	public void saveReadAndDelete() {
		this.shouldSaveReadAndDelete();
	}

	@Test
	public void validator() {
		this.shouldRefuseInvalidRecord(this.validRecord().removeFields(JnEntitySystemMessage.Fields.message));
	}

	@Test
	public void cache() {
		this.shouldServeReadFromCacheAndInvalidateOnWrite();
	}
}
