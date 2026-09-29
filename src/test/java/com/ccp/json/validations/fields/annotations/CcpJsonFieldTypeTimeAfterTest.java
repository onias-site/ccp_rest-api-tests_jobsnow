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

	private static final long ONE_DAY = 86_400_000L;

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	private final BusinessFieldTypeTimeAfter business = new BusinessFieldTypeTimeAfter();

	private CcpJsonRepresentation jsonComDiasAFrente(RulesFieldTypeTimeAfter field, long dias) {
		long currentTimeMillis = System.currentTimeMillis();
		long timestamp = currentTimeMillis + (dias * ONE_DAY);
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(field, timestamp);
		return json;
	}

	@Test
	public void withinMaxLimitTest() {
		CcpJsonRepresentation json = this.jsonComDiasAFrente(RulesFieldTypeTimeAfter.noMaximoSeteDiasAFrente, 3);
		FieldValidation.accepts(this.business, json);
	}

	@Test
	public void beyondMaxLimitTest() {
		CcpJsonRepresentation json = this.jsonComDiasAFrente(RulesFieldTypeTimeAfter.noMaximoSeteDiasAFrente, 30);
		FieldValidation.refuses(this.business, json, RulesFieldTypeTimeAfter.noMaximoSeteDiasAFrente, CcpJsonFieldTypeError.timeMaxValueAfterCurrentTime);
	}

	@Test
	public void withinMinLimitTest() {
		CcpJsonRepresentation json = this.jsonComDiasAFrente(RulesFieldTypeTimeAfter.noMinimoDoisDiasAFrente, 5);
		FieldValidation.accepts(this.business, json);
	}

	@Test
	public void belowMinLimitTest() {
		CcpJsonRepresentation json = this.jsonComDiasAFrente(RulesFieldTypeTimeAfter.noMinimoDoisDiasAFrente, 0);
		FieldValidation.refuses(this.business, json, RulesFieldTypeTimeAfter.noMinimoDoisDiasAFrente, CcpJsonFieldTypeError.timeMinValueAfterCurrentTime);
	}
}
