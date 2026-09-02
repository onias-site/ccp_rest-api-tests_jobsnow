package com.ccp.random;


import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpStringDecorator;

class AgruparVagasPorRecrutadores implements java.util.function.Consumer<CcpJsonRepresentation> {

	CcpJsonRepresentation vagasAgrupadasPorRecrutadores = CcpOtherConstants.EMPTY_JSON;

	public void accept(CcpJsonRepresentation json) {

		String recrutador = json.getAsObject(CcpRandomScripts.JsonFieldNames.mail);
		String contato = json.getAsString(CcpRandomScripts.JsonFieldNames.contato);
		String texto = json.getAsString(CcpRandomScripts.JsonFieldNames.vaga);
		String contactChannel = new CcpStringDecorator(contato.trim()).email().isValid() ? "email" : "link";

		CcpJsonRepresentation vaga = CcpOtherConstants.EMPTY_JSON.put(CcpRandomScripts.JsonFieldNames.channel, contato).put(CcpRandomScripts.JsonFieldNames.email, recrutador)
				.put(CcpRandomScripts.JsonFieldNames.description, texto).put(CcpRandomScripts.JsonFieldNames.contactChannel, contactChannel);

		this.vagasAgrupadasPorRecrutadores = this.vagasAgrupadasPorRecrutadores.addToList(new CcpFieldName(recrutador), vaga);
	}

}
