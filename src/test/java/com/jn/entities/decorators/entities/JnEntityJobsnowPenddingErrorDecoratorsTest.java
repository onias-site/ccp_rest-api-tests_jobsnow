package com.jn.entities.decorators.entities;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.jn.entities.JnEntityJobsnowPenddingError;
import com.jn.entities.decorators.EntityDecoratorTestTemplate;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/**
 * jn_jobsnow_pendding_error: gêmea de jn_jobsnow_solved_error, {@code @CcpEntityCache(3600)},
 * versionável, transformador (carimba {@code timestamp}/{@code date}) e validador.
 */
public class JnEntityJobsnowPenddingErrorDecoratorsTest extends EntityDecoratorTestTemplate {

	protected CcpEntity entityUnderTest() {
		return JnEntityJobsnowPenddingError.ENTITY;
	}

	protected CcpJsonRepresentation validRecord() {
		return this.com(JnEntityJobsnowPenddingError.Fields.stackTraceHash, "hash" + this.unique)
				.put(JnEntityJobsnowPenddingError.Fields.type, "java.lang.RuntimeException")
				.put(JnJsonCommonsFields.message, "erro de teste " + this.unique);
	}

	@Test
	public void chain() {
		this.shouldHaveChain("DecoratorFieldsValidatorEntity", "DecoratorFieldsTransformerEntity", "JnVersionablePurgeEntity", "DecoratorTwinEntity", "DecoratorCacheEntity", "JnVersionableEntity", "DefaultImplementationEntity");
	}

	@Test
	public void saveReadAndDelete() {
		this.shouldSaveReadAndDelete();
	}

	@Test
	public void validator() {
		this.shouldRefuseInvalidRecord(this.validRecord().put(JnJsonCommonsFields.message, "ops"));
	}

	@Test
	public void transformer() {
		this.shouldStampDateAndTime();
	}

	@Test
	public void cache() {
		this.shouldServeReadFromCacheAndInvalidateOnWrite();
	}

	@Test
	public void twin() {
		this.shouldAlternateBetweenMainAndTwin("jn_jobsnow_solved_error");
	}

	@Test
	public void redirectToTwin() {
		this.shouldRedirectOnGetOneByIdWhenRecordMovedToTwin();
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
