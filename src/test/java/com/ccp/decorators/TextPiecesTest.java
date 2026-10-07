package com.ccp.decorators;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

/**
 * Proves {@link CcpTextDecorator#getPieces(String, String)} and {@link CcpTextDecorator#removePieces(String, String)}
 * when the delimiters are out of order, missing or equal (finding 10): until 2026-10-07 the end delimiter was searched
 * from the start of the text and its absence was not detected, which raised StringIndexOutOfBoundsException, returned a
 * wrong piece or looped forever. Every test has a timeout, so the old loop fails instead of hanging the suite.
 */
public class TextPiecesTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	private List<String> piecesOf(String text, String beginDelimiter, String endDelimiter) {
		CcpTextDecorator textDecorator = new CcpStringDecorator(text).text();
		List<String> pieces = textDecorator.getPieces(beginDelimiter, endDelimiter);
		return pieces;
	}

	@Test(timeout = 2000)
	public void theEndDelimiterBeforeTheBeginOneIsIgnored() {
		assertEquals(Arrays.asList("[isto]"), this.piecesOf("a] veja [isto]", "[", "]"));
	}

	@Test(timeout = 2000)
	public void aPieceNeverClosedAfterTheStartIsIgnored() {
		assertEquals(Arrays.asList("[fechado]"), this.piecesOf("x [fechado] e [aberto", "[", "]"));
	}

	@Test(timeout = 2000)
	public void aPieceNeverClosedAtTheStartDoesNotLoopForever() {
		assertEquals(Arrays.asList(), this.piecesOf("[aberto", "[", "]"));
	}

	@Test(timeout = 2000)
	public void aLongEndDelimiterThatIsMissingGivesNoPiece() {
		assertEquals(Arrays.asList(), this.piecesOf("[[aberto", "[[", "]]"));
	}

	@Test(timeout = 2000)
	public void equalDelimitersOpenAndClosePieces() {
		assertEquals(Arrays.asList("*um*", "*dois*"), this.piecesOf("*um* e *dois* e *tres", "*", "*"));
	}

	@Test(timeout = 2000)
	public void emptyDelimitersGiveNoPiece() {
		assertEquals(Arrays.asList(), this.piecesOf("texto", "", ""));
	}

	@Test(timeout = 2000)
	public void theWellFormedPiecesAreStillFound() {
		assertEquals(Arrays.asList("[mundo]", "[java]"), this.piecesOf("ola [mundo] e [java]", "[", "]"));
	}

	@Test(timeout = 2000)
	public void theRemovalOfTagsKeepsTheTextOfATagNeverClosed() {
		CcpTextDecorator textDecorator = new CcpStringDecorator("a > b <i>c</i> <d").text();

		String withoutTags = textDecorator.removePieces("<", ">").content;

		assertEquals("a > b  c  <d", withoutTags);
	}
}
