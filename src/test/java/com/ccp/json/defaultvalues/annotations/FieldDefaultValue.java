package com.ccp.json.defaultvalues.annotations;

import static org.junit.Assert.fail;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.json.validations.global.engine.CcpJsonValidationError;

/**
 * Apoio para os testes de {@code @CcpJsonFieldDefaultValue}. Executa o negócio pelo {@code execute}
 * e devolve o JSON já com os valores padrão aplicados.
 *
 * Não dá para reaproveitar o {@code FieldValidation.accepts}: ele exige que a saída seja igual à
 * entrada, e o objetivo aqui é justamente que a saída traga campos que a entrada não tinha.
 */
public class FieldDefaultValue {

	private FieldDefaultValue() {}

	/**
	 * Executa o negócio sobre a classe de regras informada e devolve o JSON resultante, falhando o
	 * teste caso a validação recuse a entrada.
	 */
	public static CcpJsonRepresentation applyDefaultValues(Class<?> rulesClass, CcpJsonRepresentation json) {

		DefaultValueBusiness business = new DefaultValueBusiness(rulesClass);

		try {
			CcpJsonRepresentation retorno = business.execute(json);
			return retorno;
		} catch (CcpJsonValidationError e) {
			String message = e.getExplanedMessage();
			fail("O json deveria ter passado na validacao, mas foi recusado com: " + message);
			return CcpOtherConstants.EMPTY_JSON;
		}
	}
}
