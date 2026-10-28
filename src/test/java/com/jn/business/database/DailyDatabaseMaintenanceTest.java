package com.jn.business.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpTimeDecorator;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.utils.CcpDbRequester;
import com.ccp.especifications.db.utils.entity.CcpEntity;
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
import com.jn.entities.JnEntityVirtualDisposableRecordToPurge;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.utils.JnGroupers;

/**
 * Proves the daily maintenance of the database (2026-10-28): every write of a disposable entity whose expired records are
 * purged ({@code removeCompletely}, the default) keeps the document of its period in the group of the record among the
 * records to purge; the purge deletes the documents whose period has ended, the copy in {@code jn_disposable_record} once
 * everything has expired, and takes them out of the group; an entity with {@code removeCompletely = false} is never in a
 * group nor purged; the backup is a snapshot of the given entities in the repository of the bucket; and the daily process
 * purges before it backs up.
 * <p>
 * A record of a past period cannot be written yet (the {@code disposablePeriod} of a save does not reach the decorator),
 * so the expired records come from entities of the tests by the second, after a pause of more than a second, and the
 * document of an expired period next to a valid one is put in the group by hand. Each test uses keys of its own in the
 * indexes of the tests, created here.
 */
public class DailyDatabaseMaintenanceTest {

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

	private static final String TEST_BUCKET = "jobsnow-backup-test";

	private static final String INDEX_SCRIPT = "{\"mappings\":{\"dynamic\":\"strict\",\"properties\":{\"key\":{\"type\":\"keyword\"},\"value\":{\"type\":\"keyword\"}}}}";

	private static final List<CcpEntity> TEST_ENTITIES = List.of(JnEntityTestPurgedDisposable.ENTITY, JnEntityTestPurgedEverySecond.ENTITY, JnEntityTestKeptDisposable.ENTITY);

	private final String key = "chave" + System.currentTimeMillis();

	@BeforeClass
	public static void createTheIndexesOfTheTests() {
		for (CcpEntity entity : TEST_ENTITIES) {
			String entityName = entity.getEntityMetaData().entityName;
			deleteQuietly("/" + entityName);
			database().executeHttpRequest("createTheIndexOfTheTest", "/" + entityName, CcpHttpMethods.PUT, 200, INDEX_SCRIPT, CcpOtherConstants.EMPTY_JSON, CcpHttpResponseType.singleRecord);
		}
	}

	@AfterClass
	public static void deleteTheIndexesAndTheRepositoryOfTheTests() {
		for (CcpEntity entity : TEST_ENTITIES) {
			deleteQuietly("/" + entity.getEntityMetaData().entityName);
		}
		deleteQuietly("/_snapshot/" + TEST_BUCKET + "/_all");
		deleteQuietly("/_snapshot/" + TEST_BUCKET);
	}

	@Test
	public void theEndOfAPeriodIsItsFirstInstantPlusOnePeriodInBrazil() {
		long secondOfJanuary = new CcpTimeDecorator("02012020 00:00", "ddMMyyyy HH:mm").content;
		assertEquals(Long.valueOf(secondOfJanuary), CcpEntityExpurgableOptions.daily.getEndOfThePeriod("01012020"));
		assertEquals(Long.valueOf(secondOfJanuary), CcpEntityExpurgableOptions.hourly.getEndOfThePeriod("01012020 23"));
		long firstOfMarch = new CcpTimeDecorator("01032020 00:00", "ddMMyyyy HH:mm").content;
		assertEquals(Long.valueOf(firstOfMarch), CcpEntityExpurgableOptions.monthly.getEndOfThePeriod("202002"));
		long firstOfJanuary = new CcpTimeDecorator("01012020", "ddMMyyyy").content;
		assertEquals("01/01/2020 00:00", new CcpTimeDecorator(firstOfJanuary).getFormattedDateTime("dd/MM/yyyy HH:mm"));
	}

