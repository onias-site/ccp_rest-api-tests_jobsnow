package com.vis.services;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.vis.entities.VisEntityGroupPositionsBySkills;

/**
 * Proves {@link VisEntityGroupPositionsBySkills#getWordStatus(String)} against the skill groups seeded in the local
 * Elasticsearch: 0 for a known word, 1 when there is no group for its initials, 2 when the group lacks the word.
 */
public class WordStatusTest {

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
	public void aKnownWordIsZeroInAnyCase() {
		assertEquals(0, VisEntityGroupPositionsBySkills.getWordStatus("java"));
		assertEquals(0, VisEntityGroupPositionsBySkills.getWordStatus("JAVA"));
	}

	@Test
	public void wordsWithoutAGroupForTheirInitialsAreOne() {
		assertEquals(1, VisEntityGroupPositionsBySkills.getWordStatus("QZXYW"));
	}

	@Test
	public void aGroupWithoutTheWordIsTwo() {
		assertEquals(2, VisEntityGroupPositionsBySkills.getWordStatus("JAQZXYW"));
	}

	/** Finding 46, corrected in 2026-10-22: until then a one letter word broke with StringIndexOutOfBoundsException. */
	@Test
	public void aWordShorterThanTwoLettersHasNoGroup() {
		assertEquals(1, VisEntityGroupPositionsBySkills.getWordStatus("J"));
		assertEquals(1, VisEntityGroupPositionsBySkills.getWordStatus(""));
	}
}
