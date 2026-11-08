package com.jn.business.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.utils.CcpDbRequester;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityExpurgableOptions;
import com.ccp.especifications.http.CcpHttpMethods;
import com.ccp.especifications.http.CcpHttpResponseType;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.implementations.password.mindrot.CcpMindrotPasswordHandler;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.jn.entities.JnEntityDisposableRecord;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/**
 * Proves that the copy of a disposable record ({@code jn_disposable_record}) answers only for the current period: reading
 * a past period with no document of its own finds nothing, instead of the record of the current period that the copy
 * keeps. The current period still finds the record.
 */
public class DisposablePastPeriodReadTest {

	static {
		CcpDependencyInjection.removeAllDependencies();
		CcpDependencyInjection.loadAllDependencies(
				CcpLocalInstances.syncMensageriaListener,
				CcpLocalInstances.bucket,
				CcpLocalInstances.email,
				CcpLocalInstances.instantMessenger,
				CcpElasticSearchQueryExecutor.INSTANCE,
				CcpElasticSearchDbRequest.INSTANCE,
				CcpMindrotPasswordHandler.INSTANCE,
				CcpLocalCacheInstances.mock,
				CcpElasticSerchDbBulk.INSTANCE,
				CcpElasticSearchCrud.INSTANCE,
				CcpGsonJsonHandler.INSTANCE,
				CcpApacheMimeHttp.INSTANCE
		);
	}

	private static final String INDEX_SCRIPT = "{\"mappings\":{\"dynamic\":\"strict\",\"properties\":{\"key\":{\"type\":\"keyword\"},\"value\":{\"type\":\"keyword\"}}}}";

	private static final String PAST_DAY = "01012020";

	private final String key = "chave" + System.currentTimeMillis();

	@BeforeClass
	public static void createTheIndexOfTheTest() {
		deleteQuietly("/" + indexName());
		database().executeHttpRequest("createTheIndexOfTheTest", "/" + indexName(), CcpHttpMethods.PUT, 200, INDEX_SCRIPT, CcpOtherConstants.EMPTY_JSON, CcpHttpResponseType.singleRecord);
	}

	@AfterClass
	public static void deleteTheIndexOfTheTest() {
		deleteQuietly("/" + indexName());
	}

	@Test
	public void theCurrentPeriodFindsTheRecord() {
		CcpJsonRepresentation record = this.savedRecord();
		CcpJsonRepresentation currentDay = record.put(JnJsonCommonsFields.disposablePeriod, CcpEntityExpurgableOptions.daily.getFormattedDate());

		assertTrue(JnEntityTestDailyDisposable.ENTITY.exists(record));
		assertTrue(JnEntityTestDailyDisposable.ENTITY.exists(currentDay));
		assertEquals("valor", JnEntityTestDailyDisposable.ENTITY.getOneById(currentDay).getAsString(JnEntityTestDailyDisposable.Fields.value));
		this.deleteTheRecord(record);
	}

	@Test
	public void aPastPeriodWithNoDocumentIsNotAnsweredByTheCopy() {
		CcpJsonRepresentation record = this.savedRecord();
		CcpJsonRepresentation pastDay = record.put(JnJsonCommonsFields.disposablePeriod, PAST_DAY);

		assertTrue("the copy of the current period exists", JnEntityDisposableRecord.ENTITY.exists(JnEntityTestDailyDisposable.ENTITY.getIdToSearchDisposableRecord(record)));
		assertFalse(JnEntityTestDailyDisposable.ENTITY.exists(pastDay));
		try {
			CcpJsonRepresentation found = JnEntityTestDailyDisposable.ENTITY.getOneById(pastDay);
			fail("a past period with no document must not be found, found " + found);
		} catch (AssertionError failure) {
			throw failure;
		} catch (RuntimeException notFound) {
			// expected: the record of the current period does not answer for a past one
		}
		this.deleteTheRecord(record);
	}

	private CcpJsonRepresentation savedRecord() {
		CcpJsonRepresentation record = CcpOtherConstants.EMPTY_JSON
				.put(JnEntityTestDailyDisposable.Fields.key, this.key)
				.put(JnEntityTestDailyDisposable.Fields.value, "valor");
		JnEntityTestDailyDisposable.ENTITY.save(record);
		return record;
	}

	private void deleteTheRecord(CcpJsonRepresentation record) {
		JnEntityTestDailyDisposable.ENTITY.delete(record);
		JnEntityDisposableRecord.ENTITY.delete(JnEntityTestDailyDisposable.ENTITY.getIdToSearchDisposableRecord(record));
	}

	private static String indexName() {
		return JnEntityTestDailyDisposable.ENTITY.getEntityMetaData().entityName;
	}

	private static CcpDbRequester database() {
		return CcpDependencyInjection.getDependency(CcpDbRequester.class);
	}

	private static void deleteQuietly(String path) {
		try {
			database().executeHttpRequest("deleteOfTheTest", path, CcpHttpMethods.DELETE, 200, CcpOtherConstants.EMPTY_JSON, CcpHttpResponseType.singleRecord);
		} catch (Throwable notThere) {
			// nothing to delete
		}
	}
}
