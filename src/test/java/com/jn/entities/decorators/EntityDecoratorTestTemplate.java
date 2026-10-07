package com.jn.entities.decorators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.BeforeClass;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.dependency.injection.CcpInstanceProvider;
import com.ccp.especifications.cache.CcpCache;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.flow.CcpErrorFlowDisturb;
import com.ccp.process.CcpProcessStatus;
import com.ccp.process.CcpProcessStatusDefault;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityTwin;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityMetaData;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityExpurgableOptions;
import com.ccp.especifications.email.CcpEmailSender;
import com.ccp.especifications.instant.messenger.CcpInstantMessenger;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.implementations.password.mindrot.CcpMindrotPasswordHandler;
import com.ccp.json.validations.global.engine.CcpJsonValidationError;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.jn.entities.JnEntityDisposableRecord;
import com.jn.entities.decorators.annotations.JnEntityDisposable;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/**
 * Base of the tests that prove, entity by entity, that each decorator annotated on it delivers the
 * promised behavior. Each subclass represents <b>one</b> entity: it provides the entity and a valid
 * record, and declares one {@code @Test} per decorator the entity carries, calling the matching
 * contract here. Whoever reads the subclass sees, in its list of tests, exactly what the entity's
 * annotations promise.
 *
 * <p>The database is the real local Elasticsearch; the cache is the in-memory map (not the mock that
 * stores nothing), because that is the only way to prove a read served by the cache; messaging is
 * synchronous; Telegram and e-mail are test doubles that count what would have been sent.
 *
 * <p>Each test uses unique values derived from the clock, so it does not depend on cleaning up
 * previous runs.
 */
public abstract class EntityDecoratorTestTemplate {

	@BeforeClass
	public static void loadDependencies() {
		CcpInstanceProvider<CcpInstantMessenger> telegram = () -> new CountingInstantMessenger();
		CcpInstanceProvider<CcpEmailSender> email = () -> new CountingEmailSender();

		CcpDependencyInjection.removeAllDependencies();
		CcpDependencyInjection.loadAllDependencies(
				CcpLocalInstances.syncMensageriaListener,
				CcpLocalInstances.bucket,
				new CcpElasticSearchQueryExecutor(),
				new CcpElasticSearchDbRequest(),
				new CcpMindrotPasswordHandler(),
				CcpLocalCacheInstances.map,
				new CcpElasticSerchDbBulk(),
				new CcpElasticSearchCrud(),
				new CcpGsonJsonHandler(),
				new CcpApacheMimeHttp(),
				telegram,
				email
		);
	}

	/** Different in each test: each method runs in a new instance of the class. */
	protected final long unique = System.nanoTime();

	protected final String email = "decorator" + this.unique + "@jobsnow.com";

	protected abstract CcpEntity entityUnderTest();

	/** A record that passes the entity's validation and does not exist in the database yet. */
	protected abstract CcpJsonRepresentation validRecord();

	// ── chain built by the factory ────────────────────────────────────────────

	/**
	 * The decorator chain, from outside to inside, must be exactly the given one. It proves that the
	 * annotation priorities produce the order the behavior relies on.
	 */
	protected void shouldHaveChain(String... outsideToInside) {
		List<String> expected = Arrays.asList(outsideToInside);
		List<String> actual = this.chain(this.entityUnderTest());
		assertEquals("decorator chain of " + this.name(), expected, actual);
	}

	protected List<String> chain(CcpEntity entityUnderTest) {
		List<String> layerNames = new ArrayList<>();
		CcpEntity layer = entityUnderTest;
		while(true) {
			layerNames.add(layer.getClass().getSimpleName());
			CcpEntity innerLayer = layer.getWrapedEntity();
			if(innerLayer == layer) {
				return layerNames;
			}
			layer = innerLayer;
		}
	}

	// ── basic contract ───────────────────────────────────────────────────────

