package com.vis.entities;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.After;
import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpTimeDecorator;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.dependency.injection.CcpInstanceProvider;
import com.ccp.especifications.db.utils.entity.CcpEntityOperationType;
import com.ccp.especifications.db.utils.entity.decorators.engine.SilentInstantMessenger;
import com.ccp.especifications.email.CcpEmailProvider;
import com.ccp.especifications.instant.messenger.CcpInstantMessenger;
import com.ccp.especifications.mensageria.receiver.CcpMensageriaReceiver;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.implementations.password.mindrot.CcpMindrotPasswordHandler;
import com.ccp.json.fields.validation.CcpJsonCommonsFields;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.jn.db.bulk.JnExecuteBulkOperation;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.mensageria.JnMensageriaReceiver;
import com.jn.utils.JnDeleteKeysFromCache;
import com.vis.business.statistics.VisBusinessResumeTotals;
import com.vis.business.statistics.VisResumeStatisticsSources;
import com.vis.business.statistics.VisResumeTotalsCounters;

/**
 * 2026-11-16: in production a resume saved twice in 70 ms (a double submit) was counted twice in the counters of
 * insertion (by day, by month and by ddd). The queue of the resume runs 4 consumer threads, and the counter decorator
 * reads the stored record before the write and again after it: two saves of a new resume running at the same time both
 * read it absent before and present after, so both count it as an insertion. Each message of an entity write now runs
 * holding the lock of its record ({@code CcpMensageriaRecordLocks}), and two saves of the same new resume, delivered
 * at the same moment to two consumer threads, count it once.
 */
public class ResumeConcurrentSavesTest {

	static {
		CcpInstanceProvider<CcpInstantMessenger> telegram = () -> SilentInstantMessenger.INSTANCE;
		CcpInstanceProvider<CcpEmailProvider> email = () -> RecordingEmailSender.INSTANCE;

		CcpDependencyInjection.removeAllDependencies();
		CcpDependencyInjection.loadAllDependencies(
				CcpLocalInstances.syncMensageriaListener,
				CcpLocalInstances.bucket,
				CcpElasticSearchQueryExecutor.INSTANCE,
				CcpElasticSearchDbRequest.INSTANCE,
				CcpMindrotPasswordHandler.INSTANCE,
				CcpLocalCacheInstances.mock,
				CcpElasticSerchDbBulk.INSTANCE,
				CcpElasticSearchCrud.INSTANCE,
				CcpGsonJsonHandler.INSTANCE,
				CcpApacheMimeHttp.INSTANCE,
				telegram,
				email
		);
		JnExecuteBulkOperation.INSTANCE.executeBulk(VisEntityResume.INSTANCE.getFirstRecordsToInsert(), JnDeleteKeysFromCache.INSTANCE);
	}

	/** A ddd that few other tests use, so its counter only moves with this test. */
	private static final int DDD = 69;

	/** Each round saves a new resume twice at the same moment: the race does not happen every time without the lock. */
	private static final int ROUNDS = 5;

	private final long now = System.currentTimeMillis();

	private final List<String> emails = new ArrayList<>();

	private CcpJsonRepresentation resume(String email) {
		CcpJsonRepresentation skill = new CcpJsonRepresentation("{\"skill\":\"JAVA\",\"word\":\"JAVA\",\"type\":\"skillsFromResume\",\"category\":\"skillsFromText\"}");
		CcpJsonRepresentation english = new CcpJsonRepresentation("{\"name\":\"English\",\"level\":2,\"id\":0}");
		return CcpOtherConstants.EMPTY_JSON
				.put(VisEntityResume.Fields.email, email)
				.put(VisEntityResume.Fields.ddd, Arrays.asList(DDD))
				.put(VisEntityResume.Fields.desiredJob, "developer")
				.put(VisEntityResume.Fields.disponibility, 0)
				.put(VisEntityResume.Fields.experience, 3)
				.put(VisEntityResume.Fields.linkedinAddress, "https://www.linkedin.com/in/concurrent" + this.now)
				.put(VisEntityResume.Fields.resumeType, 1)
				.put(VisEntityResume.Fields.temporallyJobTime, 0)
				.put(VisEntityResume.Fields.pj, 9_000)
				.put(VisEntityResume.Fields.skill, Arrays.asList(skill))
				.put(VisEntityResume.Fields.spokenLanguage, Arrays.asList(english));
	}

