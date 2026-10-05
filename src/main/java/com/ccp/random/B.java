package com.ccp.random;

/** Fixture of the toString aspect tests: a subclass that adds only a static attribute. */
class B extends A{
	/** A static attribute, which the generated toString ignores. */
	static Object b;
}
