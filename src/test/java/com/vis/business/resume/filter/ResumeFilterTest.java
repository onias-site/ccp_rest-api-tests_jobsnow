package com.vis.business.resume.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.cache.CcpCacheDecorator;
import com.ccp.dependency.injection.CcpInstanceProvider;
import com.ccp.especifications.db.crud.CcpCrud;
import com.ccp.especifications.db.utils.CcpDbRequester;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityMetaData;
import com.ccp.especifications.db.utils.entity.decorators.engine.SilentEmailSender;
import com.ccp.especifications.db.utils.entity.decorators.engine.SilentInstantMessenger;
import com.ccp.especifications.email.CcpEmailProvider;
import com.ccp.especifications.instant.messenger.CcpInstantMessenger;
import com.ccp.flow.CcpErrorFlowDisturb;
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
import com.ccp.process.CcpProcessStatusDefault;
import com.jn.db.bulk.JnExecuteBulkOperation;
import com.jn.entities.JnEntityDisposableRecord;
import com.jn.entities.JnEntityGrouper;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.utils.JnDeleteKeysFromCache;
import com.vis.business.position.VisBusinessResumesOfTheFilters;
import com.vis.entities.VisEntityResume;
import com.vis.entities.VisEntityResumeFilter;
import com.vis.entities.VisEntityResumeFilterDescription;
import com.vis.json.fields.validation.VisJsonFieldsResumeFilter;
import com.vis.services.VisServiceResumeFilter;

/**
 * Proves the search of resumes by filters of the screen "Currículos - Buscar" and of the position form (since
 * 2026-11-25): the filters, as the fields of the position, are saved in {@code vis_resume_filter} and their insertion
 * counts the resumes with the search of the position form, caching the count for 1 hour under the id of the filters; the
 * same filters asked again (in any order) have the same id and come from the cache, with no second search; the id reads
 * the filters back; only the area is required.
 */
public class ResumeFilterTest {

	static {
		CcpInstanceProvider<CcpInstantMessenger> telegram = () -> SilentInstantMessenger.INSTANCE;
		CcpInstanceProvider<CcpEmailProvider> email = () -> SilentEmailSender.INSTANCE;

		CcpDependencyInjection.removeAllDependencies();
		CcpDependencyInjection.loadAllDependencies(
				CcpLocalInstances.syncMensageriaListener,
				CcpLocalInstances.bucket,
				CcpElasticSearchQueryExecutor.INSTANCE,
				CcpElasticSearchDbRequest.INSTANCE,
				CcpMindrotPasswordHandler.INSTANCE,
				// a cache that keeps the values: the count of the filters is read from it
				CcpLocalCacheInstances.map,
				CcpElasticSerchDbBulk.INSTANCE,
				CcpElasticSearchCrud.INSTANCE,
				CcpGsonJsonHandler.INSTANCE,
				CcpApacheMimeHttp.INSTANCE,
				telegram,
				email
		);
		JnExecuteBulkOperation.INSTANCE.executeBulk(VisEntityResume.INSTANCE.getFirstRecordsToInsert(), JnDeleteKeysFromCache.INSTANCE);
	}

	private final long now = System.currentTimeMillis();

	private final List<CcpJsonRepresentation> savedResumes = new ArrayList<>();

	/** A resume of information technology in ddds 11 and 21, pleno, CLT 99,500, with the skill PHP. */
	private CcpJsonRepresentation resume(String name) {
		CcpJsonRepresentation skill = new CcpJsonRepresentation("{\"skill\":\"PHP\",\"word\":\"PHP\",\"type\":\"skillsFromResume\",\"category\":\"skillsFromText\"}");
		return CcpOtherConstants.EMPTY_JSON
				.put(VisEntityResume.Fields.email, name + this.now + "@jobsnow.com")
				.put(VisEntityResume.Fields.ddd, Arrays.asList(11, 21))
				.put(VisEntityResume.Fields.desiredJob, "developer")
				.put(VisEntityResume.Fields.disponibility, 10)
				.put(VisEntityResume.Fields.experience, 4)
				.put(VisEntityResume.Fields.linkedinAddress, "https://www.linkedin.com/in/" + name + "-" + this.now)
				.put(VisEntityResume.Fields.clt, 99500)
				.put(VisEntityResume.Fields.resumeType, 1)
				.put(VisEntityResume.Fields.temporallyJobTime, 6)
				.put(VisEntityResume.Fields.negotiableClaim, false)
				.put(VisEntityResume.Fields.pcd, false)
				.put(VisEntityResume.Fields.travel, false)
				.put(VisEntityResume.Fields.skill, Arrays.asList(skill));
	}

