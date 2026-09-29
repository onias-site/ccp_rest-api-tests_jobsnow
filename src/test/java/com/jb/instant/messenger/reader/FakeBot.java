package com.jb.instant.messenger.reader;

import java.util.ArrayList;
import java.util.List;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;
import com.jn.business.messages.JnMessageType;

/**
 * Bot substituto para os testes de leitura de mensagens. O {@code readNewMessages} descobre qual bot
 * está sendo lido através do {@code name()} do {@code CcpBusiness} recebido, por isso este dublê
 * devolve o nome do {@link JnBotType} informado ao invés do nome da classe. Cada mensagem entregue
 * pelo leitor fica guardada em {@link #received}, permitindo verificar o que foi lido em cada chamada.
 */
class FakeBot implements CcpBusiness {

	final List<CcpJsonRepresentation> received = new ArrayList<>();

	private final String botName;

	FakeBot(JnMessageType.JnBotType botType) {
		this.botName = botType.name();
	}

	public String name() {
		return this.botName;
	}

	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		this.received.add(json);
		return json;
	}
}
