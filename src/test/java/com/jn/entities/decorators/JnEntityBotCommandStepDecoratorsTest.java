package com.jn.entities.decorators;

import com.ccp.especifications.db.utils.entity.decorators.EntityDecoratorTestTemplate;
import java.util.Arrays;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.jn.entities.JnEntityBotCommandStep;
import com.jn.entities.subfields.JnNextStepFields;

/** jn_bot_command_step: {@code @CcpEntityCache(3600)}, versionable, transformer and validator. */
public class JnEntityBotCommandStepDecoratorsTest extends EntityDecoratorTestTemplate {

	protected CcpEntity entityUnderTest() {
		return JnEntityBotCommandStep.ENTITY;
	}

	protected CcpJsonRepresentation validRecord() {
		CcpJsonRepresentation proximoPasso = CcpOtherConstants.EMPTY_JSON
				.put(JnNextStepFields.status, 200)
				.put(JnNextStepFields.nextStep, "fim");
		return this.com(JnEntityBotCommandStep.Fields.stepName, "passo" + this.unique)
				.put(JnEntityBotCommandStep.Fields.stepFlow, Arrays.asList(proximoPasso))
				.put(JnEntityBotCommandStep.Fields.engine, "com.jb.business.Teste");
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
		this.shouldRefuseInvalidRecord(this.validRecord().removeFields(JnEntityBotCommandStep.Fields.engine));
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