	/** Saves, finds, reads and deletes. It is the minimum the whole decorator stack must preserve. */
	protected void shouldSaveReadAndDelete() {
		CcpEntity entityUnderTest = this.entityUnderTest();
		CcpJsonRepresentation record = this.validRecord();

		entityUnderTest.save(record);
		assertTrue("did not save in " + this.name(), entityUnderTest.exists(record));

		CcpJsonRepresentation readRecord = entityUnderTest.getOneById(record);
		assertFalse("getOneById returned empty in " + this.name(), readRecord.isEmpty());

		entityUnderTest.delete(record);
		assertFalse("delete left the record in " + this.name(), entityUnderTest.exists(record));
	}

	// ── @CcpEntityFieldsValidator ────────────────────────────────────────────

	/** The validator is the outermost decorator: the invalid record must be blocked before anything else. */
	protected void shouldRefuseInvalidRecord(CcpJsonRepresentation invalidRecord) {
		try {
			this.entityUnderTest().save(invalidRecord);
		} catch (CcpJsonValidationError e) {
			return;
		}
		fail(this.name() + " accepted an invalid record: " + invalidRecord.asUgglyJson());
	}

	// ── @CcpEntityFieldsTransformer ──────────────────────────────────────────

	/** The document as it ended up in the main index, read from outside the chain. */
	protected CcpJsonRepresentation asStored(CcpJsonRepresentation record) {
		String id = this.idInTheDatabase(record);
		CcpJsonRepresentation document = ElasticsearchLocal.document(this.name(), id);
		return document;
	}

	/** The e-mail must reach the database as a hash, never in plain text. */
	protected void shouldStoreEmailAsHash() {
		CcpJsonRepresentation record = this.validRecord();
		this.entityUnderTest().save(record);
		CcpJsonRepresentation stored = this.asStored(record);
		assertFalse("did not find the stored document in " + this.name(), stored.isEmpty());
		String storedEmail = stored.getAsString(JnJsonCommonsFields.email);
		assertNotEquals("email stored in plain text in " + this.name(), this.email, storedEmail);
		// the framework's SHA1 is printed as a signed number: it may start with '-' and lose leading zeros
		assertTrue("stored email does not look like a SHA1 hash in " + this.name() + ": " + storedEmail, storedEmail.matches("-?[0-9a-f]{30,40}"));
	}

	/** Entities that declare {@code JnJsonTransformersFieldsEntityDoNothing} on the e-mail store it in plain text. */
	protected void shouldStoreEmailInPlainText() {
		CcpJsonRepresentation record = this.validRecord();
		this.entityUnderTest().save(record);
		CcpJsonRepresentation stored = this.asStored(record);
		assertFalse("did not find the stored document in " + this.name(), stored.isEmpty());
		String storedEmail = stored.getAsString(JnJsonCommonsFields.email);
		assertEquals("email should be kept in plain text in " + this.name(), this.email, storedEmail);
	}

	/** The field must reach the database transformed (hash, BCrypt...), never with the original value. */
	protected void shouldStoreTransformed(CcpJsonFieldName field, String originalValue) {
		CcpJsonRepresentation record = this.validRecord();
		this.entityUnderTest().save(record);
		CcpJsonRepresentation stored = this.asStored(record);
		assertFalse("did not find the stored document in " + this.name(), stored.isEmpty());
		String storedValue = stored.getAsString(field);
		assertFalse(field + " was not stored in " + this.name(), storedValue.isEmpty());
		assertNotEquals(field + " stored untransformed in " + this.name(), originalValue, storedValue);
	}

	/** Without {@code timestamp} in the record, the transformer stamps {@code timestamp} and {@code date}. */
	protected void shouldStampDateAndTime() {
		CcpJsonRepresentation record = this.validRecord().removeFields(JnJsonCommonsFields.timestamp, JnJsonCommonsFields.date);
		long before = System.currentTimeMillis();
		this.entityUnderTest().save(record);
		CcpJsonRepresentation stored = this.asStored(record);
		assertFalse("did not find the stored document in " + this.name(), stored.isEmpty());
		assertTrue("timestamp was not stamped in " + this.name(), stored.getAsLongNumber(JnJsonCommonsFields.timestamp) >= before);
		assertFalse("date was not stamped in " + this.name(), stored.getAsString(JnJsonCommonsFields.date).isEmpty());
	}

