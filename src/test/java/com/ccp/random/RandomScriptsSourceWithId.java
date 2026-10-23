package com.ccp.random;

import java.util.function.Function;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.random.CcpRandomScripts.JsonFieldNames;

/** Function of {@link CcpRandomScripts#createPositionsFile()}: the {@code _source} of a document of the {@code _mget}, with its {@code _id} as {@code id}. */
class RandomScriptsSourceWithId implements Function<CcpJsonRepresentation, CcpJsonRepresentation> {

	/** The single instance. */
	public static final RandomScriptsSourceWithId INSTANCE = new RandomScriptsSourceWithId();

	private RandomScriptsSourceWithId() {}

	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		String id = json.getAsString(JsonFieldNames._id);
		CcpJsonRepresentation source = json.getInnerJson(JsonFieldNames._source);
		CcpJsonRepresentation put = source.put(JsonFieldNames.id, id);
		return put;
	}
}
