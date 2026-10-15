package com.vis.json.fields.validation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.vis.entities.VisEntityResume;

/**
 * Proves the rule of the screen in the backend: the home office (ddd 10) is chosen alone or not at all. Until 2026-10-14
 * the backend accepted {@code [10, 11]}.
 */
public class VisJsonValidatorHomeOfficeAloneTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	private boolean hasError(List<Integer> ddds) {
		CcpJsonRepresentation resume = CcpOtherConstants.EMPTY_JSON.put(VisEntityResume.Fields.ddd, ddds);
		boolean hasError = new VisJsonValidatorHomeOfficeAlone().hasError(resume, VisEntityResume.Fields.class);
		return hasError;
	}

	@Test
	public void homeOfficeWithRegionsIsRefused() {
		assertTrue(this.hasError(Arrays.asList(10, 11)));
		assertTrue(this.hasError(Arrays.asList(11, 21, 10)));
	}

	@Test
	public void homeOfficeAloneIsAccepted() {
		assertFalse(this.hasError(Arrays.asList(10)));
	}

	@Test
	public void regionsWithoutHomeOfficeAreAccepted() {
		assertFalse(this.hasError(Arrays.asList(11)));
		assertFalse(this.hasError(Arrays.asList(11, 21, 31)));
	}

	@Test
	public void aResumeWithoutDddIsLeftToTheFieldRules() {
		assertFalse(new VisJsonValidatorHomeOfficeAlone().hasError(CcpOtherConstants.EMPTY_JSON, VisEntityResume.Fields.class));
	}
}
