package com.ccp.validation.enums;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.ccp.constantes.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

public class ObjectNumberTests {
	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler(), new CcpElasticSearchCrud(),
				new CcpElasticSearchDbRequest(), new CcpApacheMimeHttp()
				);		
	}
	
	
	@Test
	public void equalsTo() {
		int number = 5;
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put("field1", number)
				.put("field2", number)

				;
//		givenTheJson.itIsTrueThatTheFollowingFields("field1", "field2").ifTheyAreAllArrayValuesThenEachOne().isNumberAndItIs();
//		assertTrue(givenTheJson.itIsTrueThatTheFollowingFields("field1", "field2").ifTheyAreAll().numbersThenEachOneIs().equalsTo(5d));
		assertTrue(CcpObjectNumberValidations.equalsTo.isValidJson(json, 5d, "field1", "field2"));

	}

	@Test
	public void equalsOrGreaterThan() {
		int number = 5;;
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put("field1", number)
				.put("field2", number) 

				;
//		assertTrue(givenTheJson.itIsTrueThatTheFollowingFields("field1", "field2").ifTheyAreAll().numbersThenEachOneIs().equalsOrGreaterThan(4d));
//		assertTrue(givenTheJson.itIsTrueThatTheFollowingFields("field1", "field2").ifTheyAreAll().numbersThenEachOneIs().equalsOrGreaterThan(5d));
		assertTrue(CcpObjectNumberValidations.equalsOrGreaterThan.isValidJson(json, 5d, "field1", "field2"));
	}

	@Test
	public void equalsOrLessThan() {
		int number = 5;
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put("field1", number)
				.put("field2", number) 

				;
//		assertTrue(givenTheJson.itIsTrueThatTheFollowingFields("field1", "field2").ifTheyAreAll().numbersThenEachOneIs().equalsOrLessThan(6d));
//		assertTrue(givenTheJson.itIsTrueThatTheFollowingFields("field1", "field2").ifTheyAreAll().numbersThenEachOneIs().equalsOrLessThan(5d));
		assertTrue(CcpObjectNumberValidations.equalsOrLessThan.isValidJson(json, 6d, "field1", "field2"));
		assertTrue(CcpObjectNumberValidations.equalsOrLessThan.isValidJson(json, 5d, "field1", "field2"));
	}

	
	@Test
	public void greaterThan() {
		int number = 5;;
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put("field1", number)
				.put("field2", number) 

				;
//		assertTrue(givenTheJson.itIsTrueThatTheFollowingFields("field1", "field2").ifTheyAreAll().numbersThenEachOneIs().greaterThan(4d));
		assertTrue(CcpObjectNumberValidations.greaterThan.isValidJson(json, 4d, "field1", "field2"));
}

	@Test
	public void lessThan() {
		int number = 5;
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put("field1", number)
				.put("field2", 5) 
				;
//		assertTrue(givenTheJson.itIsTrueThatTheFollowingFields("field1", "field2").ifTheyAreAll().numbersThenEachOneIs().lessThan(6d));
		assertTrue(CcpObjectNumberValidations.lessThan.isValidJson(json, 6d, "field1", "field2"));
	}

	
}
