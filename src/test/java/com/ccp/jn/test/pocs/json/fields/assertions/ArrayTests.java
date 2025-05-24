package com.ccp.jn.test.pocs.json.fields.assertions;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import com.ccp.constantes.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.validation.enums.SimpleArrayValidations;

public class ArrayTests {
	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler(), new CcpElasticSearchCrud(),
				new CcpElasticSearchDbRequest(), new CcpApacheMimeHttp()
				);		
	}

	@Test
	public void booleanFields() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put("field1", "[false]").put("field2", "[true]");
		String[] fields = new String[] {"field1", "field2"};
		assertTrue(SimpleArrayValidations.booleanItems.isValidJson(json, fields));
	}

	@Test
	public void doubleFields() {
		String[] fields = new String[] {"field1", "field2", "field3", "field4"};
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put("field1", "[1.5]").put("field2", "[2.5, 1]").put("field3", "[3]").put("field4", "[]");
		assertTrue(SimpleArrayValidations.doubleItems.isValidJson(json, fields));
	}
	
	@Test
	public void integerFields() {
		String[] fields = new String[] {"field1", "field2", "field3", "field4"};
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put("field1", "[1]").put("field2", Arrays.asList(2)).put("field3", "[3]").put("field4", "[4]");
		assertTrue(SimpleArrayValidations.integerItems.isValidJson(json, fields));
	}
	
	@Test
	public void jsonFields() {
		String[] fields = new String[] {"field1", "field2"};
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put("field1",Arrays.asList(CcpOtherConstants.EMPTY_JSON)).put("field2", "[" +CcpOtherConstants.EMPTY_JSON.put("teste", 1) + "]");
		assertTrue(SimpleArrayValidations.jsonItems.isValidJson(json, fields));
	}


	@Test
	public void listFields() {
		String[] fields = new String[] {"field1", "field2", "field3", "field4"};
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put("field1", Arrays.asList())
				.put("field2","[[1, 2], [3, 4]]")
				.put("field3","[['A', 'B']]")
				.put("field4","[[{}], [{'nome':'onias', 'idade':38}]]")
				;
		assertTrue(SimpleArrayValidations.listItems.isValidJson(json, fields));
	}

	@Test
	public void nonRepeatedLists() {
		String[] fields = new String[] {"field1", "field2", "field3", "field4", "field5"};
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put("field1", Arrays.asList(1, 2, 3))
				.put("field2", Arrays.asList(1d, 2d, 3d))//FORGOT REVER ESSA LOGICA
				.put("field3","[['A', 'B', 'C'], ['D', 'B', 'C']]")
				.put("field4","[1.0, 2.0, 3.0]")
				.put("field5","[true, false]")
				;
		assertTrue(SimpleArrayValidations.nonRepeatedItems.isValidJson(json, fields));
	}
	@Test
	public void notEmptyArray() {
		String[] fields = new String[] {"field1", "field2", "field3", "field4", "field5"};
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put("field1", Arrays.asList(1, 2, 3))
				.put("field2", Arrays.asList(1d, 2d, 3d))//FORGOT REVER ESSA LOGICA
				.put("field3","[['A', 'B', 'C'], ['D', 'B', 'C']]")
				.put("field4","[1.0, 2.0, 3.0]")
				.put("field5","[true, false]")
				;
		assertTrue(SimpleArrayValidations.notEmptyArray.isValidJson(json, fields));
	}
}
