package com.jb.business.bots.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.email.CcpEmailSender;
import com.ccp.especifications.http.CcpHttpContentType;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/**
 * Guarda os emails que sairiam pelo provedor em vez de entregá-los. O dublê local que os outros testes
 * usam grava o corpo num arquivo, o que serviria para conferir à mão mas não para o teste ler de volta
 * o token que foi enviado ao usuário.
 */
class CaixaDeEmailFalsa implements CcpEmailSender {

	final List<CcpJsonRepresentation> enviados = new ArrayList<>();

	public CcpJsonRepresentation sendSimpleTextEmailMessage(String providerToken, String providerUrl, String templateId, String sender, String subject, String message, CcpHttpContentType contentType, String... recipients) {

		List<String> destinatarios = Arrays.asList(recipients);

		CcpJsonRepresentation enviado = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonCommonsFields.templateId, templateId)
				.put(JnJsonCommonsFields.subject, subject)
				.put(JnJsonCommonsFields.message, message)
				.put(JnJsonCommonsFields.email, destinatarios);

		this.enviados.add(enviado);

		return CcpOtherConstants.EMPTY_JSON;
	}
}
