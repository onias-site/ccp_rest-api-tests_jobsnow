package com.jn.entities.decorators.entities;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.jn.entities.JnEntityMessageDidNotSent;
import com.jn.entities.decorators.EntityDecoratorTestTemplate;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/** jn_message_did_not_sent: {@code @CcpEntityCache(3600)}, transformador e validador. */
public class JnEntityMessageDidNotSentDecoratorsTest extends EntityDecoratorTestTemplate {

	protected CcpEntity entityUnderTest() {
		return JnEntityMessageDidNotSent.ENTITY;
	}

	protected CcpJsonRepresentation validRecord() {
		return this.com(JnJsonCommonsFields.subjectType, "login")
				.put(JnJsonCommonsFields.email, this.email)
				.put(JnEntityMessageDidNotSent.Fields.reasonType, "jn_login_email")
				.put(JnEntityMessageDidNotSent.Fields.reasonDescription, "alreadySentEntities")
				.put(JnEntityMessageDidNotSent.Fields.reasonDetails, "isPresentInThisUnionAll");
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
		this.shouldRefuseInvalidRecord(this.validRecord().put(JnEntityMessageDidNotSent.Fields.reasonDetails, "motivoInventado"));
	}

	@Test
	public void transformer() {
		this.shouldStoreEmailAsHash();
	}

	@Test
	public void cache() {
		this.shouldServeReadFromCacheAndInvalidateOnWrite();
	}
}
