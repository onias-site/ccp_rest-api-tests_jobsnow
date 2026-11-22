package com.vis.business.position;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
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
import com.ccp.json.validations.global.engine.CcpJsonValidationError;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.jn.db.bulk.JnExecuteBulkOperation;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.utils.JnDeleteKeysFromCache;
import com.vis.entities.VisEntityResume;
import com.vis.services.VisServicePosition;

/**
 * Proves the endpoint of the tab "Opções" of the position form: for each ddd asked, the active resumes of its group in
 * jn_grouper, with part of the resume and its id and never a contact; a resume of two ddds comes in both, a ddd with no
 * resume comes as an empty list, and an unknown or repeated ddd is refused.
 */
public class ResumesOfTheDddsTest {

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
		JnExecuteBulkOperation.INSTANCE.executeBulk(VisEntityResume.INSTANCE.getFirstRecordsToInsert(), JnDeleteKeysFromCache.INSTANCE);
	}

	private final long now = System.currentTimeMillis();

	private final String resumeEmail = "resumesOfTheDdds" + this.now + "@jobsnow.com";

	private CcpJsonRepresentation resume() {
		CcpJsonRepresentation skill = new CcpJsonRepresentation("{\"skill\":\"JAVA\",\"word\":\"JAVA\",\"type\":\"skillsFromResume\",\"category\":\"skillsFromText\"}");
		return CcpOtherConstants.EMPTY_JSON
				.put(VisEntityResume.Fields.email, this.resumeEmail)
				.put(VisEntityResume.Fields.ddd, Arrays.asList(11, 21))
				.put(VisEntityResume.Fields.desiredJob, "developer")
				.put(VisEntityResume.Fields.disponibility, 0)
				.put(VisEntityResume.Fields.experience, 3)
				.put(VisEntityResume.Fields.linkedinAddress, "https://www.linkedin.com/in/resumes-of-the-ddds-" + this.now)
				.put(VisEntityResume.Fields.clt, 9000)
				.put(VisEntityResume.Fields.resumeType, 1)
				.put(VisEntityResume.Fields.temporallyJobTime, 0)
				.put(VisEntityResume.Fields.skill, Arrays.asList(skill));
	}

	private static CcpJsonRepresentation ask(Object... ddds) {
		CcpJsonRepresentation input = CcpOtherConstants.EMPTY_JSON.put(VisBusinessResumesOfTheDdds.Fields.ddd, Arrays.asList(ddds));
		// como numa requisição HTTP: os números chegam do texto JSON
		CcpJsonRepresentation parsed = new CcpJsonRepresentation(input.asUgglyJson());
		CcpJsonRepresentation response = VisServicePosition.GetResumesOfTheDdds.execute(parsed);
		CcpJsonRepresentation resumes = response.getInnerJson(VisBusinessResumesOfTheDdds.Fields.resumes);
		return resumes;
	}

	private static List<CcpJsonRepresentation> resumesOf(CcpJsonRepresentation resumesByDdd, int ddd) {
		CcpFieldName dddField = new CcpFieldName(String.valueOf(ddd));
		List<CcpJsonRepresentation> resumes = resumesByDdd.getAsJsonList(dddField);
		return resumes;
	}

	private static boolean hasTheResume(List<CcpJsonRepresentation> resumes, String resumeId) {
		boolean found = resumes.stream().anyMatch(resume -> resumeId.equals(resume.getAsString(JnJsonCommonsFields.id)));
		return found;
	}

	@After
	public void deleteTheResume() {
		VisEntityResume.ENTITY.deleteAnyWhere(CcpOtherConstants.EMPTY_JSON.put(VisEntityResume.Fields.email, this.resumeEmail));
	}

	@Test
	public void eachDddBringsItsActiveResumesWithoutContact() {
		CcpJsonRepresentation resume = this.resume();
		VisEntityResume.ENTITY.save(resume);
		// the id comes from the stored record: the e-mail is transformed on the way in
		CcpJsonRepresentation stored = VisEntityResume.ENTITY.getOneById(resume);
		String resumeId = VisEntityResume.ENTITY.calculateId(stored);

		CcpJsonRepresentation resumesByDdd = ask(11, 21, 10);

		assertEquals("one key per ddd asked", 3, resumesByDdd.fieldSet().size());
		List<CcpJsonRepresentation> ofDdd11 = resumesOf(resumesByDdd, 11);
		assertTrue("the resume is in the group of each of its ddds", hasTheResume(ofDdd11, resumeId) && hasTheResume(resumesOf(resumesByDdd, 21), resumeId));
		CcpJsonRepresentation child = ofDdd11.stream().filter(item -> resumeId.equals(item.getAsString(JnJsonCommonsFields.id))).findFirst().get();
		assertEquals("the child carries part of the resume", 9000, child.getAsIntegerNumber(VisEntityResume.Fields.clt).intValue());
		assertFalse("never a contact", child.containsAllFields(VisEntityResume.Fields.email) || child.containsAllFields(VisEntityResume.Fields.linkedinAddress));

		VisEntityResume.ENTITY.delete(resume);
		CcpJsonRepresentation afterTheDelete = ask(11);
		assertFalse("only the active resumes", hasTheResume(resumesOf(afterTheDelete, 11), resumeId));
	}

	@Test
	public void withNoDddEveryDddComes() {
		CcpJsonRepresentation resume = this.resume();
		VisEntityResume.ENTITY.save(resume);
		CcpJsonRepresentation stored = VisEntityResume.ENTITY.getOneById(resume);
		String resumeId = VisEntityResume.ENTITY.calculateId(stored);

		CcpJsonRepresentation response = VisServicePosition.GetResumesOfTheDdds.execute(CcpOtherConstants.EMPTY_JSON);
		CcpJsonRepresentation resumesByDdd = response.getInnerJson(VisBusinessResumesOfTheDdds.Fields.resumes);

		assertEquals("one key per allowed ddd: the 67 regions and home office", 68, resumesByDdd.fieldSet().size());
		assertTrue("the resume comes in each of its ddds", hasTheResume(resumesOf(resumesByDdd, 11), resumeId) && hasTheResume(resumesOf(resumesByDdd, 21), resumeId));
		assertTrue("home office is one of them", resumesByDdd.fieldSet().contains("10"));
	}

	@Test
	public void anUnknownDddIsRefused() {
		try {
			ask(10, 1);
			fail("ddd 1 does not exist");
		} catch (CcpJsonValidationError expected) {
		}
		try {
			ask();
			fail("at least one ddd");
		} catch (CcpJsonValidationError expected) {
		}
		try {
			ask(11, 11);
			fail("a repeated ddd");
		} catch (CcpJsonValidationError expected) {
		}
	}
}
