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
		assertTrue(CcpObjectTextSizeValidations.equalsTo.isValidJson(json, 5d, "field1", "field2"));

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
		assertTrue(CcpObjectTextSizeValidations.equalsOrGreaterThan.isValidJson(json, 4d, "field1", "field2"));
		assertTrue(CcpObjectTextSizeValidations.equalsOrGreaterThan.isValidJson(json, 5d, "field1", "field2"));
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
		assertTrue(CcpObjectTextSizeValidations.equalsOrLessThan.isValidJson(json, 6d, "field1", "field2"));
		assertTrue(CcpObjectTextSizeValidations.equalsOrLessThan.isValidJson(json, 5d, "field1", "field2"));
	}

	
	@Test
	public void greaterThan() {
		String str = "onias";
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put("field1", str)
				.put("field2", str) 

				;
//		assertTrue(givenTheJson.itIsTrueThatTheFollowingFields("field1", "field2").ifTheyAreAll().textsThenEachOneHasTheSizeThatIs().greaterThan(4d));
		assertTrue(CcpObjectTextSizeValidations.greaterThan.isValidJson(json, 4d, "field1", "field2"));
	}

	@Test
	public void lessThan() {
		String str = "onias";
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put("field1", str)
				.put("field2", str) 
				;
//		assertTrue(givenTheJson.itIsTrueThatTheFollowingFields("field1", "field2").ifTheyAreAll().textsThenEachOneHasTheSizeThatIs().lessThan(6d));
		assertTrue(CcpObjectTextSizeValidations.lessThan.isValidJson(json, 6d, "field1", "field2"));
	}

	
}
