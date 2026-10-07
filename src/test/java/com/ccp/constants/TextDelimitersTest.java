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
	 * The set of this expression differs from {@link CcpOtherConstants#DELIMITERS_ARRAY} on purpose until the vis decides
	 * how skills are recognized: the hyphen splits, the slash does not.
	 */
	@Test
	public void theHyphenSplitsAndTheSlashDoesNotUntilTheSetIsDecided() {
		assertEquals(Arrays.asList("front", "end"), this.wordsOf("front-end"));
		assertEquals(Arrays.asList("Java/Spring"), this.wordsOf("Java/Spring"));
	}
}
