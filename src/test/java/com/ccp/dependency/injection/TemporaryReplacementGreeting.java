package com.ccp.dependency.injection;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.TemporaryReplacementTest.Greeter;

/** Business of {@link TemporaryReplacementTest}: puts in the JSON the greeting of the {@link Greeter} in use. */
class TemporaryReplacementGreeting implements CcpBusiness {

	/** The single instance. */
	public static final TemporaryReplacementGreeting INSTANCE = new TemporaryReplacementGreeting();

	/** The field that receives the greeting. */
	static final CcpFieldName greeting = new CcpFieldName("greeting");

	private TemporaryReplacementGreeting() {}

	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		String greetingInside = CcpDependencyInjection.getDependency(Greeter.class).greet();
		return json.put(greeting, greetingInside);
	}
}
