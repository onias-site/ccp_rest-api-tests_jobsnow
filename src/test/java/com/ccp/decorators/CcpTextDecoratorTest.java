package com.ccp.decorators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Predicate;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.aop.CcpNullReturnException;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

import static com.ccp.decorators.JsonFieldNames.*;

public class CcpTextDecoratorTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	// ── completeLeft ──────────────────────────────────────────────────────────

	@Test
	public void completeLeftAddsZerosTest() {
		CcpTextDecorator t = new CcpStringDecorator("7").text();
		assertEquals("007", t.completeLeft('0', 3).content);
	}

	@Test
	public void completeLeftContentAlreadyLongEnoughTest() {
		CcpTextDecorator t = new CcpStringDecorator("abcde").text();
		assertEquals("abcde", t.completeLeft('0', 3).content);
	}

	// ── stripAccents ──────────────────────────────────────────────────────────

	@Test
	public void stripAccentsRemovesAccentsTest() {
		CcpTextDecorator t = new CcpStringDecorator("ação café").text();
		String result = t.stripAccents().content;
		assertFalse(result.contains("ã"));
		assertFalse(result.contains("é"));
	}

	@Test
	public void stripAccentsKeepsHashTest() {
		CcpTextDecorator t = new CcpStringDecorator("senha#123").text();
		assertTrue(t.stripAccents().content.contains("#"));
	}

	// ── getPieces ─────────────────────────────────────────────────────────────

	@Test
	public void getPiecesWithDelimitersTest() {
		CcpTextDecorator t = new CcpStringDecorator("ola [mundo] e [java]").text();
		List<String> pieces = t.getPieces("[", "]");
		assertEquals(2, pieces.size());
		assertTrue(pieces.get(0).contains("mundo"));
		assertTrue(pieces.get(1).contains("java"));
	}

	@Test
	public void getPiecesWithPredicateTest() {
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
	public void removePiecesWithDelimitersTest() {
		CcpTextDecorator t = new CcpStringDecorator("texto [remover] aqui").text();
		String result = t.removePieces("[", "]").content;
		assertFalse(result.contains("[remover]"));
	}

	// ── generateToken ─────────────────────────────────────────────────────────

	@Test
	public void generateTokenCorrectLengthTest() {
		CcpTextDecorator t = new CcpStringDecorator("abcdefghijklmnopqrstuvwxyz").text();
		CcpTextDecorator token = t.generateToken(10);
		assertEquals(10, token.content.length());
	}

	@Test
	public void generateTokenUsesOnlyCharactersOfTheAlphabetTest() {
		String alphabet = "abcdef";
		CcpTextDecorator token = new CcpStringDecorator(alphabet).text().generateToken(20);
		for (char c : token.content.toCharArray()) {
			assertTrue(alphabet.indexOf(c) >= 0);
		}
	}

	/**
	 * The tokens guard the login and the password creation, so their characters must come from a cryptographically secure
	 * source; until 2026-10-06 they came from {@code java.util.Random}, whose next values can be predicted.
	 */
	@Test
	public void generateTokenUsesASecureRandomSourceTest() throws Exception {
		Field source = CcpTextDecorator.class.getDeclaredField("TOKEN_RANDOM");
		source.setAccessible(true);

		assertTrue(source.get(null) instanceof java.security.SecureRandom);
		assertTrue(java.lang.reflect.Modifier.isStatic(source.getModifiers()));
	}

	@Test
	public void generateTokenDoesNotRepeatTokensTest() {
		CcpTextDecorator alphabet = CcpOtherConstants.LETTERS_AND_NUMBERS.text();
		java.util.Set<String> tokens = new java.util.HashSet<>();

		for (int k = 0; k < 1000; k++) {
			tokens.add(alphabet.generateToken(8).content);
		}

		assertEquals(1000, tokens.size());
	}

	@Test
	public void generateTokenReachesEveryCharacterOfTheAlphabetTest() {
		String alphabet = CcpOtherConstants.LETTERS_AND_NUMBERS.content;
		String manyCharacters = CcpOtherConstants.LETTERS_AND_NUMBERS.text().generateToken(20000).content;

		for (char c : alphabet.toCharArray()) {
			assertTrue("never drawn: " + c, manyCharacters.indexOf(c) >= 0);
		}
	}

	// ── resolveTemplate ───────────────────────────────────────────────────────

	@Test
	public void resolveTemplateReplacesFieldsTest() {
		CcpJsonRepresentation params = CcpOtherConstants.EMPTY_JSON.put(name, "Onias");
		CcpTextDecorator template = new CcpStringDecorator("Olá, {name}!").text();
		String result = template.resolveTemplate(params).content;
		assertEquals("Olá, Onias!", result);
	}

	@Test
	public void resolveTemplateMultipleFieldsTest() {
		CcpJsonRepresentation params = CcpOtherConstants.EMPTY_JSON
				.put(name, "João")
				.put(cidade, "Santos");
		CcpTextDecorator template = new CcpStringDecorator("{name} mora em {cidade}").text();
		String result = template.resolveTemplate(params).content;
		assertEquals("João mora em Santos", result);
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
	public void removeStartingCharactersWithoutPrefixDoesNotChangeTest() {
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

	@Test
	public void fromBase64UndoesAsBase64Test() {
		String original = "{\"topic\":\"jobsnow\",\"attempt\":1}";
		String base64 = new CcpStringDecorator(original).text().asBase64().content;

		String decoded = new CcpStringDecorator(base64).text().fromBase64().content;

		assertEquals(original, decoded);
	}

	/**
	 * Pub/Sub delivers to the push endpoint the bytes the publisher sent, in Base64; a JSON with accents comes back intact
	 * only when decoded as UTF-8.
	 */
	@Test
	public void fromBase64ReadsTheBytesAsUtf8Test() {
		String original = "{\"message\":\"configuração\"}";
		String base64 = java.util.Base64.getEncoder().encodeToString(original.getBytes(java.nio.charset.StandardCharsets.UTF_8));

		String decoded = new CcpStringDecorator(base64).text().fromBase64().content;

		assertEquals(original, decoded);
	}

	// ── capitalize ────────────────────────────────────────────────────────────

	@Test
	public void capitalizeTest() {
		assertEquals("Onias", new CcpStringDecorator("onias").text().capitalize().content);
	}

	@Test
	public void capitalizeEmptyStringTest() {
		assertEquals("", new CcpStringDecorator("").text().capitalize().content);
	}

	// ── toCamelCase / toSnakeCase ─────────────────────────────────────────────

	@Test
	public void toCamelCaseTest() {
		assertEquals("NomeDoCampo", new CcpStringDecorator("nome_do_campo").text().toCamelCase().content);
	}

	@Test
	public void toSnakeCaseTest() {
		String result = new CcpStringDecorator("NomeDoCampo").text().toSnakeCase().content;
		assertEquals("nome_do_campo", result);
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
	public void sanitizeRemovesAccentsAndCapitalizesTest() {
		CcpTextDecorator result = new CcpStringDecorator("Olá João").text().sanitize();
		assertFalse(result.content.contains("á"));
		assertFalse(result.content.contains("ã"));
		assertEquals(result.content, result.content.toUpperCase());
	}

	@Test
	public void sanitizeWithCustomDelimitersTest() {
		String[] delimiters = {",", ";"};
		CcpTextDecorator result = new CcpStringDecorator("java,python;ruby").text().sanitize(delimiters);
		assertFalse(result.content.contains(","));
		assertFalse(result.content.contains(";"));
		assertTrue(result.content.contains("PYTHON"));
	}

	// ── contains(String[], String) ────────────────────────────────────────────

	@Test
	public void containsWithCustomDelimitersTrueTest() {
		String[] delimiters = {","};
		assertTrue(new CcpStringDecorator("java,python,ruby").text().contains(delimiters, "python"));
	}

	@Test
	public void containsWithCustomDelimitersFalseTest() {
		String[] delimiters = {","};
		assertFalse(new CcpStringDecorator("java,python,ruby").text().contains(delimiters, "go"));
	}

	// ── removePieces(Predicate, String) ──────────────────────────────────────

	@Test
	public void removePiecesWithPredicateTest() {
		CcpTextDecorator result = new CcpStringDecorator("um dois tres quatro").text()
				.removePieces(s -> s.length() > 3, " ");
		assertFalse(result.content.contains("quatro"));
		assertFalse(result.content.contains("tres"));
		assertFalse(result.content.contains("dois"));
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

	// ── resolveTemplate with CcpTemplateFunctions ─────────────────────────────

	@Test
	public void resolveTemplateComCurrentTimeMillisTest() {
		CcpTextDecorator template = new CcpStringDecorator("ts={currentTimeMillis()}").text();
		String result = template.resolveTemplate(CcpOtherConstants.EMPTY_JSON).content;
		assertFalse(result.contains("{currentTimeMillis()}"));
		assertTrue(result.matches("ts=\\d+"));
	}

	// ── null-parameter tests (AOP) ────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void getPiecesDelimitersBeginNullTest() {
		new CcpStringDecorator("x").text().getPieces((String) null, "]");
	}

	@Test(expected = CcpNullParameterException.class)
	public void getPiecesDelimitersEndNullTest() {
		new CcpStringDecorator("x").text().getPieces("[", (String) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getPiecesPredicateNullTest() {
		new CcpStringDecorator("x").text().getPieces((Predicate<String>) null, " ");
	}

	@Test(expected = CcpNullParameterException.class)
	public void getPiecesPredicateDelimiterNullTest() {
		new CcpStringDecorator("x").text().getPieces(s -> true, (String) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void removePiecesPredicateNullTest() {
		new CcpStringDecorator("x").text().removePieces((Predicate<String>) null, " ");
	}

	@Test(expected = CcpNullParameterException.class)
	public void removePiecesDelimitersBeginNullTest() {
		new CcpStringDecorator("x").text().removePieces((String) null, "]");
	}

	@Test(expected = CcpNullParameterException.class)
	public void removePiecesListNullTest() {
		new CcpStringDecorator("x").text().removePieces((List<String>) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void replaceOldNullTest() {
		new CcpStringDecorator("x").text().replace(null, "y");
	}

	@Test(expected = CcpNullParameterException.class)
	public void replaceNewNullTest() {
		new CcpStringDecorator("x").text().replace("x", null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void resolveTemplateNullTest() {
		new CcpStringDecorator("x").text().resolveTemplate(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void regexMatchesNullTest() {
		new CcpStringDecorator("x").text().regexMatches(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void containsPhraseNullTest() {
		new CcpStringDecorator("x").text().contains((String) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void containsDelimitersNullTest() {
		new CcpStringDecorator("x").text().contains((String[]) null, "y");
	}

	@Test(expected = CcpNullParameterException.class)
	public void containsPhraseWithDelimitersNullTest() {
		new CcpStringDecorator("x").text().contains(new String[] {","}, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void sanitizeDelimitersNullTest() {
		new CcpStringDecorator("x").text().sanitize((String[]) null);
	}

	// ── null-return tests (AOP) ───────────────────────────────────────────────

	private static CcpTextDecorator withNullContent() throws Exception {
		CcpTextDecorator d = new CcpStringDecorator("x").text();
		Field f = CcpTextDecorator.class.getDeclaredField("content");
		f.setAccessible(true);
		f.set(d, null);
		return d;
	}

	@Test(expected = CcpNullReturnException.class)
	public void getContentNullReturnTest() throws Exception {
		withNullContent().getContent();
	}

	@Test(expected = CcpNullReturnException.class)
	public void toStringNullReturnTest() throws Exception {
		withNullContent().toString();
	}
}
