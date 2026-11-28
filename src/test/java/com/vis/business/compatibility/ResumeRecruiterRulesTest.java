package com.vis.business.compatibility;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.implementations.password.mindrot.CcpMindrotPasswordHandler;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.vis.business.recruiter.resumes.VisRecruiterResumesFields;
import com.vis.entities.VisEntityResume;

/**
 * 2026-11-28: the rules between recruiter and resume live in a single place ({@link VisResumeRecruiterRule}) and every
 * flow asks {@link VisResumeRecruiterCompatibility}. Each rule has a scenario that breaks it alone: a new rule without
 * a scenario fails {@link #everyRuleHasAScenarioThatBreaksItAlone()}, and each scenario must block the sending and
 * follow what the rule says about the screens, the resume alone and the recruiter alone.
 */
public class ResumeRecruiterRulesTest {

	static {
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
				CcpApacheMimeHttp.INSTANCE
		);
	}

	private static final String SEPARATOR = "|";

	private static final String RECRUITER_DOMAIN = "consultoria-regras.com.br";

	private final CcpJsonRepresentation recruiter = VisResumeRecruiterCompatibility.getRecruiter("ana@" + RECRUITER_DOMAIN);

	private final CcpJsonRepresentation storedResume = CcpOtherConstants.EMPTY_JSON
			.put(VisEntityResume.Fields.email, "-1a2b3c4d5e6f")
			.put(VisEntityResume.Fields.linkedinAddress, "https://www.linkedin.com/in/regras-entre-recrutador-e-curriculo")
			.put(VisEntityResume.Fields.notAllowedCompany, List.of("outra-empresa.com.br"));

	private final CcpJsonRepresentation resume = VisResumeRecruiterCompatibility.getResume(this.storedResume);

	private final String recruiterHash = this.recruiter.getAsString(VisRecruiterResumesFields.emailHash);

	private final String resumeHash = this.resume.getAsString(VisRecruiterResumesFields.emailHash);

	private final String linkedinHash = this.resume.getAsString(VisRecruiterResumesFields.linkedinHash);

	private final VisResumeRecruiterFacts noFacts = facts(Set.of(), Set.of(), Set.of());

	private static VisResumeRecruiterFacts facts(Set<String> unsubscribed, Set<String> restricted, Set<String> unwantedPairs) {
		VisResumeRecruiterFacts facts = new VisResumeRecruiterFactsInMemory(unsubscribed, restricted, unwantedPairs, SEPARATOR);
		return facts;
	}

	/** For each rule, the resume and the facts that break it and no other rule (the recruiter is always the same). */
	private Map<VisResumeRecruiterRule, CcpJsonRepresentation> resumes() {
		Map<VisResumeRecruiterRule, CcpJsonRepresentation> resumes = new EnumMap<>(VisResumeRecruiterRule.class);
		resumes.put(VisResumeRecruiterRule.recruiterUnsubscribed, this.resume);
		resumes.put(VisResumeRecruiterRule.resumeRestricted, this.resume);
		resumes.put(VisResumeRecruiterRule.linkedinConfirmedInvalid, this.resume.put(VisEntityResume.Fields.invalidLinkedinAddress, true));
		resumes.put(VisResumeRecruiterRule.companyForbidden, this.resume.put(VisEntityResume.Fields.notAllowedCompany, List.of(RECRUITER_DOMAIN)));
		resumes.put(VisResumeRecruiterRule.resumeUnwantedByTheRecruiter, this.resume);
		return resumes;
	}

	private Map<VisResumeRecruiterRule, VisResumeRecruiterFacts> factsOfTheScenarios() {
		Map<VisResumeRecruiterRule, VisResumeRecruiterFacts> facts = new EnumMap<>(VisResumeRecruiterRule.class);
		facts.put(VisResumeRecruiterRule.recruiterUnsubscribed, facts(Set.of(this.recruiterHash), Set.of(), Set.of()));
		facts.put(VisResumeRecruiterRule.resumeRestricted, facts(Set.of(), Set.of(this.resumeHash), Set.of()));
		facts.put(VisResumeRecruiterRule.linkedinConfirmedInvalid, this.noFacts);
		facts.put(VisResumeRecruiterRule.companyForbidden, this.noFacts);
		facts.put(VisResumeRecruiterRule.resumeUnwantedByTheRecruiter, facts(Set.of(), Set.of(), Set.of(this.recruiterHash + SEPARATOR + this.linkedinHash)));
		return facts;
	}

	@Test
	public void everyRuleHasAScenarioThatBreaksItAlone() {
		Set<VisResumeRecruiterRule> rules = Set.of(VisResumeRecruiterRule.values());
		assertEquals("a new rule needs its scenario here", rules, this.resumes().keySet());
		assertEquals("a new rule needs its scenario here", rules, this.factsOfTheScenarios().keySet());
	}

	@Test
	public void aPairThatBreaksNoRuleIsCompatibleEverywhere() {
		assertTrue(VisResumeRecruiterCompatibility.mayReceive(this.recruiter, this.resume, this.noFacts));
		assertTrue(VisResumeRecruiterCompatibility.mayBeShown(this.recruiter, this.resume, this.noFacts));
		assertFalse(VisResumeRecruiterCompatibility.isTheResumeBlocked(this.resume, this.noFacts));
		assertFalse(VisResumeRecruiterCompatibility.receivesNothing(this.recruiter, this.noFacts));
	}

	@Test
	public void eachRuleAloneBlocksTheSendingAndFollowsItsScope() {
		Map<VisResumeRecruiterRule, CcpJsonRepresentation> resumes = this.resumes();
		Map<VisResumeRecruiterRule, VisResumeRecruiterFacts> facts = this.factsOfTheScenarios();
		for (VisResumeRecruiterRule rule : VisResumeRecruiterRule.values()) {
			CcpJsonRepresentation resumeOfTheScenario = resumes.get(rule);
			VisResumeRecruiterFacts factsOfTheScenario = facts.get(rule);
			for (VisResumeRecruiterRule otherRule : VisResumeRecruiterRule.values()) {
				boolean broken = otherRule.isBroken(this.recruiter, resumeOfTheScenario, factsOfTheScenario);
				assertEquals(rule + " breaks only itself, not " + otherRule, rule == otherRule, broken);
			}
			assertFalse(rule + " blocks the sending", VisResumeRecruiterCompatibility.mayReceive(this.recruiter, resumeOfTheScenario, factsOfTheScenario));
			assertEquals(rule + " on the screens", rule.holdsOnTheScreensOfTheRecruiter(), false == VisResumeRecruiterCompatibility.mayBeShown(this.recruiter, resumeOfTheScenario, factsOfTheScenario));
			assertEquals(rule + " blocks the resume alone", rule.concernsOnlyTheResume(), VisResumeRecruiterCompatibility.isTheResumeBlocked(resumeOfTheScenario, factsOfTheScenario));
			assertEquals(rule + " leaves the recruiter without resumes", rule.concernsOnlyTheRecruiter(), VisResumeRecruiterCompatibility.receivesNothing(this.recruiter, factsOfTheScenario));
		}
	}

	@Test
	public void aPersonalEmailHasNoCompanyToForbid() {
		CcpJsonRepresentation personalRecruiter = VisResumeRecruiterCompatibility.getRecruiter("consultoriaregras@gmail.com");
		CcpJsonRepresentation forbiddingGmail = this.resume.put(VisEntityResume.Fields.notAllowedCompany, List.of("gmail.com"));
		assertFalse(VisResumeRecruiterRule.companyForbidden.isBroken(personalRecruiter, forbiddingGmail, this.noFacts));
	}

	@Test
	public void theOwnerOfAPositionIsJudgedByTheDomainKeptInIt() {
		CcpJsonRepresentation owner = VisResumeRecruiterCompatibility.getRecruiterOfTheDomain(this.recruiterHash, RECRUITER_DOMAIN);
		CcpJsonRepresentation forbidding = this.resume.put(VisEntityResume.Fields.notAllowedCompany, List.of(RECRUITER_DOMAIN));
		assertFalse(VisResumeRecruiterCompatibility.mayReceive(owner, forbidding, this.noFacts));
		CcpJsonRepresentation ownerWithoutDomain = VisResumeRecruiterCompatibility.getRecruiterOfTheDomain(this.recruiterHash, "");
		assertTrue("without the domain there is no company to compare", VisResumeRecruiterCompatibility.mayReceive(ownerWithoutDomain, forbidding, this.noFacts));
	}
}
