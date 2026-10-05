package com.ccp.random;

/** Fixture: a class with its own toString, which the aspect must keep. */
class Person {
	/** The age. */
	final int age;
	/** The name. */
	final String name;

	/**
	 * Builds the person.
	 * @param age the age
	 * @param name the name
	 */
	public Person(int age, String name) {
		this.age = age;
		this.name = name;
	}

	/**
	 * Hand-written toString.
	 * @return {@code Person [age=..., name=...]}
	 */
	@Override
	public String toString() {
		return "Person [age=" + age + ", name=" + name + "]";
	}
}