	/** Saves the resume and answers the id the count returns (the SHA-1 of the e-mail, as stored). */
	private String save(CcpJsonRepresentation resume) {
		VisEntityResume.ENTITY.save(resume);
		this.savedResumes.add(resume);
		CcpEntityMetaData grouperDetails = JnEntityGrouper.ENTITY.getEntityMetaData();
		CcpDbRequester requester = CcpDependencyInjection.getDependency(CcpDbRequester.class);
		requester.refresh(grouperDetails.entityName);
		CcpJsonRepresentation stored = VisEntityResume.ENTITY.getOneById(resume);
		String emailHash = stored.getAsString(VisEntityResume.Fields.email);
		return emailHash;
	}

	@After
	public void deleteTheResumes() {
		for (CcpJsonRepresentation resume : this.savedResumes) {
			VisEntityResume.ENTITY.deleteAnyWhere(resume);
		}
	}

	/**
	 * Filters of information technology in ddd 11, with a domain of this run only: it filters nothing out, but makes
	 * the filters (and so their id and their cache) new on every run.
	 */
	private CcpJsonRepresentation filters() {
		return CcpOtherConstants.EMPTY_JSON
				.put(VisJsonFieldsResumeFilter.resumeType, 1)
				.put(VisJsonFieldsResumeFilter.ddd, Arrays.asList(11))
				.put(VisJsonFieldsResumeFilter.recruiterDomain, "empresa" + this.now + ".com.br");
	}

	private static CcpJsonRepresentation search(CcpJsonRepresentation filters) {
		// como numa requisição HTTP: os números chegam do texto JSON
		CcpJsonRepresentation parsed = new CcpJsonRepresentation(filters.asUgglyJson());
		CcpJsonRepresentation response = VisServiceResumeFilter.SearchResumes.execute(parsed);
		return response;
	}

	private static List<String> idsOf(CcpJsonRepresentation response) {
		List<String> ids = response.getAsStringList(VisBusinessResumesOfTheFilters.Fields.ids);
		return ids;
	}

	private static CcpJsonRepresentation filtersOfTheId(String filterId) {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(VisBusinessGetTheResumeFilter.Fields.id, filterId);
		CcpJsonRepresentation filters = VisServiceResumeFilter.GetResumeFilter.execute(json);
		return filters;
	}

	@Test
	public void theFiltersAreSavedAndTheirInsertionCachesTheCount() {
		String resumeId = this.save(this.resume("resumeFilterSaved"));
		CcpJsonRepresentation filters = this.filters();

		CcpJsonRepresentation response = search(filters);
		assertTrue("the resume of the filters is counted", idsOf(response).contains(resumeId));
		assertEquals("the count is the size of the ids", idsOf(response).size(), response.getAsIntegerNumber(VisBusinessResumesOfTheFilters.Fields.resumes).intValue());

		String filterId = response.getAsString(JnJsonCommonsFields.id);
		// the id is the SHA-1 of the filters with their keys in alphabetical order, with no day
		CcpJsonRepresentation parsedFilters = new CcpJsonRepresentation(filters.asUgglyJson());
		Map<String, Object> inAlphabeticalOrder = new LinkedHashMap<>();
		inAlphabeticalOrder.put(VisJsonFieldsResumeFilter.ddd.name(), Arrays.asList(11));
		inAlphabeticalOrder.put(VisJsonFieldsResumeFilter.recruiterDomain.name(), filters.getAsString(VisJsonFieldsResumeFilter.recruiterDomain));
		inAlphabeticalOrder.put(VisJsonFieldsResumeFilter.resumeType.name(), 1);
		// through the text, as the transformer does: the numbers of one type
		String inAlphabeticalOrderText = new CcpJsonRepresentation(inAlphabeticalOrder).asUgglyJson();
		String sortedFiltersText = new CcpJsonRepresentation(inAlphabeticalOrderText).asUgglyJson();
		String expectedId = new CcpStringDecorator(sortedFiltersText).hash().asString(CcpHashAlgorithm.SHA1);
		assertEquals("the id is the SHA-1 of the sorted filters", expectedId, filterId);
		CcpJsonRepresentation record = CcpOtherConstants.EMPTY_JSON.put(VisEntityResumeFilter.Fields.json, parsedFilters);
		CcpJsonRepresentation handledRecord = VisEntityResumeFilter.ENTITY.getJsonWithHandledPrimaryKey(record);
		assertEquals("the id is the one of vis_resume_filter", filterId, handledRecord.getAsString(VisEntityResumeFilter.Fields.id));
		CcpJsonRepresentation idOfTheFilters = CcpOtherConstants.EMPTY_JSON.put(VisEntityResumeFilter.Fields.id, filterId);
		assertTrue("the filters were saved under the id", VisEntityResumeFilter.ENTITY.exists(idOfTheFilters));

		CcpJsonRepresentation cachedCount = VisBusinessCountTheResumesOfTheFilter.getCache(filterId).getOrDefault(CcpOtherConstants.EMPTY_JSON);
		assertTrue("the count is cached under the id", idsOf(cachedCount).contains(resumeId));
	}

