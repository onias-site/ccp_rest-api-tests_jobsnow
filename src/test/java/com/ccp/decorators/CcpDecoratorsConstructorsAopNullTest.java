package com.ccp.decorators;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;

/**
 * Coverage of {@code CcpNullParameterAspect} over the constructors of the decorators that are only
 * reachable from inside their own package ({@code protected} visibility).
 */
public class CcpDecoratorsConstructorsAopNullTest {

	@Test(expected = CcpNullParameterException.class)
	public void fileDecoratorConstructorNullTest() {
		new CcpFileDecorator(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void folderDecoratorConstructorNullTest() {
		new CcpFolderDecorator(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void hashDecoratorConstructorNullTest() {
		new CcpHashDecorator(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void inputStreamDecoratorConstructorNullTest() {
		new CcpInputStreamDecorator(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void passwordDecoratorConstructorNullTest() {
		new CcpPasswordDecorator(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void propertiesDecoratorConstructorNullTest() {
		new CcpPropertiesDecorator(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void textDecoratorConstructorNullTest() {
		new CcpTextDecorator(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void urlDecoratorConstructorNullTest() {
		new CcpUrlDecorator(null);
	}

	/** {@code CcpReflectionOptionsDecorator} is abstract; its constructor is reached through a subclass. */
	@Test(expected = CcpNullParameterException.class)
	public void reflectionOptionsDecoratorConstructorNullTest() {
		new CcpReflectionOptionsDecorator(null) {
		};
	}

	@Test(expected = CcpNullParameterException.class)
	public void reflectionStaticContextDecoratorConstructorNullTest() {
		new CcpReflectionStaticContextDecorator(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void reflectionNewInstanceDecoratorObjectNullTest() {
		new CcpReflectionNewInstanceDecorator(null, String.class);
	}

	@Test(expected = CcpNullParameterException.class)
	public void reflectionNewInstanceDecoratorClassNullTest() {
		new CcpReflectionNewInstanceDecorator("instance", null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void reflectionNewInstanceDecoratorFromConstructorNullTest() {
		new CcpReflectionNewInstanceDecorator((CcpReflectionConstructorDecorator) null);
	}
}
