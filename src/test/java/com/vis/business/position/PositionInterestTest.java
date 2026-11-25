package com.vis.business.position;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpTimeDecorator;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.dependency.injection.CcpInstanceProvider;
import com.ccp.especifications.db.utils.entity.decorators.engine.SilentInstantMessenger;
import com.ccp.especifications.email.CcpEmailProvider;
import com.ccp.especifications.instant.messenger.CcpInstantMessenger;
import com.ccp.flow.CcpErrorFlowDisturb;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.implementations.password.mindrot.CcpMindrotPasswordHandler;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.utils.JnGroupers;
import com.vis.business.recruiter.resumes.VisBusinessSendTheResumesOfTheInterestedCandidates;
import com.vis.entities.RecordingEmailSender;
import com.vis.entities.VisEntityPosition;
import com.vis.entities.VisEntityPositionInterest;
import com.vis.entities.VisEntityPositionResumeSent;
import com.vis.entities.VisEntityResume;
import com.vis.entities.VisEntityResumeRestriction;
import com.vis.entities.VisEntityViewedResume;
import com.vis.entities.VisEntityVirtualPositionInterestHour;
import com.vis.entities.VisEntityVirtualPositionInterestedResumes;

/**
 * 2026-11-24, against the local Elasticsearch: the candidate of the screen "Minhas vagas" sends their resume to a position
 * open to the candidates; the interest is saved for the next full hour, with the resume in the group of the position and
 * hour and the position in the group of the hour ({@code jn_grouper}); both sides are checked at the confirmation (a
 * second interest, a restricted resume, a position of a company the resume forbids, of a recruiter who does not want the
 * resume or of another frequency are refused, and the last three are not even listed) and again at the sending of the
 * full hour, which sends each resume once. The positions ask a combination no stored resume has (ddd 69 with the three
 * flags), as in {@link PositionsOfTheResumeTest}.
 */
