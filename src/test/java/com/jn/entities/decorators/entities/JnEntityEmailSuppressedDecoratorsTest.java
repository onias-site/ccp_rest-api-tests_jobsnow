package com.jn.entities.decorators.entities;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.EntityDecoratorTestTemplate;
import com.jn.entities.JnEntityEmailSuppressed;

/** jn_email_suppressed: {@code @CcpEntityCache(3600)}, transformer, validator and the publication of the first write. */
public class JnEntityEmailSuppressedDecoratorsTest extends EntityDecoratorTestTemplate {

	protected CcpEntity entityUnderTest() {
		return JnEntityEmailSuppressed.ENTITY;
	}

	protected CcpJsonRepresentation validRecord() {
		return this.com(JnEntityEmailSuppressed.Fields.email, this.email)
				.put(JnEntityEmailSuppressed.Fields.suppressionList, "bounces")
				.put(JnEntityEmailSuppressed.Fields.created, 1700000000L)
				.put(JnEntityEmailSuppressed.Fields.reason, "550 5.1.1 unknown user")
				.put(JnEntityEmailSuppressed.Fields.status, "5.1.1");
	}

	@Test
	public void chain() {
		this.shouldHaveChain("DecoratorFieldsValidatorEntity", "DecoratorFieldsTransformerEntity", "DecoratorWriteSideEffectEntity", "DecoratorCacheEntity", "DefaultImplementationEntity");
	}

	@Test
	public void saveReadAndDelete() {
		this.shouldSaveReadAndDelete();
	}

	@Test
	public void validator() {
		this.shouldRefuseInvalidRecord(this.validRecord().removeFields(JnEntityEmailSuppressed.Fields.created));
		this.shouldRefuseInvalidRecord(this.validRecord().put(JnEntityEmailSuppressed.Fields.suppressionList, "blocks_of_another_kind"));
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
