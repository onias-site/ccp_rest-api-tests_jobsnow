package com.ccp.json.defaultvalues.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.json.defaultvalues.annotations.RegrasValorPadraoStrings;

/**
 * Verifica a engine que aplica {@code @CcpJsonFieldDefaultValue}, chamada diretamente e não pelo
 * {@code CcpBusiness.execute}.
 */
public class CcpJsonFieldDefaultValuesEngineTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	@Test
	public void instanciaUnicaTest() {
		assertNotNull(CcpJsonFieldDefaultValuesEngine.INSTANCE);
	}

	/**
	 * Classe sem nenhum campo anotado não tem o que preencher, então o próprio JSON recebido é
	 * devolvido, sem cópia intermediária.
	 */
	@Test
	public void classeSemCamposAnotadosDevolveOMesmoJsonTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON;
		CcpJsonRepresentation retorno = CcpJsonFieldDefaultValuesEngine.INSTANCE.putDefaultValues(Object.class, json);
		assertSame(json, retorno);
	}

	@Test
	public void aplicaValorPadraoSemPassarPeloExecuteTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(RegrasValorPadraoStrings.nome, "onias");
		CcpJsonRepresentation retorno = CcpJsonFieldDefaultValuesEngine.INSTANCE.putDefaultValues(RegrasValorPadraoStrings.class, json);
		String valor = retorno.getAsString(RegrasValorPadraoStrings.comTemplate);
		assertEquals("ola onias", valor);
	}

	// ── null-parameter tests (AOP) ────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void putDefaultValuesClasseNulaTest() {
		CcpJsonFieldDefaultValuesEngine.INSTANCE.putDefaultValues(null, CcpOtherConstants.EMPTY_JSON);
	}

	@Test(expected = CcpNullParameterException.class)
	public void putDefaultValuesJsonNuloTest() {
		CcpJsonFieldDefaultValuesEngine.INSTANCE.putDefaultValues(Object.class, null);
	}
}
