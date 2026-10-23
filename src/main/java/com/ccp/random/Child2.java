package com.ccp.random;

/** Fixture: another concrete subclass of {@link Parent}. */
class Child2 extends Parent {
	/** The single instance: the class keeps no state. */
	public final static Child2 INSTANCE = new Child2();

	private Child2() {}

	/** Does nothing. */
	void a() {

	}
}
