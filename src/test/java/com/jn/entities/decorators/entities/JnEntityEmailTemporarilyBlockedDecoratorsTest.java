package com.jn.entities.decorators.entities;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.EntityDecoratorTestTemplate;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityExpurgableOptions;
import com.jn.entities.JnEntityEmailTemporarilyBlocked;

/** jn_email_temporarily_blocked: {@code @CcpEntityCache(3600)}, monthly disposable, transformer and validator. */
public class JnEntityEmailTemporarilyBlockedDecoratorsTest extends EntityDecoratorTestTemplate {

	protected CcpEntity entityUnderTest() {
		return JnEntityEmailTemporarilyBlocked.ENTITY;
	}

	protected CcpJsonRepresentation validRecord() {
		return this.com(JnEntityEmailTemporarilyBlocked.Fields.email, this.email)
				.put(JnEntityEmailTemporarilyBlocked.Fields.created, 1700000000L)
				.put(JnEntityEmailTemporarilyBlocked.Fields.reason, "421 try again later")
				.put(JnEntityEmailTemporarilyBlocked.Fields.status, "4.2.1");
	}

	@Test
	public void chain() {
		this.shouldHaveChain("DecoratorFieldsValidatorEntity", "DecoratorFieldsTransformerEntity", "DecoratorCacheEntity", "JnDisposableEntity", "DefaultImplementationEntity");
	}

	@Test
	public void saveReadAndDelete() {
		this.shouldSaveReadAndDelete();
	}

	@Test
	public void validator() {
		this.shouldRefuseInvalidRecord(this.validRecord().removeFields(JnEntityEmailTemporarilyBlocked.Fields.created));
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
	public void disposable() {
		this.shouldKeepDisposableCopyUntilDeadline(CcpEntityExpurgableOptions.monthly);
	}
}
