package com.jn.mensageria;

import static org.junit.Assert.assertNotNull;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import org.junit.Test;

public class JnFunctionMensageriaSenderTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler(),
				com.ccp.local.testings.implementations.CcpLocalInstances.syncMensageriaListener);
	}



	/** Finding 37: until 2026-10-07 every sender described itself as java.lang.String. */
	@Test
	public void toStringShouldNameTheTopic() {
		String description = new JnFunctionMensageriaSender(new NoopBusiness()).toString();
		org.junit.Assert.assertEquals(NoopBusiness.class.getName(), description);
	}

	@Test
	public void toStringOfAnEntityOperationNamesTheOperationAndTheEntity() {
		com.ccp.especifications.db.utils.entity.CcpEntity entity = com.jn.entities.JnEntityAsyncTask.ENTITY;
		com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityMetaData metaData = entity.getEntityMetaData();

		String description = new JnFunctionMensageriaSender(entity, com.ccp.especifications.db.utils.entity.CcpEntityOperationType.save).toString();

		String expected = metaData.configurationClass.getName() + " (save, " + metaData.entityName + ")";
		org.junit.Assert.assertEquals(expected, description);
	}

	@Test
	public void constructorTest() {
		assertNotNull(new JnFunctionMensageriaSender(new NoopBusiness()));
	}

	@Test(expected = CcpNullParameterException.class)
	public void constructorNullTest() {
		new JnFunctionMensageriaSender(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void applyJsonNullTest() {
		new JnFunctionMensageriaSender(new NoopBusiness()).execute((CcpJsonRepresentation) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void applyMapNullTest() {
		new JnFunctionMensageriaSender(new NoopBusiness()).apply((java.util.Map<String, Object>) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void sendToMensageriaListNullTest() {
		new JnFunctionMensageriaSender(new NoopBusiness()).sendToMensageria((java.util.List<CcpJsonRepresentation>) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void sendToMensageriaArrayNullTest() {
		new JnFunctionMensageriaSender(new NoopBusiness()).sendToMensageria((CcpJsonRepresentation[]) null);
	}
}
