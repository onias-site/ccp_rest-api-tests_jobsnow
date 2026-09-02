package com.jb.instant.messenger.reader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.dependency.injection.CcpInstanceProvider;
import com.ccp.especifications.db.crud.CcpCrud;
import com.ccp.especifications.http.CcpHttpRequester;
import com.ccp.especifications.http.CcpHttpTooManyRequests;
import com.ccp.especifications.instant.messenger.CcpErrorInstantMessageThisBotWasBlockedByThisUser;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.jb.instant.messenger.reader.JbInstantMessengerMessageReader.JsonFieldNames;
import com.jn.business.messages.JnBusinessSendInstantMessage.JnBotType;
import org.junit.Test;

/**
 * Testa a leitura das mensagens recebidas pelo bot de suporte. A api do Telegram é substituída por
 * um {@code CcpHttpRequester} falso injetado no {@code CcpDependencyInjection}, de forma que os
 * testes exercitem a interpretação do {@code getUpdates} e o mapeamento de status do
 * {@code CcpHttpHandler} sem depender da rede. O offset, que é gravado na entidade
 * {@code JbEntityBotUpdateId}, é guardado por um {@code CcpCrud} em memória.
 */
public class JbInstantMessengerMessageReaderTest {

	static {
		CcpInstanceProvider<CcpCrud> bancoEmMemoria = () -> new FakeCrud();
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler(), CcpLocalCacheInstances.map, bancoEmMemoria);
	}

	/**
	 * O leitor identifica o bot pelo nome, e não pelo enum, por isso os testes convertem o
	 * {@link JnBotType} uma única vez aqui, mantendo o enum como fonte da verdade.
	 */
	private static final String SUPORTE = JnBotType.support.name();

	private static final String USUARIO = JnBotType.user.name();

	private static final String DUAS_MENSAGENS_E_UMA_EDICAO = "{\"ok\":true,\"result\":["
			+ "{\"update_id\":100,\"message\":{\"message_id\":11,\"date\":1700000000,\"from\":{\"id\":55,\"username\":\"onias\"},\"chat\":{\"id\":55},\"text\":\"/solveLoginTokenTicket\"}},"
			+ "{\"update_id\":101,\"edited_message\":{\"message_id\":11,\"chat\":{\"id\":55},\"text\":\"texto editado\"}},"
			+ "{\"update_id\":102,\"message\":{\"message_id\":12,\"date\":1700000060,\"from\":{\"id\":66,\"username\":\"maria\"},\"chat\":{\"id\":66},\"text\":\"bom dia\"}}"
			+ "]}";

	private static final String NENHUMA_MENSAGEM = "{\"ok\":true,\"result\":[]}";

	private static final String RESPOSTA_NAO_OK = "{\"ok\":false,\"error_code\":401,\"description\":\"Unauthorized\"}";

	private static final String BOT_BLOQUEADO = "{\"ok\":false,\"error_code\":403,\"description\":\"Forbidden: bot was blocked by the user\"}";

	private static final String EXCESSO_DE_REQUISICOES = "{\"ok\":false,\"error_code\":429,\"description\":\"Too Many Requests\"}";

	// ── leitura das mensagens do bot de suporte ───────────────────────────────

	@Test
	public void lerMensagensDoBotDeSuporteTest() {

		this.telegramRespondendo(200, DUAS_MENSAGENS_E_UMA_EDICAO);

		List<CcpJsonRepresentation> mensagens = JbInstantMessengerMessageReader.INSTANCE.readMessages(SUPORTE, 0L, 0);

		assertEquals(2, mensagens.size());

		CcpJsonRepresentation primeira = mensagens.get(0);

		assertEquals("support", primeira.getAsString(JsonFieldNames.botName));
		// o texto sai do leitor no campo `message`; quem o renomeia para `typedValue` é o
		// JbBotEngine.Bot, já dentro do fluxo de atendimento do bot
		assertEquals("/solveLoginTokenTicket", primeira.getAsString(JsonFieldNames.message));
		assertEquals("onias", primeira.getAsString(JsonFieldNames.userName));
		assertEquals(55L, primeira.getAsLongNumber(JsonFieldNames.chatId).longValue());
		assertEquals(11L, primeira.getAsLongNumber(JsonFieldNames.message_id).longValue());
		assertEquals(100L, primeira.getAsLongNumber(JsonFieldNames.updateId).longValue());
		assertEquals(1700000000L, primeira.getAsLongNumber(JsonFieldNames.sentAt).longValue());

		CcpJsonRepresentation segunda = mensagens.get(1);

		assertEquals("bom dia", segunda.getAsString(JsonFieldNames.message));
		assertEquals(66L, segunda.getAsLongNumber(JsonFieldNames.chatId).longValue());
		assertEquals(102L, segunda.getAsLongNumber(JsonFieldNames.updateId).longValue());
	}

	@Test
	public void atualizacaoSemMensagemEhIgnoradaTest() {

		this.telegramRespondendo(200, DUAS_MENSAGENS_E_UMA_EDICAO);

		List<CcpJsonRepresentation> mensagens = JbInstantMessengerMessageReader.INSTANCE.readMessages(SUPORTE, 0L, 0);

		boolean edicaoFoiDevolvida = mensagens.stream()
				.anyMatch(x -> 101L == x.getAsLongNumber(JsonFieldNames.updateId).longValue());

		assertFalse(edicaoFoiDevolvida);
	}

	@Test
	public void semMensagensParaLerTest() {

		this.telegramRespondendo(200, NENHUMA_MENSAGEM);

		List<CcpJsonRepresentation> mensagens = JbInstantMessengerMessageReader.INSTANCE.readMessages(SUPORTE, 0L, 0);

		assertTrue(mensagens.isEmpty());
	}

	@Test
	public void offsetAvancaParaNaoRelerAsMesmasMensagensTest() {

		FakeHttpRequester telegram = this.telegramRespondendo(200, DUAS_MENSAGENS_E_UMA_EDICAO, NENHUMA_MENSAGEM);

		this.salvarOffset(SUPORTE, 0L);

		BotFalso primeiraLeitura = this.lerMensagensNovas(JnBotType.support);

		BotFalso segundaLeitura = this.lerMensagensNovas(JnBotType.support);

		CcpJsonRepresentation segundaRequisicao = new CcpStringDecorator(telegram.lastRequest).json();

		assertEquals(2, primeiraLeitura.recebidas.size());
		assertEquals(103L, segundaRequisicao.getAsLongNumber(JsonFieldNames.offset).longValue());
		assertTrue(segundaLeitura.recebidas.isEmpty());
	}

	// ── offset gravado na entidade JbEntityBotUpdateId ────────────────────────

	@Test
	public void offsetSalvoEhRecuperadoTest() {

		this.salvarOffset(SUPORTE, 500L);

		Long offset = JbInstantMessengerMessageReader.INSTANCE.getOffset(SUPORTE);

		assertEquals(500L, offset.longValue());
	}

	@Test
	public void offsetIncrementadoEhGravadoAoLerMensagensNovasTest() {

		this.telegramRespondendo(200, DUAS_MENSAGENS_E_UMA_EDICAO);

		this.salvarOffset(SUPORTE, 0L);

		this.lerMensagensNovas(JnBotType.support);

		Long offset = JbInstantMessengerMessageReader.INSTANCE.getOffset(SUPORTE);

		assertEquals(103L, offset.longValue());
	}

	@Test
	public void offsetNaoEhGravadoQuandoNaoHaMensagensNovasTest() {

		this.telegramRespondendo(200, NENHUMA_MENSAGEM);

		this.salvarOffset(SUPORTE, 777L);

		this.lerMensagensNovas(JnBotType.support);

		Long offset = JbInstantMessengerMessageReader.INSTANCE.getOffset(SUPORTE);

		assertEquals(777L, offset.longValue());
	}

	@Test
	public void cadaBotTemSeuProprioOffsetTest() {

		this.salvarOffset(SUPORTE, 111L);
		this.salvarOffset(USUARIO, 222L);

		Long offsetDoSuporte = JbInstantMessengerMessageReader.INSTANCE.getOffset(SUPORTE);
		Long offsetDoUsuario = JbInstantMessengerMessageReader.INSTANCE.getOffset(USUARIO);

		assertEquals(111L, offsetDoSuporte.longValue());
		assertEquals(222L, offsetDoUsuario.longValue());
	}

	@Test
	public void tokenDoBotDeSuporteTest() {

		String botToken = JbInstantMessengerMessageReader.INSTANCE.getBotToken(SUPORTE);

		assertFalse(botToken.trim().isEmpty());
	}

	// ── tratamento dos status devolvidos pela api ─────────────────────────────

	@Test(expected = JbErrorUnableToReadInstantMessages.class)
	public void respostaNaoOkTest() {
		this.telegramRespondendo(200, RESPOSTA_NAO_OK);
		JbInstantMessengerMessageReader.INSTANCE.readMessages(SUPORTE, 0L, 0);
	}

	@Test(expected = CcpErrorInstantMessageThisBotWasBlockedByThisUser.class)
	public void botBloqueadoPeloUsuarioTest() {
		this.telegramRespondendo(403, BOT_BLOQUEADO);
		JbInstantMessengerMessageReader.INSTANCE.readMessages(SUPORTE, 0L, 0);
	}

	@Test(expected = CcpHttpTooManyRequests.class)
	public void excessoDeRequisicoesTest() {
		this.telegramRespondendo(429, EXCESSO_DE_REQUISICOES);
		JbInstantMessengerMessageReader.INSTANCE.readMessages(SUPORTE, 0L, 0);
	}

	// ── null-parameter tests (AOP) ────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void getUpdatesBotTypeNullTest() {
		JbInstantMessengerMessageReader.INSTANCE.getUpdates(null, 0L, 0);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getUpdatesOffsetNullTest() {
		JbInstantMessengerMessageReader.INSTANCE.getUpdates(SUPORTE, null, 0);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getUpdatesTimeoutNullTest() {
		JbInstantMessengerMessageReader.INSTANCE.getUpdates(SUPORTE, 0L, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void readMessagesBotTypeNullTest() {
		JbInstantMessengerMessageReader.INSTANCE.readMessages(null, 0L, 0);
	}

	@Test(expected = CcpNullParameterException.class)
	public void readMessagesOffsetNullTest() {
		JbInstantMessengerMessageReader.INSTANCE.readMessages(SUPORTE, null, 0);
	}

	@Test(expected = CcpNullParameterException.class)
	public void readMessagesTimeoutNullTest() {
		JbInstantMessengerMessageReader.INSTANCE.readMessages(SUPORTE, 0L, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void readNewMessagesMessageReaderNullTest() {
		JbInstantMessengerMessageReader.INSTANCE.readNewMessages(0, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void readNewMessagesTimeoutNullTest() {
		JbInstantMessengerMessageReader.INSTANCE.readNewMessages(null, new BotFalso(JnBotType.support));
	}

	@Test(expected = CcpNullParameterException.class)
	public void getOffsetBotTypeNullTest() {
		JbInstantMessengerMessageReader.INSTANCE.getOffset(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void saveOffsetBotTypeNullTest() {
		JbInstantMessengerMessageReader.INSTANCE.saveOffset(null, 0L, new ArrayList<>());
	}

	@Test(expected = CcpNullParameterException.class)
	public void saveOffsetMessagesNullTest() {
		JbInstantMessengerMessageReader.INSTANCE.saveOffset(SUPORTE, 0L, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getBotTokenBotTypeNullTest() {
		JbInstantMessengerMessageReader.INSTANCE.getBotToken(null);
	}

	// ── api do Telegram substituída ───────────────────────────────────────────

	private FakeHttpRequester telegramRespondendo(int httpStatus, String... respostas) {
		FakeHttpRequester telegram = new FakeHttpRequester(httpStatus, respostas);
		CcpInstanceProvider<CcpHttpRequester> provider = () -> telegram;
		CcpDependencyInjection.loadAllDependencies(provider);
		return telegram;
	}

	/**
	 * Grava o offset do bot sem nenhuma mensagem lida. Como o {@code saveOffset} guarda o maior valor
	 * entre o offset informado e o da última mensagem da lista, uma lista vazia faz com que o valor
	 * informado seja gravado tal e qual.
	 */
	private void salvarOffset(String botType, long offset) {
		JbInstantMessengerMessageReader.INSTANCE.saveOffset(botType, offset, new ArrayList<>());
	}

	/**
	 * Lê as mensagens novas do bot de suporte devolvendo o dublê que as recebeu, já que o
	 * {@code readNewMessages} entrega cada mensagem ao {@code CcpBusiness} ao invés de devolvê-las.
	 */
	private BotFalso lerMensagensNovas(JnBotType botType) {
		BotFalso bot = new BotFalso(botType);
		JbInstantMessengerMessageReader.INSTANCE.readNewMessages(0, bot);
		return bot;
	}



	// ── banco de dados substituído ────────────────────────────────────────────


}