	@Test
	public void theWriteOfAPurgedEntityPutsTheDocumentOfItsPeriodInTheGroupOfTheRecord() {
		CcpJsonRepresentation record = this.record();
		JnEntityTestPurgedDisposable.ENTITY.save(record);

		List<String> children = groupOf(JnEntityTestPurgedDisposable.ENTITY, this.key);
		assertEquals(1, children.size());
		CcpJsonRepresentation child = new CcpJsonRepresentation(children.get(0));
		assertEquals(JnEntityTestPurgedDisposable.ENTITY.getEntityMetaData().entityName, child.getAsString(JnJsonCommonsFields.entity));
		assertEquals(JnEntityTestPurgedDisposable.ENTITY.calculateId(record), child.getAsString(JnJsonCommonsFields.id));
		String currentMinute = CcpEntityExpurgableOptions.minute.getFormattedDate();
		Long endOfTheMinute = CcpEntityExpurgableOptions.minute.getEndOfThePeriod(currentMinute);
		Long childTimestamp = child.getAsLongNumber(JnJsonCommonsFields.timestamp);
		// the minute may have turned between the save and this check
		assertTrue(childTimestamp.equals(endOfTheMinute) || childTimestamp.equals(endOfTheMinute - 60_000));

		// the same period again does not repeat the child, and a delete adds none
		JnEntityTestPurgedDisposable.ENTITY.save(record);
		JnEntityTestPurgedDisposable.ENTITY.delete(record);
		List<String> childrenAfterTheDelete = groupOf(JnEntityTestPurgedDisposable.ENTITY, this.key);
		assertTrue(childrenAfterTheDelete.containsAll(children));
		assertTrue(childrenAfterTheDelete.size() <= 2);
	}

	@Test
	public void theWriteOfAKeptEntityPutsNothingInTheGroupsAndIsNeverPurged() {
		CcpJsonRepresentation record = this.record();
		waitForTheStartOfASecond();
		JnEntityTestKeptDisposable.ENTITY.save(record);
		String id = JnEntityTestKeptDisposable.ENTITY.calculateId(record);
		assertTrue(groupOf(JnEntityTestKeptDisposable.ENTITY, this.key).isEmpty());

		waitForTheTurnOfTheSecond();
		purge();

		assertTrue(documentExists(JnEntityTestKeptDisposable.ENTITY, id));
	}

	@Test
	public void thePurgeRemovesTheExpiredDocumentTheExpiredCopyAndTheEmptyGroup() {
		CcpJsonRepresentation record = this.record();
		waitForTheStartOfASecond();
		JnEntityTestPurgedEverySecond.ENTITY.save(record);
		String id = JnEntityTestPurgedEverySecond.ENTITY.calculateId(record);
		assertTrue(documentExists(JnEntityTestPurgedEverySecond.ENTITY, id));
		assertTrue(copyExists(JnEntityTestPurgedEverySecond.ENTITY, this.key));
		assertEquals(1, groupOf(JnEntityTestPurgedEverySecond.ENTITY, this.key).size());

		waitForTheTurnOfTheSecond();
		CcpJsonRepresentation firstCounters = purge();
		// the copy expires one period after the write, possibly after the first purge: the second one finds it expired
		waitForTheTurnOfTheSecond();
		CcpJsonRepresentation secondCounters = purge();

		Long firstPurgedCopies = firstCounters.getAsLongNumber(JnBusinessPurgeExpiredDisposableRecords.Fields.purgedCopies);
		Long secondPurgedCopies = secondCounters.getAsLongNumber(JnBusinessPurgeExpiredDisposableRecords.Fields.purgedCopies);
		assertTrue(firstCounters.getAsLongNumber(JnBusinessPurgeExpiredDisposableRecords.Fields.purgedDocuments) >= 1);
		assertTrue(firstPurgedCopies + secondPurgedCopies >= 1);
		assertFalse(documentExists(JnEntityTestPurgedEverySecond.ENTITY, id));
		assertFalse(copyExists(JnEntityTestPurgedEverySecond.ENTITY, this.key));
		assertTrue(groupOf(JnEntityTestPurgedEverySecond.ENTITY, this.key).isEmpty());
	}

