package com.vis.business.bots.skill.hierarchy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.vis.business.skill.VisSkillFixHierarchyDecisions;

/**
 * Proves how {@link VisSupportSkillFixHierarchyAnswer} reads what the operator typed in the {@code fixSkillHierarchy}
 * command, in Portuguese or English and ignoring case.
 */
public class OperatorAnswerReadingTest {

	{
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE);
	}

	private VisSupportSkillFixHierarchyAnswer read(String typedValue) {
		VisSupportSkillFixHierarchyAnswer answer = VisSupportSkillFixHierarchyAnswer.read(CcpOtherConstants.EMPTY_JSON.put(JnJsonCommonsFields.typedValue, typedValue));
		return answer;
	}

	@Test
	public void oneByOneIsReadInBothLanguages() {
		assertTrue(this.read("um a um").isOneByOne());
		assertTrue(this.read("  One By One ").isOneByOne());
	}

	@Test
	public void ignoreIsReadInBothLanguages() {
		assertTrue(this.read("ignorar").isIgnore());
		assertTrue(this.read("IGNORE").isIgnore());
	}

	@Test
	public void yesAndNoAreReadInBothLanguages() {
		assertTrue(this.read("sim").isYes());
		assertTrue(this.read("yes").isYes());
		assertTrue(this.read("nao").isNo());
		assertTrue(this.read("no").isNo());
	}

	@Test
	public void anApprovalKeepsTheRestOfTheTextAsTheJustification() {
		VisSupportSkillFixHierarchyAnswer answer = this.read("aprovar   it is in the resume");

		assertTrue(answer.isDecisionWithJustification());
		assertEquals("it is in the resume", answer.justification);
		assertEquals(VisSkillFixHierarchyDecisions.approved, answer.getDecision());
	}

	@Test
	public void everyRejectionVerbIsARejection() {
		assertEquals(VisSkillFixHierarchyDecisions.rejected, this.read("rejeitar no evidence").getDecision());
		assertEquals(VisSkillFixHierarchyDecisions.rejected, this.read("reprovar no evidence").getDecision());
		assertEquals(VisSkillFixHierarchyDecisions.rejected, this.read("Reject no evidence").getDecision());
	}

	@Test
	public void aDecisionWithoutJustificationDoesNotCount() {
		assertFalse(this.read("approve").isDecisionWithJustification());
	}

	@Test
	public void anythingElseIsNotUnderstood() {
		VisSupportSkillFixHierarchyAnswer answer = this.read("maybe later");

		assertEquals(VisSupportSkillFixHierarchyAnswerType.notUnderstood, answer.type);
		assertFalse(answer.isDecisionWithJustification());
		assertFalse(answer.isYes());
		assertFalse(answer.isNo());
	}
}
