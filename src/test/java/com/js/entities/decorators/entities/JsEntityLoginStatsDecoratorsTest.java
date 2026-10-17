package com.js.entities.decorators.entities;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.js.entities.JsEntityLoginStats;
import com.ccp.especifications.db.utils.entity.decorators.EntityDecoratorTestTemplate;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/**
 * js_login_stats: {@code @CcpEntityCache(3600)}, transformer and validator. The e-mail has a length rule of its
 * own (35 to 50), stricter than the common one, and checked before the hash.
 */
public class JsEntityLoginStatsDecoratorsTest extends EntityDecoratorTestTemplate {

	private final String emailLongo = "estatisticas.decorator" + this.unique + "@jobsnow.com";

	protected CcpEntity entityUnderTest() {
		return JsEntityLoginStats.ENTITY;
	}

	protected CcpJsonRepresentation validRecord() {
		long yesterday = System.currentTimeMillis() - 24L * 60L * 60L * 1000L;
		return this.com(JnJsonCommonsFields.email, this.emailLongo)
				.put(JsEntityLoginStats.Fields.balance, 0)
				.put(JsEntityLoginStats.Fields.lastAccess, String.valueOf(yesterday))
				.put(JsEntityLoginStats.Fields.countAccess, 1)
				.put(JsEntityLoginStats.Fields.openedTickets, 0)
				.put(JsEntityLoginStats.Fields.closedTickets, 0)
				.put(JsEntityLoginStats.Fields.balanceTransacionsCount, 0);
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
		this.shouldRefuseInvalidRecord(this.validRecord().put(JnJsonCommonsFields.email, "curto@jobsnow.com"));
	}

	@Test
	public void transformer() {
		this.shouldStoreTransformed(JnJsonCommonsFields.email, this.emailLongo);
	}

	@Test
	public void cache() {
		this.shouldServeReadFromCacheAndInvalidateOnWrite();
	}
}
