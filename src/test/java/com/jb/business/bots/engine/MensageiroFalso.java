package com.jb.business.bots.engine;

import java.util.ArrayList;
import java.util.List;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.instant.messenger.CcpInstantMessenger;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;

/**
 * Guarda as mensagens que sairiam para o Telegram em vez de entregá-las ao bot de verdade. O token do
 * bot de suporte que está nas propriedades do ambiente é real, então sem este dublê o teste mandaria
 * mensagem para o celular de quem atende o suporte a cada execução.
 */
class MensageiroFalso implements CcpInstantMessenger {

	final List<CcpJsonRepresentation> enviadas = new ArrayList<>();

	public CcpJsonRepresentation sendTextMessage(CcpJsonFieldName botType, String botToken, Long chatId, Long replyTo, String message) {
		this.guardar(botType, chatId, message);
		return CcpOtherConstants.EMPTY_JSON;
	}

	public CcpJsonRepresentation sendFile(CcpJsonFieldName botType, String botToken, Long chatId, Long replyTo, String fileName, String caption, Byte[] fileContent) {
		this.guardar(botType, chatId, caption);
		return CcpOtherConstants.EMPTY_JSON;
	}

	private void guardar(CcpJsonFieldName botType, Long chatId, String message) {
		String botName = botType.name();
		CcpJsonRepresentation enviada = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonInstantMessengerFields.botName, botName)
				.put(JnJsonInstantMessengerFields.chatId, chatId)
				.put(JnJsonCommonsFields.message, message);
		this.enviadas.add(enviada);
	}

	/**
	 * Devolve a última mensagem enviada ao chat informado. O fluxo manda mais de uma mensagem ao suporte
	 * (a do ticket pendente, na montagem do cenário, e a do ticket atendido), por isso o teste pergunta
	 * pela última e não pela única.
	 */
	CcpJsonRepresentation ultimaMensagemPara(Long chatId) {
		for (int indice = this.enviadas.size() - 1; indice >= 0; indice--) {
			CcpJsonRepresentation enviada = this.enviadas.get(indice);
			Long destinatario = enviada.getAsLongNumber(JnJsonInstantMessengerFields.chatId);
			boolean ehOutroChat = false == destinatario.equals(chatId);
			if (ehOutroChat) {
				continue;
			}
			return enviada;
		}
		return CcpOtherConstants.EMPTY_JSON;
	}
}
