package com.jb.business.bots.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.dependency.injection.CcpInstanceProvider;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.email.CcpEmailSender;
import com.ccp.especifications.instant.messenger.CcpInstantMessenger;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.implementations.password.mindrot.CcpMindrotPasswordHandler;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.jb.business.bots.login.token.JbSupportLoginTokenTypes;
import com.jn.business.messages.JnInstantMessageType;
import com.jn.business.messages.JnMessages;
import com.jn.entities.JnEntityInstantMessengerMessageSent;
import com.jn.entities.JnEntityInstantMessengerTemplateMessage;
import com.jn.entities.JnEntityLoginToken;
import com.jn.entities.JnEntityLoginTokenRequestUnlock;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;

/**
 * Exercita o atendimento de um ticket de desbloqueio de token do começo ao fim: a mensagem chega como o
 * leitor do Telegram a entrega, o {@code Bot} identifica o comando e o motor
 * {@code JbSupportLoginToken} resolve o ticket.
 *
 * <p>O banco é o Elasticsearch de verdade, como nos demais testes de fluxo, porque é dele que o
 * {@code JbBotEngine} lê toda a configuração dos bots. Já o Telegram e o provedor de email são
 * substituídos por dublês: o token do bot de suporte configurado no ambiente é real, e o teste precisa
 * ler de volta o token que foi para o usuário para conferir que é o mesmo que foi para o suporte.
 */
public class AoDesbloquearTokenPeloBotDeSuporteTest {

	private static final MensageiroFalso TELEGRAM = new MensageiroFalso();

	private static final CaixaDeEmailFalsa CAIXA_DE_EMAIL = new CaixaDeEmailFalsa();

	static {
		CcpInstanceProvider<CcpInstantMessenger> telegram = () -> TELEGRAM;
		CcpInstanceProvider<CcpEmailSender> email = () -> CAIXA_DE_EMAIL;

		CcpDependencyInjection.removeAllDependencies();
		CcpDependencyInjection.loadAllDependencies(
				CcpLocalInstances.syncMensageriaListener,
				new CcpElasticSearchDbRequest(),
				new CcpMindrotPasswordHandler(),
				CcpLocalCacheInstances.mock,
				new CcpElasticSerchDbBulk(),
				new CcpElasticSearchCrud(),
				new CcpGsonJsonHandler(),
				new CcpApacheMimeHttp(),
				telegram,
				email
		);
	}

	private static final String EMAIL_DO_USUARIO = "onias85@gmail.com";

	private static final Long CHAT_DO_SUPORTE = 751717896L;

	private static final String MENSAGEM_RECEBIDA_PELO_BOT = "{"
			+ "\"botName\": \"support\","
			+ "\"chatId\": 751717896,"
			+ "\"message\": \"/solveLoginTokenTicket unlockToken " + EMAIL_DO_USUARIO + "\","
			+ "\"message_id\": 363,"
			+ "\"sentAt\": 1790121741,"
			+ "\"updateId\": 840339225,"
			+ "\"userName\": \"OniasJr\""
			+ "}";

	/**
	 * O texto é o que está cadastrado em {@code JnEntityInstantMessengerTemplateMessage}, com o
	 * {@code {email}} resolvido. Só o token fica de fora: ele é sorteado a cada atendimento, e o teste o
	 * lê da própria mensagem para conferir contra o que foi enviado por email.
	 */
	private static final String AVISO_AO_SUPORTE_ATE_O_TOKEN = "Ao endereço " + EMAIL_DO_USUARIO
			+ ", envie a seguinte mensagem:\n\n\n"
			+ "Você solicitou o desbloqueio de seu token para (re) cadastro / desbloqueio de senha."
			+ " Atendendo ao seu pedido. O token que você deve informar no campo de token é ";

	@Before
	public void montarOCenario() {

		this.regravarOsTemplatesDeMensagem();

		CcpJsonRepresentation usuario = this.usuario();

		// gravar na gêmea apaga o registro da principal: é assim que o token fica bloqueado
		JnEntityLoginToken.ENTITY.getTwinEntity().save(usuario);

		this.abrirOTicketDeDesbloqueio(usuario);

		TELEGRAM.enviadas.clear();
		CAIXA_DE_EMAIL.enviados.clear();
	}

	/**
	 * Deixa o ticket aberto na entidade principal, gravando — gravar numa entidade gêmea escreve na
	 * principal e apaga da gêmea, então a gravação resolve tanto o caso de não haver ticket nenhum
	 * quanto o de haver um atendido numa execução anterior.
	 *
	 * <p>Gravar é o que avisa o suporte, e por isso o registro daquele aviso precisa sair da frente: o
	 * texto é fixo, então ele seria recusado como repetição do aviso da execução anterior.
	 *
	 * <p>Uma versão anterior consultava o {@code exists} para decidir entre gravar e transferir da gêmea
	 * de volta. Não serve: a entidade é descartável por dia, e o {@code exists} de uma descartável
	 * responde pela cópia em {@code disposable_record}, que continua válida depois de o documento já ter
	 * mudado de balde. Na virada do dia ele dizia que o registro existia e a transferência não o
	 * encontrava.
	 */
	private void abrirOTicketDeDesbloqueio(CcpJsonRepresentation usuario) {

		this.apagarOAvisoDeTicketPendenteJaEnviado();

		JnEntityLoginTokenRequestUnlock.ENTITY.save(usuario);
	}

