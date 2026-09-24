package com.ccp.json.defaultvalues.annotations;

import static org.junit.Assert.fail;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.json.validations.global.engine.CcpJsonValidationError;

/**
 * Apoio para os testes de {@code @CcpJsonFieldDefaultValue}. Executa o negócio pelo {@code execute}
 * e devolve o JSON já com os valores padrão aplicados.
 *
 * Não dá para reaproveitar o {@code ValidacaoDeCampo.aceita}: ele exige que a saída seja igual à
 * entrada, e o objetivo aqui é justamente que a saída traga campos que a entrada não tinha.
 */
public class ValorPadraoDeCampo {

	private ValorPadraoDeCampo() {}

	/**
	 * Executa o negócio sobre a classe de regras informada e devolve o JSON resultante, falhando o
	 * teste caso a validação recuse a entrada.
	 */
	public static CcpJsonRepresentation executa(Class<?> classeDeRegras, CcpJsonRepresentation json) {

		NegocioDeValorPadrao negocio = new NegocioDeValorPadrao(classeDeRegras);

		try {
			CcpJsonRepresentation retorno = negocio.execute(json);
			return retorno;
		} catch (CcpJsonValidationError e) {
			String mensagem = e.getExplanedMessage();
			fail("O json deveria ter passado na validacao, mas foi recusado com: " + mensagem);
			return CcpOtherConstants.EMPTY_JSON;
		}
	}
}
