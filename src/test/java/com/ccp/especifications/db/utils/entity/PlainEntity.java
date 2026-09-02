package com.ccp.especifications.db.utils.entity;

import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityMetaData;
import com.jn.entities.JnEntityJobsnowError;

final class PlainEntity implements CcpEntity {

	public CcpEntityMetaData getEntityMetaData() {
		return JnEntityJobsnowError.ENTITY.getEntityMetaData();
	}
}
