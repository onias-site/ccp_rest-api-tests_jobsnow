package com.jn.business.database;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;

/**
 * The input of {@link JnBusinessBackupTheDatabase} after its validation and its default values, with no backup: the
 * {@code execute} of a business validates and fills the defaults before its {@code apply}, which here gives the input
 * back.
 */
public class JnBusinessBackupTheDatabaseDefaults implements CcpBusiness {

	/** The single instance. */
	public static final JnBusinessBackupTheDatabaseDefaults INSTANCE = new JnBusinessBackupTheDatabaseDefaults();

	private JnBusinessBackupTheDatabaseDefaults() {}

	public Class<?> getJsonValidationClass() {
		return JnBusinessBackupTheDatabase.Fields.class;
	}

	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		return json;
	}
}
