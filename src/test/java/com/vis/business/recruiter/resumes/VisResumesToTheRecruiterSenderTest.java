package com.vis.business.recruiter.resumes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.vis.entities.VisEntityRecruiter;

/**
 * {@link VisResumesToTheRecruiterSender} skips, without sending, every recruiter after the limit of the day, the
 * recruiter out of a restricted run, the address that is not valid and the recruiter with no resume to receive. The
 * order of the recruiters leaves out who stopped viewing, puts the most recent views first and continues the queue from the
 * last position. The sending itself (the list of the screen "see all" and the e-mail) is proved by
 * {@link ResumesToRecruitersTest}, whose runs go through this class.
 */
public class VisResumesToTheRecruiterSenderTest {

	static {
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE);
	}

	private static final String RECRUITER = "recrutador@empresa.com.br";

	private static final long VIEWS_SINCE = 1_000L;

	private final int[] emailsSent = {0};

	private final CcpJsonRepresentation recruiter = CcpOtherConstants.EMPTY_JSON.put(VisEntityRecruiter.Fields.email, RECRUITER);

	private final String[] lastQueuePosition = {""};

	private final String[] lastLegacyQueuePosition = {""};

	private VisResumesToTheRecruiterSender sender(List<String> restrictedTo, List<CcpJsonRepresentation> resumes) {
		return new VisResumesToTheRecruiterSender(restrictedTo, resumes, new HashMap<>(), new HashMap<>(), "http://localhost", "http://localhost:8081", this.emailsSent, this.lastQueuePosition, this.lastLegacyQueuePosition);
	}

	private static List<String> emailsOf(List<CcpJsonRepresentation> recruiters) {
		List<String> emails = new ArrayList<>();
		recruiters.forEach(recruiter -> emails.add(recruiter.getAsString(VisEntityRecruiter.Fields.email)));
		return emails;
	}

	private static String hashOf(String email) {
		return VisBusinessSendResumesToRecruiters.getEmailHash(email);
	}

	private static CcpJsonRepresentation recruiterOf(String email) {
		return CcpOtherConstants.EMPTY_JSON.put(VisEntityRecruiter.Fields.email, email);
	}

	/** The most recent view goes first, ties go by the hash, and who never viewed completes the list by the hash. */
	@Test
	public void theMostRecentViewersComeFirstAndWhoStoppedViewingIsLeftOut() {
		List<String> tiedViewers = new ArrayList<>(List.of("v1@empresa.com.br", "v2@empresa.com.br"));
		List<String> others = new ArrayList<>(List.of("o1@empresa.com.br", "o2@empresa.com.br", "o3@empresa.com.br"));
		Map<String, Long> lastViewByRecruiter = new HashMap<>();
		lastViewByRecruiter.put(hashOf("recente@empresa.com.br"), VIEWS_SINCE + 2);
		for (String viewer : tiedViewers) {
			lastViewByRecruiter.put(hashOf(viewer), VIEWS_SINCE);
		}
		lastViewByRecruiter.put(hashOf("antigo@empresa.com.br"), VIEWS_SINCE - 1);
		List<CcpJsonRepresentation> recruiters = new ArrayList<>();
		others.forEach(email -> recruiters.add(recruiterOf(email)));
		tiedViewers.forEach(email -> recruiters.add(recruiterOf(email)));
		recruiters.add(recruiterOf("antigo@empresa.com.br"));
		recruiters.add(recruiterOf("recente@empresa.com.br"));

		List<CcpJsonRepresentation> inOrder = VisBusinessSendResumesToRecruiters.getRecruitersInOrder(recruiters, lastViewByRecruiter, VIEWS_SINCE, "", Set.of(), 0L, "");

		Comparator<String> byHash = Comparator.comparing(email -> hashOf(email));
		tiedViewers.sort(byHash);
		others.sort(byHash);
		List<String> expected = new ArrayList<>(List.of("recente@empresa.com.br"));
		expected.addAll(tiedViewers);
		expected.addAll(others);
		assertEquals(expected, emailsOf(inOrder));
	}

	/** The next run starts right after the last position and, at the end of the queue, goes back to its beginning. */
	@Test
	public void theQueueContinuesFromTheLastPositionAndStartsOverAtItsEnd() {
		List<String> queue = new ArrayList<>(List.of("q1@empresa.com.br", "q2@empresa.com.br", "q3@empresa.com.br", "q4@empresa.com.br"));
		Comparator<String> byHash = Comparator.comparing(email -> hashOf(email));
		queue.sort(byHash);
		String viewer = "viewer@empresa.com.br";
		Map<String, Long> lastViewByRecruiter = new HashMap<>();
		lastViewByRecruiter.put(hashOf(viewer), VIEWS_SINCE);
		List<CcpJsonRepresentation> recruiters = new ArrayList<>();
		queue.forEach(email -> recruiters.add(recruiterOf(email)));
		recruiters.add(recruiterOf(viewer));
		String positionOfTheSecond = "1" + hashOf(queue.get(1));

		List<CcpJsonRepresentation> inOrder = VisBusinessSendResumesToRecruiters.getRecruitersInOrder(recruiters, lastViewByRecruiter, VIEWS_SINCE, positionOfTheSecond, Set.of(), 0L, "");

		assertEquals(List.of(viewer, queue.get(2), queue.get(3), queue.get(0), queue.get(1)), emailsOf(inOrder));
	}

	/** Each recruiter of the queue has one turn per round: the sign-up first, then the others, even when one of them stopped there. */
	@Test
	public void aRoundPassesThroughTheSignUpOnceBeforeTheOthers() {
		String login = VisEntityRecruiter.Sources.login.getValue();
		String legacy = VisEntityRecruiter.Sources.legacy.getValue();
		CcpJsonRepresentation signedUp = recruiterOf("signedup@empresa.com.br").put(VisEntityRecruiter.Fields.source, login);
		CcpJsonRepresentation first = recruiterOf("first@empresa.com.br").put(VisEntityRecruiter.Fields.source, legacy);
		String lastPosition = "0" + hashOf("signedup@empresa.com.br");

		Set<String> withFirstEmail = Set.of(hashOf("signedup@empresa.com.br"));

		List<CcpJsonRepresentation> inOrder = VisBusinessSendResumesToRecruiters.getRecruitersInOrder(List.of(signedUp, first), new HashMap<>(), VIEWS_SINCE, lastPosition, withFirstEmail, 0L, "");

		assertEquals(List.of("first@empresa.com.br", "signedup@empresa.com.br"), emailsOf(inOrder));
	}

	/** Who just signed up goes before the queue, wherever the cursor is, and does not move it; after the first e-mail, follows the queue. */
	@Test
	public void aRecruiterOfTheSignUpGoesBeforeTheQueueUntilTheFirstEmail() {
		String login = VisEntityRecruiter.Sources.login.getValue();
		String legacy = VisEntityRecruiter.Sources.legacy.getValue();
		CcpJsonRepresentation signedUp = recruiterOf("signedup@empresa.com.br").put(VisEntityRecruiter.Fields.source, login);
		CcpJsonRepresentation other = recruiterOf("other@empresa.com.br").put(VisEntityRecruiter.Fields.source, legacy);
		CcpJsonRepresentation viewer = recruiterOf("viewer@empresa.com.br").put(VisEntityRecruiter.Fields.source, legacy);
		Map<String, Long> lastViewByRecruiter = new HashMap<>();
		lastViewByRecruiter.put(hashOf("viewer@empresa.com.br"), VIEWS_SINCE);
		String cursorAlreadyInTheOthers = "1" + "0".repeat(40);

		List<CcpJsonRepresentation> waiting = VisBusinessSendResumesToRecruiters.getRecruitersInOrder(List.of(other, signedUp, viewer), lastViewByRecruiter, VIEWS_SINCE, cursorAlreadyInTheOthers, Set.of(), 0L, "");

		assertEquals(List.of("viewer@empresa.com.br", "signedup@empresa.com.br", "other@empresa.com.br"), emailsOf(waiting));
		CcpJsonRepresentation waitingRecruiter = waiting.get(1);
		assertTrue(waitingRecruiter.containsField(VisRecruiterResumesFields.firstEmail));
		assertFalse("the queue stays where it was", waitingRecruiter.containsField(VisRecruiterResumesFields.queuePosition));

		Set<String> withFirstEmail = Set.of(hashOf("signedup@empresa.com.br"));
		List<CcpJsonRepresentation> afterTheFirstEmail = VisBusinessSendResumesToRecruiters.getRecruitersInOrder(List.of(other, signedUp, viewer), lastViewByRecruiter, VIEWS_SINCE, cursorAlreadyInTheOthers, withFirstEmail, 0L, "");

		assertEquals(List.of("viewer@empresa.com.br", "other@empresa.com.br", "signedup@empresa.com.br"), emailsOf(afterTheFirstEmail));
		assertFalse(afterTheFirstEmail.get(2).containsField(VisRecruiterResumesFields.firstEmail));
	}

	/** Only who viewed before the limit is inactivated, never who never viewed; a restricted run touches only its recruiters. */
	@Test
	public void onlyWhoStoppedViewingIsInactivatedAndARestrictedRunTouchesOnlyItsRecruiters() {
		Map<String, Long> lastViewByRecruiter = new HashMap<>();
		lastViewByRecruiter.put(hashOf("antigo@empresa.com.br"), VIEWS_SINCE - 1);
		lastViewByRecruiter.put(hashOf("outro@empresa.com.br"), VIEWS_SINCE - 1);
		lastViewByRecruiter.put(hashOf("limite@empresa.com.br"), VIEWS_SINCE);
		List<CcpJsonRepresentation> recruiters = new ArrayList<>();
		for (String email : List.of("antigo@empresa.com.br", "outro@empresa.com.br", "limite@empresa.com.br", "nunca@empresa.com.br")) {
			recruiters.add(recruiterOf(email));
		}

		List<CcpJsonRepresentation> everyRun = VisBusinessSendResumesToRecruiters.getRecruitersWhoStoppedViewing(recruiters, lastViewByRecruiter, VIEWS_SINCE, List.of());
		List<CcpJsonRepresentation> restrictedRun = VisBusinessSendResumesToRecruiters.getRecruitersWhoStoppedViewing(recruiters, lastViewByRecruiter, VIEWS_SINCE, List.of("antigo@empresa.com.br"));

		assertEquals(List.of("antigo@empresa.com.br", "outro@empresa.com.br"), emailsOf(everyRun));
		assertEquals(List.of("antigo@empresa.com.br"), emailsOf(restrictedRun));
	}

	/** A return counts when it is more recent than the view; who never viewed stays out of the views. */
	@Test
	public void aSignUpReturnCountsAsAViewOnlyForWhoAlreadyViewed() {
		Map<String, Long> lastViewByRecruiter = new HashMap<>();
		lastViewByRecruiter.put("voltou", 10L);
		lastViewByRecruiter.put("viuDepois", 50L);
		Map<String, Long> signUpReturns = Map.of("voltou", 30L, "viuDepois", 30L, "nunca", 30L);

		VisBusinessSendResumesToRecruiters.countTheSignUpReturnsAsViews(lastViewByRecruiter, signUpReturns);

		assertEquals(Map.of("voltou", 30L, "viuDepois", 50L), lastViewByRecruiter);
	}

	/** Who stopped viewing is left out even without the first e-mail. */
	@Test
	public void aRecruiterOfTheSignUpWhoStoppedViewingIsLeftOut() {
		String login = VisEntityRecruiter.Sources.login.getValue();
		CcpJsonRepresentation signedUp = recruiterOf("signedup@empresa.com.br").put(VisEntityRecruiter.Fields.source, login);
		Map<String, Long> lastViewByRecruiter = new HashMap<>();
		lastViewByRecruiter.put(hashOf("signedup@empresa.com.br"), VIEWS_SINCE - 1);

		List<CcpJsonRepresentation> inOrder = VisBusinessSendResumesToRecruiters.getRecruitersInOrder(List.of(signedUp), lastViewByRecruiter, VIEWS_SINCE, "", Set.of(), 0L, "");

		assertTrue(inOrder.isEmpty());
	}

	private static final long TELEGRAM_SINCE = 5_000L;

	private static CcpJsonRepresentation fromTelegram(String email, long appearance) {
		return recruiterOf(email).put(VisEntityRecruiter.Fields.lastTelegramAppearance, appearance);
	}

	private static CcpJsonRepresentation fromTheLegacy(String email, long lastLegacyView) {
		return recruiterOf(email).put(VisEntityRecruiter.Fields.lastLegacyView, lastLegacyView);
	}

	/**
	 * First the recent viewers (even the ones who appeared on Telegram); then the sign-up waiting for the first e-mail (even
	 * the ones who appeared on Telegram); then who appeared on Telegram in the last six months, the most recent first; then
	 * who viewed on the legacy, the most recent view first (an old appearance on Telegram counts as none); then the queue.
	 */
	@Test
	public void theRecentViewersAndTheFirstEmailComeBeforeTelegramAndTheLegacyViews() {
		String viewer = "viewer@empresa.com.br";
		String viewerOfTelegram = "viewer-telegram@empresa.com.br";
		String signedUp = "signedup-telegram@empresa.com.br";
		String login = VisEntityRecruiter.Sources.login.getValue();
		Map<String, Long> lastViewByRecruiter = new HashMap<>();
		lastViewByRecruiter.put(hashOf(viewer), VIEWS_SINCE + 1);
		lastViewByRecruiter.put(hashOf(viewerOfTelegram), VIEWS_SINCE);
		List<CcpJsonRepresentation> recruiters = List.of(
				recruiterOf("other@empresa.com.br"),
				fromTelegram(signedUp, TELEGRAM_SINCE + 20).put(VisEntityRecruiter.Fields.source, login),
				fromTelegram(viewerOfTelegram, TELEGRAM_SINCE + 30),
				recruiterOf(viewer),
				fromTheLegacy("legacy-old@empresa.com.br", 100L),
				fromTelegram("telegram-old@empresa.com.br", TELEGRAM_SINCE - 1).put(VisEntityRecruiter.Fields.lastLegacyView, 200L),
				fromTelegram("telegram-old-only@empresa.com.br", TELEGRAM_SINCE - 1),
				fromTheLegacy("legacy-recent@empresa.com.br", 300L),
				fromTelegram("telegram-limit@empresa.com.br", TELEGRAM_SINCE),
				fromTelegram("telegram-recent@empresa.com.br", TELEGRAM_SINCE + 10).put(VisEntityRecruiter.Fields.lastLegacyView, 999L));

		List<CcpJsonRepresentation> inOrder = VisBusinessSendResumesToRecruiters.getRecruitersInOrder(recruiters, lastViewByRecruiter, VIEWS_SINCE, "", Set.of(), TELEGRAM_SINCE, "");

		List<String> expectedQueue = new ArrayList<>(List.of("other@empresa.com.br", "telegram-old-only@empresa.com.br"));
		expectedQueue.sort(Comparator.comparing(email -> hashOf(email)));
		List<String> expected = new ArrayList<>(List.of(viewer, viewerOfTelegram, signedUp, "telegram-recent@empresa.com.br", "telegram-limit@empresa.com.br",
				"legacy-recent@empresa.com.br", "telegram-old@empresa.com.br", "legacy-old@empresa.com.br"));
		expected.addAll(expectedQueue);
		assertEquals(expected, emailsOf(inOrder));
		assertTrue(inOrder.get(2).containsField(VisRecruiterResumesFields.firstEmail));
		assertFalse("Telegram has no turns", inOrder.get(3).containsField(VisRecruiterResumesFields.legacyQueuePosition));
		assertTrue(inOrder.get(5).containsField(VisRecruiterResumesFields.legacyQueuePosition));
		assertFalse(inOrder.get(5).containsField(VisRecruiterResumesFields.queuePosition));
	}

	/** The queue of the legacy views goes on after its last position and starts over at its end. */
	@Test
	public void theQueueOfTheLegacyViewsContinuesFromItsLastPosition() {
		List<CcpJsonRepresentation> recruiters = List.of(fromTheLegacy("l1@empresa.com.br", 300L), fromTheLegacy("l2@empresa.com.br", 200L), fromTheLegacy("l3@empresa.com.br", 100L));
		List<CcpJsonRepresentation> firstRun = VisBusinessSendResumesToRecruiters.getRecruitersInOrder(recruiters, new HashMap<>(), VIEWS_SINCE, "", Set.of(), TELEGRAM_SINCE, "");
		String positionOfTheSecond = firstRun.get(1).getAsString(VisRecruiterResumesFields.legacyQueuePosition);

		List<CcpJsonRepresentation> nextRun = VisBusinessSendResumesToRecruiters.getRecruitersInOrder(recruiters, new HashMap<>(), VIEWS_SINCE, "", Set.of(), TELEGRAM_SINCE, positionOfTheSecond);

		assertEquals(List.of("l1@empresa.com.br", "l2@empresa.com.br", "l3@empresa.com.br"), emailsOf(firstRun));
		assertTrue(positionOfTheSecond, positionOfTheSecond.startsWith("9999999999799"));
		assertEquals(List.of("l3@empresa.com.br", "l1@empresa.com.br", "l2@empresa.com.br"), emailsOf(nextRun));
	}

	/** Who stopped viewing in the JobsNow is inactivated, and Telegram or the legacy do not bring them back to the e-mail. */
	@Test
	public void whoStoppedViewingIsLeftOutWhateverTelegramOrTheLegacySay() {
		Map<String, Long> lastViewByRecruiter = new HashMap<>();
		lastViewByRecruiter.put(hashOf("t@empresa.com.br"), VIEWS_SINCE - 1);
		lastViewByRecruiter.put(hashOf("l@empresa.com.br"), VIEWS_SINCE - 1);
		List<CcpJsonRepresentation> recruiters = List.of(fromTelegram("t@empresa.com.br", TELEGRAM_SINCE), fromTheLegacy("l@empresa.com.br", 100L));

		List<CcpJsonRepresentation> inOrder = VisBusinessSendResumesToRecruiters.getRecruitersInOrder(recruiters, lastViewByRecruiter, VIEWS_SINCE, "", Set.of(), TELEGRAM_SINCE, "");

		assertTrue(inOrder.isEmpty());
	}

	/** Each queue keeps its own last position; a recruiter of Telegram moves none of them. */
	@Test
	public void theSenderKeepsTheLastPositionOfEachQueue() {
		// an address that is not valid stops right after the positions, before the rows of the e-mail
		CcpJsonRepresentation notAnAddress = recruiterOf("not an address");
		CcpJsonRepresentation ofTheLegacy = notAnAddress.put(VisRecruiterResumesFields.legacyQueuePosition, "9abc");

		this.sender(List.of(), List.of()).accept(ofTheLegacy);
		assertEquals("9abc", this.lastLegacyQueuePosition[0]);
		assertEquals("", this.lastQueuePosition[0]);

		CcpJsonRepresentation ofTelegram = notAnAddress.put(VisEntityRecruiter.Fields.lastTelegramAppearance, TELEGRAM_SINCE);
		this.sender(List.of(), List.of()).accept(ofTelegram);
		assertEquals("9abc", this.lastLegacyQueuePosition[0]);
		assertEquals("", this.lastQueuePosition[0]);
	}

	/** Who is skipped had their turn, so the queue goes past them; who is out of the run or after the limit did not. */
	@Test
	public void theSenderKeepsTheLastPositionThatHadItsTurn() {
		CcpJsonRepresentation inTheQueue = this.recruiter.put(VisRecruiterResumesFields.queuePosition, "1abc");

		this.sender(List.of("another@empresa.com.br"), List.of()).accept(inTheQueue);
		assertEquals("", this.lastQueuePosition[0]);

		this.sender(List.of(RECRUITER), List.of()).accept(inTheQueue);
		assertEquals("1abc", this.lastQueuePosition[0]);

		this.sender(List.of(RECRUITER), List.of()).accept(this.recruiter);
		assertEquals("a recent viewer has no position", "1abc", this.lastQueuePosition[0]);

		this.emailsSent[0] = VisBusinessSendResumesToRecruiters.RECRUITERS_PER_DAY;
		CcpJsonRepresentation afterTheLimit = this.recruiter.put(VisRecruiterResumesFields.queuePosition, "1def");
		this.sender(List.of(), List.of()).accept(afterTheLimit);
		assertEquals("1abc", this.lastQueuePosition[0]);
	}

	/** The sign-up comes after the recent viewers and before the others, whatever the hash of the e-mails says. */
	@Test
	public void theRecruitersOfTheSignUpComeBetweenTheViewersAndTheOthers() {
		String login = VisEntityRecruiter.Sources.login.getValue();
		String legacy = VisEntityRecruiter.Sources.legacy.getValue();
		CcpJsonRepresentation viewerOfTheSignUp = recruiterOf("viewer@empresa.com.br").put(VisEntityRecruiter.Fields.source, login);
		CcpJsonRepresentation signedUp = recruiterOf("signedup@empresa.com.br").put(VisEntityRecruiter.Fields.source, login);
		CcpJsonRepresentation other = recruiterOf("other@empresa.com.br").put(VisEntityRecruiter.Fields.source, legacy);
		Map<String, Long> lastViewByRecruiter = new HashMap<>();
		lastViewByRecruiter.put(hashOf("viewer@empresa.com.br"), VIEWS_SINCE);

		List<CcpJsonRepresentation> inOrder = VisBusinessSendResumesToRecruiters.getRecruitersInOrder(List.of(other, signedUp, viewerOfTheSignUp), lastViewByRecruiter, VIEWS_SINCE, "", Set.of(), 0L, "");

		assertEquals(List.of("viewer@empresa.com.br", "signedup@empresa.com.br", "other@empresa.com.br"), emailsOf(inOrder));
	}

	/** An address that is not valid has no entity hash, and it does not stop the run. */
	@Test
	public void anInvalidAddressKeepsItsPlaceInTheOrder() {
		List<CcpJsonRepresentation> recruiters = List.of(recruiterOf("not an address"), recruiterOf(RECRUITER));

		List<CcpJsonRepresentation> inOrder = VisBusinessSendResumesToRecruiters.getRecruitersInOrder(recruiters, new HashMap<>(), VIEWS_SINCE, "", Set.of(), 0L, "");

		assertEquals(2, inOrder.size());
	}

	/** With a place left, this recruiter with a resume would go on to the sending, which this test does not set up. */
	@Test
	public void noOneReceivesAfterTheLimitOfTheDay() {
		this.emailsSent[0] = VisBusinessSendResumesToRecruiters.RECRUITERS_PER_DAY;
		List<CcpJsonRepresentation> resumes = List.of(CcpOtherConstants.EMPTY_JSON);

		this.sender(List.of(), resumes).accept(this.recruiter);

		assertEquals(VisBusinessSendResumesToRecruiters.RECRUITERS_PER_DAY, this.emailsSent[0]);
	}

	/** Without this check the hash of the entity refuses the address and the run of every other recruiter stops here. */
	@Test
	public void anInvalidAddressIsSkippedWithoutStoppingTheRun() {
		List<CcpJsonRepresentation> resumes = List.of(CcpOtherConstants.EMPTY_JSON);

		this.sender(List.of(), resumes).accept(recruiterOf("not an address"));

		assertEquals(0, this.emailsSent[0]);
	}

	@Test
	public void aRecruiterOutOfARestrictedRunReceivesNothing() {
		this.sender(List.of("another@empresa.com.br"), List.of()).accept(this.recruiter);

		assertEquals(0, this.emailsSent[0]);
	}

	@Test
	public void aRecruiterWithoutResumesReceivesNothing() {
		this.sender(List.of(RECRUITER), List.of()).accept(this.recruiter);

		assertEquals(0, this.emailsSent[0]);
	}
}