public class PositionInterestTest {

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
	}

	private final long now = System.currentTimeMillis();

	private final String recruiter = "interesse" + this.now + "@empresa-teste.com.br";

	private final List<CcpJsonRepresentation> savedPositions = new ArrayList<>();

	private final List<CcpJsonRepresentation> savedResumes = new ArrayList<>();

	private final List<CcpJsonRepresentation> savedRecords = new ArrayList<>();

	@After
	public void deleteWhatWasSaved() {
		for (CcpJsonRepresentation resume : this.savedResumes) {
			String email = resume.getAsString(VisEntityResume.Fields.email);
			CcpJsonRepresentation restrictionKey = CcpOtherConstants.EMPTY_JSON.put(VisEntityResumeRestriction.Fields.email, email);
			VisEntityResumeRestriction.ENTITY.delete(restrictionKey);
			for (CcpJsonRepresentation position : this.savedPositions) {
				String positionId = positionIdOf(position);
				CcpJsonRepresentation interestKey = CcpOtherConstants.EMPTY_JSON
						.put(VisEntityPositionInterest.Fields.position, positionId)
						.put(VisEntityPositionInterest.Fields.email, email);
				VisEntityPositionInterest.ENTITY.delete(interestKey);
			}
		}
		this.savedRecords.forEach(sentPair -> VisEntityPositionResumeSent.ENTITY.delete(sentPair));
		this.savedPositions.forEach(position -> VisEntityPosition.ENTITY.deleteAnyWhere(position));
		this.savedResumes.forEach(resume -> VisEntityResume.ENTITY.deleteAnyWhere(resume));
	}

	private CcpJsonRepresentation position(String title, String frequencyType, String resumesEmail) {
		String max = String.valueOf(this.now + 2 * 86_400_000L);
		CcpJsonRepresentation expiration = new CcpJsonRepresentation("{\"min\":\"" + this.now + "\",\"max\":\"" + max + "\"}");
		CcpJsonRepresentation sort = new CcpJsonRepresentation("{\"type\":\"asc\",\"field\":[\"clt\"]}");
		CcpJsonRepresentation contact = new CcpJsonRepresentation("{\"type\":\"email\",\"value\":\"selecao@empresa-teste.com.br\"}");
		String description = "position used to test the interest of the candidates, with a description long enough to pass the minimum of one hundred characters";
		CcpJsonRepresentation position = CcpOtherConstants.EMPTY_JSON
				.put(VisEntityPosition.Fields.recruiter, this.recruiter)
				.put(VisEntityPosition.Fields.yearMonth, VisPositionOfTheRecruiter.getCurrentYearMonth())
				.put(VisEntityPosition.Fields.title, title)
				.put(VisEntityPosition.Fields.resumeType, 1)
				.put(VisEntityPosition.Fields.clt, new CcpJsonRepresentation("{\"min\":99000,\"max\":100000}"))
				.put(VisEntityPosition.Fields.ddd, Arrays.asList(69))
				.put(VisEntityPosition.Fields.description, description)
				.put(VisEntityPosition.Fields.disponibility, 30)
				.put(VisEntityPosition.Fields.expiration, expiration)
				.put(VisEntityPosition.Fields.seniority, List.of("pleno"))
				.put(VisEntityPosition.Fields.pcd, true)
				.put(VisEntityPosition.Fields.travel, true)
				.put(VisEntityPosition.Fields.negotiableClaim, true)
				.put(VisEntityPosition.Fields.sort, Arrays.asList(sort))
				.put(VisEntityPosition.Fields.contact, Arrays.asList(contact))
				.put(VisEntityPosition.Fields.temporallyJobTime, 0)
				.put(VisEntityPosition.Fields.frequencyType, frequencyType)
				.put(VisEntityPosition.Fields.mustSendInGroups, false)
				.put(VisEntityPosition.Fields.resumesEmail, resumesEmail);
		VisEntityPosition.ENTITY.save(position);
		this.savedPositions.add(position);
		return position;
	}

	private CcpJsonRepresentation position(String title) {
		CcpJsonRepresentation position = this.position(title, "byTheCandidates", this.recruiter);
		return position;
	}

	private String resume(String name, List<String> notAllowedCompanies) {
		String email = name + this.now + "@jobsnow.com";
		CcpJsonRepresentation resume = CcpOtherConstants.EMPTY_JSON
				.put(VisEntityResume.Fields.email, email)
				.put(VisEntityResume.Fields.ddd, Arrays.asList(69))
				.put(VisEntityResume.Fields.desiredJob, "developer")
				.put(VisEntityResume.Fields.disponibility, 10)
				.put(VisEntityResume.Fields.experience, 4)
				.put(VisEntityResume.Fields.linkedinAddress, this.linkedinOf(name))
				.put(VisEntityResume.Fields.clt, 99500)
				.put(VisEntityResume.Fields.resumeType, 1)
				.put(VisEntityResume.Fields.temporallyJobTime, 6)
				.put(VisEntityResume.Fields.negotiableClaim, true)
				.put(VisEntityResume.Fields.pcd, true)
				.put(VisEntityResume.Fields.travel, true)
				.put(VisEntityResume.Fields.spokenLanguage, List.of())
				.put(VisEntityResume.Fields.notAllowedCompany, notAllowedCompanies)
				.put(VisEntityResume.Fields.skill, List.of());
		VisEntityResume.ENTITY.save(resume);
		this.savedResumes.add(resume);
		return email;
	}

	private String resume(String name) {
		String email = this.resume(name, List.of());
		return email;
	}

	private String linkedinOf(String name) {
		String linkedin = "https://www.linkedin.com/in/" + name + "-" + this.now;
		return linkedin;
	}

	private static CcpJsonRepresentation positionsOf(String email) {
		CcpJsonRepresentation input = CcpOtherConstants.EMPTY_JSON.put(VisBusinessPositionsOfTheResume.Fields.email, email);
		CcpJsonRepresentation response = VisBusinessPositionsOfTheResume.INSTANCE.execute(input);
		return response;
	}

	private static List<CcpJsonRepresentation> listedPositionsOf(String email) {
		CcpJsonRepresentation response = positionsOf(email);
		List<CcpJsonRepresentation> positions = response.getAsJsonList(VisBusinessPositionsOfTheResume.ResponseFields.positions);
		return positions;
	}

	private static boolean isListed(String email, String title) {
		List<CcpJsonRepresentation> positions = listedPositionsOf(email);
		boolean listed = positions.stream().anyMatch(position -> title.equals(position.getAsString(VisEntityPosition.Fields.title)));
		return listed;
	}

	private static CcpJsonRepresentation register(String email, CcpJsonRepresentation position) {
		String positionId = positionIdOf(position);
		CcpJsonRepresentation input = CcpOtherConstants.EMPTY_JSON
				.put(VisBusinessRegisterInterestInThePosition.Fields.email, email)
				.put(VisBusinessRegisterInterestInThePosition.Fields.position, positionId);
		CcpJsonRepresentation response = VisBusinessRegisterInterestInThePosition.INSTANCE.execute(input);
		return response;
	}

	private static void assertRefused(int status, String email, CcpJsonRepresentation position) {
		try {
			register(email, position);
			fail("the interest should be refused with " + status);
		} catch (CcpErrorFlowDisturb expected) {
			assertEquals(status, expected.status.asNumber());
		}
	}

	// the id as the entity calculates it on the save: the recruiter hashed
	private static String positionIdOf(CcpJsonRepresentation position) {
		CcpJsonRepresentation positionKey = VisEntityPosition.ENTITY.getJsonWithHandledPrimaryKey(position);
		String positionId = VisEntityPosition.ENTITY.calculateId(positionKey);
		return positionId;
	}

	private static String resumeIdOf(String email) {
		CcpJsonRepresentation resumeKey = CcpOtherConstants.EMPTY_JSON.put(VisEntityResume.Fields.email, email);
		CcpJsonRepresentation resume = VisEntityResume.ENTITY.getOneById(resumeKey);
		String resumeId = VisEntityResume.ENTITY.calculateId(resume);
		return resumeId;
	}

	@Test
	public void theInterestGoesToTheNextFullHourWithTheResumeInTheGroupOfThePositionAndThePositionInTheGroupOfTheHour() {
		String title = "Vaga com interesse " + this.now;
		CcpJsonRepresentation position = this.position(title);
		String email = this.resume("interessado");
		List<CcpJsonRepresentation> before = listedPositionsOf(email);
		CcpJsonRepresentation listed = before.stream().filter(item -> title.equals(item.getAsString(VisEntityPosition.Fields.title))).findFirst().get();
		String positionId = positionIdOf(position);
		assertEquals("the screen gets the id of the position", positionId, listed.getAsString(JnJsonCommonsFields.id));
		assertFalse(listed.getAsBoolean(VisBusinessPositionsOfTheResume.ResponseFields.resumeSent));
		assertFalse(listed.containsAllFields(VisEntityPositionInterest.Fields.sendingHour));

		long beforeTheInterest = System.currentTimeMillis();
		CcpJsonRepresentation response = register(email, position);
		long afterTheInterest = System.currentTimeMillis();

		String sendingHour = response.getAsString(VisEntityPositionInterest.Fields.sendingHour);
		String earliest = VisEntityVirtualPositionInterestHour.getNextSendingHour(beforeTheInterest);
		String latest = VisEntityVirtualPositionInterestHour.getNextSendingHour(afterTheInterest);
		assertTrue(sendingHour, sendingHour.equals(earliest) || sendingHour.equals(latest));
		String childName = VisEntityPositionInterest.ENTITY.getEntityMetaData().entityName;
		String resumesParentName = VisEntityVirtualPositionInterestedResumes.ENTITY.getEntityMetaData().entityName;
		String resumesParentId = VisEntityVirtualPositionInterestedResumes.getParentId(positionId, sendingHour);
		List<String> interestedResumes = JnGroupers.getChildren(false, resumesParentName, childName, resumesParentId);
		String resumeId = resumeIdOf(email);
		assertEquals(List.of(resumeId), interestedResumes);
		String hourParentName = VisEntityVirtualPositionInterestHour.ENTITY.getEntityMetaData().entityName;
		String hourParentId = VisEntityVirtualPositionInterestHour.getParentId(sendingHour);
		List<String> positionsOfTheHour = JnGroupers.getChildren(false, hourParentName, childName, hourParentId);
		assertTrue(positionsOfTheHour.toString(), positionsOfTheHour.contains(positionId));

		List<CcpJsonRepresentation> after = listedPositionsOf(email);
		CcpJsonRepresentation listedAfter = after.stream().filter(item -> title.equals(item.getAsString(VisEntityPosition.Fields.title))).findFirst().get();
		assertEquals(sendingHour, listedAfter.getAsString(VisEntityPositionInterest.Fields.sendingHour));
		assertRefused(409, email, position);
	}

	@Test
	public void theNextFullHourOfAnInterestRegisteredBeforeAndAtTheTurnOfTheHour() {
		long ten = new CcpTimeDecorator("24/11/2026 21:59:59", "dd/MM/yyyy HH:mm:ss").getContent();
		long turn = new CcpTimeDecorator("24/11/2026 22:00:00", "dd/MM/yyyy HH:mm:ss").getContent();
		long oneMinuteLater = new CcpTimeDecorator("24/11/2026 22:01:00", "dd/MM/yyyy HH:mm:ss").getContent();
		assertEquals("2026112422", VisEntityVirtualPositionInterestHour.getNextSendingHour(ten));
		assertEquals("2026112423", VisEntityVirtualPositionInterestHour.getNextSendingHour(turn));
		assertEquals("2026112423", VisEntityVirtualPositionInterestHour.getNextSendingHour(oneMinuteLater));
		assertEquals("2026112422", VisEntityVirtualPositionInterestHour.getSendingHour(turn));
	}

	@Test
	public void aResumeWithARestrictionSeesNoPositionAndCanNotSendTheResume() {
		CcpJsonRepresentation position = this.position("Vaga para restrito " + this.now);
		String email = this.resume("restrito");
		CcpJsonRepresentation restriction = CcpOtherConstants.EMPTY_JSON
				.put(VisEntityResumeRestriction.Fields.email, email)
				.put(VisEntityResumeRestriction.Fields.message, List.of("Corrija o seu currículo antes de enviar"));
		VisEntityResumeRestriction.ENTITY.save(restriction);

		CcpJsonRepresentation response = positionsOf(email);

		assertTrue(response.getAsBoolean(VisBusinessPositionsOfTheResume.ResponseFields.resumeWithPendingIssues));
		assertTrue(response.getAsJsonList(VisBusinessPositionsOfTheResume.ResponseFields.positions).isEmpty());
		assertRefused(422, email, position);
	}

	@Test
	public void aResumeWithTheLinkedinConfirmedInvalidHasNoPositionOpen() {
		CcpJsonRepresentation position = this.position("Vaga para linkedin invalido " + this.now);
		String email = this.resume("linkedininvalido");
		CcpJsonRepresentation resumeKey = CcpOtherConstants.EMPTY_JSON.put(VisEntityResume.Fields.email, email);
		CcpJsonRepresentation resume = VisEntityResume.ENTITY.getOneById(resumeKey).put(VisEntityResume.Fields.invalidLinkedinAddress, true);
		String positionId = positionIdOf(position);
		CcpJsonRepresentation storedPosition = VisEntityPosition.ENTITY.getOneById(position);
		CcpJsonRepresentation positionsById = CcpOtherConstants.EMPTY_JSON.put(new CcpFieldName(positionId), storedPosition);
		CcpJsonRepresentation input = CcpOtherConstants.EMPTY_JSON
				.put(VisBusinessPositionsOpenToTheResume.Fields.resume, resume)
				.put(VisBusinessPositionsOpenToTheResume.Fields.restricted, false)
				.put(VisBusinessPositionsOpenToTheResume.Fields.positions, positionsById);

		CcpJsonRepresentation withTheValidLinkedin = VisBusinessPositionsOpenToTheResume.INSTANCE.execute(input.put(VisBusinessPositionsOpenToTheResume.Fields.resume, resume.put(VisEntityResume.Fields.invalidLinkedinAddress, false)));
		CcpJsonRepresentation withTheInvalidLinkedin = VisBusinessPositionsOpenToTheResume.INSTANCE.execute(input);

		assertTrue(withTheValidLinkedin.getInnerJson(VisBusinessPositionsOpenToTheResume.Fields.positions).containsAllFields(new CcpFieldName(positionId)));
		assertTrue(withTheInvalidLinkedin.getInnerJson(VisBusinessPositionsOpenToTheResume.Fields.positions).isEmpty());
	}

	@Test
	public void aPositionOfACompanyTheResumeForbidsIsNeitherListedNorReceivesTheResume() {
		// the e-mail that receives the resumes is of another company: only the domain of the recruiter who owns the position
		// (recruiterDomain, calculated at the save) tells the company the resume forbids
		String title = "Vaga de empresa proibida " + this.now;
		CcpJsonRepresentation position = this.position(title, "byTheCandidates", "selecao" + this.now + "@outra-firma.com.br");
		CcpJsonRepresentation storedPosition = VisEntityPosition.ENTITY.getOneById(position);
		assertEquals("empresa-teste.com.br", storedPosition.getAsString(VisEntityPosition.Fields.recruiterDomain));
		String email = this.resume("proibe", List.of("empresa-teste.com.br"));
		String otherEmail = this.resume("naoproibe");

		assertFalse(isListed(email, title));
		assertTrue(isListed(otherEmail, title));
		assertRefused(404, email, position);
	}

	@Test
	public void aPositionOfARecruiterWhoKeepsTheResumeAmongTheUnwantedIsNeitherListedNorReceivesTheResume() {
		String title = "Vaga de recrutador que negativou " + this.now;
		CcpJsonRepresentation position = this.position(title);
		String email = this.resume("negativado");
		assertTrue(isListed(email, title));
		CcpJsonRepresentation unwantedView = CcpOtherConstants.EMPTY_JSON
				.put(VisEntityViewedResume.Fields.linkedin, this.linkedinOf("negativado"))
				.put(VisEntityViewedResume.Fields.email, this.recruiter)
				.put(VisEntityViewedResume.Fields.alias, "Negativado " + this.now);
		VisEntityViewedResume.ENTITY.getTwinEntity().save(unwantedView);
		try {
			assertFalse(isListed(email, title));
			assertRefused(404, email, position);
		} finally {
			VisEntityViewedResume.ENTITY.deleteAnyWhere(unwantedView);
		}
	}

	@Test
	public void aPositionOfAnotherFrequencyDoesNotReceiveTheInterest() {
		CcpJsonRepresentation position = this.position("Vaga de envio diario " + this.now, "daily", this.recruiter);
		String email = this.resume("outrafrequencia");
		assertRefused(404, email, position);
	}

	@Test
	public void aPositionThatDoesNotExistDoesNotReceiveTheInterest() {
		String email = this.resume("semvaga");
		CcpJsonRepresentation missingPosition = CcpOtherConstants.EMPTY_JSON
				.put(VisEntityPosition.Fields.recruiter, this.recruiter)
				.put(VisEntityPosition.Fields.yearMonth, VisPositionOfTheRecruiter.getCurrentYearMonth())
				.put(VisEntityPosition.Fields.title, "Vaga que nao existe " + this.now);
		assertRefused(404, email, missingPosition);
	}

	@Test
	public void theFullHourSendsTheInterestedResumesAndChecksBothSidesAgain() {
		String title = "Vaga enviada na hora cheia " + this.now;
		CcpJsonRepresentation position = this.position(title);
		String positionId = positionIdOf(position);
		String interested = this.resume("enviado");
		String restrictedLater = this.resume("restritodepois");
		String currentHour = VisEntityVirtualPositionInterestHour.getSendingHour(System.currentTimeMillis());
		// as registered before this hour: the interests of the hour being sent
		for (String email : List.of(interested, restrictedLater)) {
			String resumeId = resumeIdOf(email);
			CcpJsonRepresentation interest = CcpOtherConstants.EMPTY_JSON
					.put(VisEntityPositionInterest.Fields.position, positionId)
					.put(VisEntityPositionInterest.Fields.email, email)
					.put(VisEntityPositionInterest.Fields.resumeId, resumeId)
					.put(VisEntityPositionInterest.Fields.sendingHour, currentHour);
			VisEntityPositionInterest.ENTITY.save(interest);
		}
		// the restriction came after the interest: the full hour checks the resume again
		CcpJsonRepresentation restriction = CcpOtherConstants.EMPTY_JSON
				.put(VisEntityResumeRestriction.Fields.email, restrictedLater)
				.put(VisEntityResumeRestriction.Fields.message, List.of("Corrija o seu currículo antes de enviar"));
		VisEntityResumeRestriction.ENTITY.save(restriction);
		CcpJsonRepresentation sentPair = this.sentPairOf(positionId, interested);
		CcpJsonRepresentation restrictedPair = this.sentPairOf(positionId, restrictedLater);

		RecordingEmailSender.SENT.clear();
		CcpJsonRepresentation firstRun = VisBusinessSendTheResumesOfTheInterestedCandidates.INSTANCE.execute(CcpOtherConstants.EMPTY_JSON);

		// the e-mail of the lists of resumes of the positions, to the e-mail that receives the resumes, with the one resume
		String expectedEmail = "[" + this.recruiter + "] | 1 Cvs recebidos para a vaga " + title;
		long emails = RecordingEmailSender.SENT.stream().filter(sent -> sent.equalsIgnoreCase(expectedEmail)).count();
		assertEquals(RecordingEmailSender.SENT.toString(), 1, emails);

		assertTrue(firstRun.toString(), VisEntityPositionResumeSent.ENTITY.exists(sentPair));
		assertFalse(firstRun.toString(), VisEntityPositionResumeSent.ENTITY.exists(restrictedPair));
		List<CcpJsonRepresentation> listed = listedPositionsOf(interested);
		CcpJsonRepresentation listedPosition = listed.stream().filter(item -> positionId.equals(item.getAsString(JnJsonCommonsFields.id))).findFirst().get();
		assertTrue(listedPosition.getAsBoolean(VisBusinessPositionsOfTheResume.ResponseFields.resumeSent));
	}

	private CcpJsonRepresentation sentPairOf(String positionId, String email) {
		CcpJsonRepresentation resumeKey = CcpOtherConstants.EMPTY_JSON.put(VisEntityResume.Fields.email, email);
		CcpJsonRepresentation resume = VisEntityResume.ENTITY.getOneById(resumeKey);
		String emailHash = resume.getAsString(VisEntityResume.Fields.email);
		CcpJsonRepresentation pair = CcpOtherConstants.EMPTY_JSON
				.put(VisEntityPositionResumeSent.Fields.position, positionId)
				.put(VisEntityPositionResumeSent.Fields.resume, emailHash);
		this.savedRecords.add(pair);
		return pair;
	}
}
