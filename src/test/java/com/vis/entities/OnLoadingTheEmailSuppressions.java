package com.vis.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.dependency.injection.CcpInstanceProvider;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityDecoratorOperationType;
import com.ccp.especifications.email.CcpEmailProvider;
import com.ccp.especifications.email.CcpEmailSuppressionList;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.implementations.password.mindrot.CcpMindrotPasswordHandler;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.jn.business.email.suppression.JnBusinessLoadEmailSuppressions;
import com.jn.business.email.suppression.JnBusinessLoadEmailSuppressionsOrSaveAWarning;
import com.jn.business.messages.JnMessageSenderExceptionHandler;
import com.jn.business.messages.JnMessageType;
import com.jn.business.messages.JnMessages;
import com.jn.db.bulk.JnExecuteBulkOperation;
import com.jn.entities.JnEntityEmailSuppressed;
import com.jn.entities.JnEntityEmailTemporarilyBlocked;
import com.jn.entities.JnEntityJobsnowWarning;
import com.jn.entities.JnEntityWriteEventSubscribers;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.messages.JnSendMessageToUser;
import com.jn.utils.JnDeleteKeysFromCache;
import com.jn.utils.JnLanguage;
import com.vis.business.recruiter.VisBusinessInactivateSuppressedRecruiter;
import com.vis.business.recruiter.VisBusinessPrepareRecruiter;

/**
 * Proves the load of the suppression lists of the e-mail provider: where each list goes, what is left out, the period read
 * and the failure of the provider. Also proves that the first write of a suppressed address reaches the recruiters through
 * {@code jn_write_event_subscribers}, with jn not importing vis. The messaging is synchronous here, so the subscriber has
 * run when the save returns.
 */
public class OnLoadingTheEmailSuppressions {

