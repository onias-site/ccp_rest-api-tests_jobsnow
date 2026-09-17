package com.ccp.json.validations.fields.annotations;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.json.validations.fields.enums.CcpJsonFieldTypeError;

/**
 * Verifica se {@code @CcpJsonFieldTypeTimeBefore} aceita timestamps dentro do intervalo passado e
 * recusa os que ficam fora dele.
 */
public class CcpJsonFieldTypeTimeBeforeTest {

	private static final long UM_DIA = 86_400_000L;

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	private final NegocioFieldTypeTimeBefore negocio = new NegocioFieldTypeTimeBefore();

	private CcpJsonRepresentation jsonComDiasAtras(RegrasFieldTypeTimeBefore campo, long dias) {
		long currentTimeMillis = System.currentTimeMillis();
		long timestamp = currentTimeMillis - (dias * UM_DIA);
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(campo, timestamp);
		return json;
	}

	@Test
	public void dentroDoLimiteMaximoTest() {
		CcpJsonRepresentation json = this.jsonComDiasAtras(RegrasFieldTypeTimeBefore.noMaximoSeteDiasAtras, 3);
		ValidacaoDeCampo.aceita(this.negocio, json);
	}

	@Test
	public void foraDoLimiteMaximoTest() {
		CcpJsonRepresentation json = this.jsonComDiasAtras(RegrasFieldTypeTimeBefore.noMaximoSeteDiasAtras, 30);
		ValidacaoDeCampo.recusa(this.negocio, json, RegrasFieldTypeTimeBefore.noMaximoSeteDiasAtras, CcpJsonFieldTypeError.timeMaxValueBeforeCurrentTime);
	}

	@Test
	public void dentroDoLimiteMinimoTest() {
		CcpJsonRepresentation json = this.jsonComDiasAtras(RegrasFieldTypeTimeBefore.noMinimoDoisDiasAtras, 5);
		ValidacaoDeCampo.aceita(this.negocio, json);
	}

	@Test
	public void foraDoLimiteMinimoTest() {
		CcpJsonRepresentation json = this.jsonComDiasAtras(RegrasFieldTypeTimeBefore.noMinimoDoisDiasAtras, 0);
		ValidacaoDeCampo.recusa(this.negocio, json, RegrasFieldTypeTimeBefore.noMinimoDoisDiasAtras, CcpJsonFieldTypeError.timeMinValueBeforeCurrentTime);
	}
}
