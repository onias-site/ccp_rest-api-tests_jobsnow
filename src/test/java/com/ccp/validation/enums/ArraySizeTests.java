package com.ccp.validation.enums;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.ccp.constantes.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

public class ArraySizeTests {
	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler(), new CcpElasticSearchCrud(),
				new CcpElasticSearchDbRequest(), new CcpApacheMimeHttp()
				);		
	}
	@Test
	public void equalsTo() {
		List<String> asList = Arrays.asList("onias", "saino", "teste");
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put("field1", asList)
				.put("field2", asList)

				;
//		assertTrue(givenTheJson.itIsTrueThatTheFollowingFields("field1", "field2").ifTheyAreAllArrayValuesThenEachOne().hasTheSizeThatIs().equalsTo(3d));
		assertTrue(CcpArraySizeValidations.equalsTo.isValidJson(json, 3d, "field1", "field2"));
	}

	@Test
	public void greaterOrEqualsTo() {
		List<String> asList = Arrays.asList("onias", "saino", "teste");
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put("field1", asList)
				.put("field2", asList) 

				;
//		assertTrue(json.itIsTrueThatTheFollowingFields("field1", "field2").ifTheyAreAllArrayValuesThenEachOne().hasTheSizeThatIs().equalsOrGreaterThan(2d));
//		assertTrue(json.itIsTrueThatTheFollowingFields("field1", "field2").ifTheyAreAllArrayValuesThenEachOne().hasTheSizeThatIs().equalsOrGreaterThan(3d));
		assertTrue(CcpArraySizeValidations.equalsOrGreaterThan.isValidJson(json, 2d, "field1", "field2"));
		assertTrue(CcpArraySizeValidations.equalsOrGreaterThan.isValidJson(json, 3d, "field1", "field2"));
	}

	@Test
	public void equalsOrLessThan() {
		List<String> asList = Arrays.asList("onias", "saino", "teste");
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put("field1", asList)
				.put("field2", asList) 

				;
//		assertTrue(json.itIsTrueThatTheFollowingFields("field1", "field2").ifTheyAreAllArrayValuesThenEachOne().hasTheSizeThatIs().equalsOrLessThan(4d));
//		assertTrue(json.itIsTrueThatTheFollowingFields("field1", "field2").ifTheyAreAllArrayValuesThenEachOne().hasTheSizeThatIs().equalsOrLessThan(3d));
		assertTrue(CcpArraySizeValidations.equalsOrLessThan.isValidJson(json, 4d, "field1", "field2"));
		assertTrue(CcpArraySizeValidations.equalsOrLessThan.isValidJson(json, 3d, "field1", "field2"));
	}

	
	@Test
	public void greaterThan() {
		List<String> asList = Arrays.asList("onias", "saino", "teste");
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put("field1", asList)
				.put("field2", asList) 

				;
//		assertTrue(givenTheJson.itIsTrueThatTheFollowingFields("field1", "field2").ifTheyAreAllArrayValuesThenEachOne().hasTheSizeThatIs().greaterThan(2d));
		assertTrue(CcpArraySizeValidations.greaterThan.isValidJson(json, 2d, "field1", "field2"));
	}

	@Test
	public void lessThan() {
		List<String> asList = Arrays.asList("onias", "saino", "teste");
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put("field1", asList)
				.put("field2", Arrays.asList("onias", "saino", "teste")) 
				;
		
//		assertTrue(givenTheJson.itIsTrueThatTheFollowingFields("field1", "field2").ifTheyAreAllArrayValuesThenEachOne().hasTheSizeThatIs().lessThan(4d));
		assertTrue(CcpArraySizeValidations.lessThan.isValidJson(json, 4d, "field1", "field2"));
	}

	
}
