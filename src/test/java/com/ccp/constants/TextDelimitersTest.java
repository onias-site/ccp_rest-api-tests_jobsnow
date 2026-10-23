package com.ccp.constants;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

/**
 * Proves the regular expression of text delimiters, {@link CcpOtherConstants#DELIMITERS} (finding 1): until 2026-10-07
 * the closing parenthesis was a delimiter only when followed by a space, so it stayed glued to the last word.
 */
public class TextDelimitersTest {

	private List<String> wordsOf(String text) {
		String[] pieces = text.split(CcpOtherConstants.DELIMITERS);
		List<String> words = Arrays.stream(pieces).filter(piece -> false == piece.isEmpty()).collect(Collectors.toList());
		return words;
	}

	@Test
	public void aClosingParenthesisAtTheEndOfAWordIsADelimiter() {
		assertEquals(Arrays.asList("Spring", "Boot"), this.wordsOf("Spring (Boot)"));
		assertEquals(Arrays.asList("Java", "8"), this.wordsOf("Java(8),"));
	}

	@Test
	public void aClosingParenthesisFollowedByASpaceIsStillADelimiter() {
		assertEquals(Arrays.asList("Java", "Spring"), this.wordsOf("(Java) Spring"));
	}

	/**
	 * The hyphen splits; the slash and the backslash do not, so short skills such as CI/CD and UI/UX stay whole words.
	 */
	@Test
	public void theHyphenSplitsAndTheSlashDoesNot() {
		assertEquals(Arrays.asList("front", "end"), this.wordsOf("front-end"));
		assertEquals(Arrays.asList("CI/CD", "UI\\UX"), this.wordsOf("CI/CD UI\\UX"));
	}

	/**
	 * Finding 1, corrected in 2026-10-22: the expression and {@link CcpOtherConstants#DELIMITERS_ARRAY} are the same set.
	 * Until then the expression split on the hyphen and white space, and the array on the slash and the backslash.
	 */
	@Test
	public void theExpressionSplitsOnEveryItemOfTheArrayAndOnNothingElse() {
		for (String delimiter : CcpOtherConstants.DELIMITERS_ARRAY) {
			assertEquals(delimiter, Arrays.asList("a", "b"), this.wordsOf("a" + delimiter + "b"));
		}
		for (char character = 32; character < 127; character++) {
			String text = String.valueOf(character);
			boolean isDelimiter = Arrays.asList(CcpOtherConstants.DELIMITERS_ARRAY).contains(text);
			assertEquals(text, isDelimiter, text.matches(CcpOtherConstants.DELIMITERS));
		}
	}
}
