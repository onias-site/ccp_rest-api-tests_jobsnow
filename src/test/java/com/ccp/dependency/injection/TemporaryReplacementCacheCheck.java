package com.ccp.dependency.injection;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.atomic.AtomicBoolean;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.cache.CcpCache;

/** Business of {@link TemporaryReplacementTest}: checks that the cache in use is the replacement and marks that it ran. */
class TemporaryReplacementCacheCheck implements CcpBusiness {

	/** The class of the replacement cache. */
	private final Class<?> replacementClass;

	/** Marks that the business ran. */
	private final AtomicBoolean ran;

	TemporaryReplacementCacheCheck(Class<?> replacementClass, AtomicBoolean ran) {
		this.replacementClass = replacementClass;
		this.ran = ran;
	}

	public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
		assertEquals(this.replacementClass, CcpDependencyInjection.getDependency(CcpCache.class).getClass());
		this.ran.set(true);
		return json;
	}
}
