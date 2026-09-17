package com.ccp.json.validations.fields.annotations;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;

/**
 * Campos anotados com {@code @CcpJsonFieldTypeString}, um para cada restrição que a anotação
 * oferece: comprimento mínimo, máximo, exato, string vazia, regex e valores vindos de enum.
 */
public enum RegrasFieldTypeString implements CcpJsonFieldName {

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

	@CcpJsonFieldTypeString(allowedValuesEnum = ValoresPermitidos.class)
	valorPermitido,
	;

	public static enum ValoresPermitidos {
		sim, nao
	}
}
