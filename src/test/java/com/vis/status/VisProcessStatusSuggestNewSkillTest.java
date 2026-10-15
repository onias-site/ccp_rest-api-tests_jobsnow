package com.vis.status;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/** The HTTP status of each answer to the suggestion of a new skill in a position. */
public class VisProcessStatusSuggestNewSkillTest {

	@Test
	public void eachAnswerHasItsStatus() {
		assertEquals(420, VisProcessStatusSuggestNewSkill.rejectedSkill.asNumber());
		assertEquals(200, VisProcessStatusSuggestNewSkill.approvedSkill.asNumber());
		assertEquals(202, VisProcessStatusSuggestNewSkill.pendingSkill.asNumber());
		assertEquals(409, VisProcessStatusSuggestNewSkill.alreadyExists.asNumber());
		assertEquals(4, VisProcessStatusSuggestNewSkill.values().length);
	}
}
