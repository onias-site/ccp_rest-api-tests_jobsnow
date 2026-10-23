package com.vis.entities;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.dependency.injection.CcpInstanceProvider;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.query.CcpQueryExecutor;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

/**
 * Proves the corrections of finding 46 (2026-10-22) in the seeds of the skills and of the companies: the resume count
 * reads the synonyms as the {@code {skill}} records they are, the parents of a word are found up to the top, and the
 * groups of companies neither leak from one call to the next nor hide a failure of the legacy index.
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
	public void theParentsOfAWordGoUpToTheTop() throws Exception {
		List<CcpJsonRepresentation> synonyms = Arrays.asList(
				new CcpJsonRepresentation("{\"skill\":\"SPRING BOOT\",\"synonym\":[],\"parent\":[\"SPRING\"]}"),
				new CcpJsonRepresentation("{\"skill\":\"SPRING\",\"synonym\":[{\"skill\":\"SPRING FRAMEWORK\"}],\"parent\":[\"JAVA\"]}"),
				new CcpJsonRepresentation("{\"skill\":\"JAVA\",\"synonym\":[],\"parent\":[\"JVM\"]}"),
				new CcpJsonRepresentation("{\"skill\":\"JVM\",\"synonym\":[],\"parent\":[\"JAVA\"]}"));
		Set<String> allParents = new HashSet<>(Arrays.asList("SPRING"));

		Set<String> found = this.getAllParents(synonyms, "SPRING", allParents);

		assertEquals(new TreeSet<>(Arrays.asList("SPRING", "JAVA", "JVM")), new TreeSet<>(found));
	}

	@Test
	public void theParentsOfASynonymAreTheParentsOfItsSkill() throws Exception {
		List<CcpJsonRepresentation> synonyms = Arrays.asList(
				new CcpJsonRepresentation("{\"skill\":\"SPRING\",\"synonym\":[{\"skill\":\"SPRING FRAMEWORK\"}],\"parent\":[\"JAVA\"]}"),
				new CcpJsonRepresentation("{\"skill\":\"JAVA\",\"synonym\":[],\"parent\":[\"JVM\"]}"));

		Set<String> found = this.getAllParents(synonyms, "SPRING FRAMEWORK", new HashSet<>());

		assertEquals(new TreeSet<>(Arrays.asList("JAVA", "JVM")), new TreeSet<>(found));
	}

	@SuppressWarnings("unchecked")
	private Set<String> getAllParents(List<CcpJsonRepresentation> synonyms, String word, Set<String> allParents) throws Exception {
		Method getAllParents = VisEntityGroupPositionsBySkills.class.getDeclaredMethod("getAllParents", List.class, String.class, Set.class);
		getAllParents.setAccessible(true);
		Set<String> found = (Set<String>) getAllParents.invoke(VisEntityGroupPositionsBySkills.INSTANCE, synonyms, word, allParents);
		return found;
	}

	@Test
	public void theCompaniesOfOneSeedDoNotLeakIntoTheNext() {
		this.legacyRecruitersAre("ana@jobsnow.com", "bia@jobsbrasil.com.br", "caio@gmail.com");
		List<CcpBulkItem> firstSeed = VisEntityGroupCompaniesByTheirFirstThreeInitials.INSTANCE.getFirstRecordsToInsert();
		assertEquals(Arrays.asList("GMA=[Gmail]", "JOB=[Jobsnow, Jobsbrasil]"), this.groupsOf(firstSeed));

		this.legacyRecruitersAre("davi@jobsnow.com");
		List<CcpBulkItem> secondSeed = VisEntityGroupCompaniesByTheirFirstThreeInitials.INSTANCE.getFirstRecordsToInsert();

		assertEquals(Arrays.asList("JOB=[Jobsnow]"), this.groupsOf(secondSeed));
	}

	@Test
	public void aFailureOfTheLegacyIndexReachesTheCaller() {
		CcpQueryExecutor executor = (CcpQueryExecutor) Proxy.newProxyInstance(this.getClass().getClassLoader(),
				new Class<?>[] { CcpQueryExecutor.class }, LegacyIndexFailureHandler.INSTANCE);
		CcpInstanceProvider<CcpQueryExecutor> provider = () -> executor;
		CcpDependencyInjection.loadAllDependencies(provider);

		try {
			VisEntityGroupCompaniesByTheirFirstThreeInitials.INSTANCE.getFirstRecordsToInsert();
		} catch (IllegalStateException e) {
			return;
		}
		throw new AssertionError("the failure of the legacy index was swallowed and the seed came empty");
	}

	/** Makes the query executor hand these ids (the e-mails of the legacy recruiters) to the consumer. */
	private void legacyRecruitersAre(String... emails) {
		CcpQueryExecutor executor = (CcpQueryExecutor) Proxy.newProxyInstance(this.getClass().getClassLoader(),
				new Class<?>[] { CcpQueryExecutor.class }, new LegacyRecruitersQueryHandler(emails));
		CcpInstanceProvider<CcpQueryExecutor> provider = () -> executor;
		CcpDependencyInjection.loadAllDependencies(provider);
	}

	private List<String> groupsOf(List<CcpBulkItem> seed) {
		List<String> groups = seed.stream()
				.map(item -> item.json.getAsString(VisEntityGroupCompaniesByTheirFirstThreeInitials.Fields.firstThreeInitials)
						+ "=" + item.json.getAsStringList(VisEntityGroupCompaniesByTheirFirstThreeInitials.Fields.companies))
				.sorted()
				.collect(Collectors.toList());
		return groups;
	}
}