	// ── @CcpEntityCache ──────────────────────────────────────────────────────

	/**
	 * Once read, the record starts being answered by the cache: it disappears from the index (removed
	 * from outside the entity) and keeps existing. And a write through the entity invalidates it: after
	 * the {@code delete} the key leaves the cache and the record no longer exists.
	 *
	 * <p>For the final {@code delete} the document is put back into the index, also from outside the
	 * entity. A new {@code save} would do, but it would be an insert, and in entities that notify
	 * someone on insert the repeated notice is refused — which would mix a message rule into the cache
	 * contract.
	 */
	protected void shouldServeReadFromCacheAndInvalidateOnWrite() {
		CcpEntity entityUnderTest = this.entityUnderTest();
		CcpJsonRepresentation record = this.validRecord();
		CcpCache cache = CcpDependencyInjection.getDependency(CcpCache.class);

		entityUnderTest.save(record);
		assertTrue("did not save in " + this.name(), entityUnderTest.exists(record));

		String cacheKey = this.cacheKey(record);
		assertTrue("exists did not leave the record in the cache of " + this.name() + " (key " + cacheKey + ")", cache.isPresent(cacheKey));

		String id = this.idInTheDatabase(record);
		CcpJsonRepresentation document = ElasticsearchLocal.document(this.name(), id);
		String disposableCopyId = this.isDisposable() ? this.disposableCopyId(record) : "";
		CcpJsonRepresentation disposableCopy = this.isDisposable() ? ElasticsearchLocal.document("jn_disposable_record", disposableCopyId) : CcpOtherConstants.EMPTY_JSON;

		this.deleteOutsideTheEntity(record);
		assertTrue("exists was not answered by the cache in " + this.name(), entityUnderTest.exists(record));

		ElasticsearchLocal.save(this.name(), id, document);
		if(this.isDisposable()) {
			ElasticsearchLocal.save("jn_disposable_record", disposableCopyId, disposableCopy);
		}
		entityUnderTest.delete(record);
		assertFalse("delete did not invalidate the cache of " + this.name(), cache.isPresent(cacheKey));
		assertFalse("deleted record still exists in " + this.name(), entityUnderTest.exists(record));
	}

	protected String cacheKey(CcpJsonRepresentation record) {
		String id = this.idInTheDatabase(record);
		String cacheKey = "records.entity." + this.name() + ".id." + id;
		return cacheKey;
	}

	protected void clearCache(CcpJsonRepresentation record) {
		CcpCache cache = CcpDependencyInjection.getDependency(CcpCache.class);
		String cacheKey = this.cacheKey(record);
		cache.delete(cacheKey);
	}

	// ── @CcpEntityOlyReadable ────────────────────────────────────────────────

	protected void shouldRefuseWriteBecauseReadOnly() {
		CcpEntity entityUnderTest = this.entityUnderTest();
		CcpJsonRepresentation record = this.validRecord();

		assertFalse("save returned true in read-only entity " + this.name(), entityUnderTest.save(record));
		assertFalse("save wrote in read-only entity " + this.name(), entityUnderTest.exists(record));
		assertTrue("save wrote in read-only entity " + this.name(), this.asStored(record).isEmpty());
		assertFalse("delete returned true in read-only entity " + this.name(), entityUnderTest.delete(record));
		assertFalse("deleteAnyWhere returned true in read-only entity " + this.name(), entityUnderTest.deleteAnyWhere(record));

		// a write through the API, outside the flow of the decorators that own the entity, is refused (since 2026-10-06)
		for (com.ccp.especifications.db.bulk.CcpBulkEntityOperationType write : java.util.Arrays.asList(
				com.ccp.especifications.db.bulk.CcpBulkEntityOperationType.create,
				com.ccp.especifications.db.bulk.CcpBulkEntityOperationType.update,
				com.ccp.especifications.db.bulk.CcpBulkEntityOperationType.delete)) {
			try {
				entityUnderTest.toBulkItems(record, write);
				org.junit.Assert.fail("toBulkItems " + write + " was accepted in read-only entity " + this.name());
			} catch (com.ccp.especifications.db.utils.entity.decorators.engine.CcpErrorEntityReadOnly e) {
				// expected
			}
		}
		// reading through the bulk goes on
		entityUnderTest.toBulkItems(record, com.ccp.especifications.db.bulk.CcpBulkEntityOperationType.noop);
	}

