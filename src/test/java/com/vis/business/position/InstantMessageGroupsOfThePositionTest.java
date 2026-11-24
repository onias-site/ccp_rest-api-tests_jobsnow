package com.vis.business.position;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.json.validations.global.engine.CcpJsonValidationError;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.vis.entities.VisEntityInstantMessageGroup;
import com.vis.services.VisServicePosition;

/**
 * 2026-11-23: the groups of the modal of the position form, read by id from the groups of each combination of the
 * position and from the name and the link of each chat. Runs over the groups loaded by the scripts 113 and 114 (the same
 * of production): area 1 in "Chuva de Vagas", area 2 in it and in "Vagas RH".
 */
public class InstantMessageGroupsOfThePositionTest {

	static {
		CcpDependencyInjection.loadAllDependencies(
				CcpGsonJsonHandler.INSTANCE,
				CcpElasticSearchDbRequest.INSTANCE,
				CcpElasticSearchCrud.INSTANCE,
				CcpApacheMimeHttp.INSTANCE,
				CcpLocalCacheInstances.mock
		);
	}

	private List<String> titles(Object resumeType, List<String> seniority, List<Integer> ddd) {
		CcpJsonRepresentation position = CcpOtherConstants.EMPTY_JSON
				.put(VisBusinessInstantMessageGroupsOfThePosition.Fields.resumeType, resumeType)
				.put(VisBusinessInstantMessageGroupsOfThePosition.Fields.seniority, seniority)
				.put(VisBusinessInstantMessageGroupsOfThePosition.Fields.ddd, ddd);
		CcpJsonRepresentation response = VisServicePosition.GetInstantMessageGroups.execute(position);
		List<CcpJsonRepresentation> groups = response.getAsJsonList(VisBusinessInstantMessageGroupsOfThePosition.Fields.groups);
		for (CcpJsonRepresentation group : groups) {
			assertTrue("every group has its link", group.getAsString(VisEntityInstantMessageGroup.Fields.link).startsWith("https://t.me/"));
		}
		return groups.stream().map(group -> group.getAsString(VisEntityInstantMessageGroup.Fields.title)).collect(Collectors.toList());
	}

	@Test
	public void eachAreaHasItsGroupsOnceAndSortedByName() {
		assertEquals(List.of("Chuva de Vagas T.I + RH / R&S"), this.titles(1, List.of("junior", "senior"), List.of(11, 21, 10)));
		assertEquals(List.of("Chuva de Vagas T.I + RH / R&S", "Vagas RH / Melhorias JobsNow"), this.titles(2, List.of("pleno"), List.of(11)));
		assertEquals(List.of("JN Vagas de nível universitário, técnico ou profissionalizante"), this.titles(3, List.of("estag"), List.of(99)));
		assertEquals(List.of("JN Vagas sem escolaridade ou diploma"), this.titles(4, List.of("unknown", "especialista"), List.of(55, 61)));
	}

	@Test
	public void theThreeFieldsAreRequired() {
		CcpJsonRepresentation noDdd = CcpOtherConstants.EMPTY_JSON
				.put(VisBusinessInstantMessageGroupsOfThePosition.Fields.resumeType, 1)
				.put(VisBusinessInstantMessageGroupsOfThePosition.Fields.seniority, List.of("pleno"));
		try {
			VisServicePosition.GetInstantMessageGroups.execute(noDdd);
			fail("a position with no ddd was accepted");
		} catch (CcpJsonValidationError expected) {
		}
	}
}
