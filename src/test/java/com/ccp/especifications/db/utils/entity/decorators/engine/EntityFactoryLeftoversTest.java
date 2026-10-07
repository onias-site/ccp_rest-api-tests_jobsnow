package com.ccp.especifications.db.utils.entity.decorators.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.fields.CcpEntityField;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldPrimaryKey;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.jn.entities.JnEntityLoginToken;

/**
 * Proves the leftovers of finding 21 fixed on 2026-10-06: an entity without {@code @CcpEntityFieldsValidator} takes its
 * fields from its {@code Fields} enum (it used to break with {@code NullPointerException}), and the metadata of an entity
 * is bound once (for the twin side it used to rebuild the whole twin entity on every call).
 */
public class EntityFactoryLeftoversTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	/** A configurator with the {@code Fields} enum and without {@code @CcpEntityFieldsValidator}. */
	public static class WithoutFieldsValidator {
		public static enum Fields implements CcpJsonFieldName {
			@CcpEntityFieldPrimaryKey
			email,
			name
		}
	}

	@Test
	public void theFieldsComeFromTheFieldsEnumWithoutTheValidatorAnnotation() {
		CcpEntityField[] fields = CcpEntityFactory.getFields(WithoutFieldsValidator.class);

		List<String> names = Arrays.stream(fields).map(x -> x.name()).collect(Collectors.toList());
		assertEquals(Arrays.asList("email", "name"), names);
		assertTrue("email is the primary key", fields[0].primaryKey);
	}

	@Test
	public void theMetadataOfTheTwinIsBoundOnce() {
		CcpEntity twin = JnEntityLoginToken.ENTITY.getTwinEntity();

		CcpEntityMetaData first = twin.getEntityMetaData();
		CcpEntityMetaData second = twin.getEntityMetaData();

		assertNotNull(first.entity);
		assertSame("the twin entity was built again", first, second);
	}

	@Test
	public void theMetadataOfTheMainEntityIsBoundOnce() {
		CcpEntityMetaData first = JnEntityLoginToken.ENTITY.getEntityMetaData();
		CcpEntityMetaData second = JnEntityLoginToken.ENTITY.getEntityMetaData();

		assertNotNull(first.entity);
		assertSame(first, second);
	}
}
