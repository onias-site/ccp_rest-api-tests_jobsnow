package com.ccp.especifications.db.utils.entity.decorators.engine;

final class DefaultDelegatorForTest extends CcpDefaultEntityDelegator<Object> {

	DefaultDelegatorForTest() {
		super(CcpEntityEngineAopNullTest.ENTITY, CcpEntityEngineAopNullTest.bulkOperation(), CcpEntityEngineAopNullTest.keysToDelete());
	}
}
