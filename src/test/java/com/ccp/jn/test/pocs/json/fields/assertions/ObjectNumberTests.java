package com.ccp.jn.test.pocs.json.fields.assertions;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.ccp.constantes.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.validation.enums.ObjectNumberValidations;

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
		assertTrue(ObjectNumberValidations.equalsTo.isValidJson(json, 5d, "field1", "field2"));

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
		assertTrue(ObjectNumberValidations.equalsOrGreaterThan.isValidJson(json, 5d, "field1", "field2"));
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
		assertTrue(ObjectNumberValidations.equalsOrLessThan.isValidJson(json, 6d, "field1", "field2"));
		assertTrue(ObjectNumberValidations.equalsOrLessThan.isValidJson(json, 5d, "field1", "field2"));
	}

	
	@Test
	public void greaterThan() {
		int number = 5;;
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put("field1", number)
				.put("field2", number) 

				;
//		assertTrue(givenTheJson.itIsTrueThatTheFollowingFields("field1", "field2").ifTheyAreAll().numbersThenEachOneIs().greaterThan(4d));
		assertTrue(ObjectNumberValidations.greaterThan.isValidJson(json, 4d, "field1", "field2"));
}

	@Test
	public void lessThan() {
		int number = 5;
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put("field1", number)
				.put("field2", 5) 
				;
//		assertTrue(givenTheJson.itIsTrueThatTheFollowingFields("field1", "field2").ifTheyAreAll().numbersThenEachOneIs().lessThan(6d));
		assertTrue(ObjectNumberValidations.lessThan.isValidJson(json, 6d, "field1", "field2"));
	}

	
}
