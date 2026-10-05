package com.ccp.json.defaultvalues.annotations;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;

/**
 * JSON producer triggered by the {@code jsonProducer} attribute. It proves two things at once: that the instance
 * is created by reflection (there is no reference to it outside the annotation) and that the JSON being handled
 * reaches the producer whole, since the stored value is derived from another field.
 */
public class DefaultValueProducer implements CcpBusiness {

	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {

		String name = json.getAsString(RulesDefaultValueJsonProducer.name);
		String value = "produzido para " + name;
		CcpJsonRepresentation produzido = json.put(RulesDefaultValueJsonProducer.produzido, value);

		return produzido;
	}
}
