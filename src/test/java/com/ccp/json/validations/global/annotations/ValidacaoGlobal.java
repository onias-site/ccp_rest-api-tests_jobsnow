package com.ccp.json.validations.global.annotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.json.validations.global.engine.CcpJsonValidationError;
import com.ccp.json.validations.global.engine.CcpJsonValidationRulesEngine;
import com.ccp.json.validations.global.engine.CcpJsonValidatorEngine;
import com.ccp.json.validations.global.engine.CcpJsonValidationError.CcpValidationErrorFields;

/**
 * Apoio para os testes de {@code @CcpJsonGlobalValidations}. Ao contrário das validações de campo, as
 * validações globais registram os erros sob a chave do nome da classe portadora das regras, e a
 * mensagem é texto livre devolvido por {@code getErrorMessage} — por isso a extração é feita aqui.
 */
public class ValidacaoGlobal {

	private ValidacaoGlobal() {}

	private static final String NOME_DA_FUNCIONALIDADE = "teste de validacoes globais";

	/** Confere que o json passa pelas validações globais da classe de regras informada. */
	public static void aceita(Class<?> regras, CcpJsonRepresentation json) {
		try {
			CcpJsonRepresentation retorno = CcpJsonValidatorEngine.INSTANCE.validateJson(regras, json, NOME_DA_FUNCIONALIDADE);
			assertEquals(json, retorno);
		} catch (CcpJsonValidationError e) {
			List<String> mensagens = extraiMensagens(e, regras);
			fail("O json deveria ter passado na validacao global, mas foi recusado com: " + mensagens);
		}
	}

	/**
	 * Confere que o json é recusado pelas validações globais da classe de regras e devolve as mensagens
	 * de erro registradas sob a chave dessa classe.
	 */
	public static List<String> recusa(Class<?> regras, CcpJsonRepresentation json) {
		try {
			CcpJsonValidatorEngine.INSTANCE.validateJson(regras, json, NOME_DA_FUNCIONALIDADE);
		} catch (CcpJsonValidationError e) {
			List<String> mensagens = extraiMensagens(e, regras);
			boolean semMensagens = mensagens.isEmpty();
			assertTrue("O json foi recusado, mas nenhum erro foi registrado para a classe de regras", false == semMensagens);
			return mensagens;
		}

		String nomeDasRegras = regras.getName();
		fail("O json deveria ter sido recusado pelas validacoes globais de '" + nomeDasRegras + "', mas passou na validacao");
		return new ArrayList<>();
	}

	/** Confere que ao menos uma das mensagens de erro contém o trecho informado. */
	public static void contemMensagem(List<String> mensagens, String trecho) {
		for (String mensagem : mensagens) {
			boolean contem = mensagem.contains(trecho);
			if(contem) {
				return;
			}
		}
		fail("Era esperada uma mensagem contendo '" + trecho + "', mas as mensagens foram: " + mensagens);
	}

	/** Confere que nenhuma das mensagens de erro contém o trecho informado. */
	public static void naoContemMensagem(List<String> mensagens, String trecho) {
		for (String mensagem : mensagens) {
			boolean contem = mensagem.contains(trecho);
			if(contem) {
				fail("Nao era esperada uma mensagem contendo '" + trecho + "', mas as mensagens foram: " + mensagens);
			}
		}
	}

	/**
	 * Devolve as explicações das regras globais da classe informada — o texto que acompanha o erro para
	 * dizer ao chamador o que a anotação exige.
	 */
	public static List<String> explicacoesDasRegras(Class<?> regras) {
		CcpJsonRepresentation rulesExplanation = CcpJsonValidationRulesEngine.INSTANCE.getRulesExplanation(regras);
		String nomeDasRegras = regras.getName();
		CcpFieldName chave = new CcpFieldName(nomeDasRegras);
		List<Object> explicacoes = rulesExplanation.getAsObjectList(chave);
		List<String> mensagens = new ArrayList<>();
		achata(explicacoes, mensagens);
		return mensagens;
	}

	private static List<String> extraiMensagens(CcpJsonValidationError e, Class<?> regras) {
		CcpJsonRepresentation errors = e.json.getInnerJson(CcpValidationErrorFields.errors);
		String nomeDasRegras = regras.getName();
		CcpFieldName chave = new CcpFieldName(nomeDasRegras);
		List<Object> erros = errors.getAsObjectList(chave);
		List<String> mensagens = new ArrayList<>();
		achata(erros, mensagens);
		return mensagens;
	}

	private static void achata(Collection<?> valores, List<String> mensagens) {
		for (Object valor : valores) {
			boolean ehColecao = valor instanceof Collection;
			if(ehColecao) {
				Collection<?> colecao = (Collection<?>) valor;
				achata(colecao, mensagens);
				continue;
			}
			String mensagem = "" + valor;
			mensagens.add(mensagem);
		}
	}
}
