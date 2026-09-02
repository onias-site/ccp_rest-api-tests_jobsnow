package com.ccp.especifications;

import java.util.function.Consumer;

import com.ccp.especifications.db.bulk.CcpExecuteBulkOperation;
import com.ccp.especifications.mensageria.receiver.CcpMensageriaReceiver;

final class MensageriaReceiverForTest extends CcpMensageriaReceiver {

	MensageriaReceiverForTest() {
		super("operation");
	}

	public CcpExecuteBulkOperation getExecuteBulkOperation() {
		return com.jn.db.bulk.JnExecuteBulkOperation.INSTANCE;
	}

	public Consumer<String[]> getFunctionToDeleteKeysInTheCache() {
		return com.jn.utils.JnDeleteKeysFromCache.INSTANCE;
	}
}
