package com.jb.entities.decorators;

import java.util.Arrays;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.jb.entities.JbEntityBotCommandStep;
import com.jb.entities.subfields.JbNextStepFields;
import com.jn.entities.decorators.EntityDecoratorTestTemplate;

/** jb_bot_command_step: {@code @CcpEntityCache(3600)}, versionable, transformer and validator. */
public class JbEntityBotCommandStepDecoratorsTest extends EntityDecoratorTestTemplate {

	protected CcpEntity entityUnderTest() {
		return JbEntityBotCommandStep.ENTITY;
	}

	protected CcpJsonRepresentation validRecord() {
		CcpJsonRepresentation proximoPasso = CcpOtherConstants.EMPTY_JSON
				.put(JbNextStepFields.status, 200)
				.put(JbNextStepFields.nextStep, "fim");
		return this.com(JbEntityBotCommandStep.Fields.stepName, "passo" + this.unique)
				.put(JbEntityBotCommandStep.Fields.stepFlow, Arrays.asList(proximoPasso))
				.put(JbEntityBotCommandStep.Fields.engine, "com.jb.business.Teste");
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
		this.shouldRefuseInvalidRecord(this.validRecord().removeFields(JbEntityBotCommandStep.Fields.engine));
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
