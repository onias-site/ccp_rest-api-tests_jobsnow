package com.jn.business.grouper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.flow.CcpErrorFlowDisturb;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.implementations.password.mindrot.CcpMindrotPasswordHandler;
import com.ccp.json.validations.global.engine.CcpJsonValidationError;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.ccp.process.CcpProcessStatusDefault;
import com.jn.entities.JnEntityGrouper;
import com.jn.utils.JnGroupers;
import com.js.services.JsServiceGrouper;

/**
 * Proves the common read of the groups ({@code JnBusinessGetGroup}): a group by its primary key on the side asked
 * (active by default, or inactive), an empty json when it does not exist there, the validation of the keys and of the
 * side, the readers of {@code JnGroupers} going through it, and the 404 of the service of the endpoint of the js.
 */
public class GetGroupTest {

	static {
		CcpDependencyInjection.removeAllDependencies();
		CcpDependencyInjection.loadAllDependencies(
				CcpLocalInstances.syncMensageriaListener,
				CcpLocalInstances.bucket,
				CcpLocalInstances.email,
				CcpLocalInstances.instantMessenger,
				CcpElasticSearchQueryExecutor.INSTANCE,
				CcpElasticSearchDbRequest.INSTANCE,
				CcpMindrotPasswordHandler.INSTANCE,
				CcpLocalCacheInstances.mock,
				CcpElasticSerchDbBulk.INSTANCE,
				CcpElasticSearchCrud.INSTANCE,
				CcpGsonJsonHandler.INSTANCE,
				CcpApacheMimeHttp.INSTANCE
		);
	}

	private static final String PARENT_NAME = "zz_test_parent";

	private static final String CHILD_NAME = "zz_test_child";

	private final String parentId = "grupo" + System.currentTimeMillis();

	private CcpJsonRepresentation groupKey() {
		return JnGroupers.getGroupKey(PARENT_NAME, CHILD_NAME, this.parentId);
	}

	private CcpJsonRepresentation input(String side) {
		return this.groupKey().put(JnBusinessGetGroup.Fields.side, side);
	}

	private void addChild(boolean twinSide, String childId) {
		CcpJsonRepresentation parentJson = CcpOtherConstants.EMPTY_JSON.put(JnEntityGrouper.Fields.parentId, this.parentId);
		Map<String, CcpJsonRepresentation> parents = Map.of(this.parentId, parentJson);
		JnGroupers.addChild(JnGroupers.getGrouper(twinSide), PARENT_NAME, CHILD_NAME, childId, parents);
	}

	@Before
	public void createTheGroupOnBothSides() {
		this.addChild(false, "filhoAtivo");
		this.addChild(true, "filhoInativo");
	}

	@After
	public void deleteTheGroup() {
		JnEntityGrouper.ENTITY.deleteAnyWhere(this.groupKey());
	}

	@Test
	public void withNoSideTheActiveGroupIsRead() {
		CcpJsonRepresentation group = JnBusinessGetGroup.INSTANCE.execute(this.groupKey());

		assertEquals(Arrays.asList("filhoAtivo"), group.getAsStringList(JnEntityGrouper.Fields.child));
		assertEquals(Integer.valueOf(1), group.getAsIntegerNumber(JnEntityGrouper.Fields.childCount));
		assertEquals(this.parentId, group.getAsString(JnEntityGrouper.Fields.parentId));
	}

	@Test
	public void theInactiveSideReadsTheTwinGroup() {
		CcpJsonRepresentation group = JnBusinessGetGroup.INSTANCE.execute(this.input("twinEntity"));

		assertEquals(Arrays.asList("filhoInativo"), group.getAsStringList(JnEntityGrouper.Fields.child));
	}

	/**
	 * A group only on the active side. It is created apart: deleting the inactive group of the other tests would transfer
	 * it to the active side (the contract of the twin entities).
	 */
	@Test
	public void aGroupOnlyOnTheActiveSideIsEmptyOnTheInactiveOne() {
		String activeOnlyParentId = this.parentId + "soAtivo";
		CcpJsonRepresentation parentJson = CcpOtherConstants.EMPTY_JSON.put(JnEntityGrouper.Fields.parentId, activeOnlyParentId);
		Map<String, CcpJsonRepresentation> parents = Map.of(activeOnlyParentId, parentJson);
		JnGroupers.addChild(JnGroupers.getGrouper(false), PARENT_NAME, CHILD_NAME, "filhoAtivo", parents);
		CcpJsonRepresentation activeOnlyKey = JnGroupers.getGroupKey(PARENT_NAME, CHILD_NAME, activeOnlyParentId);
		try {
			CcpJsonRepresentation inactiveInput = activeOnlyKey.put(JnBusinessGetGroup.Fields.side, "twinEntity");
			CcpJsonRepresentation activeInput = activeOnlyKey.put(JnBusinessGetGroup.Fields.side, "mainEntity");
			CcpJsonRepresentation inactiveGroup = JnBusinessGetGroup.INSTANCE.execute(inactiveInput);
			CcpJsonRepresentation activeGroup = JnBusinessGetGroup.INSTANCE.execute(activeInput);

			assertTrue(inactiveGroup.isEmpty());
			assertEquals(Arrays.asList("filhoAtivo"), activeGroup.getAsStringList(JnEntityGrouper.Fields.child));
		} finally {
			JnEntityGrouper.ENTITY.deleteAnyWhere(activeOnlyKey);
		}
	}

