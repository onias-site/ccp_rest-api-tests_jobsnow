package com.ccp.json.validations.fields.annotations;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeNumberInteger;

/**
 * Campos anotados com {@code @CcpJsonFieldTypeNumberInteger} (long), um para cada restrição: valor
 * mínimo, máximo, exato e lista de valores permitidos.
 */
public enum RegrasFieldTypeNumberInteger implements CcpJsonFieldName {

	@CcpJsonFieldTypeNumberInteger(minValue = 10)
	valorMinimo,

	@CcpJsonFieldTypeNumberInteger(maxValue = 20)
	valorMaximo,

	// 1500 fica fora do cache de Long (-128..127) de proposito: se a comparacao do validador for
	// feita por referencia em vez de por valor, o defeito aparece aqui e some com numeros pequenos.
	@CcpJsonFieldTypeNumberInteger(exactValue = 1500)
	valorExato,

	@CcpJsonFieldTypeNumberInteger(allowedValues = {1, 2, 3})
	valorPermitido,

	@CcpJsonFieldTypeNumberInteger
	semRestricao,
	;
}
