package com.ccp.json.defaultvalues.annotations;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;

/**
 * Produtor de JSON acionado pelo atributo {@code jsonProducer}. Prova duas coisas de uma vez: que a
 * instância é criada por reflexão (não há nenhuma referência a ela fora da anotação) e que o JSON em
 * tratamento chega inteiro ao produtor, já que o valor gravado é derivado de outro campo.
 */
public class ProdutorDeValorPadrao implements CcpBusiness {

	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {

		String nome = json.getAsString(RegrasValorPadraoJsonProducer.nome);
		String valor = "produzido para " + nome;
		CcpJsonRepresentation produzido = json.put(RegrasValorPadraoJsonProducer.produzido, valor);

		return produzido;
	}
}
