package com.vis.json.fields.validation;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpTimeDecorator;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.json.validations.fields.annotations.FieldValidation;
import com.ccp.json.validations.fields.enums.CcpJsonFieldError;

/**
 * The month of the statistics ({@link VisYearMonthFieldType}): six digits as {@code yyyyMM}, year from 2015 up to the
 * current year (America/Sao_Paulo), month from 01 to 12.
 */
public class VisYearMonthFieldTypeTest {

	{
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE);
	}

	private final BusinessYearMonth business = BusinessYearMonth.INSTANCE;

	private CcpJsonRepresentation json(Object value) {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(RulesYearMonth.yearMonth, value);
		return json;
	}

	private int currentYear() {
		CcpTimeDecorator now = new CcpTimeDecorator();
		int year = now.getYear();
		return year;
	}

	private void refuses(Object value) {
		CcpJsonRepresentation json = this.json(value);
		FieldValidation.refuses(this.business, json, RulesYearMonth.yearMonth, CcpJsonFieldError.incompatibleType);
	}

	@Test
	public void firstMonthOfTheMinimumYearAcceptedTest() {
		CcpJsonRepresentation json = this.json("201501");
		FieldValidation.accepts(this.business, json);
	}

	@Test
	public void decemberOfTheCurrentYearAcceptedTest() {
		int year = this.currentYear();
		CcpJsonRepresentation json = this.json(year + "12");
		FieldValidation.accepts(this.business, json);
	}

	@Test
	public void monthAsANumberAcceptedTest() {
		CcpJsonRepresentation json = this.json(202306);
		FieldValidation.accepts(this.business, json);
	}

	@Test
	public void yearBeforeTheMinimumRefusedTest() {
		this.refuses("201412");
	}

	@Test
	public void yearAfterTheCurrentRefusedTest() {
		int currentYear = this.currentYear();
		int nextYear = currentYear + 1;
		this.refuses(nextYear + "01");
	}

	@Test
	public void monthZeroRefusedTest() {
		this.refuses("202000");
	}

	@Test
	public void monthThirteenRefusedTest() {
		this.refuses("202013");
	}

	@Test
	public void fiveDigitsRefusedTest() {
		this.refuses("20201");
	}

	@Test
	public void sevenDigitsRefusedTest() {
		this.refuses("2020011");
	}

	@Test
	public void letterRefusedTest() {
		this.refuses("2020a1");
	}

	@Test
	public void separatorRefusedTest() {
		this.refuses("2020-1");
	}
}
