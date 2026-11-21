package com.jn.entities.decorators.entities;

import java.util.Arrays;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.jn.entities.JnEntitySystemParameter;
import com.ccp.especifications.db.utils.entity.decorators.EntityDecoratorTestTemplate;

/** jn_system_parameter: {@code @CcpEntityCache(3600)}, transformador e validador. */
public class JnEntitySystemParameterDecoratorsTest extends EntityDecoratorTestTemplate {

	protected CcpEntity entityUnderTest() {
		return JnEntitySystemParameter.ENTITY;
	}

	protected CcpJsonRepresentation validRecord() {
		return this.com(JnEntitySystemParameter.Fields.systemMessageName, "parametro" + this.unique)
				.put(JnEntitySystemParameter.Fields.message, Arrays.asList("parametro de sistema de teste"));
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
		this.shouldRefuseInvalidRecord(this.validRecord().removeFields(JnEntitySystemParameter.Fields.message));
	}

	@Test
	public void cache() {
		this.shouldServeReadFromCacheAndInvalidateOnWrite();
	}
}
