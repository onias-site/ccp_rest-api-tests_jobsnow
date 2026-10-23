package com.ccp.rest.api.spring.openapi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.PathParameter;
import io.swagger.v3.oas.models.parameters.QueryParameter;

/**
 * {@link CcpMissingPathParamsCustomizer} declares, in every operation of a path, the template variables of the path
 * that the operation did not declare as path parameters, required and typed as string; the ones already declared are
 * kept as they are, and a path with no variable or a document with no path is left untouched.
 */
public class CcpMissingPathParamsCustomizerTest {

	private static List<String> pathParameterNames(Operation operation) {
		return operation.getParameters().stream().filter(parameter -> "path".equals(parameter.getIn())).map(Parameter::getName).collect(Collectors.toList());
	}

	@Test
	public void theUndeclaredVariablesOfThePathAreAddedToEveryOperation() {
		Operation get = new Operation();
		Operation delete = new Operation().addParametersItem(new PathParameter().name("email"));
		PathItem pathItem = new PathItem().get(get).delete(delete);
		OpenAPI openApi = new OpenAPI().paths(new Paths().addPathItem("/resume/{email}/skill/{skill}", pathItem));

		CcpMissingPathParamsCustomizer.INSTANCE.customise(openApi);

		List<String> getParameters = pathParameterNames(get);
		assertEquals(2, getParameters.size());
		assertTrue(getParameters.containsAll(List.of("email", "skill")));
		assertEquals(List.of("email", "skill"), pathParameterNames(delete));
		Parameter added = delete.getParameters().get(1);
		assertTrue(added.getRequired());
		assertEquals("string", added.getSchema().getType());
	}

	@Test
	public void aQueryParameterWithTheSameNameDoesNotCountAsDeclared() {
		Operation get = new Operation().addParametersItem(new QueryParameter().name("email"));
		OpenAPI openApi = new OpenAPI().paths(new Paths().addPathItem("/resume/{email}", new PathItem().get(get)));

		CcpMissingPathParamsCustomizer.INSTANCE.customise(openApi);

		assertEquals(2, get.getParameters().size());
		assertEquals(List.of("email"), pathParameterNames(get));
	}

	@Test
	public void aPathWithoutVariablesIsLeftUntouched() {
		Operation get = new Operation();
		OpenAPI openApi = new OpenAPI().paths(new Paths().addPathItem("/resume", new PathItem().get(get)));

		CcpMissingPathParamsCustomizer.INSTANCE.customise(openApi);

		assertNull(get.getParameters());
	}

	@Test
	public void aDocumentWithoutPathsIsLeftUntouched() {
		OpenAPI openApi = new OpenAPI();

		CcpMissingPathParamsCustomizer.INSTANCE.customise(openApi);

		assertNull(openApi.getPaths());
	}
}