	static {
		CcpInstanceProvider<CcpEmailProvider> email = () -> FixedSuppressionsEmailProvider.INSTANCE;
		CcpDependencyInjection.removeAllDependencies();
		CcpDependencyInjection.loadAllDependencies(
				CcpLocalInstances.syncMensageriaListener,
				CcpLocalInstances.bucket,
				email,
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

	private static final long CREATED = 1700000000L;

	private final FixedSuppressionsEmailProvider provider = FixedSuppressionsEmailProvider.INSTANCE;

	private final long now = System.currentTimeMillis();

	private final CcpEntity twin = VisEntityRecruiter.ENTITY.getTwinEntity();

	private List<CcpBulkItem> getSubscriptionSeeds() {
		return JnEntityWriteEventSubscribers.getSeeds(VisEntityRecruiter.ListenedEntities.jn_email_suppressed, CcpEntityDecoratorOperationType.insert, VisBusinessInactivateSuppressedRecruiter.class);
	}

	/** The seed of vis, as {@code CcpCreateEntities} writes it: by bulk. */
	@Before
	public void subscribe() {
		this.provider.clear();
		List<CcpBulkItem> subscriptionSeeds = this.getSubscriptionSeeds();
		JnExecuteBulkOperation.INSTANCE.executeBulk(subscriptionSeeds, JnDeleteKeysFromCache.INSTANCE);
	}

	private String email(String prefix) {
		return prefix + this.now + "@recrutadora.com.br";
	}

	private CcpJsonRepresentation key(String email) {
		return CcpOtherConstants.EMPTY_JSON.put(JnEntityEmailSuppressed.Fields.email, email);
	}

	/** A record as the provider gives it: {@code created} as the double of the JSON parser, an empty reason when there is none. */
	private CcpJsonRepresentation providerRecord(String email) {
		return this.key(email).put(JnEntityEmailSuppressed.Fields.created, Double.valueOf(CREATED));
	}

	private void give(CcpEmailSuppressionList list, CcpJsonRepresentation... records) {
		this.provider.lists.put(list, List.of(records));
	}

	private CcpJsonRepresentation load() {
		return JnBusinessLoadEmailSuppressions.INSTANCE.execute(CcpOtherConstants.EMPTY_JSON);
	}

	private CcpJsonRepresentation recruiter(String email, String source) {
		CcpJsonRepresentation input = CcpOtherConstants.EMPTY_JSON
				.put(VisEntityRecruiter.Fields.email, email)
				.put(VisEntityRecruiter.Fields.source, source);
		return VisBusinessPrepareRecruiter.INSTANCE.execute(input);
	}

	private CcpJsonRepresentation recruiterKey(String email) {
		return CcpOtherConstants.EMPTY_JSON.put(VisEntityRecruiter.Fields.email, email);
	}

	@Test
	public void eachListGoesToItsEntity() {
		String bounced = this.email("bounce");
		String invalid = this.email("invalid");
		String spam = this.email("spam");
		String blocked = this.email("block");
		this.give(CcpEmailSuppressionList.bounces, this.providerRecord(bounced)
				.put(JnEntityEmailSuppressed.Fields.reason, "550 5.1.1 unknown user")
				.put(JnEntityEmailSuppressed.Fields.status, "5.1.1"));
		this.give(CcpEmailSuppressionList.invalid_emails, this.providerRecord(invalid)
				.put(JnEntityEmailSuppressed.Fields.reason, "Mail domain mentioned in email address is unknown"));
		this.give(CcpEmailSuppressionList.spam_reports, this.providerRecord(spam)
				.put(JnEntityEmailSuppressed.Fields.ip, "10.63.202.100"));
		this.give(CcpEmailSuppressionList.blocks, this.providerRecord(blocked)
				.put(JnEntityEmailSuppressed.Fields.reason, "421 try again later")
				.put(JnEntityEmailSuppressed.Fields.status, "4.2.1"));

		CcpJsonRepresentation result = this.load();

		assertEquals(Long.valueOf(4), result.getAsLongNumber(JnBusinessLoadEmailSuppressions.Fields.savedSuppressions));
		assertEquals(Long.valueOf(0), result.getAsLongNumber(JnBusinessLoadEmailSuppressions.Fields.ignoredSuppressions));
		CcpJsonRepresentation storedBounce = JnEntityEmailSuppressed.ENTITY.getOneById(this.key(bounced));
		assertEquals("bounces", storedBounce.getAsString(JnEntityEmailSuppressed.Fields.suppressionList));
		assertEquals("5.1.1", storedBounce.getAsString(JnEntityEmailSuppressed.Fields.status));
		assertEquals(Long.valueOf(CREATED), storedBounce.getAsLongNumber(JnEntityEmailSuppressed.Fields.created));
		CcpJsonRepresentation storedSpam = JnEntityEmailSuppressed.ENTITY.getOneById(this.key(spam));
		assertEquals("spam_reports", storedSpam.getAsString(JnEntityEmailSuppressed.Fields.suppressionList));
		assertEquals("10.63.202.100", storedSpam.getAsString(JnEntityEmailSuppressed.Fields.ip));
		assertTrue(JnEntityEmailSuppressed.ENTITY.exists(this.key(invalid)));
		assertFalse(JnEntityEmailSuppressed.ENTITY.exists(this.key(blocked)));
		CcpJsonRepresentation storedBlock = JnEntityEmailTemporarilyBlocked.ENTITY.getOneById(this.key(blocked));
		assertEquals("4.2.1", storedBlock.getAsString(JnEntityEmailTemporarilyBlocked.Fields.status));
	}

	@Test
	public void anAddressIsKeptInLowerCaseAndAnEmptyFieldIsLeftOut() {
		String email = this.email("maiuscula");
		this.give(CcpEmailSuppressionList.bounces, this.providerRecord(email.toUpperCase())
				.put(JnEntityEmailSuppressed.Fields.reason, "")
				.put(JnEntityEmailSuppressed.Fields.status, "   "));

		CcpJsonRepresentation result = this.load();

		assertEquals(Long.valueOf(1), result.getAsLongNumber(JnBusinessLoadEmailSuppressions.Fields.savedSuppressions));
		assertTrue(JnEntityEmailSuppressed.ENTITY.exists(this.key(email)));
	}

	@Test
	public void aMalformedAddressIsCountedAndDoesNotStopTheOthers() {
		String good = this.email("depois");
		this.give(CcpEmailSuppressionList.invalid_emails, this.providerRecord("not an address"), this.providerRecord(good));

		CcpJsonRepresentation result = this.load();

		assertEquals(Long.valueOf(1), result.getAsLongNumber(JnBusinessLoadEmailSuppressions.Fields.savedSuppressions));
		assertEquals(Long.valueOf(1), result.getAsLongNumber(JnBusinessLoadEmailSuppressions.Fields.ignoredSuppressions));
		assertTrue(JnEntityEmailSuppressed.ENTITY.exists(this.key(good)));
	}

	@Test
	public void byDefaultTheLoadReadsTheLastSevenDaysOfEveryList() {
		long nowInSeconds = System.currentTimeMillis() / 1000;

		this.load();

		assertEquals(CcpEmailSuppressionList.values().length, this.provider.startTimes.size());
		long distance = nowInSeconds - this.provider.startTimes.get(0);
		assertTrue("distance: " + distance, Math.abs(distance - 7 * 86_400) < 60);
	}

	@Test
	public void theFirstLoadAsksForTheWholeLists() {
		CcpJsonRepresentation input = CcpOtherConstants.EMPTY_JSON.put(JnBusinessLoadEmailSuppressions.Fields.startTimeInSeconds, 1L);

		JnBusinessLoadEmailSuppressions.INSTANCE.execute(input);

		assertEquals(Long.valueOf(1), this.provider.startTimes.get(0));
	}

	@Test
	public void aFailureOfTheProviderBecomesAWarningAndNotAnError() {
		String message = "provider failure " + this.now;
		this.provider.failure = new IllegalStateException(message);

		CcpJsonRepresentation warning = JnBusinessLoadEmailSuppressionsOrSaveAWarning.INSTANCE.execute(CcpOtherConstants.EMPTY_JSON);

		assertEquals(message, warning.getAsString(new CcpFieldName("message")));
		assertTrue(JnEntityJobsnowWarning.ENTITY.exists(warning));
	}

	@Test
	public void theLoadGivesTheCountersToTheProcessThatCalledIt() {
		String email = this.email("contador");
		this.give(CcpEmailSuppressionList.bounces, this.providerRecord(email));

		CcpJsonRepresentation counters = JnBusinessLoadEmailSuppressionsOrSaveAWarning.INSTANCE.execute(CcpOtherConstants.EMPTY_JSON);

		assertEquals(Long.valueOf(1), counters.getAsLongNumber(JnBusinessLoadEmailSuppressions.Fields.savedSuppressions));
	}

	@Test
	public void aSuppressedRecruiterGoesToTheInactiveOnes() {
		String email = this.email("recrutador");
		VisEntityRecruiter.ENTITY.save(this.recruiter(email, "telegram"));
		this.give(CcpEmailSuppressionList.bounces, this.providerRecord(email));

		this.load();

		assertFalse(VisEntityRecruiter.ENTITY.exists(this.recruiterKey(email)));
		CcpJsonRepresentation inactive = this.twin.getOneById(this.recruiterKey(email));
		assertEquals("telegram", inactive.getAsString(VisEntityRecruiter.Fields.source));
	}

	@Test
	public void anAddressThatIsNotOfARecruiterChangesNothing() {
		String email = this.email("candidato");
		this.give(CcpEmailSuppressionList.spam_reports, this.providerRecord(email));

		this.load();

		assertFalse(VisEntityRecruiter.ENTITY.exists(this.recruiterKey(email)));
		assertFalse(this.twin.exists(this.recruiterKey(email)));
	}

	@Test
	public void aTemporaryBlockKeepsTheRecruiterActive() {
		String email = this.email("bloqueado");
		VisEntityRecruiter.ENTITY.save(this.recruiter(email, "telegram"));
		this.give(CcpEmailSuppressionList.blocks, this.providerRecord(email));

		this.load();

		assertTrue(VisEntityRecruiter.ENTITY.exists(this.recruiterKey(email)));
	}

	@Test
	public void readingTheSameSuppressionAgainDoesNotInactivateARecruiterThatCameBack() {
		String email = this.email("voltou");
		VisEntityRecruiter.ENTITY.save(this.recruiter(email, "telegram"));
		this.give(CcpEmailSuppressionList.bounces, this.providerRecord(email));
		this.load();
		this.twin.delete(this.recruiter(email, "telegram"));
		VisEntityRecruiter.ENTITY.save(this.recruiter(email, "login"));

		this.load();

		assertTrue(VisEntityRecruiter.ENTITY.exists(this.recruiterKey(email)));
	}

	private void sendTheLoginToken(String email) {
		CcpJsonRepresentation values = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonCommonsFields.email, email)
				.put(JnJsonCommonsFields.language, JnLanguage.portuguese)
				.put(JnJsonCommonsFields.token, "ABCD1234");
		String templateId = JnMessages.JnNotifyUserAboutLoginToken.class.getName();
		JnMessageType[] messageTypes = {JnMessageType.email};
		new JnSendMessageToUser().sendAllMessages(values, templateId, messageTypes, JnMessageSenderExceptionHandler.LENIENT);
	}

	@Test
	public void aSuppressedAddressReceivesNoMoreEmails() {
		String email = this.email("suprimido");
		this.give(CcpEmailSuppressionList.bounces, this.providerRecord(email));
		this.load();

		this.sendTheLoginToken(email);

		assertEquals(List.of(), this.provider.recipients);
	}

	@Test
	public void aTemporarilyBlockedAddressKeepsReceivingEmails() {
		String email = this.email("temporario");
		this.give(CcpEmailSuppressionList.blocks, this.providerRecord(email));
		this.load();

		this.sendTheLoginToken(email);

		assertEquals(List.of(email), this.provider.recipients);
	}
}
