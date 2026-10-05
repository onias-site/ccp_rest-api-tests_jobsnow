package com.vis.services;

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
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
				new CcpElasticSearchQueryExecutor(),
				new CcpElasticSearchDbRequest(),
				CcpLocalCacheInstances.mock,
				new CcpElasticSerchDbBulk(),
				new CcpElasticSearchCrud(),
				new CcpGsonJsonHandler(),
				new CcpApacheMimeHttp());
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

	@Test(expected = StringIndexOutOfBoundsException.class)
	public void aOneLetterWordCurrentlyBreaks() {
		VisEntityGroupPositionsBySkills.getWordStatus("J");
	}

	@Ignore("finding 46: a word shorter than two letters has no group and should be 1, not an exception")
	@Test
	public void aOneLetterWordShouldHaveNoGroup() {
		assertEquals(1, VisEntityGroupPositionsBySkills.getWordStatus("J"));
	}
}
