package com.ccp.especifications.db.bulk;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.especifications.db.utils.entity.CcpEntity;

public class CcpErrorBulkEntityRecordNotFoundTest {

	@Test
	public void ctorStringStringTest() {
		assertNotNull(new CcpErrorBulkEntityRecordNotFound("ent", "id"));
	}

	// ── null-parameter tests (AOP) ────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void ctorStringEntityNameNullTest() {
		new CcpErrorBulkEntityRecordNotFound((String) null, "id");
	}

	@Test(expected = CcpNullParameterException.class)
	public void ctorStringIdNullTest() {
		new CcpErrorBulkEntityRecordNotFound("ent", (String) null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void ctorEntityNullTest() {
		new CcpErrorBulkEntityRecordNotFound((CcpEntity) null, CcpOtherConstants.EMPTY_JSON);
	}

	@Test(expected = CcpNullParameterException.class)
	public void ctorEntityJsonNullTest() {
		new CcpErrorBulkEntityRecordNotFound((CcpEntity) null, null);
	}
}
