package com.ccp.json.validations.fields.annotations;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;

/**
 * Fields annotated with {@code @CcpJsonFieldTypeString}, one for each restriction the annotation offers:
 * minimum, maximum and exact length, empty string, regex, values coming from an enum and name of a Java class
 * existing in the class loader.
 */
public enum RulesFieldTypeString implements CcpJsonFieldName {

	@CcpJsonFieldTypeString(minLength = 3)
	comprimentoMinimo,

	@CcpJsonFieldTypeString(maxLength = 5)
	comprimentoMaximo,

	@CcpJsonFieldTypeString(exactLength = 4)
	comprimentoExato,

	@CcpJsonFieldTypeString
	naoAceitaVazio,

	@CcpJsonFieldTypeString(allowsEmptyString = true)
	aceitaVazio,

	@CcpJsonFieldTypeString(regexValidation = "^[0-9]{3}$")
	apenasTresDigitos,

	@CcpJsonFieldTypeString(allowedValuesEnum = AllowedValues.class)
	valorPermitido,

	@CcpJsonFieldTypeString(isJavaClass = true)
	nomeDeClasseJava,
	;

	public static enum AllowedValues {
		yes, no
	}
}
