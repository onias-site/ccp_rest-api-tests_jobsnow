package com.ccp.especifications.db.crud;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityMetaData;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.json.fields.validation.CcpJsonCommonsFields;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.jn.entities.JnEntityJobsnowError;

/**
 * Ensures the shape of the condensed map of {@code CcpSelectUnionAll}: {@code explainedSearch}
 * lives inside the record itself (not as a sibling {@code explainedSearch.<id>}) and stays
 * present even when the record was not found, so that an audit knows how the search was made
 * despite the empty result.
 */
public class CcpSelectUnionAllExplainedSearchTest {

	static {
		CcpDependencyInjection.loadAllDependencies(
				new CcpGsonJsonHandler(),
				new CcpApacheMimeHttp(),
				new CcpElasticSearchDbRequest(),
				new CcpElasticSearchCrud(),
				CcpLocalCacheInstances.mock);
	}

	private static final CcpEntity ENTITY = JnEntityJobsnowError.ENTITY;

	private static final String STACK_TRACE_HASH = "testhash";

	private static final String TYPE = "testtype";

	private static String entityName() {
		CcpEntityMetaData entityMetaData = ENTITY.getEntityMetaData();
		String entityName = entityMetaData.entityName;
		return entityName;
	}

	private static CcpJsonRepresentation searchParameter() {
		CcpJsonRepresentation searchParameterJson = CcpOtherConstants.EMPTY_JSON
				.put(JnEntityJobsnowError.Fields.stackTraceHash, STACK_TRACE_HASH)
				.put(JnEntityJobsnowError.Fields.type, TYPE);
		return searchParameterJson;
	}

	private static CcpSelectUnionAll unionAllWithNotFoundRecord() {
		CcpJsonRepresentation searchParameter = searchParameter();
		String id = ENTITY.calculateId(searchParameter);
		CcpJsonRepresentation recordNotFound = CcpOtherConstants.EMPTY_JSON
				.put(CcpJsonCommonsFields._id, id)
				.put(CcpJsonCommonsFields._index, entityName());
		List<CcpJsonRepresentation> results = new ArrayList<>(Arrays.asList(recordNotFound));
		CcpSelectUnionAll unionAll = new CcpSelectUnionAll(new CcpJsonRepresentation[] { searchParameter }, results, ENTITY);
		return unionAll;
	}

	private static CcpJsonRepresentation condensedRecord() {
		CcpSelectUnionAll unionAll = unionAllWithNotFoundRecord();
		String id = ENTITY.calculateId(searchParameter());
		CcpFieldName entityFieldName = new CcpFieldName(entityName());
		CcpFieldName idFieldName = new CcpFieldName(id);
		CcpJsonRepresentation record = unionAll.condensed.getInnerJsonFromPath(entityFieldName, idFieldName);
		return record;
	}

	@Test
	public void explainedSearchIsSubElementOfRecordTest() {
		CcpJsonRepresentation record = condensedRecord();
		CcpFieldName explainedSearchFieldName = new CcpFieldName("explainedSearch");
		boolean containsExplainedSearch = record.containsAllFields(explainedSearchFieldName);
		assertTrue(containsExplainedSearch);
	}

	@Test
	public void siblingExplainedSearchDotIdNoLongerExistsTest() {
		CcpSelectUnionAll unionAll = unionAllWithNotFoundRecord();
		String id = ENTITY.calculateId(searchParameter());
		CcpFieldName entityFieldName = new CcpFieldName(entityName());
		CcpJsonRepresentation entityRows = unionAll.condensed.getInnerJson(entityFieldName);
		String siblingKey = "explainedSearch." + id;
		CcpFieldName siblingFieldName = new CcpFieldName(siblingKey);
		boolean containsSibling = entityRows.containsAllFields(siblingFieldName);
		assertFalse(containsSibling);
	}

	@Test
	public void recordNotFoundKeepsExplainedSearchTest() {
		CcpJsonRepresentation record = condensedRecord();
		CcpFieldName explainedSearchFieldName = new CcpFieldName("explainedSearch");
		CcpJsonRepresentation explainedSearch = record.getInnerJson(explainedSearchFieldName);
		String stackTraceHash = explainedSearch.getAsString(JnEntityJobsnowError.Fields.stackTraceHash);
		String type = explainedSearch.getAsString(JnEntityJobsnowError.Fields.type);
		assertEquals(STACK_TRACE_HASH, stackTraceHash);
		assertEquals(TYPE, type);
	}

	@Test
	public void recordNotFoundStaysAbsentForCallerTest() {
		CcpSelectUnionAll unionAll = unionAllWithNotFoundRecord();
		String id = ENTITY.calculateId(searchParameter());
		boolean present = unionAll.isPresent(entityName(), id);
		assertFalse(present);
	}

	@Test
	public void getEntityRowDoesNotReturnExplainedSearchTest() {
		CcpSelectUnionAll unionAll = unionAllWithNotFoundRecord();
		String id = ENTITY.calculateId(searchParameter());
		CcpJsonRepresentation entityRow = unionAll.getEntityRow(entityName(), id);
		boolean empty = entityRow.isEmpty();
		assertTrue(empty);
	}
}
