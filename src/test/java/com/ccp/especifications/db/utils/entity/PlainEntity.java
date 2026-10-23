package com.ccp.especifications.db.utils.entity;

import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityMetaData;
import com.jn.entities.JnEntityJobsnowError;

final class PlainEntity implements CcpEntity {
	public final static PlainEntity INSTANCE = new PlainEntity();

	private PlainEntity() {}

	public CcpEntityMetaData getEntityMetaData() {
		return JnEntityJobsnowError.ENTITY.getEntityMetaData();
	}
}
