package com.ccp.implementations.text.extractor.apache.tika;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.text.extractor.CcpTextExtractor;

public class CcpApacheTikaTextExtractorTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpApacheTikaTextExtractor());
	}
 
	private static CcpTextExtractor getExtractor() {
		return CcpDependencyInjection.getDependency(CcpTextExtractor.class);
	}

	// ── provider ──────────────────────────────────────────────────────────────

	@Test
	public void construtorProviderTest() { 
		assertNotNull(new CcpApacheTikaTextExtractor());
	}

	@Test
	public void getInstanceTest() {
		CcpTextExtractor instance = new CcpApacheTikaTextExtractor().getInstance();
		assertNotNull(instance); 
	}

	// ── null-parameter tests (AOP) ────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void extractTextContentNullTest() {
		getExtractor().extractText(null);
	}
}