	// ── @CcpEntityTwin ───────────────────────────────────────────────────────

	/**
	 * {@code save} writes to the main entity and leaves nothing in the twin; {@code delete} does not
	 * delete: it transfers to the twin. At no moment is the record in both indexes.
	 */
	protected void shouldSaveOnMainAndTransferToTwinOnDelete(String twinName) {
		CcpEntity entityUnderTest = this.entityUnderTest();
		CcpEntity twin = entityUnderTest.getTwinEntity();
		CcpJsonRepresentation record = this.validRecord();

		assertEquals("name of the twin index of " + this.name(), twinName, twin.getEntityMetaData().entityName);

		entityUnderTest.save(record);
		assertTrue("save did not write to the main entity " + this.name(), entityUnderTest.exists(record));
		assertFalse("save left a record in the twin " + twinName, twin.exists(record));

		entityUnderTest.delete(record);
		assertFalse("delete left a record in the main entity " + this.name(), entityUnderTest.exists(record));
		assertTrue("delete did not transfer to the twin " + twinName, twin.exists(record));
	}

	/**
	 * Once transferred to the twin, reading through the main entity tells where the record is
	 * ({@code REDIRECT}); a record that is in neither of them is {@code NOT_FOUND}.
	 */
	protected void shouldRedirectOnGetOneByIdWhenRecordMovedToTwin() {
		CcpEntity entityUnderTest = this.entityUnderTest();
		CcpJsonRepresentation record = this.validRecord();

		assertEquals("getOneById of a nonexistent record in " + this.name(), CcpProcessStatusDefault.NOT_FOUND, this.getOneByIdStatus(record));

		entityUnderTest.save(record);
		entityUnderTest.delete(record);

		assertEquals("getOneById of a record transferred to the twin of " + this.name(), CcpProcessStatusDefault.REDIRECT, this.getOneByIdStatus(record));
	}

	private CcpProcessStatus getOneByIdStatus(CcpJsonRepresentation record) {
		try {
			this.entityUnderTest().getOneById(record);
		} catch (CcpErrorFlowDisturb e) {
			return e.status;
		}
		throw new AssertionError("getOneById of " + this.name() + " returned a record where there was none in the main entity");
	}

	/**
	 * The full cycle: saves, transfers to the twin on {@code delete}, and a new {@code save} brings it
	 * back to the main entity, deleting it from the twin. Not suitable for twins that notify someone on
	 * insert: the new {@code save} is another insert, and the repeated notice within the same window is
	 * refused by a business rule — the real flow deletes the "message sent" record before saving again.
	 */
	protected void shouldAlternateBetweenMainAndTwin(String twinName) {
		this.shouldSaveOnMainAndTransferToTwinOnDelete(twinName);

		CcpEntity entityUnderTest = this.entityUnderTest();
		CcpEntity twin = entityUnderTest.getTwinEntity();
		CcpJsonRepresentation record = this.validRecord();

		entityUnderTest.save(record);
		assertTrue("new save did not bring it back to the main entity " + this.name(), entityUnderTest.exists(record));
		assertFalse("new save did not delete from the twin " + twinName, twin.exists(record));
	}

	// ── JnEntityVersionable ──────────────────────────────────────────────────

