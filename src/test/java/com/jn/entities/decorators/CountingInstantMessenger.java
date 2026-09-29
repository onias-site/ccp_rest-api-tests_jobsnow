package com.jn.entities.decorators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.instant.messenger.CcpInstantMessenger;

/**
 * Dublê de Telegram que não envia nada, mas guarda o texto de cada mensagem. É com ele que os testes
 * dos decorators de envio ao usuário provam que a mensagem saiu — ou que não saiu — em cada operação.
 *
 * <p>A lista é estática porque a injeção de dependência instancia o dublê por conta própria.
 */
public class CountingInstantMessenger implements CcpInstantMessenger {

	private static final List<String> sentMessages = Collections.synchronizedList(new ArrayList<>());

	public CcpJsonRepresentation sendTextMessage(CcpJsonFieldName botType, String botToken, Long chatId, Long replyTo, String message) {
		sentMessages.add(message);
		return CcpOtherConstants.EMPTY_JSON;
	}

	public CcpJsonRepresentation sendFile(CcpJsonFieldName botType, String botToken, Long chatId, Long replyTo, String fileName, String caption, Byte[] fileContent) {
		sentMessages.add(caption);
		return CcpOtherConstants.EMPTY_JSON;
	}

	/** Quantas mensagens já enviadas mencionam o texto informado. */
	public static long quantasMencionam(String texto) {
		synchronized (sentMessages) {
			long quantas = sentMessages.stream().filter(x -> x != null && x.contains(texto)).count();
			return quantas;
		}
	}

	public static int total() {
		int total = sentMessages.size();
		return total;
	}
}
