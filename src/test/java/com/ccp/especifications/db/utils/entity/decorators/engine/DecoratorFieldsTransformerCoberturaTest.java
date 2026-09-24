package com.ccp.especifications.db.utils.entity.decorators.engine;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;

/**
 * Impede que um método novo de {@code CcpEntity} nasça sem passar pelo transformador de campos.
 *
 * <p>O {@code DecoratorFieldsTransformerEntity} existe para normalizar o json — trocar o e-mail pelo
 * seu hash, por exemplo — antes que alguém calcule chave primária a partir dele. Esquecer de repassar
 * um método não quebra a compilação: o {@code CcpEntityDelegator} repassa por baixo, só que com o json
 * cru, e a chave calculada deixa de bater com a do registro gravado. Foi o que aconteceu com o
 * {@code deleteAnyWhere}, que passou despercebido enquanto um laço infinito escondia o sintoma.
 *
 * <p>A regra que o teste cobra: todo método de {@code CcpEntity} que recebe um
 * {@code CcpJsonRepresentation} tem que estar sobrescrito no decorator, ou estar nomeado abaixo como
 * dispensa consciente. Acrescentar método à interface faz este teste falhar até que alguém classifique
 * o caso novo — que é justamente a decisão que hoje passa em branco.
 */
public class DecoratorFieldsTransformerCoberturaTest {

	/**
	 * Métodos que recebem json e mesmo assim não precisam ser repassados pelo decorator. Cada um tem o
	 * seu motivo, e o motivo é o que justifica a dispensa:
	 *
	 * <ul>
	 * <li>{@code validateJson} — a validação tem que enxergar o valor como o usuário mandou. O e-mail é
	 * conferido contra a expressão regular de e-mail; depois de transformado ele é um hash, e nenhum
	 * hash passaria nessa regra.</li>
	 * <li>{@code calculateId} — quem chama já entrega o json transformado, porque a chamada nasce de
	 * dentro dos próprios métodos que este decorator repassa.</li>
	 * <li>{@code getOneByIdAnyWhere} — a implementação padrão delega para {@code getOneById} na própria
	 * instância, e esse sim é repassado com o json transformado.</li>
	 * <li>{@code getIdToSearchDisposableRecord} — quem o implementa chama {@code getHandledJson} por
	 * conta própria antes de montar o id.</li>
	 * <li>{@code toBulkItems} — <b>esta não é uma dispensa confortável.</b> Quem monta itens de bulk
	 * precisa transformar o json antes, por fora, e nada obriga a isso. Está aqui para o teste refletir
	 * o que o código faz hoje, não para dizer que está certo.</li>
	 * </ul>
	 */
	private static final List<String> DISPENSADOS = Arrays.asList(
			"calculateId(CcpJsonRepresentation)",
			"getIdToSearchDisposableRecord(CcpJsonRepresentation)",
			"getOneByIdAnyWhere(CcpJsonRepresentation)",
			"toBulkItems(CcpJsonRepresentation, CcpBulkEntityOperationType)",
			"validateJson(CcpJsonRepresentation)"
	);

	@Test
	public void todoMetodoQueRecebeJsonEhRepassadoComOJsonTransformado() {

		Set<String> exigidos = this.metodosDeEntidadeQueRecebemJson();
		exigidos.removeAll(DISPENSADOS);

		Set<String> repassados = this.metodosSobrescritosPeloDecorator();

		Set<String> faltando = new TreeSet<>(exigidos);
		faltando.removeAll(repassados);

		assertEquals("metodos de CcpEntity que recebem json e nao passam pelo transformador",
				new TreeSet<String>(), faltando);
	}

	/**
	 * As assinaturas dos métodos de {@code CcpEntity} que têm um {@code CcpJsonRepresentation} entre os
	 * parâmetros. São esses que dependem do json já normalizado para produzir o resultado certo.
	 *
	 * <p>Fica de fora, por exemplo, o {@code getRecordFromUnionAll}, que recebe um fornecedor de json e
	 * não o json: a implementação padrão dele chama {@code getHandledJson} por conta própria, então a
	 * transformação já acontece sem depender de ninguém repassar.
	 */
	private Set<String> metodosDeEntidadeQueRecebemJson() {
		Method[] declaredMethods = CcpEntity.class.getDeclaredMethods();
		Set<String> resultado = this.assinaturasQueRecebemJson(declaredMethods);
		return resultado;
	}

	/**
	 * As assinaturas que o decorator declara. A comparação é por assinatura, e não por nome, porque uma
	 * sobrecarga com parâmetros próprios não sobrescreve coisa alguma: o motor continua chamando o
	 * método da interface, que passa direto pelo delegador sem transformar nada.
	 */
	private Set<String> metodosSobrescritosPeloDecorator() {
		Method[] declaredMethods = DecoratorFieldsTransformerEntity.class.getDeclaredMethods();
		Set<String> resultado = this.assinaturasQueRecebemJson(declaredMethods);
		return resultado;
	}

	/**
	 * Os métodos sintéticos ficam de fora. Cada lambda escrita no corpo de uma classe vira um método
	 * dela, e a lambda de {@code getOneById} recebe um json — sem esse filtro ela entraria na lista de
	 * métodos a cobrir, e nenhum decorator jamais poderia "sobrescrever" uma lambda.
	 */
	private Set<String> assinaturasQueRecebemJson(Method[] methods) {
		Stream<Method> stream = Arrays.asList(methods).stream();
		Stream<Method> declarados = stream.filter(x -> false == x.isSynthetic());
		Stream<Method> comJson = declarados.filter(x -> this.recebeJson(x));
		Stream<String> assinaturas = comJson.map(x -> this.assinatura(x));
		Set<String> resultado = assinaturas.collect(Collectors.toCollection(TreeSet::new));
		return resultado;
	}

	private String assinatura(Method method) {
		Class<?>[] parameterTypes = method.getParameterTypes();
		Stream<Class<?>> stream = Arrays.asList(parameterTypes).stream();
		Stream<String> nomes = stream.map(x -> x.getSimpleName());
		String parametros = nomes.collect(Collectors.joining(", "));
		String name = method.getName();
		String assinatura = name + "(" + parametros + ")";
		return assinatura;
	}

	private boolean recebeJson(Method method) {
		Class<?>[] parameterTypes = method.getParameterTypes();
		List<Class<?>> parametros = Arrays.asList(parameterTypes);
		boolean recebeJson = parametros.contains(CcpJsonRepresentation.class);
		return recebeJson;
	}
}