	/** Each write through the entity adds exactly one history line to jn_versionable. */
	protected void shouldRecordHistoryOnEachWrite() {
		CcpEntity entityUnderTest = this.entityUnderTest();
		CcpJsonRepresentation record = this.validRecord();
		String primaryKey = this.serializedPrimaryKey(record);

		long before = this.historyLines(primaryKey);

		entityUnderTest.save(record);
		long afterInsert = this.historyLines(primaryKey);

		entityUnderTest.save(record);
		long afterUpdate = this.historyLines(primaryKey);

		entityUnderTest.delete(record);
		long afterDelete = this.historyLines(primaryKey);

		String measured = "insert +" + (afterInsert - before)
				+ ", update +" + (afterUpdate - afterInsert)
				+ ", delete +" + (afterDelete - afterUpdate);
		assertEquals("history lines per write in " + this.name(), "insert +1, update +1, delete +1", measured);
	}

	private long historyLines(String primaryKey) {
		long lines = this.historyLines(this.name(), primaryKey);
		return lines;
	}

	private long historyLines(String entityName, String primaryKey) {
		long lines = ElasticsearchLocal.count("jn_versionable", "entity", entityName, "id", primaryKey);
		return lines;
	}

	/**
	 * {@code deleteAnyWhere} deletes the record and purges all of its history. In a twin entity the
	 * record first goes through the twin ({@code delete} transfers), so that there is history under both
	 * names — each transfer writes one line at each end — and the purge must clear both.
	 */
	protected void shouldPurgeHistoryOnDeleteAnyWhere() {
		CcpEntity entityUnderTest = this.entityUnderTest();
		CcpJsonRepresentation record = this.validRecord();
		String primaryKey = this.serializedPrimaryKey(record);
		boolean isTwin = entityUnderTest.getEntityMetaData().configurationClass.isAnnotationPresent(CcpEntityTwin.class);

		entityUnderTest.save(record);
		entityUnderTest.save(record);

		String twinName = "";
		if(isTwin) {
			entityUnderTest.delete(record);
			twinName = entityUnderTest.getTwinEntity().getEntityMetaData().entityName;
			assertTrue("the transfer did not write history under the twin's name " + twinName, this.historyLines(twinName, primaryKey) > 0);
		}
		assertTrue("there was no history to purge in " + this.name(), this.historyLines(primaryKey) > 0);

		boolean existed = entityUnderTest.deleteAnyWhere(record);

		assertTrue("deleteAnyWhere denied that the record existed in " + this.name(), existed);
		assertEquals("history of " + this.name() + " was not purged", 0, this.historyLines(primaryKey));
		assertFalse("deleteAnyWhere left the record in " + this.name(), entityUnderTest.exists(record));
		if(isTwin) {
			assertEquals("history saved under the twin's name " + twinName + " was not purged", 0, this.historyLines(twinName, primaryKey));
			assertFalse("deleteAnyWhere left the record in the twin " + twinName, entityUnderTest.getTwinEntity().exists(record));
		}
	}

	// ── JnEntityDisposable ───────────────────────────────────────────────────

