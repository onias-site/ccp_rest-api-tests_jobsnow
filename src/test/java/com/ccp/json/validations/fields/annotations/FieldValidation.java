package com.ccp.json.validations.fields.annotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.json.validations.fields.interfaces.CcpJsonFieldValidatorInterface.CcpErrorFields;
import com.ccp.json.validations.global.engine.CcpJsonValidationError;
import com.ccp.json.validations.global.engine.CcpJsonValidationError.CcpValidationErrorFields;

/**
 * Apoio para os testes das anotações de validação de campo. Executa o {@code CcpBusiness} pelo
 * método {@code execute} (que dispara {@code CcpJsonValidatorEngine} sobre a classe devolvida por
 * {@code getJsonValidationClass}) e confere quais validadores acusaram erro.
 */
public class FieldValidation {

	private FieldValidation() {}

	/**
	 * Confere que o JSON passa pela validação: o {@code execute} não pode lançar
	 * {@code CcpJsonValidationError} e precisa devolver o JSON produzido pelo {@code apply}.
	 */
	public static void accepts(CcpBusiness business, CcpJsonRepresentation json) {
		try {
			CcpJsonRepresentation retorno = business.execute(json);
			assertEquals(json, retorno);
		} catch (CcpJsonValidationError e) {
			String message = e.getExplanedMessage();
			fail("O json deveria ter passado na validacao, mas foi recusado com: " + message);
		}
	}

	/**
	 * Confere que o JSON é recusado e que o validador {@code validadorEsperado} acusou erro no campo
	 * {@code field}. Devolve a descrição do erro para asserções adicionais.
	 */
	public static String refuses(CcpBusiness business, CcpJsonRepresentation json, CcpJsonFieldName field, CcpJsonFieldName validadorEsperado) {

		try {
			business.execute(json);
		} catch (CcpJsonValidationError e) {
			return extraiDescricao(e, field, validadorEsperado);
		}

		String nomeDoCampo = field.getValue();
		String nomeDoValidador = validadorEsperado.getValue();
		fail("O json deveria ter sido recusado pelo validador '" + nomeDoValidador + "' no campo '" + nomeDoCampo + "', mas passou na validacao");
		return "";
	}

	private static String extraiDescricao(CcpJsonValidationError e, CcpJsonFieldName field, CcpJsonFieldName validadorEsperado) {

		CcpJsonRepresentation errors = e.json.getInnerJson(CcpValidationErrorFields.errors);
		String nomeDoCampo = field.getValue();
		boolean containsAllFields = errors.containsAllFields(field);

		boolean campoSemErros = false == containsAllFields;

		if(campoSemErros) {
			fail("Era esperado erro no campo '" + nomeDoCampo + "', mas os erros vieram em: " + errors.fieldSet());
		}

		List<CcpJsonRepresentation> errosDoCampo = errors.getAsJsonList(field);
		String nomeDoValidador = validadorEsperado.getValue();
		List<String> validadoresEncontrados = new ArrayList<>();

		for (CcpJsonRepresentation erro : errosDoCampo) {
			String errorName = erro.getAsString(CcpErrorFields.errorName);
			validadoresEncontrados.add(errorName);
			boolean errorNameEquals = errorName.equals(nomeDoValidador);

			boolean naoEhOValidadorEsperado = false == errorNameEquals;

			if(naoEhOValidadorEsperado) {
				continue;
			}
			String errorDescription = erro.getAsString(CcpErrorFields.errorDescription);
			boolean descricaoVazia = errorDescription.trim().isEmpty();
			assertTrue("O validador '" + nomeDoValidador + "' acusou erro sem descrever o motivo", false == descricaoVazia);
			return errorDescription;
		}

		fail("Era esperado o validador '" + nomeDoValidador + "' no campo '" + nomeDoCampo + "', mas os validadores que acusaram erro foram: " + validadoresEncontrados);
		return "";
	}
}
