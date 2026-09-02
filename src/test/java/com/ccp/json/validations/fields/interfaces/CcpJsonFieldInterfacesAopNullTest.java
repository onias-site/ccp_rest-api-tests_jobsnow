package com.ccp.json.validations.fields.interfaces;

import java.lang.reflect.Field;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.json.validations.fields.enums.CcpJsonFieldDefaultTypes;
import com.ccp.json.validations.fields.enums.CcpJsonFieldError;
import com.ccp.json.validations.fields.enums.CcpJsonFieldsValidationContext;
import org.junit.Test;

/**
 * Cobertura do {@code CcpNullParameterAspect} sobre os métodos default de
 * {@code CcpJsonFieldType} e {@code CcpJsonFieldValidatorInterface}.
 */
public class CcpJsonFieldInterfacesAopNullTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	private static final CcpJsonRepresentation JSON = CcpOtherConstants.EMPTY_JSON;

	private static final CcpJsonFieldType TYPE = CcpJsonFieldDefaultTypes.String;

	private static final CcpJsonFieldValidatorInterface VALIDATOR = CcpJsonFieldError.requiredFieldIsMissing;



	private static Field field() {
		return FieldHolder.class.getDeclaredFields()[0];
	}

	// ── CcpJsonFieldType ──────────────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void typeHasErrorsJsonNullTest() {
		TYPE.hasErrors(null, field(), CcpJsonFieldsValidationContext.single);
	}

	@Test(expected = CcpNullParameterException.class)
	public void typeHasErrorsFieldNullTest() {
		TYPE.hasErrors(JSON, null, CcpJsonFieldsValidationContext.single);
	}

	@Test(expected = CcpNullParameterException.class)
	public void typeHasErrorsContextNullTest() {
		TYPE.hasErrors(JSON, field(), null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void typeGetErrorsErrorsNullTest() {
		TYPE.getErrors(null, JSON, field(), CcpJsonFieldsValidationContext.single);
	}

	@Test(expected = CcpNullParameterException.class)
	public void typeGetErrorsJsonNullTest() {
		TYPE.getErrors(JSON, null, field(), CcpJsonFieldsValidationContext.single);
	}

	@Test(expected = CcpNullParameterException.class)
	public void typeGetErrorsFieldNullTest() {
		TYPE.getErrors(JSON, JSON, null, CcpJsonFieldsValidationContext.single);
	}

	@Test(expected = CcpNullParameterException.class)
	public void typeGetErrorsContextNullTest() {
		TYPE.getErrors(JSON, JSON, field(), null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void typeUpdateRuleExplanationRuleNullTest() {
		TYPE.updateRuleExplanation(null, field());
	}

	@Test(expected = CcpNullParameterException.class)
	public void typeUpdateRuleExplanationFieldNullTest() {
		TYPE.updateRuleExplanation(JSON, null);
	}

	// ── CcpJsonFieldValidatorInterface ────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void validatorIsValidValidationContextNullTest() {
		VALIDATOR.isValidValidationContext(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void validatorGetErrorJsonNullTest() {
		VALIDATOR.getError(null, field(), TYPE);
	}

	@Test(expected = CcpNullParameterException.class)
	public void validatorGetErrorFieldNullTest() {
		VALIDATOR.getError(JSON, null, TYPE);
	}

	@Test(expected = CcpNullParameterException.class)
	public void validatorGetErrorTypeNullTest() {
		VALIDATOR.getError(JSON, field(), null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void validatorGetErrorsErrorsNullTest() {
		VALIDATOR.getErrors(null, JSON, field(), TYPE);
	}

	@Test(expected = CcpNullParameterException.class)
	public void validatorGetErrorsJsonNullTest() {
		VALIDATOR.getErrors(JSON, null, field(), TYPE);
	}

	@Test(expected = CcpNullParameterException.class)
	public void validatorGetErrorsFieldNullTest() {
		VALIDATOR.getErrors(JSON, JSON, null, TYPE);
	}

	@Test(expected = CcpNullParameterException.class)
	public void validatorGetErrorsTypeNullTest() {
		VALIDATOR.getErrors(JSON, JSON, field(), null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void validatorUpdateRuleExplanationAllRulesNullTest() {
		VALIDATOR.updateRuleExplanation(null, field(), TYPE);
	}

	@Test(expected = CcpNullParameterException.class)
	public void validatorUpdateRuleExplanationFieldNullTest() {
		VALIDATOR.updateRuleExplanation(JSON, null, TYPE);
	}

	@Test(expected = CcpNullParameterException.class)
	public void validatorUpdateRuleExplanationTypeNullTest() {
		VALIDATOR.updateRuleExplanation(JSON, field(), null);
	}
}
