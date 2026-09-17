package com.ccp.json.validations.fields.annotations;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeNumberUnsigned;

/**
 * Campos anotados com {@code @CcpJsonFieldTypeNumberUnsigned} (long não-negativo), um para cada
 * restrição: valor mínimo, máximo, exato e lista de valores permitidos.
 */
public enum RegrasFieldTypeNumberUnsigned implements CcpJsonFieldName {

	@CcpJsonFieldTypeNumberUnsigned(minValue = 10)
	valorMinimo,

	@CcpJsonFieldTypeNumberUnsigned(maxValue = 20)
	valorMaximo,

	// 1500 fica fora do cache de Long (-128..127) de proposito: se a comparacao do validador for
	// feita por referencia em vez de por valor, o defeito aparece aqui e some com numeros pequenos.
	@CcpJsonFieldTypeNumberUnsigned(exactValue = 1500)
	valorExato,

	@CcpJsonFieldTypeNumberUnsigned(allowedValues = {1, 2, 3})
	valorPermitido,

	@CcpJsonFieldTypeNumberUnsigned
	semRestricao,
	;
}
