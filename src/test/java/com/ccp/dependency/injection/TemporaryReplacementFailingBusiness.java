package com.ccp.dependency.injection;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;

/** Business of {@link TemporaryReplacementTest} that always throws {@link TemporaryReplacementBusinessFailed}. */
class TemporaryReplacementFailingBusiness implements CcpBusiness {

	/** The single instance. */
	public static final TemporaryReplacementFailingBusiness INSTANCE = new TemporaryReplacementFailingBusiness();

	private TemporaryReplacementFailingBusiness() {}

	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		throw new TemporaryReplacementBusinessFailed();
	}
}
