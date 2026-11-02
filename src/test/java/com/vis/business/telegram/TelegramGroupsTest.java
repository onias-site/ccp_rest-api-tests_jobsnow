package com.vis.business.telegram;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityCache;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.jn.entities.JnEntitySystemMessage;
import com.vis.entities.VisEntityStatisSkillPieChart;
import com.vis.services.VisServiceSkillStatistics;

/** The links of the Telegram groups are a record of {@code jn_system_message}, seeded by the vis and read by id. */
public class TelegramGroupsTest {

	static {
		CcpDependencyInjection.loadAllDependencies(
				CcpLocalInstances.syncMensageriaListener,
				CcpElasticSearchQueryExecutor.INSTANCE,
				CcpElasticSearchDbRequest.INSTANCE,
				CcpLocalCacheInstances.mock,
				CcpElasticSerchDbBulk.INSTANCE,
				CcpElasticSearchCrud.INSTANCE,
				CcpGsonJsonHandler.INSTANCE,
				CcpApacheMimeHttp.INSTANCE);
	}

	@Test
	public void theEntityOfTheMessagesIsCacheable() {
		assertTrue(JnEntitySystemMessage.class.isAnnotationPresent(CcpEntityCache.class));
	}

	@Test
	public void theLinksAreSeededByTheEntityOfTheChart() {
		List<CcpBulkItem> seeds = VisEntityStatisSkillPieChart.INSTANCE.getFirstRecordsToInsert();

		String expectedId = JnEntitySystemMessage.ENTITY.calculateId(VisTelegramSystemMessages.getGroupsRecord());
		assertEquals(1, seeds.size());
		assertEquals(expectedId, seeds.get(0).id);
	}

	@Test
	public void theServiceListsEveryLinkOfTheRecord() {
		JnEntitySystemMessage.ENTITY.save(VisTelegramSystemMessages.getGroupsRecord());

		var response = VisServiceSkillStatistics.GetTelegramGroups.execute(CcpOtherConstants.EMPTY_JSON.content);

		CcpJsonRepresentation json = new CcpJsonRepresentation(response);
		List<String> links = json.getAsStringList(VisBusinessTelegramGroups.Fields.groups);
		assertEquals(17, links.size());
		assertEquals("https://t.me/empregossp", links.get(0));
		assertEquals("https://t.me/phpbrasilvagas", links.get(16));
	}
}
