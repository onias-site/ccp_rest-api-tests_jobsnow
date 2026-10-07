package com.ccp.json.defaultvalues.annotations;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.service.CcpService;

/**
 * Proves that a service called through {@link CcpService#execute(Map)} (the way the REST endpoints call it) gets the
 * default values of {@code @CcpJsonFieldDefaultValue}, as {@code CcpBusiness.execute} already did. Until 2026-10-06 the
 * REST path skipped them, and a field with a default value (not required for that very reason) reached the service empty.
 */
public class ServiceDefaultValuesTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	/** A service that answers the JSON it got. */
	static class EchoService implements CcpService {
		public CcpJsonRepresentation apply(CcpJsonRepresentation json) {
			return json;
		}

		public Class<?> getJsonValidationClass() {
			return RulesDefaultValueWithRequired.class;
		}

		public String name() {
			return "echoService";
		}
	}

	@Test
	public void anAbsentFieldGetsItsDefaultValueThroughTheRestPath() {
		Map<String, Object> input = new HashMap<>();

		Map<String, Object> output = new EchoService().execute(input);

		assertEquals("preenchido pelo valor padrao", output.get(RulesDefaultValueWithRequired.obrigatorioComValorPadrao.name()));
		assertEquals("chave gerada", output.get(RulesDefaultValueWithRequired.chavePrimariaComValorPadrao.name()));
	}

	@Test
	public void aPresentFieldKeepsItsValueThroughTheRestPath() {
		Map<String, Object> input = new HashMap<>();
		input.put(RulesDefaultValueWithRequired.obrigatorioComValorPadrao.name(), "sent by the caller");

		Map<String, Object> output = new EchoService().execute(input);

		assertEquals("sent by the caller", output.get(RulesDefaultValueWithRequired.obrigatorioComValorPadrao.name()));
	}
}
