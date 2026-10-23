package com.ccp.especifications.db.crud;

import java.util.List;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.vis.entities.VisEntitySkillSynonym;

/** Action of {@link ForEachIdPresentInEntityTest}: collects, for each run, the key it got and the record found for that key. */
class SynonymKeyAndRecordCollector implements CcpBusiness {

	/** Receives the keys. */
	private final List<String> keysSeen;

	/** Receives the records. */
	private final List<String> recordsSeen;

	SynonymKeyAndRecordCollector(List<String> keysSeen, List<String> recordsSeen) {
		this.keysSeen = keysSeen;
		this.recordsSeen = recordsSeen;
	}

	public CcpJsonRepresentation apply(CcpJsonRepresentation keyWithItsRecord) {
		String synonym = keyWithItsRecord.getAsString(VisEntitySkillSynonym.Fields.skill);
		CcpJsonRepresentation record = keyWithItsRecord.getInnerJsonFromPath(CcpEntity.JsonFieldNames._entities, VisEntitySkillSynonym.ENTITY);
		String skill = record.getAsString(VisEntitySkillSynonym.Fields.synonym);
		this.keysSeen.add(synonym);
		this.recordsSeen.add(skill);
		return keyWithItsRecord;
	}
}
