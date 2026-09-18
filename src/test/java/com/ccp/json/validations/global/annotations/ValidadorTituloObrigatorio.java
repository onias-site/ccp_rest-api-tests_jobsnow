package com.ccp.json.validations.global.annotations;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.json.validations.global.interfaces.CcpJsonValidator;

/**
 * Segundo validador global customizado, usado para provar que {@code customJsonValidators} roda todos
 * os validadores da lista e acumula os erros de cada um.
 */
public class ValidadorTituloObrigatorio implements CcpJsonValidator {

	public static final String MENSAGEM = "O titulo da vaga e obrigatorio";

	public boolean hasError(CcpJsonRepresentation json, Class<?> clazz) {
		boolean containsAllFields = json.containsAllFields(CamposDoValidadorGlobal.titulo);

		boolean tituloAusente = false == containsAllFields;
		return tituloAusente;
	}

	public Object getErrorMessage(CcpJsonRepresentation json, Class<?> clazz) {
		return MENSAGEM;
	}

	public boolean isCriticalValidation(CcpJsonRepresentation json, Class<?> clazz) {
		return false;
	}

	public Object getRuleExplanation(Class<?> clazz) {
		return MENSAGEM;
	}
}
