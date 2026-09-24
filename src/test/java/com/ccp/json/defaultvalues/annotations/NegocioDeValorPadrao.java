package com.ccp.json.defaultvalues.annotations;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;

/**
 * Negócio que apenas devolve o JSON recebido, servindo de veículo para exercitar o {@code execute}
 * sobre uma classe de regras qualquer. Diferente dos negócios das anotações de validação, este é
 * reaproveitado por todos os cenários: a classe de regras vem pelo construtor, porque o que muda de
 * um cenário para o outro é só ela.
 */
public class NegocioDeValorPadrao implements CcpBusiness {

	private final Class<?> classeDeRegras;

	public NegocioDeValorPadrao(Class<?> classeDeRegras) {
		this.classeDeRegras = classeDeRegras;
	}

	public Class<?> getJsonValidationClass() {
		return this.classeDeRegras;
	}

	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		return json;
	}
}
