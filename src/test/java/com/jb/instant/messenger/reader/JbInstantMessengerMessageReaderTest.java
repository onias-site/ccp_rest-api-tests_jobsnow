package com.jb.instant.messenger.reader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.dependency.injection.CcpInstanceProvider;
import com.ccp.especifications.http.CcpHttpBodyBinary;
import com.ccp.especifications.http.CcpHttpBodyText;
import com.ccp.especifications.http.CcpHttpMethods;
import com.ccp.especifications.http.CcpHttpRequester;
import com.ccp.especifications.http.CcpHttpResponse;
import com.ccp.especifications.http.CcpHttpTooManyRequests;
import com.ccp.especifications.instant.messenger.CcpErrorInstantMessageThisBotWasBlockedByThisUser;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.jb.instant.messenger.reader.JbInstantMessengerMessageReader.JbErrorUnableToReadInstantMessages;
import com.jb.instant.messenger.reader.JbInstantMessengerMessageReader.JsonFieldNames;

/**
 * Testa a leitura das mensagens recebidas pelo bot de suporte. A api do Telegram é substituída por
 * um {@code CcpHttpRequester} falso injetado no {@code CcpDependencyInjection}, de forma que os
 * testes exercitem a interpretação do {@code getUpdates} e o mapeamento de status do
 * {@code CcpHttpHandler} sem depender da rede.
 */
public class JbInstantMessengerMessageReaderTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

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

		List<CcpJsonRepresentation> mensagens = JbInstantMessengerMessageReader.INSTANCE.readMessages(0L, 0);

		assertEquals(2, mensagens.size());

		CcpJsonRepresentation primeira = mensagens.get(0);

		assertEquals("support", primeira.getAsString(JsonFieldNames.botName));
		assertEquals("/solveLoginTokenTicket", primeira.getAsString(JsonFieldNames.typedValue));
		assertEquals("onias", primeira.getAsString(JsonFieldNames.userName));
		assertEquals(55L, primeira.getAsLongNumber(JsonFieldNames.chatId).longValue());
		assertEquals(11L, primeira.getAsLongNumber(JsonFieldNames.message_id).longValue());
		assertEquals(100L, primeira.getAsLongNumber(JsonFieldNames.updateId).longValue());
		assertEquals(1700000000L, primeira.getAsLongNumber(JsonFieldNames.sentAt).longValue());

		CcpJsonRepresentation segunda = mensagens.get(1);

		assertEquals("bom dia", segunda.getAsString(JsonFieldNames.typedValue));
		assertEquals(66L, segunda.getAsLongNumber(JsonFieldNames.chatId).longValue());
		assertEquals(102L, segunda.getAsLongNumber(JsonFieldNames.updateId).longValue());
	}

	@Test
	public void atualizacaoSemMensagemEhIgnoradaTest() {

		this.telegramRespondendo(200, DUAS_MENSAGENS_E_UMA_EDICAO);

		List<CcpJsonRepresentation> mensagens = JbInstantMessengerMessageReader.INSTANCE.readMessages(0L, 0);

		boolean edicaoFoiDevolvida = mensagens.stream()
				.anyMatch(x -> 101L == x.getAsLongNumber(JsonFieldNames.updateId).longValue());

		assertFalse(edicaoFoiDevolvida);
	}

	@Test
	public void semMensagensParaLerTest() {

		this.telegramRespondendo(200, NENHUMA_MENSAGEM);

		List<CcpJsonRepresentation> mensagens = JbInstantMessengerMessageReader.INSTANCE.readMessages(0L, 0);

		assertTrue(mensagens.isEmpty());
	}

	@Test
	public void offsetAvancaParaNaoRelerAsMesmasMensagensTest() {

		FakeHttpRequester telegram = this.telegramRespondendo(200, DUAS_MENSAGENS_E_UMA_EDICAO, NENHUMA_MENSAGEM);

		JbInstantMessengerMessageReader.INSTANCE.readNewMessages();

		List<CcpJsonRepresentation> segundaLeitura = JbInstantMessengerMessageReader.INSTANCE.readNewMessages();

		CcpJsonRepresentation segundaRequisicao = new CcpStringDecorator(telegram.lastRequest).json();

		assertEquals(103L, segundaRequisicao.getAsLongNumber(JsonFieldNames.offset).longValue());
		assertTrue(segundaLeitura.isEmpty());
	}

	@Test
	public void tokenDoBotDeSuporteTest() {

		String botToken = JbInstantMessengerMessageReader.INSTANCE.getSupportBotToken();

		assertFalse(botToken.trim().isEmpty());
	}

	// ── tratamento dos status devolvidos pela api ─────────────────────────────

	@Test(expected = JbErrorUnableToReadInstantMessages.class)
	public void respostaNaoOkTest() {
		this.telegramRespondendo(200, RESPOSTA_NAO_OK);
		JbInstantMessengerMessageReader.INSTANCE.readMessages(0L, 0);
	}

	@Test(expected = CcpErrorInstantMessageThisBotWasBlockedByThisUser.class)
	public void botBloqueadoPeloUsuarioTest() {
		this.telegramRespondendo(403, BOT_BLOQUEADO);
		JbInstantMessengerMessageReader.INSTANCE.readMessages(0L, 0);
	}

	@Test(expected = CcpHttpTooManyRequests.class)
	public void excessoDeRequisicoesTest() {
		this.telegramRespondendo(429, EXCESSO_DE_REQUISICOES);
		JbInstantMessengerMessageReader.INSTANCE.readMessages(0L, 0);
	}

	// ── null-parameter tests (AOP) ────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void getUpdatesOffsetNullTest() {
		JbInstantMessengerMessageReader.INSTANCE.getUpdates(null, 0);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getUpdatesTimeoutNullTest() {
		JbInstantMessengerMessageReader.INSTANCE.getUpdates(0L, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void readMessagesOffsetNullTest() {
		JbInstantMessengerMessageReader.INSTANCE.readMessages(null, 0);
	}

	@Test(expected = CcpNullParameterException.class)
	public void readMessagesTimeoutNullTest() {
		JbInstantMessengerMessageReader.INSTANCE.readMessages(0L, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void readNewMessagesTimeoutNullTest() {
		JbInstantMessengerMessageReader.INSTANCE.readNewMessages(null);
	}

	// ── api do Telegram substituída ───────────────────────────────────────────

	private FakeHttpRequester telegramRespondendo(int httpStatus, String... respostas) {
		FakeHttpRequester telegram = new FakeHttpRequester(httpStatus, respostas);
		CcpInstanceProvider<CcpHttpRequester> provider = () -> telegram;
		CcpDependencyInjection.loadAllDependencies(provider);
		return telegram;
	}

	private static class FakeHttpRequester implements CcpHttpRequester {

		private final int httpStatus;
		private final String[] respostas;
		private int chamadas = 0;
		private String lastRequest = "";

		private FakeHttpRequester(int httpStatus, String... respostas) {
			this.httpStatus = httpStatus;
			this.respostas = respostas;
		}

		public CcpHttpResponse executeHttpRequest(String url, CcpHttpMethods method, CcpJsonRepresentation headers, String body) {

			this.lastRequest = body;

			int ultima = this.respostas.length - 1;
			int indice = this.chamadas > ultima ? ultima : this.chamadas;

			this.chamadas++;

			CcpHttpResponse response = new CcpHttpResponse(this.respostas[indice], this.httpStatus, "");
			return response;
		}

		public CcpHttpResponse executeMultiPartHttpRequest(String url, CcpHttpMethods method, CcpJsonRepresentation headers, List<CcpHttpBodyText> bodyTexts, List<CcpHttpBodyBinary> bodyBinaries) {
			CcpHttpResponse response = this.executeHttpRequest(url, method, headers, "");
			return response;
		}
	}
}
