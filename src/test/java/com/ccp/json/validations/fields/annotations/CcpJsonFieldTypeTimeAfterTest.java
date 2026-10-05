package com.ccp.json.validations.fields.annotations;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.json.validations.fields.enums.CcpJsonFieldTypeError;

/**
 * Verifies that {@code @CcpJsonFieldTypeTimeAfter} accepts timestamps within the configured future interval and
 * refuses the ones outside it.
 */
public class CcpJsonFieldTypeTimeAfterTest {

	private static final long ONE_DAY = 86_400_000L;

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	private final BusinessFieldTypeTimeAfter business = new BusinessFieldTypeTimeAfter();

	private CcpJsonRepresentation jsonWithDaysAhead(RulesFieldTypeTimeAfter field, long days) {
		long currentTimeMillis = System.currentTimeMillis();
		long timestamp = currentTimeMillis + (days * ONE_DAY);
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(field, timestamp);
		return json;
	}

	@Test
	public void withinMaxLimitTest() {
		CcpJsonRepresentation json = this.jsonWithDaysAhead(RulesFieldTypeTimeAfter.noMaximoSeteDiasAFrente, 3);
		FieldValidation.accepts(this.business, json);
	}

	@Test
	public void beyondMaxLimitTest() {
		CcpJsonRepresentation json = this.jsonWithDaysAhead(RulesFieldTypeTimeAfter.noMaximoSeteDiasAFrente, 30);
		FieldValidation.refuses(this.business, json, RulesFieldTypeTimeAfter.noMaximoSeteDiasAFrente, CcpJsonFieldTypeError.timeMaxValueAfterCurrentTime);
	}

	@Test
	public void withinMinLimitTest() {
		CcpJsonRepresentation json = this.jsonWithDaysAhead(RulesFieldTypeTimeAfter.noMinimoDoisDiasAFrente, 5);
		FieldValidation.accepts(this.business, json);
	}

	@Test
	public void exactlyTheConfiguredDistanceIsAccepted() {
		// the distance is counted in whole days, truncated: "now + 3 days" checked a millisecond later is 2 days,
		// so the timestamp goes half a day into the window of the third day
		long timestamp = System.currentTimeMillis() + 3 * ONE_DAY + ONE_DAY / 2;
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(RulesFieldTypeTimeAfter.exactlyThreeDaysAhead, timestamp);
		FieldValidation.accepts(this.business, json);
	}

	@Test
	public void anotherDistanceIsRefusedByTheExactRule() {
		CcpJsonRepresentation json = this.jsonWithDaysAhead(RulesFieldTypeTimeAfter.exactlyThreeDaysAhead, 10);
		FieldValidation.refuses(this.business, json, RulesFieldTypeTimeAfter.exactlyThreeDaysAhead, CcpJsonFieldTypeError.timeExactValueAfterCurrentTime);
	}

	@Test
	public void belowMinLimitTest() {
		CcpJsonRepresentation json = this.jsonWithDaysAhead(RulesFieldTypeTimeAfter.noMinimoDoisDiasAFrente, 0);
		FieldValidation.refuses(this.business, json, RulesFieldTypeTimeAfter.noMinimoDoisDiasAFrente, CcpJsonFieldTypeError.timeMinValueAfterCurrentTime);
	}
}
