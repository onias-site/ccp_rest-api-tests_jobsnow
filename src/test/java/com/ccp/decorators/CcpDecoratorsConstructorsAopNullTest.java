package com.ccp.decorators;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;

/**
 * Cobertura do {@code CcpNullParameterAspect} sobre os construtores dos decorators que só são
 * alcançáveis de dentro do próprio pacote (visibilidade {@code protected}).
 */
public class CcpDecoratorsConstructorsAopNullTest {

	@Test(expected = CcpNullParameterException.class)
	public void fileDecoratorConstrutorNullTest() {
		new CcpFileDecorator(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void folderDecoratorConstrutorNullTest() {
		new CcpFolderDecorator(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void hashDecoratorConstrutorNullTest() {
		new CcpHashDecorator(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void inputStreamDecoratorConstrutorNullTest() {
		new CcpInputStreamDecorator(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void passwordDecoratorConstrutorNullTest() {
		new CcpPasswordDecorator(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void propertiesDecoratorConstrutorNullTest() {
		new CcpPropertiesDecorator(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void textDecoratorConstrutorNullTest() {
		new CcpTextDecorator(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void urlDecoratorConstrutorNullTest() {
		new CcpUrlDecorator(null);
	}

	/** {@code CcpReflectionOptionsDecorator} é abstrata; o construtor é alcançado por uma subclasse. */
	@Test(expected = CcpNullParameterException.class)
	public void reflectionOptionsDecoratorConstrutorNullTest() {
		new CcpReflectionOptionsDecorator(null) {
		};
	}

	@Test(expected = CcpNullParameterException.class)
	public void reflectionStaticContextDecoratorConstrutorNullTest() {
		new CcpReflectionStaticContextDecorator(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void reflectionNewInstanceDecoratorObjectNullTest() {
		new CcpReflectionNewInstanceDecorator(null, String.class);
	}

	@Test(expected = CcpNullParameterException.class)
	public void reflectionNewInstanceDecoratorClassNullTest() {
		new CcpReflectionNewInstanceDecorator("instancia", null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void reflectionNewInstanceDecoratorFromConstructorNullTest() {
		new CcpReflectionNewInstanceDecorator((CcpReflectionConstructorDecorator) null);
	}
}
