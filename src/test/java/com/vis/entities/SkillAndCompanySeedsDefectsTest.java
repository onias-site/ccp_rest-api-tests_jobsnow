package com.vis.entities;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.dependency.injection.CcpInstanceProvider;
import com.ccp.especifications.db.query.CcpQueryExecutor;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

/**
 * Proves the corrections of finding 46 (2026-10-22) in the seeds of the skills and of the companies: the resume count
 * reads the synonyms as the {@code {skill}} records they are, and the seed of the recruiters (which, since 2026-10-27, also
 * makes the groups of companies, until then an entity of their own whose seed kept its groups from one call to the next)
 * does not hide a failure of the legacy index.
 */
public class SkillAndCompanySeedsDefectsTest {

	static {
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE);
	}

	/** Gives the other test classes back the real query executor. */
	@After
	public void restoreTheRealExecutor() {
		CcpDependencyInjection.loadAllDependencies(CcpElasticSearchQueryExecutor.INSTANCE);
	}

	@Test
	public void theResumeCountAddsTheCountsOfTheSynonyms() throws Exception {
		CcpJsonRepresentation skill = new CcpJsonRepresentation("{\"skill\":\"JAVA\",\"synonym\":[{\"skill\":\"JAVA SE\"},{\"skill\":\"J2SE\"}]}");
		List<String> lines = Arrays.asList("JAVA = 10", "JAVA SE = 5", "J2SE = 2", "PYTHON = 7");
		Method getResumesCount = VisEntitySkill.class.getDeclaredMethod("getResumesCount", CcpJsonRepresentation.class, List.class);
		getResumesCount.setAccessible(true);

		int resumesCount = (int) getResumesCount.invoke(VisEntitySkill.INSTANCE, skill, lines);

		assertEquals(17, resumesCount);
	}

	@Test
	public void aFailureOfTheLegacyIndexReachesTheCaller() {
		CcpQueryExecutor executor = (CcpQueryExecutor) Proxy.newProxyInstance(this.getClass().getClassLoader(),
				new Class<?>[] { CcpQueryExecutor.class }, LegacyIndexFailureHandler.INSTANCE);
		CcpInstanceProvider<CcpQueryExecutor> provider = () -> executor;
		CcpDependencyInjection.loadAllDependencies(provider);

		try {
			VisEntityRecruiter.INSTANCE.getFirstRecordsToInsert();
		} catch (IllegalStateException e) {
			return;
		}
		throw new AssertionError("the failure of the legacy index was swallowed and the seed came empty");
	}
}
