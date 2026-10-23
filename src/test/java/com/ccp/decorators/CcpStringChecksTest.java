package com.ccp.decorators;

import static org.junit.Assert.fail;

import org.junit.Test;

import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

/**
 * The checks of {@link CcpStringDecorator}: {@link CcpStringBooleanCheck}, {@link CcpStringLongNumberCheck} and
 * {@link CcpStringJsonListCheck} accept a valid text silently and throw for an invalid one.
 */
public class CcpStringChecksTest {

	static {
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE);
	}

	private static void assertRefused(Runnable check) {
		try {
			check.run();
		} catch (RuntimeException expected) {
			return;
		}
		fail("the text was accepted");
	}

	@Test
	public void trueAndFalseInAnyCaseAreBooleans() {
		CcpStringBooleanCheck.INSTANCE.accept("true");
		CcpStringBooleanCheck.INSTANCE.accept("FALSE");
		CcpStringBooleanCheck.INSTANCE.accept("True");
	}

	@Test
	public void anyOtherTextIsNotABoolean() {
		assertRefused(() -> CcpStringBooleanCheck.INSTANCE.accept("yes"));
		assertRefused(() -> CcpStringBooleanCheck.INSTANCE.accept(""));
	}

	@Test
	public void anIntegerAndAnIntegerEndingInDecimalZeroAreLongNumbers() {
		CcpStringLongNumberCheck.INSTANCE.accept("42");
		CcpStringLongNumberCheck.INSTANCE.accept("-7");
		CcpStringLongNumberCheck.INSTANCE.accept("42.0");
	}

	@Test
	public void aTextOrADecimalIsNotALongNumber() {
		assertRefused(() -> CcpStringLongNumberCheck.INSTANCE.accept("abc"));
		assertRefused(() -> CcpStringLongNumberCheck.INSTANCE.accept("4.5"));
	}

	@Test
	public void aJsonArrayIsAList() {
		CcpStringJsonListCheck.INSTANCE.accept("[1, 2, 3]");
		CcpStringJsonListCheck.INSTANCE.accept("[]");
	}

	@Test
	public void aTextThatIsNotAJsonArrayIsNotAList() {
		assertRefused(() -> CcpStringJsonListCheck.INSTANCE.accept("not a list"));
	}
}
