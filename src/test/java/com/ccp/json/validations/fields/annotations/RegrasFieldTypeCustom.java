package com.ccp.json.validations.fields.annotations;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeCustom;

/**
 * Campo anotado com {@code @CcpJsonFieldTypeCustom}, apontando para o tipo customizado
 * {@code TipoCampoApenasVogais}.
 */
public enum RegrasFieldTypeCustom implements CcpJsonFieldName {

	@CcpJsonFieldTypeCustom(TipoCampoApenasVogais.class)
	apenasVogais,
	;
}
