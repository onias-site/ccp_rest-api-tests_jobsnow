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

	private static final long ONE_DAY = 86_400_000L;

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	private final BusinessFieldTypeTimeBefore business = new BusinessFieldTypeTimeBefore();

	private CcpJsonRepresentation jsonComDiasAtras(RulesFieldTypeTimeBefore field, long dias) {
		long currentTimeMillis = System.currentTimeMillis();
		long timestamp = currentTimeMillis - (dias * ONE_DAY);
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(field, timestamp);
		return json;
	}

	@Test
	public void withinMaxLimitTest() {
		CcpJsonRepresentation json = this.jsonComDiasAtras(RulesFieldTypeTimeBefore.noMaximoSeteDiasAtras, 3);
		FieldValidation.accepts(this.business, json);
	}

	@Test
	public void beyondMaxLimitTest() {
		CcpJsonRepresentation json = this.jsonComDiasAtras(RulesFieldTypeTimeBefore.noMaximoSeteDiasAtras, 30);
		FieldValidation.refuses(this.business, json, RulesFieldTypeTimeBefore.noMaximoSeteDiasAtras, CcpJsonFieldTypeError.timeMaxValueBeforeCurrentTime);
	}

	@Test
	public void withinMinLimitTest() {
		CcpJsonRepresentation json = this.jsonComDiasAtras(RulesFieldTypeTimeBefore.noMinimoDoisDiasAtras, 5);
		FieldValidation.accepts(this.business, json);
	}

	@Test
	public void belowMinLimitTest() {
		CcpJsonRepresentation json = this.jsonComDiasAtras(RulesFieldTypeTimeBefore.noMinimoDoisDiasAtras, 0);
		FieldValidation.refuses(this.business, json, RulesFieldTypeTimeBefore.noMinimoDoisDiasAtras, CcpJsonFieldTypeError.timeMinValueBeforeCurrentTime);
	}
}
