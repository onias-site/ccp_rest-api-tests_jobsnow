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
import com.ccp.validation.enums.SimpleObjectValidations;

public class ObjectTests {
	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler(), new CcpElasticSearchCrud(),
				new CcpElasticSearchDbRequest(), new CcpApacheMimeHttp()
				);		
	}

	@Test
	public void booleanFields() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put("field1", "false").put("field2", true);
		assertTrue(SimpleObjectValidations.booleanFields.isValidJson(json, "field1", "field2"));
	}

	@Test
	public void doubleFields() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put("field1", "1.5").put("field2", 2.5).put("field3", 3).put("field4", 4);
		assertTrue(SimpleObjectValidations.doubleFields.isValidJson(json, "field1", "field2", "field3", "field4"));
}
	
	@Test
	public void integerFields() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put("field1", "1").put("field2", 2).put("field3", 3.0).put("field4", "4.0");
		assertTrue(SimpleObjectValidations.integerFields.isValidJson(json, "field1", "field2", "field3", "field4"));
	}
	
	@Test
	public void jsonFields() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put("field1", CcpOtherConstants.EMPTY_JSON).put("field2", CcpOtherConstants.EMPTY_JSON.put("teste", 1));
		assertTrue(SimpleObjectValidations.jsonFields.isValidJson(json, "field1", "field2", "field3", "field4"));
	}


	@Test
	public void listFields() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put("field1", Arrays.asList()).put("field2","[1, 2]").put("field3","['A', 'B']").put("field4","[{}, {'nome':'onias', 'idade':38}]");
		assertTrue(SimpleObjectValidations.listFields.isValidJson(json, "field1", "field2", "field3", "field4"));
	}

	@Test
	public void nonRepeatedLists() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put("field1", Arrays.asList(1, 2, 3))
				.put("field2", Arrays.asList(1d, 2d, 3d))
				.put("field3","['A', 'B', 'C']")
				.put("field4","[1.0, 2.0, 3.0]")
				.put("field5","[true, false]")
				;
		assertTrue(SimpleObjectValidations.nonRepeatedLists.isValidJson(json, "field1", "field2", "field3", "field4", "field5"));
	}
}
