package com.ccp.random;

import java.util.List;
import java.util.function.Predicate;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.random.CcpRandomScripts.JsonFields;

/**
 * Filter of {@link CcpRandomScripts#sanitizeSynonymsFile()}: keeps the item whose word has from 3 to 50 characters, is not
 * removed and is not the excluded word.
 */
class RandomScriptsValidWord implements Predicate<CcpJsonRepresentation> {

	/** The removed words. */
	private final List<String> removed;

	/** The field that holds the word. */
	private final JsonFields wordField;

	/** A word that is not kept (the skill itself, for its synonyms). */
	private final String excludedWord;

	/** Whether {@code _} in the word stands for a space. */
	private final boolean underscoreAsSpace;

	RandomScriptsValidWord(List<String> removed, JsonFields wordField, String excludedWord, boolean underscoreAsSpace) {
		this.removed = removed;
		this.wordField = wordField;
		this.excludedWord = excludedWord;
		this.underscoreAsSpace = underscoreAsSpace;
	}

	public boolean test(CcpJsonRepresentation x) {
		String rawWord = x.getAsString(this.wordField);
		String word = this.underscoreAsSpace ? rawWord.replace("_", " ") : rawWord;
		return word.length() > 2 && word.length() <= 50 && false == this.removed.contains(word) && false == this.excludedWord.equals(word);
	}
}
