package com.ccp.especifications.db.utils.entity.decorators.engine;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.utils.entity.CcpEntity.CcpEntityNoDefinedPrimaryKey;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsValidator;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;

/**
 * Proves the rule that every entity has a primary key, checked when the entity is built (finding 6): until 2026-10-07
 * an entity without primary key was built normally and only failed at its first read or write.
 */
public class EntityWithoutPrimaryKeyTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	/** An entity that forgot to declare its primary key. */
	@CcpEntityFieldsValidator(classReferenceWithTheFields = FakeEntityWithoutKey.Fields.class)
	public static class FakeEntityWithoutKey implements CcpEntityConfigurator {
		/** The fields, none of them the key. */
		public static enum Fields implements CcpJsonFieldName {
			/** A plain field. */
			@CcpJsonFieldTypeString
			description
		}
	}

	@Test
	public void anEntityWithoutPrimaryKeyIsRefusedWhenItIsBuilt() {
		try {
			new CcpEntityFactory(FakeEntityWithoutKey.class);
			fail("an entity without primary key must not be built");
		} catch (CcpEntityNoDefinedPrimaryKey expected) {
			assertTrue(expected.getMessage(), expected.getMessage().contains("has no defined primary key"));
		}
	}
}
