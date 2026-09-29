package com.jb.entities.decorators;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.jb.entities.JbEntityBotUpdateId;
import com.jn.entities.decorators.EntityDecoratorTestTemplate;

/** jb_bot_update_id: {@code @CcpEntityCache(3600)}, transformador e validador. */
public class JbEntityBotUpdateIdDecoratorsTest extends EntityDecoratorTestTemplate {

	protected CcpEntity entityUnderTest() {
		return JbEntityBotUpdateId.ENTITY;
	}

	protected CcpJsonRepresentation validRecord() {
		return this.com(JbEntityBotUpdateId.Fields.botName, "bot" + this.unique)
				.put(JbEntityBotUpdateId.Fields.updateId, 42);
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
		this.shouldRefuseInvalidRecord(this.validRecord().put(JbEntityBotUpdateId.Fields.updateId, -1));
	}

	@Test
	public void cache() {
		this.shouldServeReadFromCacheAndInvalidateOnWrite();
	}
}