	/**
	 * Saving leaves a copy in jn_disposable_record with a deadline in the future. While the deadline
	 * holds, the copy sustains the record's existence even if the main index loses it; once the
	 * deadline has passed, the record no longer exists.
	 */
	protected void shouldKeepDisposableCopyUntilDeadline(CcpEntityExpurgableOptions deadline) {
		CcpEntity entityUnderTest = this.entityUnderTest();
		CcpJsonRepresentation record = this.validRecord();

		long beforeSave = System.currentTimeMillis();
		entityUnderTest.save(record);

		String disposableCopyId = this.disposableCopyId(record);
		CcpJsonRepresentation disposableCopy = ElasticsearchLocal.document("jn_disposable_record", disposableCopyId);
		assertFalse("save did not write a disposable copy of " + this.name(), disposableCopy.isEmpty());
		assertEquals("deadline format of the copy of " + this.name(), deadline.format, disposableCopy.getAsString(JnEntityDisposableRecord.Fields.format));
		Long expiration = disposableCopy.getAsLongNumber(JnJsonCommonsFields.timestamp);
		assertTrue("copy of " + this.name() + " was already expired when created", expiration > beforeSave);

		ElasticsearchLocal.delete(this.name(), this.idInTheDatabase(record));
		this.clearCache(record);
		assertTrue("valid copy did not sustain the existence of " + this.name(), entityUnderTest.exists(record));

		CcpJsonRepresentation expiredTimestamp = CcpOtherConstants.EMPTY_JSON.put(JnJsonCommonsFields.timestamp, beforeSave - 1000);
		ElasticsearchLocal.update("jn_disposable_record", disposableCopyId, expiredTimestamp);
		this.clearCache(record);
		assertFalse("expired copy still sustains the existence of " + this.name(), entityUnderTest.exists(record));
	}

