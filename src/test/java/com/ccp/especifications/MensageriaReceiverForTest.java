package com.ccp.especifications;

import java.util.function.Consumer;

import com.ccp.especifications.db.bulk.CcpExecuteBulkOperation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
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

	@Override
	protected CcpEntity getTwinEntity(CcpEntity entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected CcpEntity getCustomEntity(Object newInstance) {
		// TODO Auto-generated method stub
		return null;
	}
}
