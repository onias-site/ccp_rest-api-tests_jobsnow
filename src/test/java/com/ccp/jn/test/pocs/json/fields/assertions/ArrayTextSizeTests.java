package com.ccp.jn.test.pocs.json.fields.assertions;

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
import com.ccp.validation.enums.ArrayTextSizeValidations;

public class ArrayTextSizeTests {
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
		
//		assertTrue(json.itIsTrueThatTheFollowingFields("field1", "field2").ifTheyAreAllArrayValuesThenEachOne().isTextAndHasSizeThatIs().equalsTo(5d));
		assertTrue(ArrayTextSizeValidations.equalsTo.isValidJson(json, 5d, "field1", "field2"));
	}

	@Test
	public void equalsOrGreaterThan() {
		List<String> asList = Arrays.asList("onias", "saino", "teste");
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put("field1", asList)
				.put("field2", asList) 

				;
//		assertTrue(json.itIsTrueThatTheFollowingFields("field1", "field2").ifTheyAreAllArrayValuesThenEachOne().isTextAndHasSizeThatIs().equalsOrGreaterThan(4d));
//		assertTrue(json.itIsTrueThatTheFollowingFields("field1", "field2").ifTheyAreAllArrayValuesThenEachOne().isTextAndHasSizeThatIs().equalsOrGreaterThan(5d));
		assertTrue(ArrayTextSizeValidations.equalsOrGreaterThan.isValidJson(json, 4d, "field1", "field2"));
		assertTrue(ArrayTextSizeValidations.equalsOrGreaterThan.isValidJson(json, 5d, "field1", "field2"));
	}

	@Test
	public void equalsOrLessThan() {
		List<String> asList = Arrays.asList("onias", "saino", "teste");
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put("field1", asList)
				.put("field2", asList) 

				;
//		assertTrue(givenTheJson.itIsTrueThatTheFollowingFields("field1", "field2").ifTheyAreAllArrayValuesThenEachOne().isTextAndHasSizeThatIs().equalsOrLessThan(6d));
//		assertTrue(givenTheJson.itIsTrueThatTheFollowingFields("field1", "field2").ifTheyAreAllArrayValuesThenEachOne().isTextAndHasSizeThatIs().equalsOrLessThan(5d));
		assertTrue(ArrayTextSizeValidations.equalsOrLessThan.isValidJson(json, 6d, "field1", "field2"));
		assertTrue(ArrayTextSizeValidations.equalsOrLessThan.isValidJson(json, 5d, "field1", "field2"));
	}

	
	@Test
	public void greaterThan() {
		List<String> asList = Arrays.asList("onias", "saino", "teste");
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put("field1", asList)
				.put("field2", asList) 

				;
//		assertTrue(givenTheJson.itIsTrueThatTheFollowingFields("field1", "field2").ifTheyAreAllArrayValuesThenEachOne().isTextAndHasSizeThatIs().greaterThan(4d));
		assertTrue(ArrayTextSizeValidations.greaterThan.isValidJson(json, 4.9999999d, "field1", "field2"));
	}

	@Test
	public void lessThan() {
		List<String> asList = Arrays.asList("onias", "saino", "teste");
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put("field1", asList)
				.put("field2", Arrays.asList("onias", "saino", "teste")) 
				;
//		assertTrue(givenTheJson.itIsTrueThatTheFollowingFields("field1", "field2").ifTheyAreAllArrayValuesThenEachOne().isTextAndHasSizeThatIs().lessThan(6d));
		assertTrue(ArrayTextSizeValidations.lessThan.isValidJson(json, 6d, "field1", "field2"));
	}

	
}
