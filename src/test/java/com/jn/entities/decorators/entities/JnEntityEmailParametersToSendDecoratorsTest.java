package com.jn.entities.decorators.entities;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.jn.entities.JnEntityEmailParametersToSend;
import com.jn.entities.decorators.EntityDecoratorTestTemplate;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/**
 * jn_email_parameters_to_send: {@code @CcpEntityCache(3600)}, versionável, transformador e validador.
 * O {@code templateId} tem que ser nome de classe Java existente; usa-se o desta própria classe de
 * teste para não sobrescrever parâmetros reais de envio no banco local. O e-mail declara
 * {@code JnJsonTransformersFieldsEntityDoNothing}: fica em claro.
 */
public class JnEntityEmailParametersToSendDecoratorsTest extends EntityDecoratorTestTemplate {

	protected CcpEntity entityUnderTest() {
		return JnEntityEmailParametersToSend.ENTITY;
	}

	protected CcpJsonRepresentation validRecord() {
		return this.com(JnJsonCommonsFields.templateId, this.getClass().getName())
				.put(JnJsonCommonsFields.email, this.email)
				.put(JnJsonCommonsFields.sender, "devs.jobsnow@gmail.com")
				.put(JnJsonCommonsFields.subjectType, "teste");
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
		this.shouldRefuseInvalidRecord(this.validRecord().put(JnJsonCommonsFields.templateId, "classe.que.nao.Existe"));
	}

	@Test
	public void transformer() {
		this.shouldStoreEmailInPlainText();
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
