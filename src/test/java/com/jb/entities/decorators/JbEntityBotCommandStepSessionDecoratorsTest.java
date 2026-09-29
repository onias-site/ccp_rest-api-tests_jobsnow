package com.jb.entities.decorators;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityExpurgableOptions;
import com.jb.entities.JbEntityBotCommandStepSession;
import com.jn.entities.decorators.EntityDecoratorTestTemplate;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/**
 * jb_bot_command_step_session: {@code @CcpEntityCache(3600)}, descartável diário (prioridade 1),
 * transformador e validador. A sessão do passo do bot vale até o fim do dia.
 */
public class JbEntityBotCommandStepSessionDecoratorsTest extends EntityDecoratorTestTemplate {

	protected CcpEntity entityUnderTest() {
		return JbEntityBotCommandStepSession.ENTITY;
	}

	protected CcpJsonRepresentation validRecord() {
		return this.com(JbEntityBotCommandStepSession.Fields.chatId, this.unique)
				.put(JbEntityBotCommandStepSession.Fields.botName, "bot" + this.unique)
				.put(JbEntityBotCommandStepSession.Fields.commandName, "/comando")
				.put(JbEntityBotCommandStepSession.Fields.stepName, "passo")
				.put(JbEntityBotCommandStepSession.Fields.json, this.com(JnJsonCommonsFields.email, this.email))
				.put(JbEntityBotCommandStepSession.Fields.language, "portuguese");
	}

	@Test
	public void chain() {
		this.shouldHaveChain("DecoratorFieldsValidatorEntity", "DecoratorFieldsTransformerEntity", "DecoratorCacheEntity", "JnDisposableEntity", "DefaultImplementationEntity");
	}

	@Test
	public void saveReadAndDelete() {
		this.shouldSaveReadAndDelete();
	}

	@Test
	public void validator() {
		this.shouldRefuseInvalidRecord(this.validRecord().removeFields(JbEntityBotCommandStepSession.Fields.stepName));
	}

	@Test
	public void cache() {
		this.shouldServeReadFromCacheAndInvalidateOnWrite();
	}

	@Test
	public void disposable() {
		this.shouldKeepDisposableCopyUntilDeadline(CcpEntityExpurgableOptions.daily);
	}
}
