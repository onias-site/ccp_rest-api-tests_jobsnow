package com.ccp.decorators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import org.junit.Test;

import com.ccp.constantes.CcpOtherConstants;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

import static com.ccp.decorators.JsonFieldNames.*;

public class CcpTextDecoratorTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	// ── completeLeft ──────────────────────────────────────────────────────────

	@Test
	public void completeLeftAdicionaZerosTest() {
		CcpTextDecorator t = new CcpStringDecorator("7").text();
		assertEquals("007", t.completeLeft('0', 3).content);
	}

	@Test
	public void completeLeftConteudoJaSuficientementeGrandeTest() {
		CcpTextDecorator t = new CcpStringDecorator("abcde").text();
		assertEquals("abcde", t.completeLeft('0', 3).content);
	}

	// ── stripAccents ──────────────────────────────────────────────────────────

	@Test
	public void stripAccentsRemoveAcentosTest() {
		CcpTextDecorator t = new CcpStringDecorator("ação café").text();
		String resultado = t.stripAccents().content;
		assertFalse(resultado.contains("ã"));
		assertFalse(resultado.contains("é"));
	}

	@Test
	public void stripAccentsMantemHashTest() {
		CcpTextDecorator t = new CcpStringDecorator("senha#123").text();
		assertTrue(t.stripAccents().content.contains("#"));
	}

	// ── getPieces ─────────────────────────────────────────────────────────────

	@Test
	public void getPiecesComDelimitadoresTest() {
		CcpTextDecorator t = new CcpStringDecorator("ola [mundo] e [java]").text();
		List<String> pieces = t.getPieces("[", "]");
		assertEquals(2, pieces.size());
		assertTrue(pieces.get(0).contains("mundo"));
		assertTrue(pieces.get(1).contains("java"));
	}

	@Test
	public void getPiecesComPredicadoTest() {
		CcpTextDecorator t = new CcpStringDecorator("um dois tres quatro").text();
		List<String> pieces = t.getPieces(s -> s.length() > 3, " ");
		assertTrue(pieces.contains("quatro"));
		assertFalse(pieces.contains("um"));
	}

	// ── replace ───────────────────────────────────────────────────────────────

	@Test
	public void replaceTest() {
		CcpTextDecorator t = new CcpStringDecorator("bom dia mundo").text();
		assertEquals("bom dia java", t.replace("mundo", "java").content);
	}

	// ── removePieces ──────────────────────────────────────────────────────────

	@Test
	public void removePiecesComDelimitadoresTest() {
		CcpTextDecorator t = new CcpStringDecorator("texto [remover] aqui").text();
		String resultado = t.removePieces("[", "]").content;
		assertFalse(resultado.contains("[remover]"));
	}

	// ── generateToken ─────────────────────────────────────────────────────────

	@Test
	public void generateTokenTamanhoCorretoTest() {
		CcpTextDecorator t = new CcpStringDecorator("abcdefghijklmnopqrstuvwxyz").text();
		CcpTextDecorator token = t.generateToken(10);
		assertEquals(10, token.content.length());
	}

	@Test
	public void generateTokenUsaApenasCaracteresDoAlfabetoTest() {
		String alfabeto = "abcdef";
		CcpTextDecorator token = new CcpStringDecorator(alfabeto).text().generateToken(20);
		for (char c : token.content.toCharArray()) {
			assertTrue(alfabeto.indexOf(c) >= 0);
		}
	}

	// ── resolveTemplate ───────────────────────────────────────────────────────

	@Test
	public void resolveTemplateSubstituiCamposTest() {
		CcpJsonRepresentation params = CcpOtherConstants.EMPTY_JSON.put(nome, "Onias");
		CcpTextDecorator template = new CcpStringDecorator("Olá, {nome}!").text();
		String resultado = template.resolveTemplate(params).content;
		assertEquals("Olá, Onias!", resultado);
	}

	@Test
	public void resolveTemplateMultiplosCamposTest() {
		CcpJsonRepresentation params = CcpOtherConstants.EMPTY_JSON
				.put(nome, "João")
				.put(cidade, "Santos");
		CcpTextDecorator template = new CcpStringDecorator("{nome} mora em {cidade}").text();
		String resultado = template.resolveTemplate(params).content;
		assertEquals("João mora em Santos", resultado);
	}

	// ── removeStartingCharacters / removeEndingCharacters ─────────────────────

	@Test
	public void removeStartingCharactersTest() {
		CcpTextDecorator t = new CcpStringDecorator("///caminho").text();
		assertEquals("caminho", t.removeStartingCharacters('/').content);
	}

	@Test
	public void removeEndingCharactersTest() {
		CcpTextDecorator t = new CcpStringDecorator("caminho///").text();
		assertEquals("caminho", t.removeEndingCharacters('/').content);
	}

	@Test
	public void removeStartingCharactersSemPrefixoNaoMudaTest() {
		CcpTextDecorator t = new CcpStringDecorator("caminho").text();
		assertEquals("caminho", t.removeStartingCharacters('/').content);
	}

	// ── isValidSingleJson ─────────────────────────────────────────────────────

	@Test
	public void isValidSingleJsonTrueTest() {
		assertTrue(new CcpStringDecorator("{'a':1}").text().isValidSingleJson());
	}

	@Test
	public void isValidSingleJsonFalseTest() {
		assertFalse(new CcpStringDecorator("nao sou json").text().isValidSingleJson());
	}

	// ── asBase64 / getByteArrayFromBase64String ───────────────────────────────

	@Test
	public void asBase64RoundtripTest() {
		String original = "jobsnow";
		String base64 = new CcpStringDecorator(original).text().asBase64().content;
		byte[] decoded = new CcpStringDecorator(base64).text().getByteArrayFromBase64String();
		assertEquals(original, new String(decoded));
	}

	// ── capitalize ────────────────────────────────────────────────────────────

	@Test
	public void capitalizeTest() {
		assertEquals("Onias", new CcpStringDecorator("onias").text().capitalize().content);
	}

	@Test
	public void capitalizeStringVaziaTest() {
		assertEquals("", new CcpStringDecorator("").text().capitalize().content);
	}

	// ── toCamelCase / toSnakeCase ─────────────────────────────────────────────

	@Test
	public void toCamelCaseTest() {
		assertEquals("NomeDoCampo", new CcpStringDecorator("nome_do_campo").text().toCamelCase().content);
	}

	@Test
	public void toSnakeCaseTest() {
		String resultado = new CcpStringDecorator("NomeDoCampo").text().toSnakeCase().content;
		assertEquals("nome_do_campo", resultado);
	}

	// ── lenght ────────────────────────────────────────────────────────────────

	@Test
	public void lenghtTest() {
		CcpNumberDecorator tamanho = new CcpStringDecorator("abcde").text().lenght();
		assertTrue(tamanho.equalsTo(5d));
	}

	// ── regexMatches ──────────────────────────────────────────────────────────

	@Test
	public void regexMatchesTrueTest() {
		assertTrue(new CcpStringDecorator("abc123").text().regexMatches("[a-z]+\\d+"));
	}

	@Test
	public void regexMatchesFalseTest() {
		assertFalse(new CcpStringDecorator("abcdef").text().regexMatches("^\\d+$"));
	}

	// ── contains ─────────────────────────────────────────────────────────────

	@Test
	public void containsTrueTest() {
		assertTrue(new CcpStringDecorator("desenvolvedor java senior").text().contains("java"));
	}

	@Test
	public void containsFalseTest() {
		assertFalse(new CcpStringDecorator("desenvolvedor java senior").text().contains("python"));
	}

	// ── toString ─────────────────────────────────────────────────────────────

	@Test
	public void toStringTest() {
		assertEquals("valor", new CcpStringDecorator("valor").text().toString());
	}

	// ── getContent ────────────────────────────────────────────────────────────

	@Test
	public void getContentTest() {
		assertEquals("conteudo", new CcpStringDecorator("conteudo").text().getContent());
	}

	// ── sanitize ──────────────────────────────────────────────────────────────

	@Test
	public void sanitizeRemoveAcentosEMaiusculizaTest() {
		CcpTextDecorator resultado = new CcpStringDecorator("Olá João").text().sanitize();
		assertFalse(resultado.content.contains("á"));
		assertFalse(resultado.content.contains("ã"));
		assertEquals(resultado.content, resultado.content.toUpperCase());
	}

	@Test
	public void sanitizeComDelimitadoresCustomTest() {
		String[] delimiters = {",", ";"};
		CcpTextDecorator resultado = new CcpStringDecorator("java,python;ruby").text().sanitize(delimiters);
		assertFalse(resultado.content.contains(","));
		assertFalse(resultado.content.contains(";"));
		assertTrue(resultado.content.contains("PYTHON"));
	}

	// ── contains(String[], String) ────────────────────────────────────────────

	@Test
	public void containsComDelimitadoresCustomTrueTest() {
		String[] delimiters = {","};
		assertTrue(new CcpStringDecorator("java,python,ruby").text().contains(delimiters, "python"));
	}

	@Test
	public void containsComDelimitadoresCustomFalseTest() {
		String[] delimiters = {","};
		assertFalse(new CcpStringDecorator("java,python,ruby").text().contains(delimiters, "go"));
	}

	// ── removePieces(Predicate, String) ──────────────────────────────────────

	@Test
	public void removePiecesComPredicadoTest() {
		CcpTextDecorator resultado = new CcpStringDecorator("um dois tres quatro").text()
				.removePieces(s -> s.length() > 3, " ");
		assertFalse(resultado.content.contains("quatro"));
		assertFalse(resultado.content.contains("tres"));
		assertFalse(resultado.content.contains("dois"));
	}

	// ── getByteArrayInputStream ───────────────────────────────────────────────

	@Test
	public void getByteArrayInputStreamTest() throws Exception {
		String original = "texto de teste";
		String base64 = new CcpStringDecorator(original).text().asBase64().content;
		InputStream is = new CcpStringDecorator(base64).text().getByteArrayInputStream();
		assertNotNull(is);
		byte[] bytes = is.readAllBytes();
		assertEquals(original, new String(bytes));
	}

	// ── getParameterAsByteArrayInputStream ───────────────────────────────────

	@Test
	public void getParameterAsByteArrayInputStreamTest() throws Exception {
		String original = "parametro";
		String base64 = new CcpStringDecorator(original).text().asBase64().content;
		ByteArrayInputStream bais = new CcpStringDecorator(base64).text().getParameterAsByteArrayInputStream();
		assertNotNull(bais);
		byte[] bytes = bais.readAllBytes();
		assertEquals(original, new String(bytes));
	}

	// ── resolveTemplate com CcpTemplateFunctions ──────────────────────────────

	@Test
	public void resolveTemplateComCurrentTimeMillisTest() {
		CcpTextDecorator template = new CcpStringDecorator("ts={currentTimeMillis()}").text();
		String resultado = template.resolveTemplate(CcpOtherConstants.EMPTY_JSON).content;
		assertFalse(resultado.contains("{currentTimeMillis()}"));
		assertTrue(resultado.matches("ts=\\d+"));
	}
}
