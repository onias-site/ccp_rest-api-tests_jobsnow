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
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.flow.CcpErrorFlowDisturb;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.dependency.injection.CcpInstanceProvider;
import com.ccp.especifications.db.utils.entity.decorators.engine.SilentEmailSender;
import com.ccp.especifications.db.utils.entity.decorators.engine.SilentInstantMessenger;
import com.ccp.especifications.email.CcpEmailProvider;
import com.ccp.especifications.instant.messenger.CcpInstantMessenger;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.implementations.password.mindrot.CcpMindrotPasswordHandler;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.vis.entities.VisEntityPosition;
import com.vis.entities.VisEntityResume;

/**
 * 2026-11-24, against the local Elasticsearch: the screen "Minhas vagas" of the candidate lists, read by id, the positions
 * open to the candidates themselves ({@code byTheCandidates}) that the resume satisfies, with only the fields the
 * candidate may see; a position of another frequency, or asking another region, stays out; a candidate with no resume
 * gets the error of the resume not found. The positions ask a combination no stored resume has (ddd 69 with the three
 * flags), as in {@code ResumesOfThePositionsSendingTest}.
 */
public class PositionsOfTheResumeTest {

	static {
		CcpInstanceProvider<CcpInstantMessenger> telegram = () -> SilentInstantMessenger.INSTANCE;
		CcpInstanceProvider<CcpEmailProvider> email = () -> SilentEmailSender.INSTANCE;
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

	private final String recruiter = "vagas" + this.now + "@empresa-teste.com.br";

	private final List<CcpJsonRepresentation> savedPositions = new ArrayList<>();

	private final List<CcpJsonRepresentation> savedResumes = new ArrayList<>();

	@After
	public void deleteWhatWasSaved() {
		this.savedPositions.forEach(position -> VisEntityPosition.ENTITY.deleteAnyWhere(position));
		this.savedResumes.forEach(resume -> VisEntityResume.ENTITY.deleteAnyWhere(resume));
	}

	private void position(String title, String frequencyType, int ddd) {
		String max = String.valueOf(this.now + 2 * 86_400_000L);
		CcpJsonRepresentation expiration = new CcpJsonRepresentation("{\"min\":\"" + this.now + "\",\"max\":\"" + max + "\"}");
		CcpJsonRepresentation sort = new CcpJsonRepresentation("{\"type\":\"asc\",\"field\":[\"clt\"]}");
		CcpJsonRepresentation contact = new CcpJsonRepresentation("{\"type\":\"email\",\"value\":\"selecao@empresa-teste.com.br\"}");
		String description = "position used to test the positions of the resume, with a description long enough to pass the minimum of one hundred characters";
		CcpJsonRepresentation position = CcpOtherConstants.EMPTY_JSON
				.put(VisEntityPosition.Fields.recruiter, this.recruiter)
				.put(VisEntityPosition.Fields.title, title)
				.put(VisEntityPosition.Fields.resumeType, 1)
				.put(VisEntityPosition.Fields.clt, new CcpJsonRepresentation("{\"min\":99000,\"max\":100000}"))
				.put(VisEntityPosition.Fields.ddd, Arrays.asList(ddd))
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
				.put(VisEntityPosition.Fields.resumesEmail, this.recruiter);
		VisEntityPosition.ENTITY.save(position);
		this.savedPositions.add(position);
	}

	private String resume() {
		String email = "candidato" + this.now + "@jobsnow.com";
		CcpJsonRepresentation resume = CcpOtherConstants.EMPTY_JSON
				.put(VisEntityResume.Fields.email, email)
				.put(VisEntityResume.Fields.ddd, Arrays.asList(69))
				.put(VisEntityResume.Fields.desiredJob, "developer")
				.put(VisEntityResume.Fields.disponibility, 10)
				.put(VisEntityResume.Fields.experience, 4)
				.put(VisEntityResume.Fields.linkedinAddress, "https://www.linkedin.com/in/candidato-" + this.now)
				.put(VisEntityResume.Fields.clt, 99500)
				.put(VisEntityResume.Fields.resumeType, 1)
				.put(VisEntityResume.Fields.temporallyJobTime, 6)
				.put(VisEntityResume.Fields.negotiableClaim, true)
				.put(VisEntityResume.Fields.pcd, true)
				.put(VisEntityResume.Fields.travel, true)
				.put(VisEntityResume.Fields.spokenLanguage, List.of())
				.put(VisEntityResume.Fields.notAllowedCompany, List.of())
				.put(VisEntityResume.Fields.skill, List.of());
		VisEntityResume.ENTITY.save(resume);
		this.savedResumes.add(resume);
		return email;
	}

	private static List<CcpJsonRepresentation> positionsOf(String email) {
		CcpJsonRepresentation input = CcpOtherConstants.EMPTY_JSON.put(VisBusinessPositionsOfTheResume.Fields.email, email);
		CcpJsonRepresentation response = VisBusinessPositionsOfTheResume.INSTANCE.execute(input);
		return response.getAsJsonList(VisBusinessPositionsOfTheResume.ResponseFields.positions);
	}

	@Test
	public void onlyThePositionsOpenToTheCandidatesThatTheResumeSatisfiesAreListed() {
		String open = "Vaga aberta aos candidatos " + this.now;
		String daily = "Vaga de envio diario " + this.now;
		String otherRegion = "Vaga de outra regiao " + this.now;
		this.position(open, "byTheCandidates", 69);
		this.position(daily, "daily", 69);
		this.position(otherRegion, "byTheCandidates", 11);
		String email = this.resume();

		List<CcpJsonRepresentation> positions = positionsOf(email);

		List<String> titles = positions.stream().map(position -> position.getAsString(VisEntityPosition.Fields.title)).toList();
		assertTrue(titles.toString(), titles.contains(open));
		assertFalse("another frequency", titles.contains(daily));
		assertFalse("another region", titles.contains(otherRegion));
		CcpJsonRepresentation listed = positions.stream().filter(position -> open.equals(position.getAsString(VisEntityPosition.Fields.title))).findFirst().get();
		assertFalse("the recruiter is not shown", listed.containsAllFields(VisEntityPosition.Fields.recruiter));
		assertFalse("the e-mail that receives the resumes is not shown", listed.containsAllFields(VisEntityPosition.Fields.resumesEmail));
		assertEquals(1, listed.getAsJsonList(VisEntityPosition.Fields.contact).size());
	}

	@Test
	public void aCandidateWithNoResumeGetsTheErrorOfTheResumeNotFound() {
		try {
			positionsOf("sem-curriculo" + this.now + "@jobsnow.com");
			fail("a candidate with no resume");
		} catch (CcpErrorFlowDisturb expected) {
			assertEquals(404, expected.status.asNumber());
		}
	}
}
