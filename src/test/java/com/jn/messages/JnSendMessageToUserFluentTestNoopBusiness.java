package com.jn.messages;

import com.jn.business.http.JnBusinessSendHttpRequest;
import com.jn.business.messages.JnMessageSenderExceptionHandler;

class JnSendMessageToUserFluentTestNoopBusiness extends JnBusinessSendHttpRequest {
	JnSendMessageToUserFluentTestNoopBusiness() {
		super(json -> json, JnMessageSenderExceptionHandler.THROWS);
	}
}