	@Test
	public void thePurgeKeepsTheDocumentOfTheCurrentPeriodItsCopyAndItsChild() {
		CcpJsonRepresentation record = this.record();
		JnEntityTestPurgedDisposable.ENTITY.save(record);
		String currentId = JnEntityTestPurgedDisposable.ENTITY.calculateId(record);
		// the document of a period that has ended, next to the current one in the group of the same record
		String entityName = JnEntityTestPurgedDisposable.ENTITY.getEntityMetaData().entityName;
		String expiredId = "expirado" + this.key;
		String expiredDocument = CcpOtherConstants.EMPTY_JSON.put(JnEntityTestPurgedDisposable.Fields.key, this.key).asUgglyJson();
		database().executeHttpRequest("indexTheExpiredDocument", "/" + entityName + "/_doc/" + expiredId + "?refresh=true", CcpHttpMethods.PUT, 201, expiredDocument, CcpOtherConstants.EMPTY_JSON, CcpHttpResponseType.singleRecord);
		CcpJsonRepresentation expiredChild = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonCommonsFields.entity, entityName)
				.put(JnJsonCommonsFields.id, expiredId)
				.put(JnJsonCommonsFields.timestamp, 1L);
		String parentId = parentIdOf(JnEntityTestPurgedDisposable.ENTITY, this.key);
		JnGroupers.addChildren(JnGroupers.getGrouper(false), parentName(), childName(), Map.of(parentId, List.of(expiredChild.asUgglyJson())), Map.of(parentId, copyKeyOf(JnEntityTestPurgedDisposable.ENTITY, this.key)));
		assertEquals(2, groupOf(JnEntityTestPurgedDisposable.ENTITY, this.key).size());

		purge();