	@Test
	public void theInsertionCountsWhenTheCacheLostTheCount() {
		String resumeId = this.save(this.resume("resumeFilterInsertion"));
		CcpJsonRepresentation filters = this.filters();
		String filterId = search(filters).getAsString(JnJsonCommonsFields.id);
		VisBusinessCountTheResumesOfTheFilter.getCache(filterId).delete();

		// the insertion, in the consumer of the queue of the entity, finds no count in the cache
		CcpJsonRepresentation parsedFilters = new CcpJsonRepresentation(filters.asUgglyJson());
		CcpJsonRepresentation record = CcpOtherConstants.EMPTY_JSON.put(VisEntityResumeFilter.Fields.json, parsedFilters);
		VisBusinessCacheTheResumesOfTheFilter.INSTANCE.execute(record);
		CcpJsonRepresentation cachedCount = VisBusinessCountTheResumesOfTheFilter.getCache(filterId).getOrDefault(CcpOtherConstants.EMPTY_JSON);
		assertTrue("the insertion counted and cached again", idsOf(cachedCount).contains(resumeId));
	}

	@Test
	public void theSameFiltersInAnotherOrderComeFromTheCache() {
		String firstResume = this.save(this.resume("resumeFilterCachedA"));
		CcpJsonRepresentation filters = this.filters();
		CcpJsonRepresentation first = search(filters);
		assertTrue(idsOf(first).contains(firstResume));

		// a resume saved after the count is not seen by the same filters in the hour: they are answered by the cache
		String secondResume = this.save(this.resume("resumeFilterCachedB"));
		Map<String, Object> inAnotherOrder = new LinkedHashMap<>();
		inAnotherOrder.put(VisJsonFieldsResumeFilter.recruiterDomain.name(), filters.getAsString(VisJsonFieldsResumeFilter.recruiterDomain));
		inAnotherOrder.put(VisJsonFieldsResumeFilter.ddd.name(), Arrays.asList(11));
		inAnotherOrder.put(VisJsonFieldsResumeFilter.resumeType.name(), 1);
		CcpJsonRepresentation second = search(new CcpJsonRepresentation(inAnotherOrder));
		assertEquals("the same filters in another order have the same id", first.getAsString(JnJsonCommonsFields.id), second.getAsString(JnJsonCommonsFields.id));
		assertEquals("the same count, from the cache", idsOf(first), idsOf(second));
		assertFalse("no second search", idsOf(second).contains(secondResume));

		// another filter is another id and another search
		CcpJsonRepresentation withSeniority = search(filters.put(VisJsonFieldsResumeFilter.seniority, List.of("pleno")));
		assertNotEquals("another filter, another id", first.getAsString(JnJsonCommonsFields.id), withSeniority.getAsString(JnJsonCommonsFields.id));
		assertTrue("the new search sees the resume saved later", idsOf(withSeniority).contains(secondResume));
	}

