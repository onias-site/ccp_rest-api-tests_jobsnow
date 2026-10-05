package com.vis.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpTimeDecorator;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.vis.entities.VisEntityPosition;
import com.vis.json.fields.validation.VisJsonCommonsFields;
import com.vis.json.fields.validation.VisSeniorityTypes;

/**
 * Proves the value generators used by the compatibility hashes of the matching: money values, availabilities,
 * disability flags and seniority, each one with its resume and position variants.
 */
public class MatchingValueGeneratorsTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	@Test
	public void resumeMoneyValuesGoFromTheDeclaredValueUpTo100000InStepsOf100() {
		CcpJsonRepresentation resume = CcpOtherConstants.EMPTY_JSON.put(VisJsonCommonsFields.clt, 99_700);

		List<CcpJsonRepresentation> values = GetMoneyValuesFromJson.resume.apply(resume, VisJsonCommonsFields.clt.name());

		assertEquals(4, values.size());
		assertEquals(99_700, (int) values.get(0).getAsIntegerNumber(GetMoneyValuesFromJson.JsonFieldNames.moneyValue));
		assertEquals(100_000, (int) values.get(3).getAsIntegerNumber(GetMoneyValuesFromJson.JsonFieldNames.moneyValue));
		assertEquals("clt", values.get(0).getAsString(GetMoneyValuesFromJson.JsonFieldNames.moneyType));
	}

	@Test
	public void positionMoneyValuesGoFromTheDeclaredMaximumDownTo1000InStepsOf100() {
		CcpJsonRepresentation position = CcpOtherConstants.EMPTY_JSON.put(VisJsonCommonsFields.pj, 1_250);

		List<CcpJsonRepresentation> values = GetMoneyValuesFromJson.position.apply(position, VisJsonCommonsFields.pj.name());

		assertEquals(3, values.size());
		assertEquals(1_250, (int) values.get(0).getAsIntegerNumber(GetMoneyValuesFromJson.JsonFieldNames.moneyValue));
		assertEquals(1_050, (int) values.get(2).getAsIntegerNumber(GetMoneyValuesFromJson.JsonFieldNames.moneyValue));
	}

	@Test
	public void anAbsentMoneyFieldGeneratesNoValue() {
		assertTrue(GetMoneyValuesFromJson.resume.apply(CcpOtherConstants.EMPTY_JSON, "btc").isEmpty());
		assertTrue(GetMoneyValuesFromJson.position.apply(CcpOtherConstants.EMPTY_JSON, "btc").isEmpty());
	}

	@Test
	public void resumeAvailabilitiesGoFromTheDeclaredOneUpTo70Days() {
		CcpJsonRepresentation resume = CcpOtherConstants.EMPTY_JSON.put(VisJsonCommonsFields.disponibility, 68);

		List<Integer> availabilities = VisFunctionsGetDisponibilityValuesFromJson.resume.apply(resume);

		assertEquals(Arrays.asList(68, 69, 70), availabilities);
	}

	@Test
	public void positionAvailabilitiesGoFromTheDeclaredMaximumDownToZero() {
		CcpJsonRepresentation position = CcpOtherConstants.EMPTY_JSON.put(VisJsonCommonsFields.disponibility, 2);

		List<Integer> availabilities = VisFunctionsGetDisponibilityValuesFromJson.position.apply(position);

		assertEquals(Arrays.asList(2, 1, 0), availabilities);
	}

	@Test
	public void aCandidateWithADisabilityGetsBothFlagsAndTheOthersOnlyFalse() {
		CcpJsonRepresentation withDisability = CcpOtherConstants.EMPTY_JSON.put(VisEntityPosition.Fields.pcd, true);
		CcpJsonRepresentation withoutDisability = CcpOtherConstants.EMPTY_JSON.put(VisEntityPosition.Fields.pcd, false);

		assertEquals(Arrays.asList(true, false), VisFunctionsGetPcdValuesFromJson.resume.apply(withDisability));
		assertEquals(Arrays.asList(false), VisFunctionsGetPcdValuesFromJson.resume.apply(withoutDisability));
	}

	@Test
	public void aPositionForPeopleWithADisabilityGetsOnlyTrueAndTheOthersBothFlags() {
		CcpJsonRepresentation forDisability = CcpOtherConstants.EMPTY_JSON.put(VisEntityPosition.Fields.pcd, true);
		CcpJsonRepresentation regular = CcpOtherConstants.EMPTY_JSON.put(VisEntityPosition.Fields.pcd, false);

		assertEquals(Arrays.asList(true), VisFunctionsGetPcdValuesFromJson.position.apply(forDisability));
		assertEquals(Arrays.asList(true, false), VisFunctionsGetPcdValuesFromJson.position.apply(regular));
	}

	@Test
	public void resumeSeniorityComesFromTheYearsOfExperience() {
		int currentYear = new CcpTimeDecorator().getYear();

		assertEquals(VisSeniorityTypes.ES.name(), this.seniorityOfAResumeStartedIn(currentYear - 11));
		assertEquals(VisSeniorityTypes.SR.name(), this.seniorityOfAResumeStartedIn(currentYear - 10));
		assertEquals(VisSeniorityTypes.SR.name(), this.seniorityOfAResumeStartedIn(currentYear - 6));
		assertEquals(VisSeniorityTypes.PL.name(), this.seniorityOfAResumeStartedIn(currentYear - 5));
		assertEquals(VisSeniorityTypes.PL.name(), this.seniorityOfAResumeStartedIn(currentYear - 3));
		assertEquals(VisSeniorityTypes.JR.name(), this.seniorityOfAResumeStartedIn(currentYear - 2));
		assertEquals(VisSeniorityTypes.JR.name(), this.seniorityOfAResumeStartedIn(currentYear));
	}

	@Test
	public void positionSeniorityIsReadFromTheField() {
		CcpJsonRepresentation position = CcpOtherConstants.EMPTY_JSON.put(VisJsonCommonsFields.seniority, VisSeniorityTypes.PL.name());

		assertEquals("PL", VisFunctionsGetSeniorityValueFromJson.position.apply(position));
	}

	private String seniorityOfAResumeStartedIn(int year) {
		CcpJsonRepresentation resume = CcpOtherConstants.EMPTY_JSON.put(VisJsonCommonsFields.experience, year);
		String seniority = VisFunctionsGetSeniorityValueFromJson.resume.apply(resume);
		return seniority;
	}
}
