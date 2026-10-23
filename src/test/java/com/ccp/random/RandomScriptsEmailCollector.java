package com.ccp.random;

import java.util.Set;
import java.util.function.Consumer;

import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;

/** Consumer of {@link CcpRandomScripts#getEmails}: keeps the non empty values of the fields, trimmed and in lower case. */
class RandomScriptsEmailCollector implements Consumer<CcpJsonRepresentation> {

	/** Receives the values. */
	private final Set<Object> set;

	/** The fields read. */
	private final String[] fields;

	RandomScriptsEmailCollector(Set<Object> set, String... fields) {
		this.set = set;
		this.fields = fields;
	}

	public void accept(CcpJsonRepresentation json) {
		for (String field : this.fields) {
			String value = json.getAsTextDecorator(new CcpFieldName(field)).content.trim().toLowerCase();
			if (value.isEmpty()) {
				continue;
			}
			this.set.add(value);
		}
	}
}
