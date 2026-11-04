package com.jn.entities.decorators.entities;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.EntityDecoratorTestTemplate;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityExpurgableOptions;
import com.jn.entities.JnEntityTicketDailyLimit;
import com.jn.json.fields.validation.JnTicketQuotaGroups;

/**
 * jn_ticket_daily_limit: {@code @CcpEntityCache(3600)}, daily disposable, transformer and validator. The daily quota of the
 * tickets has no {@code timestamp} of its own: until 2026-11-03 its copy took the expiration as the moment of the write,
 * and a quota spent today blocked the e-mail for 48 hours instead of 24.
 */
public class JnEntityTicketDailyLimitDecoratorsTest extends EntityDecoratorTestTemplate {

	protected CcpEntity entityUnderTest() {
		return JnEntityTicketDailyLimit.ENTITY;
	}

	protected CcpJsonRepresentation validRecord() {
		CcpJsonRepresentation key = JnEntityTicketDailyLimit.getKey(this.email, JnTicketQuotaGroups.values()[0]);
		return key;
	}

	@Test
	public void saveReadAndDelete() {
		this.shouldSaveReadAndDelete();
	}

	@Test
	public void disposable() {
		this.shouldKeepDisposableCopyUntilDeadline(CcpEntityExpurgableOptions.daily);
	}
}
