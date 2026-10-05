package com.ccp.json.validations.fields.annotations;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.json.validations.fields.enums.CcpJsonFieldTypeError;

/**
 * Verifies that {@code @CcpJsonFieldTypeTimeBefore} accepts timestamps within the past interval and refuses the
 * ones outside it.
 */
public class CcpJsonFieldTypeTimeBeforeTest {

	private static final long ONE_DAY = 86_400_000L;

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	private final BusinessFieldTypeTimeBefore business = new BusinessFieldTypeTimeBefore();

	private CcpJsonRepresentation jsonWithDaysAgo(RulesFieldTypeTimeBefore field, long days) {
		long currentTimeMillis = System.currentTimeMillis();
		long timestamp = currentTimeMillis - (days * ONE_DAY);
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(field, timestamp);
		return json;
	}

	@Test
	public void withinMaxLimitTest() {
		CcpJsonRepresentation json = this.jsonWithDaysAgo(RulesFieldTypeTimeBefore.noMaximoSeteDiasAtras, 3);
		FieldValidation.accepts(this.business, json);
	}

	@Test
	public void beyondMaxLimitTest() {
		CcpJsonRepresentation json = this.jsonWithDaysAgo(RulesFieldTypeTimeBefore.noMaximoSeteDiasAtras, 30);
		FieldValidation.refuses(this.business, json, RulesFieldTypeTimeBefore.noMaximoSeteDiasAtras, CcpJsonFieldTypeError.timeMaxValueBeforeCurrentTime);
	}

	@Test
	public void withinMinLimitTest() {
		CcpJsonRepresentation json = this.jsonWithDaysAgo(RulesFieldTypeTimeBefore.noMinimoDoisDiasAtras, 5);
		FieldValidation.accepts(this.business, json);
	}

	@Test
	public void exactlyTheConfiguredDistanceIsAccepted() {
		CcpJsonRepresentation json = this.jsonWithDaysAgo(RulesFieldTypeTimeBefore.exactlyThreeDaysAgo, 3);
		FieldValidation.accepts(this.business, json);
	}

	@Test
	public void anotherDistanceIsRefusedByTheExactRule() {
		CcpJsonRepresentation json = this.jsonWithDaysAgo(RulesFieldTypeTimeBefore.exactlyThreeDaysAgo, 10);
		FieldValidation.refuses(this.business, json, RulesFieldTypeTimeBefore.exactlyThreeDaysAgo, CcpJsonFieldTypeError.timeExactValueBeforeCurrentTime);
	}

	@Test
	public void belowMinLimitTest() {
		CcpJsonRepresentation json = this.jsonWithDaysAgo(RulesFieldTypeTimeBefore.noMinimoDoisDiasAtras, 0);
		FieldValidation.refuses(this.business, json, RulesFieldTypeTimeBefore.noMinimoDoisDiasAtras, CcpJsonFieldTypeError.timeMinValueBeforeCurrentTime);
	}
}
