package com.jn.entities.decorators.entities;

import java.util.Arrays;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityExpurgableOptions;
import com.jn.entities.JnEntityJobsnowError;
import com.jn.entities.decorators.EntityDecoratorTestTemplate;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/**
 * jn_jobsnow_error: asynchronous writing through the queue (priority 8), notice to the support through Telegram
 * only on insert ({@code afterInsert}), {@code @CcpEntityCache(3600)}, hourly disposable, transformer and
 * validator.
 */
public class JnEntityJobsnowErrorDecoratorsTest extends EntityDecoratorTestTemplate {

	protected CcpEntity entityUnderTest() {
		return JnEntityJobsnowError.ENTITY;
	}

	protected CcpJsonRepresentation validRecord() {
		return this.com(JnEntityJobsnowError.Fields.stackTraceHash, "hash" + this.unique)
				.put(JnEntityJobsnowError.Fields.type, "java.lang.RuntimeException")
				.put(JnJsonCommonsFields.message, "erro de teste " + this.unique)
				.put(JnEntityJobsnowError.Fields.stackTrace, Arrays.asList("at com.jn.Teste.metodo(Teste.java:1)"));
	}

	@Test
	public void chain() {
		this.shouldHaveChain("DecoratorFieldsValidatorEntity", "JnAsyncWriterEntity", "JnSendMessageToUserEntityBeforeWrite", "DecoratorFieldsTransformerEntity", "JnSendMessageToUserEntityAfterWrite", "DecoratorCacheEntity", "JnDisposableEntity", "DefaultImplementationEntity");
	}

	@Test
	public void saveReadAndDelete() {
		this.shouldSaveReadAndDelete();
	}

	@Test
	public void validator() {
		this.shouldRefuseInvalidRecord(this.validRecord().removeFields(JnJsonCommonsFields.message));
	}

	/**
	 * Until 2026-09-27 it copied the rule of JnJsonCommonsFields, which did not declare type, and accepted empty
	 * text. (The string type of the framework accepts any value by design; the rule now in force is the one of
	 * non-empty text.)
	 */
	@Test
	public void typeValidator() {
		this.shouldRefuseInvalidRecord(this.validRecord().put(JnEntityJobsnowError.Fields.type, ""));
	}

	@Test
	public void asyncWrite() {
		this.shouldSaveThroughQueue();
	}

	@Test
	public void supportNoticeOnlyOnInsert() {
		this.shouldNotifyByTelegramOnlyOnInsert();
	}

	/** The notice is {@code SaveAWarning}: refused for repetition, it becomes a warning and the save goes on. */
	@Test
	public void refusedNoticeDoesNotBreakTheSave() {
		this.shouldKeepSavingWhenNoticeIsRefused();
	}

	@Test
	public void cache() {
		this.shouldServeReadFromCacheAndInvalidateOnWrite();
	}

	@Test
	public void disposable() {
		this.shouldKeepDisposableCopyUntilDeadline(CcpEntityExpurgableOptions.hourly);
	}
}
