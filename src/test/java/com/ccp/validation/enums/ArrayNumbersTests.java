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

public class ArrayNumbersTests {
	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler(), new CcpElasticSearchCrud(),
				new CcpElasticSearchDbRequest(), new CcpApacheMimeHttp()
				);		
	}
	
	
	@Test
	public void equalsTo() {
		List<Integer> asList = Arrays.asList(5,5,5);
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put("field1", asList)
				.put("field2", asList)

				;
//		assertTrue(givenTheJson.itIsTrueThatTheFollowingFields("field1", "field2").ifTheyAreAllArrayValuesThenEachOne().isNumberAndItIs().equalsTo(5d));
		assertTrue(CcpArrayNumbersValidations.equalsTo.isValidJson(json, 5d, "field1", "field2"));
	}

	@Test
	public void equalsOrGreaterThan() {
		List<Integer> asList = Arrays.asList(5,5,5);
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put("field1", asList)
				.put("field2", asList) 

				;
//		assertTrue(json.itIsTrueThatTheFollowingFields("field1", "field2").ifTheyAreAllArrayValuesThenEachOne().isNumberAndItIs().equalsOrGreaterThan(4d));
//		assertTrue(json.itIsTrueThatTheFollowingFields("field1", "field2").ifTheyAreAllArrayValuesThenEachOne().isNumberAndItIs().equalsOrGreaterThan(5d));
		assertTrue(CcpArrayNumbersValidations.equalsOrGreaterThan.isValidJson(json, 4d, "field1", "field2"));
		assertTrue(CcpArrayNumbersValidations.equalsOrGreaterThan.isValidJson(json, 5d, "field1", "field2"));
	}

	@Test
	public void equalsOrLessThan() {
		List<Integer> asList = Arrays.asList(5,5,5);
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put("field1", asList)
				.put("field2", asList) 

				;
//		assertTrue(givenTheJson.itIsTrueThatTheFollowingFields("field1", "field2").ifTheyAreAllArrayValuesThenEachOne().isNumberAndItIs().equalsOrLessThan(6d));
//		assertTrue(givenTheJson.itIsTrueThatTheFollowingFields("field1", "field2").ifTheyAreAllArrayValuesThenEachOne().isNumberAndItIs().equalsOrLessThan(5d));
		assertTrue(CcpArrayNumbersValidations.equalsOrLessThan.isValidJson(json, 6d, "field1", "field2"));
		assertTrue(CcpArrayNumbersValidations.equalsOrLessThan.isValidJson(json, 5d, "field1", "field2"));
	}

	
	@Test
	public void greaterThan() {
		List<Integer> asList = Arrays.asList(5,5,5);
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put("field1", asList)
				.put("field2", asList) 

				;
//		assertTrue(givenTheJson.itIsTrueThatTheFollowingFields("field1", "field2").ifTheyAreAllArrayValuesThenEachOne().isNumberAndItIs().greaterThan(4d));
		assertTrue(CcpArrayNumbersValidations.greaterThan.isValidJson(json, 4d, "field1", "field2"));
	}

	@Test
	public void lessThan() {
		List<Integer> asList = Arrays.asList(0,1,-2);
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put("field1", asList)
				.put("field2", Arrays.asList(3,4,5)) 
				;
//		assertTrue(givenTheJson.itIsTrueThatTheFollowingFields("field1", "field2").ifTheyAreAllArrayValuesThenEachOne().isNumberAndItIs().lessThan(6d));
		assertTrue(CcpArrayNumbersValidations.lessThan.isValidJson(json, 6d, "field1", "field2"));
	}

	
}
