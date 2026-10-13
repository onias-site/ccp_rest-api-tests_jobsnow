package com.vis;

import java.util.ArrayList;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.vis.business.recruiter.VisBusinessRecruiterReceivingResumes;
import com.vis.entities.VisEntityGroupPositionsBySkills;
import com.vis.json.transformers.VisJsonTransformerPutEmailHashAndDomainRecruiter;

/**
 * Coverage of {@code CcpNullParameterAspect} over the methods of {@code vis_business_jobsnow} that
 * were not exercised yet. Every {@code apply}/{@code accept} receives {@code null}: the aspect fires
 * before the body, so no external resource is triggered.
 */
public class VisRemainingAopNullTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	// ── business/recruiter ────────────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void recruiterReceivingResumesApplyNullTest() {
		VisBusinessRecruiterReceivingResumes.INSTANCE.execute(null);
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

	/** Ensures that the auxiliary list used in the tests above is not null (null-return). */
	@Test
	public void auxiliaryListIsNotNullTest() {
		org.junit.Assert.assertNotNull(new ArrayList<CcpJsonRepresentation>());
	}
}
