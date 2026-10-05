package com.ccp.json.defaultvalues.annotations;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;

/**
 * A business that only returns the JSON it receives, serving as the vehicle to exercise {@code execute} over any
 * rules class. Unlike the businesses of the validation annotations, this one is reused by every scenario: the
 * rules class comes through the constructor, because it is the only thing that changes from one scenario to
 * another.
 */
public class DefaultValueBusiness implements CcpBusiness {

	private final Class<?> rulesClass;

	public DefaultValueBusiness(Class<?> rulesClass) {
		this.rulesClass = rulesClass;
	}

	public Class<?> getJsonValidationClass() {
		return this.rulesClass;
	}

	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		return json;
	}
}
