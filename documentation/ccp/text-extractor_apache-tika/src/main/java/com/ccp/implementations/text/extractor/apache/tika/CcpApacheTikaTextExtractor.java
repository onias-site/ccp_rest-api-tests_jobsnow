package com.ccp.implementations.text.extractor.apache.tika;

import com.ccp.dependency.injection.CcpInstanceProvider;
import com.ccp.especifications.text.extractor.CcpTextExtractor;

/**
 * Provedor de DI que expõe {@code ApacheTikaTextExtractor} como implementação de {@code CcpTextExtractor}.
 */ 
public class CcpApacheTikaTextExtractor implements CcpInstanceProvider<CcpTextExtractor> {

	public CcpTextExtractor getInstance() { 
		ApacheTikaTextExtractor textExtractor = new ApacheTikaTextExtractor();
		return textExtractor;
	}
 
}
