package com.jb.entities.decorators;

import java.util.Arrays;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.jb.entities.JbEntityBotAllowedUser;
import com.jn.entities.decorators.EntityDecoratorTestTemplate;

/** jb_bot_allowed_user: {@code @CcpEntityCache(3600)}, transformador e validador. */
public class JbEntityBotAllowedUserDecoratorsTest extends EntityDecoratorTestTemplate {

	protected CcpEntity entityUnderTest() {
		return JbEntityBotAllowedUser.ENTITY;
	}

	protected CcpJsonRepresentation validRecord() {
		return this.com(JbEntityBotAllowedUser.Fields.botName, "bot" + this.unique)
				.put(JbEntityBotAllowedUser.Fields.allowedUser, Arrays.asList(751717896L));
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
		this.shouldRefuseInvalidRecord(this.validRecord().removeFields(JbEntityBotAllowedUser.Fields.allowedUser));
	}

	@Test
	public void cache() {
		this.shouldServeReadFromCacheAndInvalidateOnWrite();
	}
}
