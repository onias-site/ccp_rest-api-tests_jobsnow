package com.jb.instant.messenger.reader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.dependency.injection.CcpInstanceProvider;
import com.ccp.especifications.db.bulk.CcpErrorBulkEntityRecordNotFound;
import com.ccp.especifications.db.crud.CcpCrud;
import com.ccp.especifications.db.crud.CcpUnionAllExecutor;
import com.ccp.especifications.http.CcpHttpBodyBinary;
import com.ccp.especifications.http.CcpHttpBodyText;
import com.ccp.especifications.http.CcpHttpMethods;
import com.ccp.especifications.http.CcpHttpRequester;
import com.ccp.especifications.http.CcpHttpResponse;
import com.ccp.especifications.http.CcpHttpTooManyRequests;
import com.ccp.especifications.instant.messenger.CcpErrorInstantMessageThisBotWasBlockedByThisUser;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.jb.instant.messenger.reader.JbInstantMessengerMessageReader.JbErrorUnableToReadInstantMessages;
import com.jb.instant.messenger.reader.JbInstantMessengerMessageReader.JsonFieldNames;
import com.jn.business.messages.JnBusinessSendInstantMessage.JnBotType;

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

		List<CcpJsonRepresentation> mensagens = JbInstantMessengerMessageReader.INSTANCE.readMessages(JnBotType.support, 0L, 0);

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

		List<CcpJsonRepresentation> mensagens = JbInstantMessengerMessageReader.INSTANCE.readMessages(JnBotType.support, 0L, 0);

		boolean edicaoFoiDevolvida = mensagens.stream()
				.anyMatch(x -> 101L == x.getAsLongNumber(JsonFieldNames.updateId).longValue());

		assertFalse(edicaoFoiDevolvida);
	}

	@Test
	public void semMensagensParaLerTest() {

		this.telegramRespondendo(200, NENHUMA_MENSAGEM);

		List<CcpJsonRepresentation> mensagens = JbInstantMessengerMessageReader.INSTANCE.readMessages(JnBotType.support, 0L, 0);

		assertTrue(mensagens.isEmpty());
	}

	@Test
	public void offsetAvancaParaNaoRelerAsMesmasMensagensTest() {

		FakeHttpRequester telegram = this.telegramRespondendo(200, DUAS_MENSAGENS_E_UMA_EDICAO, NENHUMA_MENSAGEM);

		JbInstantMessengerMessageReader.INSTANCE.saveOffset(JnBotType.support, 0L);

		JbInstantMessengerMessageReader.INSTANCE.readNewMessages(JnBotType.support);

		List<CcpJsonRepresentation> segundaLeitura = JbInstantMessengerMessageReader.INSTANCE.readNewMessages(JnBotType.support);

		CcpJsonRepresentation segundaRequisicao = new CcpStringDecorator(telegram.lastRequest).json();

		assertEquals(103L, segundaRequisicao.getAsLongNumber(JsonFieldNames.offset).longValue());
		assertTrue(segundaLeitura.isEmpty());
	}

	// ── offset gravado na entidade JbEntityBotUpdateId ────────────────────────

	@Test
	public void offsetSalvoEhRecuperadoTest() {

		JbInstantMessengerMessageReader.INSTANCE.saveOffset(JnBotType.support, 500L);

		Long offset = JbInstantMessengerMessageReader.INSTANCE.getOffset(JnBotType.support);

		assertEquals(500L, offset.longValue());
	}

	@Test
	public void offsetIncrementadoEhGravadoAoLerMensagensNovasTest() {

		this.telegramRespondendo(200, DUAS_MENSAGENS_E_UMA_EDICAO);

		JbInstantMessengerMessageReader.INSTANCE.saveOffset(JnBotType.support, 0L);

		JbInstantMessengerMessageReader.INSTANCE.readNewMessages(JnBotType.support);

		Long offset = JbInstantMessengerMessageReader.INSTANCE.getOffset(JnBotType.support);

		assertEquals(103L, offset.longValue());
	}

	@Test
	public void offsetNaoEhGravadoQuandoNaoHaMensagensNovasTest() {

		this.telegramRespondendo(200, NENHUMA_MENSAGEM);

		JbInstantMessengerMessageReader.INSTANCE.saveOffset(JnBotType.support, 777L);

		JbInstantMessengerMessageReader.INSTANCE.readNewMessages(JnBotType.support);

		Long offset = JbInstantMessengerMessageReader.INSTANCE.getOffset(JnBotType.support);

		assertEquals(777L, offset.longValue());
	}

	@Test
	public void cadaBotTemSeuProprioOffsetTest() {

		JbInstantMessengerMessageReader.INSTANCE.saveOffset(JnBotType.support, 111L);
		JbInstantMessengerMessageReader.INSTANCE.saveOffset(JnBotType.user, 222L);

		Long offsetDoSuporte = JbInstantMessengerMessageReader.INSTANCE.getOffset(JnBotType.support);
		Long offsetDoUsuario = JbInstantMessengerMessageReader.INSTANCE.getOffset(JnBotType.user);

		assertEquals(111L, offsetDoSuporte.longValue());
		assertEquals(222L, offsetDoUsuario.longValue());
	}

	@Test
	public void tokenDoBotDeSuporteTest() {

		String botToken = JbInstantMessengerMessageReader.INSTANCE.getBotToken(JnBotType.support);

		assertFalse(botToken.trim().isEmpty());
	}

	// ── tratamento dos status devolvidos pela api ─────────────────────────────

	@Test(expected = JbErrorUnableToReadInstantMessages.class)
	public void respostaNaoOkTest() {
		this.telegramRespondendo(200, RESPOSTA_NAO_OK);
		JbInstantMessengerMessageReader.INSTANCE.readMessages(JnBotType.support, 0L, 0);
	}

	@Test(expected = CcpErrorInstantMessageThisBotWasBlockedByThisUser.class)
	public void botBloqueadoPeloUsuarioTest() {
		this.telegramRespondendo(403, BOT_BLOQUEADO);
		JbInstantMessengerMessageReader.INSTANCE.readMessages(JnBotType.support, 0L, 0);
	}

	@Test(expected = CcpHttpTooManyRequests.class)
	public void excessoDeRequisicoesTest() {
		this.telegramRespondendo(429, EXCESSO_DE_REQUISICOES);
		JbInstantMessengerMessageReader.INSTANCE.readMessages(JnBotType.support, 0L, 0);
	}

	// ── null-parameter tests (AOP) ────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void getUpdatesBotTypeNullTest() {
		JbInstantMessengerMessageReader.INSTANCE.getUpdates(null, 0L, 0);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getUpdatesOffsetNullTest() {
		JbInstantMessengerMessageReader.INSTANCE.getUpdates(JnBotType.support, null, 0);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getUpdatesTimeoutNullTest() {
		JbInstantMessengerMessageReader.INSTANCE.getUpdates(JnBotType.support, 0L, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void readMessagesBotTypeNullTest() {
		JbInstantMessengerMessageReader.INSTANCE.readMessages(null, 0L, 0);
	}

	@Test(expected = CcpNullParameterException.class)
	public void readMessagesOffsetNullTest() {
		JbInstantMessengerMessageReader.INSTANCE.readMessages(JnBotType.support, null, 0);
	}

	@Test(expected = CcpNullParameterException.class)
	public void readMessagesTimeoutNullTest() {
		JbInstantMessengerMessageReader.INSTANCE.readMessages(JnBotType.support, 0L, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void readNewMessagesBotTypeNullTest() {
		JbInstantMessengerMessageReader.INSTANCE.readNewMessages(null, 0);
	}

	@Test(expected = CcpNullParameterException.class)
	public void readNewMessagesTimeoutNullTest() {
		JbInstantMessengerMessageReader.INSTANCE.readNewMessages(JnBotType.support, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getOffsetBotTypeNullTest() {
		JbInstantMessengerMessageReader.INSTANCE.getOffset(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void saveOffsetBotTypeNullTest() {
		JbInstantMessengerMessageReader.INSTANCE.saveOffset(null, 0L);
	}

	@Test(expected = CcpNullParameterException.class)
	public void saveOffsetNullTest() {
		JbInstantMessengerMessageReader.INSTANCE.saveOffset(JnBotType.support, null);
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

	// ── banco de dados substituído ────────────────────────────────────────────

	private static class FakeCrud implements CcpCrud {

		private static final Map<String, CcpJsonRepresentation> registros = new HashMap<>();

		public CcpJsonRepresentation getOneById(String entityName, String id) {

			String key = this.getKey(entityName, id);

			boolean registroNaoEncontrado = false == registros.containsKey(key);

			if (registroNaoEncontrado) {
				throw new CcpErrorBulkEntityRecordNotFound(entityName, id);
			}

			CcpJsonRepresentation registro = registros.get(key);
			return registro;
		}

		public CcpJsonRepresentation save(String entityName, CcpJsonRepresentation json, String id) {
			registros.put(this.getKey(entityName, id), json);
			return json;
		}

		public boolean exists(String entityName, String id) {
			boolean exists = registros.containsKey(this.getKey(entityName, id));
			return exists;
		}

		public boolean delete(String entityName, String id) {
			CcpJsonRepresentation removido = registros.remove(this.getKey(entityName, id));
			boolean deleted = removido != null;
			return deleted;
		}

		public CcpUnionAllExecutor getUnionAllExecutor() {
			throw new UnsupportedOperationException();
		}

		private String getKey(String entityName, String id) {
			String key = entityName + "." + id;
			return key;
		}
	}
}
