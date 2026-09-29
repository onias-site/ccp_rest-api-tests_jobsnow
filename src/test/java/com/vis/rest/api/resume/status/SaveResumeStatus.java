package com.vis.rest.api.resume.status;

import com.ccp.process.CcpProcessStatus;

public enum SaveResumeStatus  implements CcpProcessStatus{
	didNotRegisterMessaging,
	didNotSendEmail
	;
	public int asNumber() {
		return 0;
	}
}
