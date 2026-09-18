package com.ccp.json.validations.global.annotations;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.json.validations.global.interfaces.CcpJsonValidator;

/**
 * Validador global customizado marcado como crítico: sem o tipo de contrato não faz sentido continuar
 * avaliando o resto, então ao acusar erro ele interrompe as demais validações de classe.
 */
public class ValidadorCriticoDeContrato implements CcpJsonValidator {

	public static final String MENSAGEM = "O tipo de contrato e obrigatorio e sem ele nada mais pode ser avaliado";

	public boolean hasError(CcpJsonRepresentation json, Class<?> clazz) {
		boolean containsAllFields = json.containsAllFields(CamposDoValidadorGlobal.contrato);

		boolean contratoAusente = false == containsAllFields;
		return contratoAusente;
	}

	public Object getErrorMessage(CcpJsonRepresentation json, Class<?> clazz) {
		return MENSAGEM;
	}

	public boolean isCriticalValidation(CcpJsonRepresentation json, Class<?> clazz) {
		return true;
	}

	public Object getRuleExplanation(Class<?> clazz) {
		return MENSAGEM;
	}
}
