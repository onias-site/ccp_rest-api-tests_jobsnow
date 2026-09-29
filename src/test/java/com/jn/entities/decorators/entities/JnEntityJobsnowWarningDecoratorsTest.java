package com.jn.entities.decorators.entities;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityExpurgableOptions;
import com.jn.entities.JnEntityJobsnowWarning;
import com.jn.entities.decorators.EntityDecoratorTestTemplate;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/**
 * jn_jobsnow_warning: escrita assíncrona pela fila, aviso ao suporte pelo Telegram só na inclusão,
 * {@code @CcpEntityCache(3600)}, descartável por hora, transformador e validador.
 */
public class JnEntityJobsnowWarningDecoratorsTest extends EntityDecoratorTestTemplate {

	protected CcpEntity entityUnderTest() {
		return JnEntityJobsnowWarning.ENTITY;
	}

	protected CcpJsonRepresentation validRecord() {
		return this.com(JnEntityJobsnowWarning.Fields.type, "aviso" + this.unique)
				.put(JnEntityJobsnowWarning.Fields.stackTrace, "at com.jn.Teste.metodo(Teste.java:1)")
				.put(JnJsonCommonsFields.message, "aviso de teste " + this.unique);
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
		this.shouldRefuseInvalidRecord(this.validRecord().removeFields(JnEntityJobsnowWarning.Fields.stackTrace));
	}

	@Test
	public void asyncWrite() {
		this.shouldSaveThroughQueue();
	}

	@Test
	public void supportNoticeOnlyOnInsert() {
		this.shouldNotifyByTelegramOnlyOnInsert();
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
