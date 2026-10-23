package com.jn.entities.decorators.engine;

import com.ccp.aop.CcpAllowNullReturn;
import com.ccp.aop.CcpAllowNullParameter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

import com.ccp.decorators.CcpJsonRepresentation;

/**
 * Behavior of the fake query executor of {@link VersionablePurgeConflictsTest}: answers each {@code delete} with the next
 * response and counts the deletes; any other call fails.
 */
class VersionablePurgeDeleteHandler implements InvocationHandler {

	/** The responses not given yet. */
	private final LinkedList<String> pending;

	/** Counts the deletes. */
	private final AtomicInteger deletes;

	VersionablePurgeDeleteHandler(LinkedList<String> pending, AtomicInteger deletes) {
		this.pending = pending;
		this.deletes = deletes;
	}

	@CcpAllowNullParameter
	@CcpAllowNullReturn
	public Object invoke(Object proxy, Method method, Object[] args) {
		if ("delete".equals(method.getName())) {
			this.deletes.incrementAndGet();
			return new CcpJsonRepresentation(this.pending.removeFirst());
		}
		throw new UnsupportedOperationException(method.getName());
	}
}
