package com.ccp.especifications.db.utils.entity.decorators.engine;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.email.CcpEmailSender;
import com.ccp.especifications.http.CcpHttpContentType;

/**
 * Engole os emails. Gravar o token de login dispara envio ao usuário, e o que este teste mede é o
 * estado dos dois índices depois do {@code deleteAnyWhere}, não o conteúdo da mensagem.
 */
class CaixaDeEmailSilenciosa implements CcpEmailSender {

	public CcpJsonRepresentation sendSimpleTextEmailMessage(String providerToken, String providerUrl, String templateId, String sender, String subject, String message, CcpHttpContentType contentType, String... recipients) {
		return CcpOtherConstants.EMPTY_JSON;
	}
}
