package com.ccp.json.validations.fields.enums;

import java.lang.reflect.Field;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.json.validations.fields.interfaces.CcpJsonFieldType;
import org.junit.Test;

/**
 * Cobertura do {@code CcpNullParameterAspect} sobre os enums de validação de campo
 * ({@code CcpJsonFieldError}, {@code CcpJsonFieldTypeError}, {@code TimeOptions} e
 * {@code TimeValueExtractorFromAnnotation}), incluindo os membros de visibilidade de pacote.
 */
public class CcpJsonFieldValidationsAopNullTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	private static final CcpJsonRepresentation JSON = CcpOtherConstants.EMPTY_JSON;

	private static final CcpJsonFieldType TYPE = CcpJsonFieldDefaultTypes.String;

	/** Portadora de um {@code java.lang.reflect.Field} real para as chamadas. */


	private static Field field() {
		return FieldHolder.class.getDeclaredFields()[0];
	}

	// ── CcpJsonFieldError.getProvidedValue ────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void fieldErrorGetProvidedValueJsonNullTest() {
		CcpJsonFieldError.requiredFieldIsMissing.getProvidedValue(null, field(), TYPE);
	}

	@Test(expected = CcpNullParameterException.class)
	public void fieldErrorGetProvidedValueFieldNullTest() {
		CcpJsonFieldError.requiredFieldIsMissing.getProvidedValue(JSON, null, TYPE);
	}

	@Test(expected = CcpNullParameterException.class)
	public void fieldErrorGetProvidedValueTypeNullTest() {
		CcpJsonFieldError.requiredFieldIsMissing.getProvidedValue(JSON, field(), null);
	}

	// ── CcpJsonFieldTypeError.getProvidedValue ────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void typeErrorGetProvidedValueJsonNullTest() {
		CcpJsonFieldTypeError.stringNotEmpty.getProvidedValue(null, field(), TYPE);
	}

	@Test(expected = CcpNullParameterException.class)
	public void typeErrorGetProvidedValueFieldNullTest() {
		CcpJsonFieldTypeError.stringNotEmpty.getProvidedValue(JSON, null, TYPE);
	}

	@Test(expected = CcpNullParameterException.class)
	public void typeErrorGetProvidedValueTypeNullTest() {
		CcpJsonFieldTypeError.stringNotEmpty.getProvidedValue(JSON, field(), null);
	}

	// ── TimeOptions ───────────────────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void timeOptionsGetEnlapsedTimeJsonNullTest() {
		TimeOptions._before.getEnlapsedTime(null, field());
	}

	@Test(expected = CcpNullParameterException.class)
	public void timeOptionsGetEnlapsedTimeFieldNullTest() {
		TimeOptions._before.getEnlapsedTime(JSON, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void timeOptionsGetEnlapsedIntervalJsonNullTest() {
		TimeOptions._before.getEnlapsedInterval(null, field());
	}

	@Test(expected = CcpNullParameterException.class)
	public void timeOptionsGetEnlapsedIntervalFieldNullTest() {
		TimeOptions._before.getEnlapsedInterval(JSON, null);
	}

	// ── TimeValueExtractorFromAnnotation ──────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void timeExtractorGetValueFromAnnotationJsonNullTest() {
		TimeValueExtractorFromAnnotation.max.getValueFromAnnotationInMilliseconds(null, field());
	}

	@Test(expected = CcpNullParameterException.class)
	public void timeExtractorGetValueFromAnnotationFieldNullTest() {
		TimeValueExtractorFromAnnotation.max.getValueFromAnnotationInMilliseconds(JSON, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void timeExtractorHasErrorJsonNullTest() {
		TimeValueExtractorFromAnnotation.max.hasError(null, field(), TimeOptions._before);
	}

	@Test(expected = CcpNullParameterException.class)
	public void timeExtractorHasErrorFieldNullTest() {
		TimeValueExtractorFromAnnotation.max.hasError(JSON, null, TimeOptions._before);
	}

	@Test(expected = CcpNullParameterException.class)
	public void timeExtractorHasErrorTimeOptionsNullTest() {
		TimeValueExtractorFromAnnotation.max.hasError(JSON, field(), null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void timeExtractorGetErrorMessageJsonNullTest() {
		TimeValueExtractorFromAnnotation.max.getErrorMessage(null, field(), TimeOptions._before);
	}

	@Test(expected = CcpNullParameterException.class)
	public void timeExtractorGetErrorMessageFieldNullTest() {
		TimeValueExtractorFromAnnotation.max.getErrorMessage(JSON, null, TimeOptions._before);
	}

	@Test(expected = CcpNullParameterException.class)
	public void timeExtractorGetErrorMessageTimeOptionsNullTest() {
		TimeValueExtractorFromAnnotation.max.getErrorMessage(JSON, field(), null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void timeExtractorGetRuleExplanationFieldNullTest() {
		TimeValueExtractorFromAnnotation.max.getRuleExplanation(null, TimeOptions._before);
	}

	@Test(expected = CcpNullParameterException.class)
	public void timeExtractorGetRuleExplanationTimeOptionsNullTest() {
		TimeValueExtractorFromAnnotation.max.getRuleExplanation(field(), null);
	}
}
