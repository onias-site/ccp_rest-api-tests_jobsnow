package com.ccp.json.validations.fields.annotations;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.json.validations.fields.enums.CcpJsonFieldTypeError;

/**
 * Verifica se {@code @CcpJsonFieldTypeTimeAfter} aceita timestamps dentro do intervalo futuro
 * configurado e recusa os que ficam fora dele.
 */
public class CcpJsonFieldTypeTimeAfterTest {

	private static final long UM_DIA = 86_400_000L;

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	private final NegocioFieldTypeTimeAfter negocio = new NegocioFieldTypeTimeAfter();

	private CcpJsonRepresentation jsonComDiasAFrente(RegrasFieldTypeTimeAfter campo, long dias) {
		long currentTimeMillis = System.currentTimeMillis();
		long timestamp = currentTimeMillis + (dias * UM_DIA);
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(campo, timestamp);
		return json;
	}

	@Test
	public void dentroDoLimiteMaximoTest() {
		CcpJsonRepresentation json = this.jsonComDiasAFrente(RegrasFieldTypeTimeAfter.noMaximoSeteDiasAFrente, 3);
		ValidacaoDeCampo.aceita(this.negocio, json);
	}

	@Test
	public void foraDoLimiteMaximoTest() {
		CcpJsonRepresentation json = this.jsonComDiasAFrente(RegrasFieldTypeTimeAfter.noMaximoSeteDiasAFrente, 30);
		ValidacaoDeCampo.recusa(this.negocio, json, RegrasFieldTypeTimeAfter.noMaximoSeteDiasAFrente, CcpJsonFieldTypeError.timeMaxValueAfterCurrentTime);
	}

	@Test
	public void dentroDoLimiteMinimoTest() {
		CcpJsonRepresentation json = this.jsonComDiasAFrente(RegrasFieldTypeTimeAfter.noMinimoDoisDiasAFrente, 5);
		ValidacaoDeCampo.aceita(this.negocio, json);
	}

	@Test
	public void foraDoLimiteMinimoTest() {
		CcpJsonRepresentation json = this.jsonComDiasAFrente(RegrasFieldTypeTimeAfter.noMinimoDoisDiasAFrente, 0);
		ValidacaoDeCampo.recusa(this.negocio, json, RegrasFieldTypeTimeAfter.noMinimoDoisDiasAFrente, CcpJsonFieldTypeError.timeMinValueAfterCurrentTime);
	}
}