	protected String disposableCopyId(CcpJsonRepresentation record) {
		CcpJsonRepresentation disposableKey = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonCommonsFields.entity, this.name())
				.put(JnJsonCommonsFields.id, this.serializedPrimaryKey(record));
		String id = JnEntityDisposableRecord.ENTITY.calculateId(disposableKey);
		return id;
	}

	// ── JnEntityAsyncWriter ──────────────────────────────────────────────────

	/**
	 * {@code save} does not write directly: it publishes to the queue (leaving a trace in jn_async_task
	 * with the entity's topic) and returns that the message was accepted. With the synchronous messaging
	 * of the tests, the consumer runs right away and the record shows up in the database.
	 */
	protected void shouldSaveThroughQueue() {
		CcpEntity entityUnderTest = this.entityUnderTest();
		CcpJsonRepresentation record = this.validRecord();
		String topic = entityUnderTest.getEntityMetaData().configurationClass.getName();

		long before = ElasticsearchLocal.count("jn_async_task", "topic", topic, "operation", "save");
		boolean accepted = entityUnderTest.save(record);

		assertTrue("queue refused the save of " + this.name(), accepted);
		assertEquals("save of " + this.name() + " did not go through the queue", before + 1, ElasticsearchLocal.count("jn_async_task", "topic", topic, "operation", "save"));
		assertTrue("queue consumer did not save " + this.name(), entityUnderTest.exists(record));
	}

	// ── JnEntitySendMessageToUserWhenWrite ───────────────────────────────────

	/** {@code afterInsert}: the insert notifies via Telegram; an update of the same record does not. */
	protected void shouldNotifyByTelegramOnlyOnInsert() {
		CcpEntity entityUnderTest = this.entityUnderTest();
		CcpJsonRepresentation record = this.validRecord();

		int before = CountingInstantMessenger.total();
		entityUnderTest.save(record);
		int afterInsert = CountingInstantMessenger.total();
		assertTrue("insert in " + this.name() + " did not send a message", afterInsert > before);

		entityUnderTest.save(record);
		assertEquals("update in " + this.name() + " sent a message", afterInsert, CountingInstantMessenger.total());
	}

	/**
	 * Causes an insert notice to be refused for repetition: saves, removes the record from outside the
	 * entity and saves again — it is another insert, with the same notice, within the same window.
	 * Returns what the second save threw, or empty if it went through.
	 */
	protected Optional<RuntimeException> repeatInsertNotice() {
		CcpEntity entityUnderTest = this.entityUnderTest();
		CcpJsonRepresentation record = this.validRecord();

		entityUnderTest.save(record);
		this.deleteOutsideTheEntity(record);
		this.clearCache(record);

		try {
			entityUnderTest.save(record);
			return Optional.empty();
		} catch (RuntimeException e) {
			return Optional.of(e);
		}
	}

	/** Handler {@code SaveAWarning}/{@code LogTheError}: refusing the notice does not bring down the save. */
	protected void shouldKeepSavingWhenNoticeIsRefused() {
		Optional<RuntimeException> thrown = this.repeatInsertNotice();
		if(thrown.isPresent()) {
			throw new AssertionError("refusing the notice brought down the save of " + this.name(), thrown.get());
		}
		assertTrue("record of " + this.name() + " was not saved after the notice was refused", this.entityUnderTest().exists(this.validRecord()));
	}

	/** Handler {@code ThrowAnError}: refusing the notice brings down the save. */
	protected void shouldFailWhenNoticeIsRefused() {
		Optional<RuntimeException> thrown = this.repeatInsertNotice();
		assertTrue("refusing the notice should bring down the save of " + this.name(), thrown.isPresent());
	}

	/** {@code afterDelete}: removing the record notifies via Telegram; removing one that does not exist does not. */
	protected void shouldNotifyByTelegramOnDelete() {
		CcpEntity entityUnderTest = this.entityUnderTest();
		CcpJsonRepresentation record = this.validRecord();

		entityUnderTest.save(record);
		int before = CountingInstantMessenger.total();
		entityUnderTest.delete(record);
		int afterDelete = CountingInstantMessenger.total();
		assertTrue("delete in " + this.name() + " did not send a message", afterDelete > before);

		entityUnderTest.delete(record);
		assertEquals("delete of a nonexistent record in " + this.name() + " sent a message", afterDelete, CountingInstantMessenger.total());
	}

	/** {@code afterInsert} by e-mail: the insert sends an e-mail to the record's owner; an update does not. */
	protected void shouldSendEmailOnlyOnInsert() {
		CcpEntity entityUnderTest = this.entityUnderTest();
		CcpJsonRepresentation record = this.validRecord();

		entityUnderTest.save(record);
		assertEquals("insert in " + this.name() + " did not send an email to " + this.email, 1, CountingEmailSender.howManyFor(this.email));

		entityUnderTest.save(record);
		assertEquals("update in " + this.name() + " sent an email", 1, CountingEmailSender.howManyFor(this.email));
	}

	// ── utilities ────────────────────────────────────────────────────────────

	protected String name() {
		CcpEntityMetaData metaData = this.entityUnderTest().getEntityMetaData();
		return metaData.entityName;
	}

	/** The document's {@code _id} in the main index: the primary key after the transformations. */
	protected String idInTheDatabase(CcpJsonRepresentation record) {
		CcpEntity entityUnderTest = this.entityUnderTest();
		CcpJsonRepresentation transformedRecord = entityUnderTest.getHandledJson(record);
		String id = entityUnderTest.calculateId(transformedRecord);
		return id;
	}

	/** The serialized primary key, which is how jn_versionable and jn_disposable_record point to the record. */
	protected String serializedPrimaryKey(CcpJsonRepresentation record) {
		CcpEntity entityUnderTest = this.entityUnderTest();
		CcpJsonRepresentation transformedRecord = entityUnderTest.getHandledJson(record);
		CcpEntityMetaData metaData = entityUnderTest.getEntityMetaData();
		CcpJsonRepresentation primaryKeyValues = metaData.getPrimaryKeyValues(transformedRecord.getJsonSupplier());
		String serializedPrimaryKey = primaryKeyValues.asUgglyJson();
		return serializedPrimaryKey;
	}

	protected boolean isDisposable() {
		boolean disposable = this.entityUnderTest().getEntityMetaData().configurationClass.isAnnotationPresent(JnEntityDisposable.class);
		return disposable;
	}

	/** Removes the record from the main index and from the disposable copy, without notifying any decorator. */
	protected void deleteOutsideTheEntity(CcpJsonRepresentation record) {
		ElasticsearchLocal.delete(this.name(), this.idInTheDatabase(record));
		if(this.isDisposable()) {
			ElasticsearchLocal.delete("jn_disposable_record", this.disposableCopyId(record));
		}
	}

	protected CcpJsonRepresentation com(CcpJsonFieldName field, Object value) {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(field, value);
		return json;
	}
}