	@Test
	public void oTicketEhAtendido() {

		CcpJsonRepresentation mensagem = new CcpStringDecorator(MENSAGEM_RECEBIDA_PELO_BOT).json();

		JbBotType.support.getBot().execute(mensagem);

		CcpJsonRepresentation avisoAoSuporte = TELEGRAM.ultimaMensagemPara(CHAT_DO_SUPORTE);
		String textoDoAviso = avisoAoSuporte.getAsString(JnJsonCommonsFields.message);

		boolean avisoComecaComOTextoEsperado = textoDoAviso.startsWith(AVISO_AO_SUPORTE_ATE_O_TOKEN);
		assertTrue(textoDoAviso, avisoComecaComOTextoEsperado);

		int tamanhoDoTexto = AVISO_AO_SUPORTE_ATE_O_TOKEN.length();
		String token = textoDoAviso.substring(tamanhoDoTexto);

		// 4) o suporte recebe o aviso do ticket atendido, com o token resolvido
		assertEquals(AVISO_AO_SUPORTE_ATE_O_TOKEN + token, textoDoAviso);
		assertEquals("support", avisoAoSuporte.getAsString(JnJsonInstantMessengerFields.botName));

		// 1) o usuário recebe o mesmo token por email, pelo template JnNotifyUserAboutLoginToken
		CcpJsonRepresentation email = this.emailEnviadoAoUsuario();
		String corpoDoEmail = email.getAsString(JnJsonCommonsFields.message);
		List<String> destinatarios = email.getAsStringList(JnJsonCommonsFields.email);

		assertTrue(destinatarios.contains(EMAIL_DO_USUARIO));
		assertTrue(corpoDoEmail, corpoDoEmail.contains(token));

		CcpJsonRepresentation usuario = this.usuario();

		// 2) o token anterior saiu de login_token e da gêmea; o que está lá é o token novo
		CcpEntity tokenBloqueado = JnEntityLoginToken.ENTITY.getTwinEntity();
		assertFalse(tokenBloqueado.exists(usuario));
		assertTrue(JnEntityLoginToken.ENTITY.exists(usuario));

		// 3) o ticket saiu da entidade principal e está na gêmea
		CcpEntity ticketAtendido = JnEntityLoginTokenRequestUnlock.ENTITY.getTwinEntity();
		assertFalse(JnEntityLoginTokenRequestUnlock.ENTITY.exists(usuario));
		assertTrue(ticketAtendido.exists(usuario));
	}

	private CcpJsonRepresentation emailEnviadoAoUsuario() {

		String templateDoToken = JnMessages.JnNotifyUserAboutLoginToken.class.getName();

		for (CcpJsonRepresentation enviado : CAIXA_DE_EMAIL.enviados) {
			String templateId = enviado.getAsString(JnJsonCommonsFields.templateId);
			boolean ehOutroTemplate = false == templateId.equals(templateDoToken);
			if (ehOutroTemplate) {
				continue;
			}
			return enviado;
		}
		throw new AssertionError("Nenhum email foi enviado com o template " + templateDoToken);
	}

	private CcpJsonRepresentation usuario() {
		CcpJsonRepresentation usuario = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonCommonsFields.email, EMAIL_DO_USUARIO)
				.put(JnJsonInstantMessengerFields.chatId, CHAT_DO_SUPORTE);
		return usuario;
	}

	/**
	 * Regrava os registros iniciais da entidade dos templates para que o índice acompanhe o texto
	 * declarado em java. Sem isso o teste leria o texto que estiver no banco desde a última criação das
	 * tabelas, e passaria a medir o estado do ambiente em vez do código.
	 *
	 * <p>Os registros iniciais vêm como itens de bulk, mas são gravados um a um pela entidade: a operação
	 * de bulk declarada neles é a de criação, que não sobrepõe o registro que já está lá.
	 */
	private void regravarOsTemplatesDeMensagem() {
		JnEntityInstantMessengerTemplateMessage configurador = new JnEntityInstantMessengerTemplateMessage();
		List<CcpBulkItem> templates = configurador.getFirstRecordsToInsert();
		for (CcpBulkItem template : templates) {
			template.entity.save(template.json);
		}
	}

	/**
	 * O aviso de ticket pendente tem texto fixo, então o registro de mensagem já enviada tem sempre a
	 * mesma chave. Sem apagá-lo, a abertura do ticket seria recusada como repetição do aviso anterior, e
	 * a recusa interrompe a gravação do ticket.
	 *
	 * <p>O chatId entra como {@code double} porque é assim que ele volta do banco no envio que gravou o
	 * registro, e a chave primária é calculada sobre o texto dos valores: um {@code long} produziria
	 * {@code 751717896} onde o registro gravado tem {@code 7.51717896E8}, e a chave não bateria.
	 */
	private void apagarOAvisoDeTicketPendenteJaEnviado() {

		String textoDoAviso = "/" + JbSupportBotCommands.solveLoginTokenTicket + " "
				+ JbSupportLoginTokenTypes.unlockToken + " " + EMAIL_DO_USUARIO + " ";

		double chatIdComoOBancoDevolve = CHAT_DO_SUPORTE.doubleValue();

		CcpJsonRepresentation aviso = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonInstantMessengerFields.botName, JbBotType.support)
				.put(JnJsonInstantMessengerFields.chatId, chatIdComoOBancoDevolve)
				.put(JnJsonInstantMessengerFields.instantMessageType, JnInstantMessageType.text)
				.put(JnJsonCommonsFields.message, textoDoAviso);

		JnEntityInstantMessengerMessageSent.ENTITY.delete(aviso);
	}
}
