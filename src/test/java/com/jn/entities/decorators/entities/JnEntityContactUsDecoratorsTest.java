package com.jn.entities.decorators.entities;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.jn.entities.JnEntityContactUs;
import com.jn.entities.decorators.EntityDecoratorTestTemplate;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;

/**
 * jn_contact_us: gêmea de jn_contact_us_solved (pendente na principal, resolvido na gêmea),
 * {@code @CcpEntityCache(3600)}, transformador e validador.
 */
public class JnEntityContactUsDecoratorsTest extends EntityDecoratorTestTemplate {

	protected CcpEntity entityUnderTest() {
		return JnEntityContactUs.ENTITY;
	}

	protected CcpJsonRepresentation validRecord() {
		return this.com(JnJsonCommonsFields.subjectType, "duvida")
				.put(JnJsonCommonsFields.email, this.email)
				.put(JnJsonCommonsFields.subject, "assunto de teste")
				.put(JnJsonInstantMessengerFields.chatId, 751717896L)
				.put(JnJsonCommonsFields.sender, "devs.jobsnow@gmail.com");
	}

	@Test
	public void chain() {
		this.shouldHaveChain("DecoratorFieldsValidatorEntity", "DecoratorFieldsTransformerEntity", "DecoratorTwinEntity", "DecoratorCacheEntity", "DefaultImplementationEntity");
	}

	@Test
	public void saveReadAndDelete() {
		this.shouldSaveReadAndDelete();
	}

	@Test
	public void validator() {
		this.shouldRefuseInvalidRecord(this.validRecord().removeFields(JnJsonCommonsFields.subject));
	}

	/** Até 2026-09-27 copiava a regra de JnJsonCommonsFields, que não declara chatId: aceitava qualquer coisa. */
	@Test
	public void validadorDoChatId() {
		this.shouldRefuseInvalidRecord(this.validRecord().put(JnJsonInstantMessengerFields.chatId, "nao-e-numero"));
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
	public void twin() {
		this.shouldAlternateBetweenMainAndTwin("jn_contact_us_solved");
	}

	@Test
	public void redirectToTwin() {
		this.shouldRedirectOnGetOneByIdWhenRecordMovedToTwin();
	}
}
