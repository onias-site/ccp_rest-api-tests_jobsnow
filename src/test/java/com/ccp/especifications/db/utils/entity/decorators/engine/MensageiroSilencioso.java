package com.ccp.especifications.db.utils.entity.decorators.engine;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.instant.messenger.CcpInstantMessenger;

/**
 * Engole as mensagens de bot. Gravar algumas entidades gêmeas avisa o suporte, e o token do bot que
 * está nas propriedades do ambiente é real — sem este dublê o teste mandaria mensagem de verdade a
 * cada execução.
 */
class MensageiroSilencioso implements CcpInstantMessenger {

	public CcpJsonRepresentation sendTextMessage(CcpJsonFieldName botType, String botToken, Long chatId, Long replyTo, String message) {
		return CcpOtherConstants.EMPTY_JSON;
	}

	public CcpJsonRepresentation sendFile(CcpJsonFieldName botType, String botToken, Long chatId, Long replyTo, String fileName, String caption, Byte[] fileContent) {
		return CcpOtherConstants.EMPTY_JSON;
	}
}
