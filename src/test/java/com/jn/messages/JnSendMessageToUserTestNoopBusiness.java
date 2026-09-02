package com.jn.messages;

import com.jn.business.http.JnBusinessSendHttpRequest;
import com.jn.business.messages.JnMessageSenderExceptionHandler;

class JnSendMessageToUserTestNoopBusiness extends JnBusinessSendHttpRequest {
	JnSendMessageToUserTestNoopBusiness() {
		super(json -> json, JnMessageSenderExceptionHandler.THROWS);
	}
}
