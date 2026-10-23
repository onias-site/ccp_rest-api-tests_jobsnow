package com.vis.entities;

import com.ccp.aop.CcpAllowNullReturn;
import com.ccp.aop.CcpAllowNullParameter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/** Behavior of the fake query executor of {@link SkillAndCompanySeedsDefectsTest}: every call fails as a missing legacy index. */
class LegacyIndexFailureHandler implements InvocationHandler {

	/** The single instance. */
	public static final LegacyIndexFailureHandler INSTANCE = new LegacyIndexFailureHandler();

	private LegacyIndexFailureHandler() {}

	@CcpAllowNullParameter
	@CcpAllowNullReturn
	public Object invoke(Object proxy, Method method, Object[] args) {
		throw new IllegalStateException("index_not_found_exception: old_recruiters");
	}
}
