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
import com.ccp.validation.enums.ObjectTextSizeValidations;

public class ObjectTextsSizeTests {
	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler(), new CcpElasticSearchCrud(),
				new CcpElasticSearchDbRequest(), new CcpApacheMimeHttp()
				);		
	}
	
	
	@Test
	public void equalsTo() {
		String str = "onias";
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put("field1", str)
				.put("field2", str)
				;
		
//		assertTrue(givenTheJson.itIsTrueThatTheFollowingFields("field1", "field2").ifTheyAreAll().textsThenEachOneHasTheSizeThatIs().equalsTo(5d));
		assertTrue(ObjectTextSizeValidations.equalsTo.isValidJson(json, 5d, "field1", "field2"));

	}

	@Test
	public void equalsOrGreaterThan() {
		String str = "onias";
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put("field1", str)
				.put("field2", str) 

				;
//		assertTrue(givenTheJson.itIsTrueThatTheFollowingFields("field1", "field2").ifTheyAreAll().textsThenEachOneHasTheSizeThatIs().equalsOrGreaterThan(4d));
//		assertTrue(givenTheJson.itIsTrueThatTheFollowingFields("field1", "field2").ifTheyAreAll().textsThenEachOneHasTheSizeThatIs().equalsOrGreaterThan(5d));
		assertTrue(ObjectTextSizeValidations.equalsOrGreaterThan.isValidJson(json, 4d, "field1", "field2"));
		assertTrue(ObjectTextSizeValidations.equalsOrGreaterThan.isValidJson(json, 5d, "field1", "field2"));
	}

	@Test
	public void equalsOrLessThan() {
		String str = "onias";
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put("field1", str)
				.put("field2", str) 

				;
//		assertTrue(givenTheJson.itIsTrueThatTheFollowingFields("field1", "field2").ifTheyAreAll().textsThenEachOneHasTheSizeThatIs().equalsOrLessThan(6d));
//		assertTrue(givenTheJson.itIsTrueThatTheFollowingFields("field1", "field2").ifTheyAreAll().textsThenEachOneHasTheSizeThatIs().equalsOrLessThan(5d));
		assertTrue(ObjectTextSizeValidations.equalsOrLessThan.isValidJson(json, 6d, "field1", "field2"));
		assertTrue(ObjectTextSizeValidations.equalsOrLessThan.isValidJson(json, 5d, "field1", "field2"));
	}

	
	@Test
	public void greaterThan() {
		String str = "onias";
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put("field1", str)
				.put("field2", str) 

				;
//		assertTrue(givenTheJson.itIsTrueThatTheFollowingFields("field1", "field2").ifTheyAreAll().textsThenEachOneHasTheSizeThatIs().greaterThan(4d));
		assertTrue(ObjectTextSizeValidations.greaterThan.isValidJson(json, 4d, "field1", "field2"));
	}

	@Test
	public void lessThan() {
		String str = "onias";
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put("field1", str)
				.put("field2", str) 
				;
//		assertTrue(givenTheJson.itIsTrueThatTheFollowingFields("field1", "field2").ifTheyAreAll().textsThenEachOneHasTheSizeThatIs().lessThan(6d));
		assertTrue(ObjectTextSizeValidations.lessThan.isValidJson(json, 6d, "field1", "field2"));
	}

	
}
