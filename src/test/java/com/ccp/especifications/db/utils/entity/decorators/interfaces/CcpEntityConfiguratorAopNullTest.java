package com.ccp.especifications.db.utils.entity.decorators.interfaces;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.jn.entities.JnEntityJobsnowError;

/**
 * Cobertura do {@code CcpNullParameterAspect} sobre os métodos default de
 * {@code CcpEntityConfigurator}.
 */
public class CcpEntityConfiguratorAopNullTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	private static final CcpEntity ENTITY = JnEntityJobsnowError.ENTITY;

	private static CcpEntityConfigurator configurator() {
		return new JnEntityJobsnowError();
	}

	@Test(expected = CcpNullParameterException.class)
	public void toCreateBulkItemsStringsEntityNullTest() {
		configurator().toCreateBulkItems(null, "{}");
	}

	@Test(expected = CcpNullParameterException.class)
	public void toCreateBulkItemsStringsJsonsNullTest() {
		configurator().toCreateBulkItems(ENTITY, (String[]) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void toCreateBulkItemsJsonsEntityNullTest() {
		configurator().toCreateBulkItems(null, CcpOtherConstants.EMPTY_JSON);
	}

	@Test(expected = CcpNullParameterException.class)
	public void toCreateBulkItemsJsonsNullTest() {
		configurator().toCreateBulkItems(ENTITY, (CcpJsonRepresentation[]) null);
	}
}
