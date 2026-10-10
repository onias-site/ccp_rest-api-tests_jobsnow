package com.jb.entities.decorators;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.jb.entities.JbEntityBotChatLanguage;
import com.jn.entities.decorators.EntityDecoratorTestTemplate;

/**
 * jb_bot_chat_language: {@code @CcpEntityCache(3600)}, transformer and validator.
 */
public class JbEntityBotChatLanguageDecoratorsTest extends EntityDecoratorTestTemplate {

	protected CcpEntity entityUnderTest() {
		return JbEntityBotChatLanguage.ENTITY;
	}

	protected CcpJsonRepresentation validRecord() {
		return this.com(JbEntityBotChatLanguage.Fields.chatId, this.unique)
				.put(JbEntityBotChatLanguage.Fields.botName, "bot" + this.unique)
				.put(JbEntityBotChatLanguage.Fields.language, "english");
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
		this.shouldRefuseInvalidRecord(this.validRecord().removeFields(JbEntityBotChatLanguage.Fields.language));
	}

	@Test
	public void cache() {
		this.shouldServeReadFromCacheAndInvalidateOnWrite();
	}
}
