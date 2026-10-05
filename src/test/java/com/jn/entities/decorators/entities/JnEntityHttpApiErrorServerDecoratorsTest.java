package com.jn.entities.decorators.entities;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityExpurgableOptions;
import com.jn.entities.JnEntityHttpApiErrorServer;
import com.jn.entities.decorators.EntityDecoratorTestTemplate;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/** jn_http_api_error_server: {@code @CcpEntityCache(3600)}, hourly disposable, transformer and validator. */
public class JnEntityHttpApiErrorServerDecoratorsTest extends EntityDecoratorTestTemplate {

	protected CcpEntity entityUnderTest() {
		return JnEntityHttpApiErrorServer.ENTITY;
	}

	protected CcpJsonRepresentation validRecord() {
		return this.com(JnJsonCommonsFields.url, "http://api.teste/" + this.unique)
				.put(JnJsonCommonsFields.method, "POST")
				.put(JnJsonCommonsFields.headers, this.com(JnJsonCommonsFields.userAgent, "junit"))
				.put(JnJsonCommonsFields.apiName, "apiDeTeste")
				.put(JnJsonCommonsFields.details, "detalhe do erro")
				.put(JnJsonCommonsFields.httpStatus, 500)
				.put(JnJsonCommonsFields.date, "27/09/2026 10:00:00.000")
				.put(JnJsonCommonsFields.timestamp, System.currentTimeMillis());
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
		this.shouldRefuseInvalidRecord(this.validRecord().removeFields(JnJsonCommonsFields.httpStatus));
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
