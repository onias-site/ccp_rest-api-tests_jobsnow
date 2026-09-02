package com.ccp.random;

class Pessoa {
	final int idade;
	final String nome;

	public Pessoa(int idade, String nome) {
		this.idade = idade;
		this.nome = nome;
	}

	@Override
	public String toString() {
		return "Pessoa [idade=" + idade + ", nome=" + nome + "]";
	}
}
