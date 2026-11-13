package com.vis.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.dependency.injection.CcpInstanceProvider;
import com.ccp.especifications.db.utils.entity.CcpEntity;
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
import com.ccp.especifications.db.utils.entity.decorators.engine.SilentEmailSender;
import com.ccp.especifications.db.utils.entity.decorators.engine.SilentInstantMessenger;
import com.vis.business.resume.VisBusinessSaveResumeAndSpendTheUpdate;
import com.vis.services.VisServiceResume;

/**
 * The restriction of a resume ({@code vis_resume_restriction}, since 2026-11-12): the form of the owner gets its
 * messages, and any update of the resume, active or inactive, deletes it.
 */
public class ResumeRestrictionTest {

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

	private static final String MESSAGE = "No campo 'Empresas / consultorias que não podem receber o meu linkedin' você cadastrou os seguintes registros de forma incorreta Consultoria X";

	private final long run = System.currentTimeMillis();

	private final String email = "restricao" + this.run + "@teste.com";

	@After
	public void deleteWhatTheTestCreated() {
		VisEntityResume.ENTITY.deleteAnyWhere(this.key());
		VisEntityResumeRestriction.ENTITY.delete(this.key());
	}

	/** The form of the owner gets the messages of the restriction, with the record of the resume. */
	@Test
	public void theFormShowsTheMessagesOfTheRestriction() {
		VisEntityResume.ENTITY.save(this.resume("Analista restrito"));
		this.saveTheRestriction();

		CcpJsonRepresentation data = VisServiceResume.GetData.execute(this.key());

		CcpJsonRepresentation record = data.getInnerJson(new CcpFieldName("vis_resume"));
		List<String> messages = record.getAsStringList(new CcpFieldName("restrictionMessages"));
		assertEquals(List.of(MESSAGE), messages);
	}

	/** Without a restriction, the form gets no message. */
	@Test
	public void withoutRestrictionTheFormGetsNoMessage() {
		VisEntityResume.ENTITY.save(this.resume("Analista livre"));

		CcpJsonRepresentation data = VisServiceResume.GetData.execute(this.key());

		CcpJsonRepresentation record = data.getInnerJson(new CcpFieldName("vis_resume"));
		assertFalse(record.toString(), record.containsField(new CcpFieldName("restrictionMessages")));
	}

	/** An update of the active resume releases the restriction. */
	@Test
	public void anUpdateOfTheActiveResumeReleasesTheRestriction() {
		VisEntityResume.ENTITY.save(this.resume("Analista antes"));
		this.saveTheRestriction();

		VisEntityResume.ENTITY.save(this.resume("Analista corrigido"));

		assertFalse(VisEntityResumeRestriction.ENTITY.exists(this.key()));
	}

	/** An update of the inactive resume releases the restriction too, and the resume stays inactive. */
	@Test
	public void anUpdateOfTheInactiveResumeReleasesTheRestriction() {
		VisEntityResume.ENTITY.save(this.resume("Analista inativo"));
		VisEntityResume.ENTITY.delete(this.key());
		CcpEntity inactiveResumes = VisEntityResume.ENTITY.getTwinEntity();
		assertTrue(inactiveResumes.exists(this.key()));
		this.saveTheRestriction();

		inactiveResumes.save(this.resume("Analista inativo corrigido"));

		assertFalse(VisEntityResumeRestriction.ENTITY.exists(this.key()));
		assertFalse(VisEntityResume.ENTITY.exists(this.key()));
	}

	/**
	 * The form saves an inactive resume through the main entity ({@code VisBusinessSaveResumeAndSpendTheUpdate}), as the
	 * owner who corrects the field of the inactive resume does: the restriction goes away too.
	 */
	@Test
	public void theFormSaveOfTheInactiveResumeReleasesTheRestriction() {
		VisEntityResume.ENTITY.save(this.resume("Inativo pelo formulario"));
		VisEntityResume.ENTITY.delete(this.key());
		this.saveTheRestriction();
		CcpEntity inactiveResumes = VisEntityResume.ENTITY.getTwinEntity();
		CcpJsonRepresentation storedResume = inactiveResumes.getOneById(this.key());
		CcpJsonRepresentation foundRecords = CcpOtherConstants.EMPTY_JSON.put(inactiveResumes, storedResume);
		CcpJsonRepresentation jsonAfterTheSteps = this.key().put(CcpEntity.JsonFieldNames._entities, foundRecords);
		CcpJsonRepresentation correctedResume = this.resume("Inativo corrigido no form");

		new VisBusinessSaveResumeAndSpendTheUpdate(correctedResume).execute(jsonAfterTheSteps);

		assertFalse(VisEntityResumeRestriction.ENTITY.exists(this.key()));
	}

	/** The insertion of a resume releases nothing: only an update proves the correction. */
	@Test
	public void theInsertionOfTheResumeKeepsTheRestriction() {
		this.saveTheRestriction();

		VisEntityResume.ENTITY.save(this.resume("Analista novo"));

		assertTrue(VisEntityResumeRestriction.ENTITY.exists(this.key()));
	}

	private void saveTheRestriction() {
		CcpJsonRepresentation restriction = this.key().put(VisEntityResumeRestriction.Fields.message, List.of(MESSAGE));
		VisEntityResumeRestriction.ENTITY.save(restriction);
	}

	private CcpJsonRepresentation key() {
		return CcpOtherConstants.EMPTY_JSON.put(VisEntityResume.Fields.email, this.email);
	}

	private CcpJsonRepresentation resume(String desiredJob) {
		return this.key()
				.put(VisEntityResume.Fields.ddd, Arrays.asList(11))
				.put(VisEntityResume.Fields.desiredJob, desiredJob + " " + this.run)
				.put(VisEntityResume.Fields.disponibility, 5)
				.put(VisEntityResume.Fields.experience, 4)
				.put(VisEntityResume.Fields.linkedinAddress, "https://www.linkedin.com/in/restricao-" + this.run)
				.put(VisEntityResume.Fields.resumeType, 2)
				.put(VisEntityResume.Fields.temporallyJobTime, 0)
				.put(VisEntityResume.Fields.pj, 9_000);
	}
}
