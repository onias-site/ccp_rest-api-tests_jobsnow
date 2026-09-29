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
 * jn_jobsnow_error: escrita assíncrona pela fila (prioridade 8), aviso ao suporte pelo Telegram só na
 * inclusão ({@code afterInsert}), {@code @CcpEntityCache(3600)}, descartável por hora, transformador e
 * validador.
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
	 * Até 2026-09-27 copiava a regra de JnJsonCommonsFields, que não declarava type, e aceitava texto
	 * vazio. (O tipo string do framework aceita qualquer valor por desenho; a regra que passou a valer é
	 * a de texto não vazio.)
	 */
	@Test
	public void validadorDoType() {
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

	/** O aviso é {@code SaveAWarning}: recusado por repetição, vira warning e a gravação segue. */
	@Test
	public void avisoRecusadoNaoDerrubaAGravacao() {
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