	@Test
	public void theSameListsInAnotherOrderAreOneRecord() {
		CcpJsonRepresentation java = new CcpJsonRepresentation("{\"skill\":\"JAVA\",\"word\":\"JAVA\",\"type\":\"skillsFromResume\",\"category\":\"skillsFromText\"}");
		// the keys of a skill in another order as well
		CcpJsonRepresentation sql = new CcpJsonRepresentation("{\"category\":\"skillsFromText\",\"type\":\"skillsFromResume\",\"word\":\"SQL\",\"skill\":\"SQL\"}");
		CcpJsonRepresentation javaAndSql = this.filters()
				.put(VisJsonFieldsResumeFilter.skill, Arrays.asList(java, sql))
				.put(VisJsonFieldsResumeFilter.ddd, Arrays.asList(11, 21))
				.put(VisJsonFieldsResumeFilter.seniority, List.of("pleno", "senior"));
		CcpJsonRepresentation sqlAndJava = this.filters()
				.put(VisJsonFieldsResumeFilter.skill, Arrays.asList(sql, java))
				.put(VisJsonFieldsResumeFilter.ddd, Arrays.asList(21, 11))
				.put(VisJsonFieldsResumeFilter.seniority, List.of("senior", "pleno"));

		String firstId = search(javaAndSql).getAsString(JnJsonCommonsFields.id);
		String secondId = search(sqlAndJava).getAsString(JnJsonCommonsFields.id);
		assertEquals("[JAVA, SQL] and [SQL, JAVA] are the same filters: one id, one record", firstId, secondId);

		CcpJsonRepresentation onlyJava = this.filters().put(VisJsonFieldsResumeFilter.skill, Arrays.asList(java));
		assertNotEquals("other skills, another record", firstId, search(onlyJava).getAsString(JnJsonCommonsFields.id));
	}

