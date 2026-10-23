package com.ccp.random;

import java.util.Map;
import java.util.function.Consumer;

import com.ccp.decorators.CcpFileDecorator;

/** Consumer of {@link CcpRandomScripts#getJson(CcpFileDecorator)}: puts the JSON of each file of the folder under its name without {@code .json}. */
class RandomScriptsSubJsonCollector implements Consumer<CcpFileDecorator> {

	/** Receives the JSON of each file. */
	private final Map<String, Object> json;

	RandomScriptsSubJsonCollector(Map<String, Object> json) {
		this.json = json;
	}

	public void accept(CcpFileDecorator subFile) {
		Map<String, Object> subJson = CcpRandomScripts.getJson(subFile);
		this.json.put(subFile.getName().replace(".json", ""), subJson);
	}
}
