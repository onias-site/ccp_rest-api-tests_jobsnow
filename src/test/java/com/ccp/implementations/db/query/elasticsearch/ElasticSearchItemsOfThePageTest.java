package com.ccp.implementations.db.query.elasticsearch;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

/** {@link ElasticSearchItemsOfThePage} hands each item of a page, in order, to the consumer of items. */
public class ElasticSearchItemsOfThePageTest {

	static {
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE);
	}

	private static CcpJsonRepresentation item(int number) {
		return CcpOtherConstants.EMPTY_JSON.put(new CcpFieldName("number"), number);
	}

	@Test
	public void everyItemOfThePageReachesTheConsumerInOrder() {
		List<CcpJsonRepresentation> received = new ArrayList<>();
		List<CcpJsonRepresentation> page = List.of(item(1), item(2), item(3));

		new ElasticSearchItemsOfThePage(received::add).accept(page);

		assertEquals(page, received);
	}

	@Test
	public void anEmptyPageReachesNothing() {
		List<CcpJsonRepresentation> received = new ArrayList<>();

		new ElasticSearchItemsOfThePage(received::add).accept(List.of());

		assertEquals(0, received.size());
	}
}
