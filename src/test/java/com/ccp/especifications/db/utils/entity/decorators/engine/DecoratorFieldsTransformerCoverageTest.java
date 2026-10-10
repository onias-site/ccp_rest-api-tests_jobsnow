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
 * Prevents a new {@code CcpEntity} method from being born without going through the field transformer.
 *
 * <p>{@code DecoratorFieldsTransformerEntity} exists to normalize the json — replacing the e-mail with
 * its hash, for example — before anyone calculates a primary key from it. Forgetting to forward a
 * method does not break the build: {@code CcpEntityDelegator} forwards it underneath, only with the raw
 * json, and the calculated key no longer matches the key of the saved record. That is what happened to
 * {@code deleteAnyWhere}, which went unnoticed while an infinite loop hid the symptom.
 *
 * <p>The rule this test enforces: every {@code CcpEntity} method that receives a
 * {@code CcpJsonRepresentation} must be overridden in the decorator, or be named below as a
 * deliberate exemption. Adding a method to the interface makes this test fail until someone classifies
 * the new case — which is precisely the decision that goes unnoticed today.
 */
public class DecoratorFieldsTransformerCoverageTest {

	/**
	 * Methods that receive json and still do not need to be forwarded by the decorator. Each one has
	 * its reason, and the reason is what justifies the exemption:
	 *
	 * <ul>
	 * <li>{@code validateJson} — validation must see the value as the user sent it. The e-mail is
	 * checked against the e-mail regular expression; once transformed it is a hash, and no hash would
	 * pass that rule.</li>
	 * <li>{@code calculateId} — the caller already hands over the transformed json, because the call
	 * originates inside the very methods this decorator forwards.</li>
	 * <li>{@code getOneByIdAnyWhere} — the default implementation delegates to {@code getOneById} on the
	 * same instance, and that one is forwarded with the transformed json.</li>
	 * <li>{@code getIdToSearchDisposableRecord} — its implementors call {@code getJsonWithHandledPrimaryKey} on their
	 * own before building the id.</li>
	 * <li>{@code toBulkItems} — <b>this is not a comfortable exemption.</b> Whoever builds bulk items
	 * must transform the json beforehand, from the outside, and nothing enforces it. It is here so the
	 * test reflects what the code does today, not to say it is right.</li>
	 * </ul>
	 */
	private static final List<String> EXEMPT = Arrays.asList(
			"calculateId(CcpJsonRepresentation)",
			"getIdToSearchDisposableRecord(CcpJsonRepresentation)",
			"getOneByIdAnyWhere(CcpJsonRepresentation)",
			"toBulkItems(CcpJsonRepresentation, CcpBulkEntityOperationType)",
			"validateJson(CcpJsonRepresentation)"
	);

	@Test
	public void everyMethodReceivingJsonIsForwardedWithTransformedJson() {

		Set<String> required = this.entityMethodsReceivingJson();
		required.removeAll(EXEMPT);

		Set<String> forwarded = this.methodsOverriddenByDecorator();

		Set<String> missing = new TreeSet<>(required);
		missing.removeAll(forwarded);

		assertEquals("CcpEntity methods that receive json and do not go through the transformer",
				new TreeSet<String>(), missing);
	}

	/**
	 * The signatures of the {@code CcpEntity} methods that have a {@code CcpJsonRepresentation} among
	 * their parameters. Those are the ones that depend on the already normalized json to produce the
	 * right result.
	 *
	 * <p>Left out, for example, is {@code getRecordFromUnionAll}, which receives a json supplier and
	 * not the json: its default implementation calls {@code getJsonWithHandledPrimaryKey} on its own, so the
	 * transformation already happens without depending on anyone forwarding it.
	 */
	private Set<String> entityMethodsReceivingJson() {
		Method[] declaredMethods = CcpEntity.class.getDeclaredMethods();
		Set<String> signatures = this.signaturesReceivingJson(declaredMethods);
		return signatures;
	}

	/**
	 * The signatures the decorator declares. The comparison is by signature, not by name, because an
	 * overload with its own parameters overrides nothing: the engine keeps calling the interface
	 * method, which passes straight through the delegator without transforming anything.
	 */
	private Set<String> methodsOverriddenByDecorator() {
		Method[] declaredMethods = DecoratorFieldsTransformerEntity.class.getDeclaredMethods();
		Set<String> signatures = this.signaturesReceivingJson(declaredMethods);
		return signatures;
	}

	/**
	 * Synthetic methods are left out. Every lambda written in a class body becomes a method of that
	 * class, and the {@code getOneById} lambda receives a json — without this filter it would enter the
	 * list of methods to cover, and no decorator could ever "override" a lambda.
	 */
	private Set<String> signaturesReceivingJson(Method[] methods) {
		Stream<Method> stream = Arrays.asList(methods).stream();
		Stream<Method> declared = stream.filter(x -> false == x.isSynthetic());
		Stream<Method> withJson = declared.filter(x -> this.receivesJson(x));
		Stream<String> signatures = withJson.map(x -> this.signature(x));
		Set<String> result = signatures.collect(Collectors.toCollection(TreeSet::new));
		return result;
	}

	private String signature(Method method) {
		Class<?>[] parameterTypes = method.getParameterTypes();
		Stream<Class<?>> stream = Arrays.asList(parameterTypes).stream();
		Stream<String> names = stream.map(x -> x.getSimpleName());
		String parameters = names.collect(Collectors.joining(", "));
		String name = method.getName();
		String signature = name + "(" + parameters + ")";
		return signature;
	}

	private boolean receivesJson(Method method) {
		Class<?>[] parameterTypes = method.getParameterTypes();
		List<Class<?>> parameters = Arrays.asList(parameterTypes);
		boolean receivesJson = parameters.contains(CcpJsonRepresentation.class);
		return receivesJson;
	}
}
