package com.ccp.json.defaultvalues.annotations;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;

/**
 * Classe de origem das validações copiadas. É aqui que mora a anotação de valor padrão, enquanto o
 * {@code @CcpJsonFieldValidatorRequired} fica no campo de destino — exatamente a forma do
 * {@code fileName}, cujo valor padrão mora em {@code JnJsonInstantMessengerFields}.
 */
public enum RegrasOrigemDoValorPadrao implements CcpJsonFieldName {

	@CcpJsonFieldTypeString
	@CcpJsonFieldDefaultValue(defaultStrings = "derivado de {origem}")
	copiado,
	;
}
