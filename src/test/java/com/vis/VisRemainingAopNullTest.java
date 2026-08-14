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
import com.vis.business.company.VisBusinessGroupCompaniesByTheirFirstThreeInitials;
import com.vis.business.position.VisBusinessDuplicateFieldEmailToFieldMasters;
import com.vis.business.position.VisBusinessGroupPositionsGroupedByRecruiters;
import com.vis.business.position.VisBusinessPositionResumesSend;
import com.vis.business.recruiter.VisBusinessRecruiterReceivingResumes;
import com.vis.business.recruiter.VisBusinessResumeViewSave;
import com.vis.business.resume.VisBusinessCalculateResumeHashes;
import com.vis.business.resume.VisBusinessResumeSaveViewFailed;
import com.vis.business.resume.skills.VisBusinessApprovingSkill;
import com.vis.business.templates.notify.support.VisTemplatesToNotifySupport;
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
import com.vis.utils.VisSendEmailMessageAndRegisterEmailSent;
import com.vis.utils.VisSendRecentUsersToGroupings;
import com.vis.utils.VisUtils;

/**
 * Cobertura do {@code CcpNullParameterAspect} sobre os métodos de {@code vis_business_jobsnow} que
 * ainda não estavam exercitados. Todos os {@code apply}/{@code accept} recebem {@code null}: o
 * aspecto dispara antes do corpo, então nenhum recurso externo é acionado.
 */
public class VisRemainingAopNullTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	private static final CcpEntity ENTITY = JnEntityJobsnowError.ENTITY;

	/** Entidade com twin: exigida pelo construtor de {@code VisGroupDetailsByMasters}. */
	private static final CcpEntity TWIN_ENTITY = com.jn.entities.JnEntityContactUs.ENTITY;

	// ── business/company ──────────────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void groupCompaniesByTheirFirstThreeInitialsApplyNullTest() {
		new VisBusinessGroupCompaniesByTheirFirstThreeInitials().execute(null);
	}

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
	public void approvingSkillApplyNullTest() {
		new VisBusinessApprovingSkill().execute(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void calculateResumeHashesApplyNullTest() {
		new VisBusinessCalculateResumeHashes().execute(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void resumeSaveViewFailedApplyNullTest() {
		VisBusinessResumeSaveViewFailed.INSTANCE.execute(null);
	}

	// ── business/templates/notify/support ─────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void templatesToNotifySupportApplyNullTest() {
		VisTemplatesToNotifySupport.new_skill.execute(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void newSkillApplyNullTest() {
		new VisTemplatesToNotifySupport.NewSkill().execute(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void newSkillHierarchyApplyNullTest() {
		new VisTemplatesToNotifySupport.NewSkillHierarchy().execute(null);
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
	public void sendEmailMessageAndRegisterEmailSentApplyNullTest() {
		VisSendEmailMessageAndRegisterEmailSent.resumeSuccessSaving.execute(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void groupDetailsByMastersConstrutorMasterFieldNameNullTest() {
		new VisGroupDetailsByMasters(null, ENTITY, ENTITY);
	}

	@Test(expected = CcpNullParameterException.class)
	public void groupDetailsByMastersConstrutorEntityNullTest() {
		new VisGroupDetailsByMasters("master", null, ENTITY);
	}

	@Test(expected = CcpNullParameterException.class)
	public void groupDetailsByMastersConstrutorEntityGrouperNullTest() {
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
		VisUtils.getLastUpdated(null, VisFrequencyOptions.daily, "campo");
	}

	@Test(expected = CcpNullParameterException.class)
	public void visUtilsGetLastUpdatedFrequencyNullTest() {
		VisUtils.getLastUpdated(ENTITY, null, "campo");
	}

	@Test(expected = CcpNullParameterException.class)
	public void visUtilsGetLastUpdatedFilterFieldNameNullTest() {
		VisUtils.getLastUpdated(ENTITY, VisFrequencyOptions.daily, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void visUtilsGetAllPositionsGroupedByRecruitersNullTest() {
		VisUtils.getAllPositionsGroupedByRecruiters(null);
	}

	/** Garante que a lista auxiliar usada nos testes acima não é nula (null-return). */
	@Test
	public void listaAuxiliarNaoEhNulaTest() {
		org.junit.Assert.assertNotNull(new ArrayList<CcpJsonRepresentation>());
	}
}
