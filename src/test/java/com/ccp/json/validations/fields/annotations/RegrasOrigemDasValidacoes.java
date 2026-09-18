package com.ccp.json.validations.fields.annotations;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;

/**
 * Classe de origem das validações copiadas por {@code @CcpJsonCopyFieldValidationsFrom}. É aqui
 * que as regras de fato moram; a classe de destino só aponta para cá.
 */
public enum RegrasOrigemDasValidacoes implements CcpJsonFieldName {

	@CcpJsonFieldTypeString(minLength = 3, maxLength = 8)
	apelido,

	@CcpJsonFieldTypeString
	causaComRepetidos,

	@CcpJsonFieldTypeString
	causaSemRepetidos,
	;
}
