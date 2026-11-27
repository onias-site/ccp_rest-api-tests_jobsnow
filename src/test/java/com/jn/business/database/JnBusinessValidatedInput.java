package com.jn.business.database;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;

/**
 * The input of a business after its validation and its default values, with nothing else run: the {@code execute} of a
 * business validates and fills the defaults of the given validation class before its {@code apply}, which here gives the
 * input back.
 */
public class JnBusinessValidatedInput implements CcpBusiness {

	/** The validation class of the business whose input is checked. */
	private final Class<?> jsonValidationClass;

	/**
	 * Keeps the validation class.
	 * @param jsonValidationClass the validation class of the business whose input is checked
	 */
	public JnBusinessValidatedInput(Class<?> jsonValidationClass) {
		this.jsonValidationClass = jsonValidationClass;
	}

	public Class<?> getJsonValidationClass() {
		return this.jsonValidationClass;
	}

	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		return json;
	}
}