	/**
	 * The link of a search made at 23:59 opens until 23:59 of the next day: on the next day the document of the day of the
	 * search is gone (here, deleted by hand), and the read finds the copy in jn_disposable_record, valid for 24 hours.
	 */
	@Test
	public void onTheNextDayTheIdReadsTheFiltersFromTheCopy() {
		CcpJsonRepresentation filters = this.filters().put(VisJsonFieldsResumeFilter.seniority, List.of("junior"));
		String filterId = search(filters).getAsString(JnJsonCommonsFields.id);

		CcpJsonRepresentation idOfTheFilters = CcpOtherConstants.EMPTY_JSON.put(VisEntityResumeFilter.Fields.id, filterId);
		String idOfTheDocumentOfTheDay = VisEntityResumeFilter.ENTITY.calculateId(idOfTheFilters);
		assertNotEquals("the document of the day has another id, with the day", filterId, idOfTheDocumentOfTheDay);
		CcpCrud crud = CcpDependencyInjection.getDependency(CcpCrud.class);
		CcpEntityMetaData filterDetails = VisEntityResumeFilter.ENTITY.getEntityMetaData();
		assertTrue("the document of the day was saved", crud.delete(filterDetails.entityName, idOfTheDocumentOfTheDay));
		// the next day the cache of the record (1 hour) is gone too
		new CcpCacheDecorator(VisEntityResumeFilter.ENTITY, idOfTheDocumentOfTheDay).delete();

		CcpJsonRepresentation readBack = filtersOfTheId(filterId);
		assertEquals("the filters, from the copy", List.of("junior"), readBack.getAsStringList(VisJsonFieldsResumeFilter.seniority));
		assertEquals("the domain, from the copy", filters.getAsString(VisJsonFieldsResumeFilter.recruiterDomain), readBack.getAsString(VisJsonFieldsResumeFilter.recruiterDomain));

		// with the copy gone as well (expired and purged), the link opens nothing
		String keyOfTheCopy = idOfTheFilters.asUgglyJson();
		CcpJsonRepresentation copy = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonCommonsFields.entity, filterDetails.entityName)
				.put(JnJsonCommonsFields.id, keyOfTheCopy);
		CcpJsonRepresentation handledCopy = JnEntityDisposableRecord.ENTITY.getJsonWithHandledPrimaryKey(copy);
		String idOfTheCopy = JnEntityDisposableRecord.ENTITY.calculateId(handledCopy);
		CcpEntityMetaData copyDetails = JnEntityDisposableRecord.ENTITY.getEntityMetaData();
		assertTrue("the copy was saved", crud.delete(copyDetails.entityName, idOfTheCopy));
		new CcpCacheDecorator(VisEntityResumeFilter.ENTITY, idOfTheDocumentOfTheDay).delete();
		try {
			filtersOfTheId(filterId);
			fail("no document of the day and no copy");
		} catch (CcpErrorFlowDisturb expected) {
			assertEquals(CcpProcessStatusDefault.NOT_FOUND, expected.status);
		}
	}

	private static CcpJsonRepresentation filtersOfTheIdAndTheEmail(String filterId, String email) {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(VisBusinessGetTheResumeFilter.Fields.id, filterId)
				.put(VisBusinessGetTheResumeFilter.Fields.email, email);
		CcpJsonRepresentation filters = VisServiceResumeFilter.GetResumeFilter.execute(json);
		return filters;
	}

	private static CcpJsonRepresentation saveTheDescription(String filterId, String email, String description) {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(VisEntityResumeFilterDescription.Fields.filterId, filterId)
				.put(VisEntityResumeFilterDescription.Fields.email, email)
				.put(VisEntityResumeFilterDescription.Fields.description, description);
		CcpJsonRepresentation response = VisServiceResumeFilter.SaveResumeFilterDescription.execute(json);
		assertEquals("the answer is the id of the filters", filterId, response.getAsString(VisEntityResumeFilterDescription.Fields.filterId));
		return json;
	}

	/** A text of the position of the size asked. */
	private static String textOf(int size) {
		String text = "Desenvolvedor Java com Spring Boot e Elasticsearch. ".repeat(200).substring(0, size);
		return text;
	}

	/**
	 * The link shared with the text of the position (since 2026-11-26): the text is saved under the id of the filters and
	 * the e-mail of the session, and the read with both brings the filters and the text, in one union all; without the
	 * e-mail, with an invalid one or with another e-mail, the filters alone; the same filters shared again replace the
	 * text; on the next day, the filters and the text (disposable by day since 2026-11-28) come from their copies.
	 */
	@Test
	public void theTextSharedWithTheLinkComesBackWithTheFiltersOfTheEmail() {
		CcpJsonRepresentation filters = this.filters().put(VisJsonFieldsResumeFilter.seniority, List.of("especialista"));
		String filterId = search(filters).getAsString(JnJsonCommonsFields.id);
		String email = "resumeFilterDescription" + this.now + "@jobsnow.com";
		String text = textOf(150);
		CcpJsonRepresentation saved = saveTheDescription(filterId, email, text);
		try {
			CcpJsonRepresentation readBack = filtersOfTheIdAndTheEmail(filterId, email);
			assertEquals("the text", text, readBack.getAsString(VisEntityResumeFilterDescription.Fields.description));
			assertEquals("the filters", List.of("especialista"), readBack.getAsStringList(VisJsonFieldsResumeFilter.seniority));

			CcpJsonRepresentation withoutTheEmail = filtersOfTheId(filterId);
			assertFalse("no e-mail, no text", withoutTheEmail.containsAllFields(VisEntityResumeFilterDescription.Fields.description));
			assertEquals("no e-mail, the filters", List.of("especialista"), withoutTheEmail.getAsStringList(VisJsonFieldsResumeFilter.seniority));

			CcpJsonRepresentation invalidEmail = filtersOfTheIdAndTheEmail(filterId, "resumeFilterDescription" + this.now + "jobsnow.com");
			assertFalse("an invalid e-mail is ignored", invalidEmail.containsAllFields(VisEntityResumeFilterDescription.Fields.description));
			assertEquals("an invalid e-mail, the filters", List.of("especialista"), invalidEmail.getAsStringList(VisJsonFieldsResumeFilter.seniority));

			CcpJsonRepresentation anotherEmail = filtersOfTheIdAndTheEmail(filterId, "another" + this.now + "@jobsnow.com");
			assertFalse("another e-mail shared no text", anotherEmail.containsAllFields(VisEntityResumeFilterDescription.Fields.description));

			try {
				filtersOfTheIdAndTheEmail("abc123", email);
				fail("an id of no filters, with the e-mail");
			} catch (CcpErrorFlowDisturb expected) {
				assertEquals(CcpProcessStatusDefault.NOT_FOUND, expected.status);
			}

			String newText = textOf(5000);
			saveTheDescription(filterId, email, newText);
			CcpJsonRepresentation replaced = filtersOfTheIdAndTheEmail(filterId, email);
			assertEquals("shared again, the new text", newText, replaced.getAsString(VisEntityResumeFilterDescription.Fields.description));

			// the next day: the document of the day is gone, the filters come from the copy, still with the text
			CcpJsonRepresentation idOfTheFilters = CcpOtherConstants.EMPTY_JSON.put(VisEntityResumeFilter.Fields.id, filterId);
			String idOfTheDocumentOfTheDay = VisEntityResumeFilter.ENTITY.calculateId(idOfTheFilters);
			CcpCrud crud = CcpDependencyInjection.getDependency(CcpCrud.class);
			CcpEntityMetaData filterDetails = VisEntityResumeFilter.ENTITY.getEntityMetaData();
			assertTrue("the document of the day was saved", crud.delete(filterDetails.entityName, idOfTheDocumentOfTheDay));
			new CcpCacheDecorator(VisEntityResumeFilter.ENTITY, idOfTheDocumentOfTheDay).delete();
			// the text is disposable by day as well (since 2026-11-28): its document of the day is gone too
			CcpJsonRepresentation storedKeyOfTheText = VisEntityResumeFilterDescription.ENTITY.getJsonWithHandledPrimaryKey(saved);
			String idOfTheTextOfTheDay = VisEntityResumeFilterDescription.ENTITY.calculateId(storedKeyOfTheText);
			CcpEntityMetaData descriptionDetails = VisEntityResumeFilterDescription.ENTITY.getEntityMetaData();
			assertTrue("the text of the day was saved", crud.delete(descriptionDetails.entityName, idOfTheTextOfTheDay));
			new CcpCacheDecorator(VisEntityResumeFilterDescription.ENTITY, idOfTheTextOfTheDay).delete();
			CcpJsonRepresentation fromTheCopy = filtersOfTheIdAndTheEmail(filterId, email);
			assertEquals("the filters, from the copy", List.of("especialista"), fromTheCopy.getAsStringList(VisJsonFieldsResumeFilter.seniority));
			assertEquals("the text, with the filters of the copy", newText, fromTheCopy.getAsString(VisEntityResumeFilterDescription.Fields.description));
		} finally {
			VisEntityResumeFilterDescription.ENTITY.delete(saved);
		}
	}

	/** The text shared with the link has from 100 to 5,000 characters, as the text of the position. */
	@Test
	public void theTextSharedWithTheLinkHasFrom100To5000Characters() {
		CcpJsonRepresentation filters = this.filters().put(VisJsonFieldsResumeFilter.seniority, List.of("estag"));
		String filterId = search(filters).getAsString(JnJsonCommonsFields.id);
		String email = "resumeFilterDescriptionSize" + this.now + "@jobsnow.com";
		try {
			saveTheDescription(filterId, email, textOf(99));
			fail("a text of 99 characters");
		} catch (CcpJsonValidationError expected) {
		}
		try {
			saveTheDescription(filterId, email, textOf(5001));
			fail("a text of 5,001 characters");
		} catch (CcpJsonValidationError expected) {
		}
		CcpJsonRepresentation saved = saveTheDescription(filterId, email, textOf(100));
		VisEntityResumeFilterDescription.ENTITY.delete(saved);
	}

	@Test
	public void theIdReadsTheFiltersBack() {
		CcpJsonRepresentation filters = this.filters().put(VisJsonFieldsResumeFilter.seniority, List.of("pleno", "senior"));
		String filterId = search(filters).getAsString(JnJsonCommonsFields.id);

		CcpJsonRepresentation readBack = filtersOfTheId(filterId);
		assertEquals("the area", 1, readBack.getAsIntegerNumber(VisJsonFieldsResumeFilter.resumeType).intValue());
		assertEquals("the seniorities", List.of("pleno", "senior"), readBack.getAsStringList(VisJsonFieldsResumeFilter.seniority));
		assertEquals("the domain", filters.getAsString(VisJsonFieldsResumeFilter.recruiterDomain), readBack.getAsString(VisJsonFieldsResumeFilter.recruiterDomain));

		try {
			filtersOfTheId("abc123");
			fail("an id of no filters");
		} catch (CcpErrorFlowDisturb expected) {
			assertEquals(CcpProcessStatusDefault.NOT_FOUND, expected.status);
		}
		// the hash of the Java is signed: a negative one has 41 characters, and is a valid id
		try {
			filtersOfTheId("-0000000000000000000000000000000000000001");
			fail("an id of no filters");
		} catch (CcpErrorFlowDisturb expected) {
			assertEquals("a negative hash is a valid id", CcpProcessStatusDefault.NOT_FOUND, expected.status);
		}
		try {
			filtersOfTheId("4c73-473c");
			fail("a minus sign only at the start");
		} catch (CcpJsonValidationError expected) {
		}
	}

	@Test
	public void theFiltersAreTheFieldsOfThePosition() {
		String resumeId = this.save(this.resume("resumeFilterPosition"));
		CcpJsonRepresentation php = new CcpJsonRepresentation("{\"skill\":\"PHP\",\"word\":\"PHP\",\"type\":\"skillsFromResume\",\"category\":\"skillsFromText\"}");
		CcpJsonRepresentation sql = new CcpJsonRepresentation("{\"skill\":\"SQL\",\"word\":\"SQL\",\"type\":\"skillsFromResume\",\"category\":\"skillsFromText\"}");
		CcpJsonRepresentation desiredSql = new CcpJsonRepresentation("{\"skill\":\"SQL\",\"word\":\"SQL\",\"type\":\"shouldSkills\",\"category\":\"shouldSkills\"}");

		assertTrue("a required skill of the position the resume has", idsOf(search(this.filters().put(VisJsonFieldsResumeFilter.skill, Arrays.asList(php)))).contains(resumeId));
		assertFalse("a required skill it does not have", idsOf(search(this.filters().put(VisJsonFieldsResumeFilter.skill, Arrays.asList(sql)))).contains(resumeId));
		assertTrue("a desired skill does not filter", idsOf(search(this.filters().put(VisJsonFieldsResumeFilter.skill, Arrays.asList(desiredSql)))).contains(resumeId));

		CcpJsonRepresentation homeOffice = this.filters().put(VisJsonFieldsResumeFilter.ddd, Arrays.asList(10));
		assertTrue("the home office takes every ddd", idsOf(search(homeOffice)).contains(resumeId));
		CcpJsonRepresentation noDdd = this.filters().removeFields(VisJsonFieldsResumeFilter.ddd);
		assertTrue("no ddd is every ddd", idsOf(search(noDdd)).contains(resumeId));
		assertFalse("a ddd the resume does not list", idsOf(search(this.filters().put(VisJsonFieldsResumeFilter.ddd, Arrays.asList(31)))).contains(resumeId));

		assertTrue("0 days does not filter", idsOf(search(this.filters().put(VisJsonFieldsResumeFilter.disponibility, 0))).contains(resumeId));
		assertFalse("starts in 10 days, the filter asks 5", idsOf(search(this.filters().put(VisJsonFieldsResumeFilter.disponibility, 5))).contains(resumeId));
		assertFalse("outside the CLT range", idsOf(search(this.filters().put(VisJsonFieldsResumeFilter.clt, new CcpJsonRepresentation("{\"min\":5000,\"max\":10000}")))).contains(resumeId));
	}

	@Test
	public void onlyTheAreaIsRequired() {
		CcpJsonRepresentation onlyTheArea = CcpOtherConstants.EMPTY_JSON.put(VisJsonFieldsResumeFilter.resumeType, 1);
		assertTrue("only the area is enough", search(onlyTheArea).containsAllFields(JnJsonCommonsFields.id, VisBusinessResumesOfTheFilters.Fields.resumes));

		List<CcpJsonRepresentation> invalidFilters = Arrays.asList(
				CcpOtherConstants.EMPTY_JSON.put(VisJsonFieldsResumeFilter.ddd, Arrays.asList(11)),
				this.filters().put(VisJsonFieldsResumeFilter.resumeType, 5),
				this.filters().put(VisJsonFieldsResumeFilter.ddd, Arrays.asList(11, 11)),
				this.filters().put(VisJsonFieldsResumeFilter.seniority, List.of("guru")),
				this.filters().put(VisJsonFieldsResumeFilter.disponibility, 31),
				this.filters().put(VisJsonFieldsResumeFilter.clt, new CcpJsonRepresentation("{\"min\":9000,\"max\":5000}")),
				this.filters().put(VisJsonFieldsResumeFilter.skill, Arrays.asList(new CcpJsonRepresentation("{\"skill\":\"PHP\"}")))
				);
		for (CcpJsonRepresentation invalid : invalidFilters) {
			try {
				search(invalid);
				fail("refused: " + invalid.asUgglyJson());
			} catch (CcpJsonValidationError expected) {
			}
		}
	}
}
