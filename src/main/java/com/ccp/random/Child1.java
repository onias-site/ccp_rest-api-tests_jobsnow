package com.ccp.random;

/** Fixture: a concrete subclass of {@link Parent}. */
class Child1 extends Parent {
	/** The single instance: the class keeps no state. */
	public final static Child1 INSTANCE = new Child1();

	private Child1() {}

	/** Does nothing. */
	void a() {

	}
}
