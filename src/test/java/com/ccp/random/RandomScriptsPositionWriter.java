package com.ccp.random;

import java.util.function.Consumer;

import com.ccp.decorators.CcpFileDecorator;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.random.CcpRandomScripts.JsonFieldNames;

/** Consumer of {@link CcpRandomScripts#saveOldJobsNowPosition()}: appends the text of each position to the file, numbered by {@link CcpRandomScripts#counter}. */
class RandomScriptsPositionWriter implements Consumer<CcpJsonRepresentation> {

	/** Receives the positions. */
	private final CcpFileDecorator file;

	RandomScriptsPositionWriter(CcpFileDecorator file) {
		this.file = file;
	}

	public void accept(CcpJsonRepresentation vaga) {
		String textValue = vaga.getAsString(JsonFieldNames.vaga).replace("\n", "").trim();
		String completeLeft = new CcpStringDecorator("" + ++CcpRandomScripts.counter).text().completeLeft('0', 6).content;
		this.file.append(completeLeft + ": " + textValue);
	}
}
