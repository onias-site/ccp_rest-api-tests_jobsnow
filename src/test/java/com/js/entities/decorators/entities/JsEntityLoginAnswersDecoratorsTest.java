package com.js.entities.decorators.entities;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.js.entities.JsEntityLoginAnswers;
import com.ccp.especifications.db.utils.entity.decorators.EntityDecoratorTestTemplate;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/**
 * js_login_answers: {@code @CcpEntityCache(3600)}, versionable, transformer, validator, the announcement of the insert,
 * the insertion date and the 4 counters of the screen Curiosities.
 */
public class JsEntityLoginAnswersDecoratorsTest extends EntityDecoratorTestTemplate {

	protected CcpEntity entityUnderTest() {
		return JsEntityLoginAnswers.ENTITY;
	}

	protected CcpJsonRepresentation validRecord() {
		return this.com(JnJsonCommonsFields.email, this.email)
				.put(JsEntityLoginAnswers.Fields.channel, "linkedin")
				.put(JsEntityLoginAnswers.Fields.goal, "jobs");
	}

	@Test
	public void chain() {
		// the 4 counters of the screen Curiosities (new users by day, month, goal and channel) and the insertion date
		this.shouldHaveChain("DecoratorFieldsValidatorEntity", "JnGrouperChildCurrentCounterEntityBeforeWrite", "JnGrouperChildCurrentCounterEntityBeforeWrite", "JnGrouperChildCurrentCounterEntityBeforeWrite", "JnGrouperChildCurrentCounterEntityBeforeWrite", "JnInsertionDateEntity", "DecoratorFieldsTransformerEntity", "JnVersionablePurgeEntity", "DecoratorWriteSideEffectEntity", "DecoratorCacheEntity", "JnVersionableEntity", "DefaultImplementationEntity");
	}

	@Test
	public void saveReadAndDelete() {
		this.shouldSaveReadAndDelete();
	}

	@Test
	public void validator() {
		this.shouldRefuseInvalidRecord(this.validRecord().put(JsEntityLoginAnswers.Fields.goal, "ferias"));
	}

	@Test
	public void transformer() {
		this.shouldStoreEmailAsHash();
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
