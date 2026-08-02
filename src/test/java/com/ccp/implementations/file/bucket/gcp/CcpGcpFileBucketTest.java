package com.ccp.implementations.file.bucket.gcp;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.file.bucket.CcpFileBucket;

public class CcpGcpFileBucketTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpGcpFileBucket());
	}

	private static CcpFileBucket getBucket() {
		return CcpDependencyInjection.getDependency(CcpFileBucket.class);
	}

	// ── provider ──────────────────────────────────────────────────────────────

	@Test
	public void construtorProviderTest() {
		assertNotNull(new CcpGcpFileBucket());
	}

	@Test
	public void getInstanceTest() {
		CcpFileBucket instance = new CcpGcpFileBucket().getInstance();
		assertNotNull(instance);
	}

	// ── null-parameter tests (AOP) — GcpFileBucket via interface ──────────────

	@Test(expected = CcpNullParameterException.class)
	public void getTenantNullTest() {
		getBucket().get(null, "bucket", "file");
	}

	@Test(expected = CcpNullParameterException.class)
	public void getBucketNullTest() {
		getBucket().get("tenant", null, "file");
	}

	@Test(expected = CcpNullParameterException.class)
	public void getFileNameNullTest() {
		getBucket().get("tenant", "bucket", null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void deleteFileTenantNullTest() {
		getBucket().delete(null, "bucket", "file");
	}

	@Test(expected = CcpNullParameterException.class)
	public void deleteFileBucketNullTest() {
		getBucket().delete("tenant", null, "file");
	}

	@Test(expected = CcpNullParameterException.class)
	public void deleteFileFileNameNullTest() {
		getBucket().delete("tenant", "bucket", null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void deleteBucketTenantNullTest() {
		getBucket().delete(null, "bucket");
	}

	@Test(expected = CcpNullParameterException.class)
	public void deleteBucketBucketNullTest() {
		getBucket().delete("tenant", (String) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void saveTenantNullTest() {
		getBucket().save(null, "bucket", "file", "content");
	}

	@Test(expected = CcpNullParameterException.class)
	public void saveBucketNullTest() {
		getBucket().save("tenant", null, "file", "content");
	}

	@Test(expected = CcpNullParameterException.class)
	public void saveFileNameNullTest() {
		getBucket().save("tenant", "bucket", null, "content");
	}

	@Test(expected = CcpNullParameterException.class)
	public void saveFileContentNullTest() {
		getBucket().save("tenant", "bucket", "file", null);
	}
}
