package com.vis.utils;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

public class VisUtilsTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	// ── null-parameter tests (AOP) ────────────────────────────────────────────

	@Test(expected = CcpNullParameterException.class)
	public void isInsufficientFundsFeeNullTest() {
		VisUtils.isInsufficientFunds(1, null, CcpOtherConstants.EMPTY_JSON);
	}

	@Test(expected = CcpNullParameterException.class)
	public void isInsufficientFundsBalanceNullTest() {
		VisUtils.isInsufficientFunds(1, CcpOtherConstants.EMPTY_JSON, null); 
	}

	@Test(expected = CcpNullParameterException.class)
	public void sendFilteredFrequencyNullTest() {
		VisUtils.sendFilteredAndSortedResumesAndTheirStatisByEachPositionToEachRecruiter((VisFrequencyOptions) null, j -> null, f -> null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void sendFilteredHowToObtainResumesNullTest() {
		VisUtils.sendFilteredAndSortedResumesAndTheirStatisByEachPositionToEachRecruiter(VisFrequencyOptions.daily, null, f -> null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void sendFilteredHowToObtainPositionsNullTest() {
		VisUtils.sendFilteredAndSortedResumesAndTheirStatisByEachPositionToEachRecruiter(VisFrequencyOptions.daily, j -> null, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void groupPositionsNullTest() {
		VisUtils.groupPositionsGroupedByRecruiters(null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void groupDetailsByMastersJsonNullTest() {
		VisUtils.groupDetailsByMasters(null, null, null, null, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void saveRecordsInPagesRecordsNullTest() {
		VisUtils.saveRecordsInPages(null, CcpOtherConstants.EMPTY_JSON, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void saveRecordsInPagesSupplierNullTest() {
		VisUtils.saveRecordsInPages(new java.util.ArrayList<CcpJsonRepresentation>(), null, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getRecordsInPagesRecordsNullTest() {
		VisUtils.getRecordsInPages(null, CcpOtherConstants.EMPTY_JSON, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void getRecordsInPagesSupplierNullTest() {
		VisUtils.getRecordsInPages(new java.util.ArrayList<CcpJsonRepresentation>(), null, null);
	}
}
