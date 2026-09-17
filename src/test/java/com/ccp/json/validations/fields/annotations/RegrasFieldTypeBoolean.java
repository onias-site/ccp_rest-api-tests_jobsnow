package com.ccp.json.validations.fields.annotations;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeBoolean;

/**
 * Campo anotado com {@code @CcpJsonFieldTypeBoolean}, que não tem parâmetros: a única regra é a
 * compatibilidade de tipo.
 */
public enum RegrasFieldTypeBoolean implements CcpJsonFieldName {

	@CcpJsonFieldTypeBoolean
	ativo,
	;
}
