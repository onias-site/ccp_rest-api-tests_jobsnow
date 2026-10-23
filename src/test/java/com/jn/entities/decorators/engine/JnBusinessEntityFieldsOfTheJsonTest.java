package com.jn.entities.decorators.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityMetaData;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.vis.entities.VisEntitySkillSynonym;

/**
 * {@link JnBusinessEntityFieldsOfTheJson} answers, for a record not saved yet, the fields of the entity present in the
 * json it was given, ignoring its own input: what the history pictures for a record that is being created.
 */
public class JnBusinessEntityFieldsOfTheJsonTest {

	static {
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE);
	}

	@Test
	public void onlyTheFieldsOfTheEntityComeAndTheInputIsIgnored() {
		CcpEntityMetaData entityDetails = VisEntitySkillSynonym.ENTITY.getEntityMetaData();
		CcpFieldName notAField = new CcpFieldName("notAField");
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(VisEntitySkillSynonym.Fields.skill, "JAVA SE")
				.put(VisEntitySkillSynonym.Fields.synonym, "JAVA")
				.put(notAField, "x");
		CcpJsonRepresentation input = CcpOtherConstants.EMPTY_JSON.put(VisEntitySkillSynonym.Fields.skill, "OTHER");

		CcpJsonRepresentation record = new JnBusinessEntityFieldsOfTheJson(entityDetails, json).execute(input);

		assertEquals("JAVA SE", record.getAsString(VisEntitySkillSynonym.Fields.skill));
		assertEquals("JAVA", record.getAsString(VisEntitySkillSynonym.Fields.synonym));
		assertFalse(record.containsField(notAField));
	}
}
