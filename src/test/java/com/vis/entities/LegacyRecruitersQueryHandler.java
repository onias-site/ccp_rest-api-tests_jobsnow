package com.vis.entities;

import com.ccp.aop.CcpAllowNullReturn;
import com.ccp.aop.CcpAllowNullParameter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.function.Consumer;

import com.ccp.decorators.CcpJsonRepresentation;

/**
 * Behavior of the fake query executor of {@link RecruiterLoadsTest}: {@code consumeQueryResult} hands the
 * given ids (the e-mails of the legacy recruiters) to the consumer; any other call fails.
 */
class LegacyRecruitersQueryHandler implements InvocationHandler {

	/** The e-mails of the legacy recruiters. */
	private final String[] emails;

	LegacyRecruitersQueryHandler(String... emails) {
		this.emails = emails;
	}

	@SuppressWarnings("unchecked")
	@CcpAllowNullParameter
	@CcpAllowNullReturn
	public Object invoke(Object proxy, Method method, Object[] args) {
		if ("consumeQueryResult".equals(method.getName())) {
			Consumer<CcpJsonRepresentation> consumer = (Consumer<CcpJsonRepresentation>) args[4];
			for (String email : this.emails) {
				consumer.accept(new CcpJsonRepresentation("{\"id\":\"" + email + "\"}"));
			}
			return proxy;
		}
		throw new UnsupportedOperationException(method.getName());
	}
}
