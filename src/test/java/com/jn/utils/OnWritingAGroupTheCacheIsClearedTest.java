package com.jn.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.implementations.password.mindrot.CcpMindrotPasswordHandler;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.jn.entities.JnEntityGrouper;

/**
 * {@code JnEntityGrouper} is cached and its writes are atomic scripts run by bulk, which the cache decorator does not see:
 * each write has to clear the key of the group. A read by id of a twin entity goes to the database on both sides, so
 * {@code exists} is what answers from the cache: without the cleanup, a deleted group would go on existing for an hour.
 */
public class OnWritingAGroupTheCacheIsClearedTest {

	static {
		CcpDependencyInjection.removeAllDependencies();
		CcpDependencyInjection.loadAllDependencies(
				CcpLocalInstances.syncMensageriaListener,
				CcpLocalInstances.bucket,
				CcpLocalInstances.email,
				CcpElasticSearchQueryExecutor.INSTANCE,
				CcpElasticSearchDbRequest.INSTANCE,
				CcpMindrotPasswordHandler.INSTANCE,
				CcpLocalCacheInstances.map,
				CcpElasticSerchDbBulk.INSTANCE,
				CcpElasticSearchCrud.INSTANCE,
				CcpGsonJsonHandler.INSTANCE,
				CcpApacheMimeHttp.INSTANCE
		);
	}

	private static final String PARENT_NAME = "test_parent";

	private static final String CHILD_NAME = "test_child";

	private static final String CHILD_ID = "child";

	@Test
	public void aGroupDeletedByItsLastChildNoLongerExists() {
		String parentId = "cache-" + System.currentTimeMillis();
		CcpEntity grouper = JnGroupers.getGrouper(false);
		CcpJsonRepresentation groupKey = JnGroupers.getGroupKey(PARENT_NAME, CHILD_NAME, parentId);
		CcpJsonRepresentation parentJson = CcpOtherConstants.EMPTY_JSON.put(JnEntityGrouper.Fields.parentId, parentId);
		Map<String, CcpJsonRepresentation> parents = Map.of(parentId, parentJson);

		JnGroupers.addChild(grouper, PARENT_NAME, CHILD_NAME, CHILD_ID, parents);
		assertTrue("this check leaves the group in the cache", grouper.exists(groupKey));

		List<String> parentIds = Arrays.asList(parentId);
		JnGroupers.removeChild(grouper, PARENT_NAME, CHILD_NAME, CHILD_ID, parentIds);
		assertFalse(grouper.exists(groupKey));
	}
}
