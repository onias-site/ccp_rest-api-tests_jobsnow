package com.vis.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import com.ccp.decorators.CcpTimeDecorator;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.ccp.decorators.CcpFieldName;


import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.hash.CcpHashAlgorithm;
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
import com.jn.entities.JnEntityGrouper;
import com.jn.utils.JnGroupers;
import com.vis.json.fields.validation.VisJsonCommonsFields;

/**
 * Proves the resumes viewed by the recruiters ({@code vis_viewed_resume}): the LinkedIn and the e-mail are stored only
 * as SHA-1 hashes, and the alias is required, with 5 to 200 characters.
 */
public class ViewedResumeTest {

	static {
		CcpDependencyInjection.removeAllDependencies();
		CcpDependencyInjection.loadAllDependencies(
				CcpLocalInstances.syncMensageriaListener,
				CcpLocalInstances.bucket,
				CcpLocalInstances.email,
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

	private final long now = System.currentTimeMillis();

	private final String linkedin = "https://www.linkedin.com/in/candidato-" + this.now;

	private final String email = "recrutador" + this.now + "@empresa.com.br";

	private CcpJsonRepresentation viewedResume(String alias) {
		return CcpOtherConstants.EMPTY_JSON
				.put(VisEntityViewedResume.Fields.linkedin, this.linkedin)
				.put(VisEntityViewedResume.Fields.email, this.email)
				.put(VisEntityViewedResume.Fields.alias, alias);
	}

	private static String sha1(String text) {
		return new CcpStringDecorator(text).hash().asString(CcpHashAlgorithm.SHA1);
	}

	@Test
	public void theLinkedinAndTheEmailAreStoredOnlyAsHashes() {
		CcpJsonRepresentation viewedResume = this.viewedResume("Dev Java sênior");
		VisEntityViewedResume.ENTITY.save(viewedResume);
		try {
			CcpJsonRepresentation stored = VisEntityViewedResume.ENTITY.getOneById(viewedResume);
			assertEquals(sha1(this.linkedin), stored.getAsString(VisEntityViewedResume.Fields.linkedin));
			assertEquals(sha1(this.email), stored.getAsString(VisEntityViewedResume.Fields.email));
			assertEquals("Dev Java sênior", stored.getAsString(VisEntityViewedResume.Fields.alias));
			String storedText = stored.toString();
			assertFalse(storedText.contains(this.linkedin));
			assertFalse(storedText.contains(this.email));
		} finally {
			VisEntityViewedResume.ENTITY.deleteAnyWhere(viewedResume);
		}
		assertFalse(VisEntityViewedResume.ENTITY.exists(viewedResume));
	}

	@Test
	public void theRecruiterCanRenameTheResume() {
		VisEntityViewedResume.ENTITY.save(this.viewedResume("Primeiro apelido"));
		VisEntityViewedResume.ENTITY.save(this.viewedResume("Segundo apelido"));
		CcpJsonRepresentation viewedResume = this.viewedResume("Segundo apelido");
		try {
			CcpJsonRepresentation stored = VisEntityViewedResume.ENTITY.getOneById(viewedResume);
			assertEquals("Segundo apelido", stored.getAsString(VisEntityViewedResume.Fields.alias));
			assertTrue(VisEntityViewedResume.ENTITY.exists(viewedResume));
		} finally {
			VisEntityViewedResume.ENTITY.deleteAnyWhere(viewedResume);
		}
	}

	@Test(expected = CcpJsonValidationError.class)
	public void anAliasShorterThanFiveCharactersIsRefused() {
		VisEntityViewedResume.ENTITY.save(this.viewedResume("Dev"));
	}

	@Test(expected = CcpJsonValidationError.class)
	public void anAliasLongerThanTwoHundredCharactersIsRefused() {
		VisEntityViewedResume.ENTITY.save(this.viewedResume("x".repeat(201)));
	}

	@Test(expected = CcpJsonValidationError.class)
	public void aViewedResumeWithoutAliasIsRefused() {
		CcpJsonRepresentation withoutAlias = this.viewedResume("Qualquer").removeFields(VisEntityViewedResume.Fields.alias);
		VisEntityViewedResume.ENTITY.save(withoutAlias);
	}

	@Test(expected = CcpJsonValidationError.class)
	public void anAddressThatIsNotALinkedinProfileIsRefused() {
		CcpJsonRepresentation notLinkedin = this.viewedResume("Qualquer").put(VisEntityViewedResume.Fields.linkedin, "https://example.com/perfil");
		VisEntityViewedResume.ENTITY.save(notLinkedin);
	}

	/** The children of this view in the group of views of a ddd: the JSON texts that carry its id. */
	private List<CcpJsonRepresentation> viewsInTheDddGroup(int ddd, String viewId) {
		String parentName = VisEntityVirtualViewedResumeDddGrouper.ENTITY.getEntityMetaData().entityName;
		String childName = VisEntityViewedResume.ENTITY.getEntityMetaData().entityName;
		CcpJsonRepresentation combination = CcpOtherConstants.EMPTY_JSON.put(VisEntityVirtualViewedResumeDddGrouper.Fields.ddd, ddd);
		String parentId = VisEntityVirtualViewedResumeDddGrouper.ENTITY.calculateId(combination);
		CcpJsonRepresentation group = JnGroupers.getGroup(false, parentName, childName, parentId);
		if (group.isEmpty()) {
			return Arrays.asList();
		}
		return group.getAsStringList(JnEntityGrouper.Fields.child).stream()
				.map(CcpJsonRepresentation::new)
				.filter(child -> viewId.equals(child.getAsString(VisJsonCommonsFields.id)))
				.collect(Collectors.toList());
	}

	private CcpJsonRepresentation viewWithTheResume(String alias, List<Integer> ddds, int experienceStartYear) {
		CcpJsonRepresentation english = new CcpJsonRepresentation("{\"name\":\"English\",\"level\":2}");
		return this.viewedResume(alias)
				.put(VisEntityResume.Fields.ddd, ddds)
				.put(VisEntityResume.Fields.experience, experienceStartYear)
				.put(VisEntityResume.Fields.spokenLanguage, Arrays.asList(english))
				.put(VisEntityResume.Fields.pj, 9_000)
				.put(VisEntityResume.Fields.disponibility, 5)
				.put(VisEntityResume.Fields.resumeType, 1)
				.put(VisEntityResume.Fields.temporallyJobTime, 0)
				.put(VisEntityResume.Fields.skill, Arrays.asList(
						new CcpJsonRepresentation("{\"skill\":\"JAVA\",\"word\":\"JAVA\",\"type\":\"skillsFromResume\",\"category\":\"skillsFromText\"}"),
						new CcpJsonRepresentation("{\"skill\":\"PYTHON\",\"word\":\"PYTHON\",\"type\":\"excludedSkills\",\"category\":\"excludedSkills\"}")));
	}

	/**
	 * 2026-11-12: every save of a view keeps it in the group of views of each ddd of the viewed resume (a parentName of its
	 * own, not the one of the groups of the resumes), as a JSON text with the fields of the resume handed over and the id
	 * of the view; only the ddds are stored in the view. A save again moves it to the groups of the new ddds, the delete
	 * takes it out.
	 */
	@Test
	public void theViewIsAChildOfTheGroupOfViewsOfEachDdd() {
		assertFalse(VisEntityVirtualViewedResumeDddGrouper.ENTITY.getEntityMetaData().entityName.equals(VisEntityVirtualDddGrouper.ENTITY.getEntityMetaData().entityName));
		VisEntityViewedResume.ENTITY.save(this.viewWithTheResume("Primeiro apelido", Arrays.asList(11, 21), 2018));
		CcpJsonRepresentation stored = VisEntityViewedResume.ENTITY.getOneById(this.viewedResume("Primeiro apelido"));
		String viewId = VisEntityViewedResume.ENTITY.calculateId(stored);
		assertEquals(2, stored.getAsObjectList(VisEntityViewedResume.Fields.ddd).size());
		assertFalse(stored.toString(), stored.containsField(VisEntityResume.Fields.experience));
		for (int ddd : Arrays.asList(11, 21)) {
			List<CcpJsonRepresentation> children = this.viewsInTheDddGroup(ddd, viewId);
			assertEquals("ddd " + ddd, 1, children.size());
			CcpJsonRepresentation child = children.get(0);
			List<String> expectedFields = Arrays.asList("btc", "clt", "disponibility", "experience", "spokenLanguage", "pcd", "pj", "resumeType", "temporallyJobTime", "travel", "skill", "id");
			assertTrue(child.toString(), expectedFields.containsAll(child.fieldSet()));
			assertEquals(2018, child.getAsIntegerNumber(VisEntityResume.Fields.experience).intValue());
			assertEquals("English", child.getAsJsonList(VisEntityResume.Fields.spokenLanguage).get(0).getAsString(new com.ccp.decorators.CcpFieldName("name")));
			assertEquals(Arrays.asList("JAVA"), child.getAsStringList(VisEntityResume.Fields.skill));
			assertFalse("the skills are not stored in the view", stored.containsField(VisEntityResume.Fields.skill));
		}

		VisEntityViewedResume.ENTITY.save(this.viewWithTheResume("Segundo apelido", Arrays.asList(11, 31), 2017));
		assertTrue(this.viewsInTheDddGroup(21, viewId).isEmpty());
		assertEquals(2017, this.viewsInTheDddGroup(11, viewId).get(0).getAsIntegerNumber(VisEntityResume.Fields.experience).intValue());
		assertEquals(1, this.viewsInTheDddGroup(11, viewId).size());
		assertEquals(1, this.viewsInTheDddGroup(31, viewId).size());

		VisEntityViewedResume.ENTITY.delete(this.viewedResume("Segundo apelido"));
		assertTrue(this.viewsInTheDddGroup(11, viewId).isEmpty());
		assertTrue(this.viewsInTheDddGroup(31, viewId).isEmpty());
	}
	private static int insertedThisMonth(String childName) {
		String parentName = VisEntityVirtualInsertionMonthCounter.ENTITY.getEntityMetaData().entityName;
		String parentId = VisEntityVirtualInsertionMonthCounter.getParentId();
		CcpJsonRepresentation counters = JnGroupers.getCounters(parentName, childName, parentId);
		String yearMonth = new CcpTimeDecorator().getFormattedDateTime("yyyyMM");
		CcpFieldName key = new CcpFieldName(yearMonth);
		return counters.containsAllFields(key) ? counters.getAsIntegerNumber(key) : 0;
	}

	@Test
	public void theViewsAreCountedByTheMonthOfTheirInsertion() {
		String childName = VisEntityViewedResume.ENTITY.getEntityMetaData().entityName;
		VisEntityViewedResume.ENTITY.deleteAnyWhere(this.viewedResume("Apelido do contador"));
		int before = insertedThisMonth(childName);

		VisEntityViewedResume.ENTITY.save(this.viewedResume("Apelido do contador"));
		assertEquals("the insertion adds 1", before + 1, insertedThisMonth(childName));
		String insertionDate = VisEntityViewedResume.ENTITY.getOneById(this.viewedResume("Apelido do contador")).getAsString(JnJsonCommonsFields.insertionDate);

		VisEntityViewedResume.ENTITY.save(this.viewedResume("Outro apelido do contador").put(JnJsonCommonsFields.insertionDate, "01/01/2016 10:00:00"));
		assertEquals("an update counts nothing", before + 1, insertedThisMonth(childName));
		assertEquals(insertionDate, VisEntityViewedResume.ENTITY.getOneById(this.viewedResume("Apelido do contador")).getAsString(JnJsonCommonsFields.insertionDate));

		VisEntityViewedResume.ENTITY.delete(this.viewedResume("Apelido do contador"));
		assertEquals("the delete subtracts 1", before, insertedThisMonth(childName));
	}
}
