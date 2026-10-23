package com.ccp.especifications.db.utils.entity.decorators.engine;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.jn.entities.JnEntityJobsnowError;
import com.vis.entities.VisEntitySkillSynonym;

/** {@link CcpEntityMainNameProducer} names the entity by its class: the cost center prefix, then the name after {@code Entity}, in snake case. */
public class CcpEntityMainNameProducerTest {

	@Test
	public void theNameKeepsTheCostCenterAndDropsTheWordEntity() {
		assertEquals("jn_jobsnow_error", CcpEntityMainNameProducer.INSTANCE.apply(JnEntityJobsnowError.class));
		assertEquals("vis_skill_synonym", CcpEntityMainNameProducer.INSTANCE.apply(VisEntitySkillSynonym.class));
	}
}
