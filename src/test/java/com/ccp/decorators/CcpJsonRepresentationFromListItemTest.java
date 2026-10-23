package com.ccp.decorators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

/** {@link CcpJsonRepresentationFromListItem} turns an item of a list into a json: a json stays as it is, a map is wrapped. */
public class CcpJsonRepresentationFromListItemTest {

	static {
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE);
	}

	@Test
	public void aJsonItemIsReturnedAsItIs() {
		CcpJsonRepresentation item = CcpOtherConstants.EMPTY_JSON.put(new CcpFieldName("name"), "x");

		assertSame(item, CcpJsonRepresentationFromListItem.INSTANCE.apply(item));
	}

	@Test
	public void aMapItemIsWrapped() {
		Map<String, Object> item = new LinkedHashMap<>();
		item.put("name", "x");

		CcpJsonRepresentation json = CcpJsonRepresentationFromListItem.INSTANCE.apply(item);

		assertEquals("x", json.getAsString(new CcpFieldName("name")));
	}
}
