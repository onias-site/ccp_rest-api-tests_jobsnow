package com.vis.rest.api.resume.status;

import com.ccp.process.CcpProcessStatus;

public enum SaveResumeStatus  implements CcpProcessStatus{
	naoCadastrouMensageria,
	naoEnviouEmail
	;
	public int asNumber() {
		return 0;
	}
}
