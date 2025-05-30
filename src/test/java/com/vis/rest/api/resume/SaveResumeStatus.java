package com.vis.rest.api.resume;

import com.ccp.process.CcpProcessStatus;

public enum SaveResumeStatus  implements CcpProcessStatus{
	naoCadastrouMensageria,
	naoEnviouEmail
	;
	public int asNumber() {
		return 0;
	}
}
