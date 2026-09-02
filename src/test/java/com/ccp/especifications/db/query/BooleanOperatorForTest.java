package com.ccp.especifications.db.query;


final class BooleanOperatorForTest extends CcpQueryBooleanOperator {

	BooleanOperatorForTest() {
		super(CcpQueryOptions.INSTANCE, "test");
	}

	@SuppressWarnings("unchecked")
	protected <T extends CcpQueryComponent> T getInstanceCopy() {
		return (T) new BooleanOperatorForTest();
	}
}