		assertFalse(documentExists(JnEntityTestPurgedDisposable.ENTITY, expiredId));
		assertTrue(documentExists(JnEntityTestPurgedDisposable.ENTITY, currentId));
		assertTrue(copyExists(JnEntityTestPurgedDisposable.ENTITY, this.key));
		List<String> children = groupOf(JnEntityTestPurgedDisposable.ENTITY, this.key);
		assertEquals(1, children.size());
		assertEquals(currentId, new CcpJsonRepresentation(children.get(0)).getAsString(JnJsonCommonsFields.id));
		assertTrue(JnEntityTestPurgedDisposable.ENTITY.exists(record));
	}

	@Test
	public void theBackupIsASnapshotOfTheGivenEntitiesInTheRepositoryOfTheBucket() {
		String entityName = JnEntityTestPurgedDisposable.ENTITY.getEntityMetaData().entityName;
		CcpJsonRepresentation input = CcpOtherConstants.EMPTY_JSON
				.put(JnBusinessBackupTheDatabase.Fields.bucketName, TEST_BUCKET)
				.put(JnBusinessBackupTheDatabase.Fields.entityNamePatterns, List.of(entityName));

		CcpJsonRepresentation result = JnBusinessBackupTheDatabase.INSTANCE.execute(input);

		CcpJsonRepresentation backup = result.getInnerJson(JnBusinessBackupTheDatabase.Fields.backup);
		assertEquals("SUCCESS", backup.getAsString(new CcpFieldName("state")));
		assertEquals(List.of(entityName), backup.getAsStringList(new CcpFieldName("indices")));
		assertTrue(backup.getAsString(new CcpFieldName("snapshot")).startsWith("backup-"));
	}

	@Test
	public void theBackupGoesToTheDefaultBucketWithTheFourCostCentersWhenNothingIsGiven() {
		CcpJsonRepresentation withDefaults = JnBusinessBackupTheDatabaseDefaults.INSTANCE.execute(CcpOtherConstants.EMPTY_JSON);

		assertEquals("jobsnow-backup", withDefaults.getAsString(JnBusinessBackupTheDatabase.Fields.bucketName));
		assertEquals(List.of("jn_*", "js_*", "vis_*", "jb_*"), withDefaults.getAsStringList(JnBusinessBackupTheDatabase.Fields.entityNamePatterns));
	}

	@Test
	public void theDailyMaintenancePurgesAndThenBacksUp() {
		CcpJsonRepresentation record = this.record();
		waitForTheStartOfASecond();
		JnEntityTestPurgedEverySecond.ENTITY.save(record);
		String id = JnEntityTestPurgedEverySecond.ENTITY.calculateId(record);
		String entityName = JnEntityTestPurgedEverySecond.ENTITY.getEntityMetaData().entityName;
		CcpJsonRepresentation input = CcpOtherConstants.EMPTY_JSON
				.put(JnBusinessBackupTheDatabase.Fields.bucketName, TEST_BUCKET)
				.put(JnBusinessBackupTheDatabase.Fields.entityNamePatterns, List.of(entityName));

		waitForTheTurnOfTheSecond();
		refresh();
		CcpJsonRepresentation result = JnBusinessDailyDatabaseMaintenance.INSTANCE.execute(input);

		CcpJsonRepresentation purge = result.getInnerJson(JnBusinessDailyDatabaseMaintenance.Fields.purge);
		assertTrue(purge.getAsLongNumber(JnBusinessPurgeExpiredDisposableRecords.Fields.purgedDocuments) >= 1);
		assertFalse(documentExists(JnEntityTestPurgedEverySecond.ENTITY, id));
		CcpJsonRepresentation backup = result.getInnerJson(JnBusinessBackupTheDatabase.Fields.backup);
		assertEquals("SUCCESS", backup.getAsString(new CcpFieldName("state")));
	}

	private CcpJsonRepresentation record() {
		return CcpOtherConstants.EMPTY_JSON
				.put(JnEntityTestPurgedDisposable.Fields.key, this.key)
				.put(JnEntityTestPurgedDisposable.Fields.value, "valor");
	}

	/**
	 * The purge, after a refresh of the indexes: it finds the groups by a search, which sees only what was refreshed (in
	 * production the purge reads records written long before).
	 */
	private static CcpJsonRepresentation purge() {
		refresh();
		return JnBusinessPurgeExpiredDisposableRecords.INSTANCE.execute(CcpOtherConstants.EMPTY_JSON);
	}

	private static void refresh() {
		database().executeHttpRequest("refreshOfTheTest", "/_refresh", CcpHttpMethods.POST, 200, CcpOtherConstants.EMPTY_JSON, CcpHttpResponseType.singleRecord);
	}

	/** Sleeps past the end of the current second, so a record written by the second has expired. */
	private static void waitForTheTurnOfTheSecond() {
		new CcpTimeDecorator().sleep(1_100);
	}

	/** Sleeps until just after the start of the next second, so a write and the id calculated after it fall in the same period of a record by the second. */
	private static void waitForTheStartOfASecond() {
		int millisOfTheSecond = (int) (System.currentTimeMillis() % 1_000);
		int millisToTheNextSecond = 1_000 - millisOfTheSecond;
		new CcpTimeDecorator().sleep(millisToTheNextSecond + 20);
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

	private static String parentName() {
		return JnEntityVirtualDisposableRecordToPurge.ENTITY.getEntityMetaData().entityName;
	}

	private static String childName() {
		return JnEntityDisposableRecord.ENTITY.getEntityMetaData().entityName;
	}

	/** The key of the copy of the record: the name of its entity and its primary key as compact JSON. */
	private static CcpJsonRepresentation copyKeyOf(CcpEntity entity, String key) {
		String primaryKey = CcpOtherConstants.EMPTY_JSON.put(JnEntityTestPurgedDisposable.Fields.key, key).asUgglyJson();
		return CcpOtherConstants.EMPTY_JSON
				.put(JnJsonCommonsFields.entity, entity.getEntityMetaData().entityName)
				.put(JnJsonCommonsFields.id, primaryKey);
	}

	private static String parentIdOf(CcpEntity entity, String key) {
		return JnEntityVirtualDisposableRecordToPurge.ENTITY.calculateId(copyKeyOf(entity, key));
	}

	/** The children of the group of the record among the records to purge. */
	private static List<String> groupOf(CcpEntity entity, String key) {
		return JnGroupers.getChildren(false, parentName(), childName(), parentIdOf(entity, key));
	}

	/** Whether the document is in the index, read with no copy and no cache. */
	private static boolean documentExists(CcpEntity entity, String id) {
		return countById(entity.getEntityMetaData().entityName, id) > 0;
	}

	/** Whether the copy of the record is in {@code jn_disposable_record}. */
	private static boolean copyExists(CcpEntity entity, String key) {
		String copyId = JnEntityDisposableRecord.ENTITY.calculateId(copyKeyOf(entity, key));
		return countById(childName(), copyId) > 0;
	}

	/** 1 when the document is in the index, by a real-time get (a search would miss what was not refreshed yet). */
	private static long countById(String index, String id) {
		try {
			database().executeHttpRequest("getOfTheTest", "/" + index + "/_doc/" + id, CcpHttpMethods.GET, 200, CcpOtherConstants.EMPTY_JSON, CcpHttpResponseType.singleRecord);
			return 1;
		} catch (Throwable notFound) {
			return 0;
		}
	}
}
