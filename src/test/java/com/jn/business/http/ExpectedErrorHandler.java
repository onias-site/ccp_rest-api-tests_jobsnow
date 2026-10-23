package com.jn.business.http;

import static org.junit.Assert.assertSame;

import java.util.function.Function;

import com.ccp.decorators.CcpJsonRepresentation;

/** Exception handler of {@link HttpFailuresTest}: checks that it got the expected error and answers the given JSON. */
class ExpectedErrorHandler implements Function<Throwable, CcpJsonRepresentation> {

	/** The error expected. */
	private final Throwable expected;

	/** The answer. */
	private final CcpJsonRepresentation handled;

	ExpectedErrorHandler(Throwable expected, CcpJsonRepresentation handled) {
		this.expected = expected;
		this.handled = handled;
	}

	public CcpJsonRepresentation apply(Throwable e) {
		assertSame(this.expected, e);
		return this.handled;
	}
}