	@Test
	public void anUnknownGroupIsEmpty() {
		CcpJsonRepresentation unknownGroup = JnGroupers.getGroupKey(PARENT_NAME, CHILD_NAME, this.parentId + "inexistente");

		assertTrue(JnBusinessGetGroup.INSTANCE.execute(unknownGroup).isEmpty());
	}

	@Test
	public void theReadersOfJnGroupersGoThroughTheCommonRead() {
		List<String> activeChildren = JnGroupers.getChildren(false, PARENT_NAME, CHILD_NAME, this.parentId);
		List<String> inactiveChildren = JnGroupers.getChildren(true, PARENT_NAME, CHILD_NAME, this.parentId);

		assertEquals(Arrays.asList("filhoAtivo"), activeChildren);
		assertEquals(Arrays.asList("filhoInativo"), inactiveChildren);
	}

	@Test(expected = CcpJsonValidationError.class)
	public void aSideOtherThanActiveAndInactiveIsRefused() {
		JnBusinessGetGroup.INSTANCE.execute(this.input("twin"));
	}

	@Test(expected = CcpJsonValidationError.class)
	public void aGroupWithNoParentIdIsRefused() {
		CcpJsonRepresentation withNoParentId = this.groupKey().removeFields(JnBusinessGetGroup.Fields.parentId);

		JnBusinessGetGroup.INSTANCE.execute(withNoParentId);
	}

	@Test
	public void theServiceOfTheEndpointAnswersTheGroup() {
		CcpJsonRepresentation group = JsServiceGrouper.GetGroup.execute(this.groupKey());

		assertEquals(Arrays.asList("filhoAtivo"), group.getAsStringList(JnEntityGrouper.Fields.child));
	}

	@Test
	public void bothSidesComeInASingleRead() {
		CcpJsonRepresentation groupOnBothSides = JnBusinessGetGroupOnBothSides.INSTANCE.execute(this.groupKey());
		CcpJsonRepresentation activeGroup = groupOnBothSides.getInnerJson(JnEntityGrouper.ENTITY);
		CcpJsonRepresentation inactiveGroup = groupOnBothSides.getInnerJson(JnEntityGrouper.ENTITY.getTwinEntity());

		assertEquals(Arrays.asList("filhoAtivo"), activeGroup.getAsStringList(JnEntityGrouper.Fields.child));
		assertEquals(Arrays.asList("filhoInativo"), inactiveGroup.getAsStringList(JnEntityGrouper.Fields.child));
	}

	@Test
	public void anUnknownGroupIsEmptyOnBothSides() {
		CcpJsonRepresentation unknownGroup = JnGroupers.getGroupKey(PARENT_NAME, CHILD_NAME, this.parentId + "inexistente");
		CcpJsonRepresentation groupOnBothSides = JnBusinessGetGroupOnBothSides.INSTANCE.execute(unknownGroup);

		assertTrue(groupOnBothSides.getInnerJson(JnEntityGrouper.ENTITY).isEmpty());
		assertTrue(groupOnBothSides.getInnerJson(JnEntityGrouper.ENTITY.getTwinEntity()).isEmpty());
	}

	@Test(expected = CcpJsonValidationError.class)
	public void aGroupOnBothSidesWithNoParentIdIsRefused() {
		CcpJsonRepresentation withNoParentId = this.groupKey().removeFields(JnBusinessGetGroupOnBothSides.Fields.parentId);

		JnBusinessGetGroupOnBothSides.INSTANCE.execute(withNoParentId);
	}

	@Test
	public void theServiceOfTheEndpointOnBothSidesAnswers404ForAnUnknownGroup() {
		CcpJsonRepresentation unknownGroup = JnGroupers.getGroupKey(PARENT_NAME, CHILD_NAME, this.parentId + "inexistente");
		try {
			JsServiceGrouper.GetGroupOnBothSides.execute(unknownGroup);
			fail("a group on neither side must answer 404");
		} catch (CcpErrorFlowDisturb e) {
			assertEquals(CcpProcessStatusDefault.NOT_FOUND, e.status);
		}
	}

	@Test
	public void theServiceOfTheEndpointAnswers404ForAnUnknownGroup() {
		CcpJsonRepresentation unknownGroup = JnGroupers.getGroupKey(PARENT_NAME, CHILD_NAME, this.parentId + "inexistente");
		try {
			JsServiceGrouper.GetGroup.execute(unknownGroup);
			fail("an unknown group must answer 404");
		} catch (CcpErrorFlowDisturb e) {
			assertEquals(CcpProcessStatusDefault.NOT_FOUND, e.status);
		}
	}
}
