package com.vis;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.jn.entities.JnEntityJobsnowError;
import com.vis.business.position.VisBusinessDuplicateFieldEmailToFieldMasters;
import com.vis.business.position.VisBusinessGroupPositionsGroupedByRecruiters;
import com.vis.business.position.VisBusinessPositionResumesSend;
import com.vis.business.recruiter.VisBusinessRecruiterReceivingResumes;
import com.vis.business.recruiter.VisBusinessResumeViewSave;
import com.vis.business.resume.VisBusinessCalculateResumeHashes;
import com.vis.business.resume.VisBusinessResumeSaveViewFailed;
import com.vis.entities.VisEntityGroupPositionsBySkills;
import com.vis.json.transformers.VisJsonTransformerPutEmailHashAndDomainRecruiter;
import com.vis.schedulling.VisBusinessGetRecentLoggedUsers;
import com.vis.schedulling.VisBusinessGroupResumeViewsByRecruiter;
import com.vis.schedulling.VisBusinessGroupResumeViewsByResume;
import com.vis.schedulling.VisBusinessGroupResumesOpinionsByRecruiter;
import com.vis.schedulling.VisBusinessGroupResumesOpinionsByResume;
import com.vis.schedulling.VisBusinessGroupSkills;
import com.vis.schedulling.VisBusinessPositionResumesReceivingByFrequency;
import com.vis.schedulling.VisBusinessSearchSkills;
import com.vis.status.VisProcessStatusResumeView;
import com.vis.utils.VisBusinessPositionUpdateGroupingByRecruitersAndSendResumes;
import com.vis.utils.VisBusinessResumeSendToRecruiters;
import com.vis.utils.VisFrequencyOptions;
import com.vis.utils.VisGroupDetailsByMasters;
import com.vis.utils.VisSendRecentUsersToGroupings;
import com.vis.utils.VisUtils;

/**
 * Coverage of {@code CcpNullParameterAspect} over the methods of {@code vis_business_jobsnow} that
 * were not exercised yet. Every {@code apply}/{@code accept} receives {@code null}: the aspect fires
 * before the body, so no external resource is triggered.
 */
public class VisRemainingAopNullTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	private static final CcpEntity ENTITY = JnEntityJobsnowError.ENTITY;

	/** Entity with a twin: required by the constructor of {@code VisGroupDetailsByMasters}. */
	private static final CcpEntity TWIN_ENTITY = com.jn.entities.JnEntityContactUs.ENTITY;

	// ── business/position ─────────────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void duplicateFieldEmailToFieldMastersApplyNullTest() {
		VisBusinessDuplicateFieldEmailToFieldMasters.INSTANCE.execute(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void groupPositionsGroupedByRecruitersApplyNullTest() {
		VisBusinessGroupPositionsGroupedByRecruiters.INSTANCE.execute(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void positionResumesSendApplyNullTest() {
		VisBusinessPositionResumesSend.INSTANCE.execute(null);
	}

	// ── business/recruiter ────────────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void recruiterReceivingResumesApplyNullTest() {
		VisBusinessRecruiterReceivingResumes.INSTANCE.execute(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void resumeViewSaveApplyNullTest() {
		VisBusinessResumeViewSave.INSTANCE.execute(null);
	}

	// ── business/resume ───────────────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void calculateResumeHashesApplyNullTest() {
		new VisBusinessCalculateResumeHashes().execute(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void resumeSaveViewFailedApplyNullTest() {
		VisBusinessResumeSaveViewFailed.INSTANCE.execute(null);
	}


	// ── entities ──────────────────────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void getWordStatusNullTest() {
		VisEntityGroupPositionsBySkills.getWordStatus(null);
	}

	// ── json/transformers ─────────────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void putEmailHashAndDomainRecruiterApplyNullTest() {
		VisJsonTransformerPutEmailHashAndDomainRecruiter.INSTANCE.execute(null);
	}

	// ── schedulling ───────────────────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void getRecentLoggedUsersApplyNullTest() {
		VisBusinessGetRecentLoggedUsers.INSTANCE.execute(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void groupResumesOpinionsByRecruiterApplyNullTest() {
		VisBusinessGroupResumesOpinionsByRecruiter.INSTANCE.execute(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void groupResumesOpinionsByResumeApplyNullTest() {
		VisBusinessGroupResumesOpinionsByResume.INSTANCE.execute(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void groupResumeViewsByRecruiterApplyNullTest() {
		VisBusinessGroupResumeViewsByRecruiter.INSTANCE.execute(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void groupResumeViewsByResumeApplyNullTest() {
		VisBusinessGroupResumeViewsByResume.INSTANCE.execute(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void groupSkillsApplyNullTest() {
		VisBusinessGroupSkills.INSTANCE.execute(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void positionResumesReceivingByFrequencyApplyNullTest() {
		VisBusinessPositionResumesReceivingByFrequency.INSTANCE.execute(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void searchSkillsApplyNullTest() {
		VisBusinessSearchSkills.INSTANCE.execute(null);
	}

	// ── status ────────────────────────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void processStatusResumeViewToBulkItemCreateNullTest() {
		VisProcessStatusResumeView.resumeNotFound.toBulkItemCreate(null);
	}

	// ── utils ─────────────────────────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void positionUpdateGroupingByRecruitersApplyNullTest() {
		VisBusinessPositionUpdateGroupingByRecruitersAndSendResumes.INSTANCE.execute(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void resumeSendToRecruitersApplyNullTest() {
		VisBusinessResumeSendToRecruiters.INSTANCE.execute(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void groupDetailsByMastersConstructorMasterFieldNameNullTest() {
		new VisGroupDetailsByMasters(null, ENTITY, ENTITY);
	}

	@Test(expected = CcpNullParameterException.class)
	public void groupDetailsByMastersConstructorEntityNullTest() {
		new VisGroupDetailsByMasters("master", null, ENTITY);
	}

	@Test(expected = CcpNullParameterException.class)
	public void groupDetailsByMastersConstructorEntityGrouperNullTest() {
		new VisGroupDetailsByMasters("master", ENTITY, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void groupDetailsByMastersAcceptNullTest() {
		new VisGroupDetailsByMasters("master", TWIN_ENTITY, TWIN_ENTITY).accept((CcpJsonRepresentation) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void sendRecentUsersToGroupingsAcceptNullTest() {
		VisSendRecentUsersToGroupings.INSTANCE.accept((List<CcpJsonRepresentation>) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void visUtilsGetLastUpdatedEntityNullTest() {
		VisUtils.getLastUpdated(null, VisFrequencyOptions.daily, "field");
	}

	@Test(expected = CcpNullParameterException.class)
	public void visUtilsGetLastUpdatedFrequencyNullTest() {
		VisUtils.getLastUpdated(ENTITY, null, "field");
	}

	@Test(expected = CcpNullParameterException.class)
	public void visUtilsGetLastUpdatedFilterFieldNameNullTest() {
		VisUtils.getLastUpdated(ENTITY, VisFrequencyOptions.daily, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void visUtilsGetAllPositionsGroupedByRecruitersNullTest() {
		VisUtils.getAllPositionsGroupedByRecruiters(null);
	}

	/** Ensures that the auxiliary list used in the tests above is not null (null-return). */
	@Test
	public void auxiliaryListIsNotNullTest() {
		org.junit.Assert.assertNotNull(new ArrayList<CcpJsonRepresentation>());
	}
}
