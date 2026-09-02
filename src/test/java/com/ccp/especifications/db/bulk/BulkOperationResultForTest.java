package com.ccp.especifications.db.bulk;

import com.ccp.decorators.CcpJsonRepresentation;

final class BulkOperationResultForTest implements CcpBulkOperationResult {

	public CcpJsonRepresentation getErrorDetails() {
		return CcpBulkAopNullTest.JSON;
	}

	public CcpBulkItem getBulkItem() {
		return CcpBulkAopNullTest.bulkItem();
	}

	public boolean hasError() {
		return false;
	}

	public int status() {
		return 200;
	}
}
