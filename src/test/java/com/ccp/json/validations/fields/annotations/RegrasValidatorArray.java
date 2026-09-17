package com.ccp.json.validations.fields.annotations;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;

/**
 * Campos anotados com {@code @CcpJsonFieldValidatorArray}, um para cada restrição de coleção:
 * tamanho mínimo, máximo, exato e repetição de itens. Todos carregam também uma anotação de tipo,
 * porque o engine descarta o campo que não tem tipo declarado antes mesmo de validar a coleção.
 */
public enum RegrasValidatorArray implements CcpJsonFieldName {

	@CcpJsonFieldValidatorArray(minSize = 2)
	@CcpJsonFieldTypeString
	minimoDois,

	@CcpJsonFieldValidatorArray(maxSize = 2)
	@CcpJsonFieldTypeString
	maximoDois,

	@CcpJsonFieldValidatorArray(exactSize = 2)
	@CcpJsonFieldTypeString
	exatamenteDois,

	@CcpJsonFieldValidatorArray
	@CcpJsonFieldTypeString
	semItensRepetidos,

	@CcpJsonFieldValidatorArray(nonRepeatedItems = false)
	@CcpJsonFieldTypeString
	aceitaItensRepetidos,
	;
}
