package com.ccp.random;


import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;

class GroupCandidatesByRecruiters implements java.util.function.Consumer<CcpJsonRepresentation> {

	CcpJsonRepresentation candidatesGroupedByRecruiters = CcpOtherConstants.EMPTY_JSON;

	public void accept(CcpJsonRepresentation json) {
		String candidato = json.getAsObject(CcpRandomScripts.JsonFieldNames.candidate, CcpRandomScripts.JsonFieldNames.candidato);
		String recrutador = json.getAsString(CcpRandomScripts.JsonFieldNames.email);
		this.candidatesGroupedByRecruiters = this.candidatesGroupedByRecruiters.addToList(new CcpFieldName(recrutador),
				candidato);
	}
}