	/** The message the queue of the resume delivers to the consumer, as {@code JnFunctionMensageriaSender} builds it. */
	private static CcpJsonRepresentation saveMessage(CcpJsonRepresentation resume) {
		String topic = VisEntityResume.class.getName();
		String entityName = VisEntityResume.ENTITY.getEntityMetaData().entityName;
		return resume
				.put(JnJsonCommonsFields.operation, CcpEntityOperationType.save.name())
				.put(CcpJsonCommonsFields.topic, topic)
				.put(CcpMensageriaReceiver.JsonFieldNames.mensageriaReceiver, JnMensageriaReceiver.class.getName())
				.put(CcpMensageriaReceiver.JsonFieldNames.entityName, entityName);
	}

	private static long totalOf(VisResumeTotalsCounters counter, Object key) {
		CcpJsonRepresentation totals = VisBusinessResumeTotals.INSTANCE.execute(CcpOtherConstants.EMPTY_JSON).getInnerJson(counter).getInnerJson(VisResumeStatisticsSources.resumes);
		CcpFieldName keyField = new CcpFieldName(String.valueOf(key));
		return totals.containsAllFields(keyField) ? totals.getAsLongNumber(keyField) : 0L;
	}

	/** Two consumer threads take the same message at the same moment. */
	private static void saveTwiceAtTheSameTime(CcpJsonRepresentation message) throws Exception {
		String topic = VisEntityResume.class.getName();
		CyclicBarrier together = new CyclicBarrier(2);
		ConsumerThreadTogether consumer = new ConsumerThreadTogether(together, topic, message);
		ExecutorService threads = Executors.newFixedThreadPool(2);
		try {
			Future<CcpJsonRepresentation> first = threads.submit(consumer);
			Future<CcpJsonRepresentation> second = threads.submit(consumer);
			first.get();
			second.get();
		} finally {
			threads.shutdown();
		}
	}

	@After
	public void deleteTheResumes() {
		for (String email : this.emails) {
			VisEntityResume.ENTITY.deleteAnyWhere(CcpOtherConstants.EMPTY_JSON.put(VisEntityResume.Fields.email, email));
		}
	}

	@Test
	public void twoSavesOfTheSameNewResumeAtTheSameTimeCountItOnce() throws Exception {
		String today = new CcpTimeDecorator().getFormattedDateTime("yyyyMMdd");
		String thisMonth = today.substring(0, 6);
		long byRegionBefore = totalOf(VisResumeTotalsCounters.byRegion, DDD);
		long byDayBefore = totalOf(VisResumeTotalsCounters.byDay, today);
		long byMonthBefore = totalOf(VisResumeTotalsCounters.byMonth, thisMonth);

		for (int round = 0; round < ROUNDS; round++) {
			String email = "concurrent" + round + "x" + this.now + "@jobsnow.com";
			this.emails.add(email);
			CcpJsonRepresentation message = saveMessage(this.resume(email));
			saveTwiceAtTheSameTime(message);
		}

		assertEquals("one count per resume in its ddd", byRegionBefore + ROUNDS, totalOf(VisResumeTotalsCounters.byRegion, DDD));
		assertEquals("one count per resume inserted today", byDayBefore + ROUNDS, totalOf(VisResumeTotalsCounters.byDay, today));
		assertEquals("one count per resume inserted this month", byMonthBefore + ROUNDS, totalOf(VisResumeTotalsCounters.byMonth, thisMonth));

		this.deleteTheResumes();
		this.emails.clear();
		assertEquals("the deletes take the counts back", byRegionBefore, totalOf(VisResumeTotalsCounters.byRegion, DDD));
		assertEquals(byDayBefore, totalOf(VisResumeTotalsCounters.byDay, today));
	}
}
