package com.ccp.random;

import java.util.function.Consumer;

import com.ccp.decorators.CcpFileDecorator;
import com.ccp.decorators.CcpStringDecorator;

/** Consumer of {@link CcpRandomScripts#moveFileLocation()}: moves the {@code .gitignore} of the folder out of the {@code github} path. */
class RandomScriptsGitignoreMover implements Consumer<CcpFileDecorator> {

	/** The single instance. */
	public static final RandomScriptsGitignoreMover INSTANCE = new RandomScriptsGitignoreMover();

	private RandomScriptsGitignoreMover() {}

	public void accept(CcpFileDecorator file) {
		String oldName = file.content + "\\.gitignore";
		String newName = oldName.replace("github", "");
		new CcpStringDecorator(oldName).file().rename(newName);
	}
}
