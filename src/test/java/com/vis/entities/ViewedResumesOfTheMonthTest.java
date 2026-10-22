package com.vis.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityExpurgableOptions;
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
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.utils.JnGroupers;
import com.vis.business.recruiter.resumes.VisBusinessViewedResumesOfTheMonth;
import com.vis.services.VisServiceRecruiterResumes;

/**
 * Proves the views of the month of a recruiter: every save of {@code vis_viewed_resume} keeps, in the group of the
 * recruiter in the current month ({@code jn_grouper}, parent {@code vis_viewed_resumes_of_the_month}), one JSON text per
 * resume with the LinkedIn as it came, the alias and the date of the last view; {@code VisBusinessViewedResumesOfTheMonth}
 * reads it by id from the e-mail and the month.
 */
public class ViewedResumesOfTheMonthTest {

	static {
		CcpDependencyInjection.removeAllDependencies();
		CcpDependencyInjection.loadAllDependencies(
				CcpLocalInstances.syncMensageriaListener,
				CcpLocalInstances.bucket,
				CcpLocalInstances.email,
				new CcpElasticSearchQueryExecutor(),
				new CcpElasticSearchDbRequest(),
				new CcpMindrotPasswordHandler(),
				CcpLocalCacheInstances.mock,
				new CcpElasticSerchDbBulk(),
				new CcpElasticSearchCrud(),
				new CcpGsonJsonHandler(),
				new CcpApacheMimeHttp()
		);
	}

	private static final String DATE_REGEX = "^[0-9]{2}/[0-9]{2}/[0-9]{4} [0-9]{2}:[0-9]{2}:[0-9]{2}$";

	private final long now = System.currentTimeMillis();

	private final String email = "recrutador" + this.now + "@empresa.com.br";

	private final String currentMonth = CcpEntityExpurgableOptions.monthly.getFormattedDate();

	private final List<CcpJsonRepresentation> savedViews = new ArrayList<>();

	private String linkedin(String name) {
		return "https://www.linkedin.com/in/" + name + "-" + this.now;
	}

	private CcpJsonRepresentation view(String linkedin, String alias) {
		return CcpOtherConstants.EMPTY_JSON
				.put(VisEntityViewedResume.Fields.linkedin, linkedin)
				.put(VisEntityViewedResume.Fields.email, this.email)
				.put(VisEntityViewedResume.Fields.alias, alias);
	}

	private CcpJsonRepresentation save(String linkedin, String alias) {
		CcpJsonRepresentation view = this.view(linkedin, alias);
		VisEntityViewedResume.ENTITY.save(view);
		this.savedViews.add(view);
		return view;
	}

	private static String sha1(String text) {
		return new CcpStringDecorator(text).hash().asString(CcpHashAlgorithm.SHA1);
	}

	private CcpJsonRepresentation viewsOf(String month) {
		CcpJsonRepresentation input = CcpOtherConstants.EMPTY_JSON
				.put(VisBusinessViewedResumesOfTheMonth.Fields.email, this.email)
				.put(VisBusinessViewedResumesOfTheMonth.Fields.month, month);
		return VisBusinessViewedResumesOfTheMonth.INSTANCE.execute(input);
	}

	private List<CcpJsonRepresentation> viewedResumesOf(String month) {
		List<String> texts = this.viewsOf(month).getAsStringList(VisEntityViewedResumesOfTheMonth.Fields.viewedResume);
		List<CcpJsonRepresentation> views = new ArrayList<>();
		for (String text : texts) {
			views.add(new CcpJsonRepresentation(text));
		}
		return views;
	}

	/** The id of the group of the recruiter in the month, calculated as the writer does it. */
	private String groupIdOfTheMonth(String month) {
		CcpJsonRepresentation key = CcpOtherConstants.EMPTY_JSON
				.put(VisEntityViewedResumesOfTheMonth.Fields.email, sha1(this.email))
				.put(JnJsonCommonsFields.disposablePeriod, month);
		return VisEntityViewedResumesOfTheMonth.ENTITY.calculateId(key);
	}

	private CcpJsonRepresentation groupKey() {
		String parentName = VisEntityViewedResumesOfTheMonth.ENTITY.getEntityMetaData().entityName;
		String childName = VisEntityViewedResume.ENTITY.getEntityMetaData().entityName;
		return JnGroupers.getGroupKey(parentName, childName, this.groupIdOfTheMonth(this.currentMonth));
	}

	@After
	public void removeTheViewsAndTheGroup() {
		for (CcpJsonRepresentation view : this.savedViews) {
			VisEntityViewedResume.ENTITY.deleteAnyWhere(view);
		}
		JnEntityGrouper.ENTITY.deleteAnyWhere(this.groupKey());
	}

	@Test
	public void theViewIsListedWithTheLinkedinAsItCameTheAliasAndTheDate() {
		String linkedin = this.linkedin("candidato");
		this.save(linkedin, "Dev Java sênior");

		List<CcpJsonRepresentation> views = this.viewedResumesOf(this.currentMonth);

		assertEquals(1, views.size());
		CcpJsonRepresentation view = views.get(0);
		assertEquals(linkedin, view.getAsString(VisEntityViewedResume.Fields.linkedin));
		assertEquals("Dev Java sênior", view.getAsString(VisEntityViewedResume.Fields.alias));
		assertTrue(view.getAsString(VisEntityViewedResume.Fields.date).matches(DATE_REGEX));
		assertEquals("only linkedin, alias and date", 3, view.fieldSet().size());
	}

	@Test
	public void aNewViewOfTheSameResumeReplacesTheEarlierOne() {
		String linkedin = this.linkedin("candidato");
		this.save(linkedin, "Primeiro apelido");
		this.save(linkedin, "Segundo apelido");

		List<CcpJsonRepresentation> views = this.viewedResumesOf(this.currentMonth);

		assertEquals(1, views.size());
		assertEquals("Segundo apelido", views.get(0).getAsString(VisEntityViewedResume.Fields.alias));
	}

	@Test
	public void eachResumeHasItsOwnItem() {
		String first = this.linkedin("primeiro");
		String second = this.linkedin("segundo");
		this.save(first, "Primeiro candidato");
		this.save(second, "Segundo candidato");
		this.save(first, "Primeiro de novo");

		List<CcpJsonRepresentation> views = this.viewedResumesOf(this.currentMonth);

		assertEquals(2, views.size());
		List<String> linkedins = new ArrayList<>();
		for (CcpJsonRepresentation view : views) {
			linkedins.add(view.getAsString(VisEntityViewedResume.Fields.linkedin));
		}
		assertTrue(linkedins.containsAll(Arrays.asList(first, second)));
	}

	@Test
	public void anotherMonthHasNoView() {
		this.save(this.linkedin("candidato"), "Dev Java sênior");

		CcpJsonRepresentation views = this.viewsOf("200001");

		assertEquals("200001", views.getAsString(VisBusinessViewedResumesOfTheMonth.Fields.month));
		assertTrue(views.getAsStringList(VisEntityViewedResumesOfTheMonth.Fields.viewedResume).isEmpty());
	}

	@Test
	public void theGroupKeepsOnlyTheHashOfTheEmail() {
		this.save(this.linkedin("candidato"), "Dev Java sênior");

		CcpJsonRepresentation group = JnEntityGrouper.ENTITY.getOneById(this.groupKey());

		CcpJsonRepresentation parentJson = group.getInnerJson(JnEntityGrouper.Fields.parentJson);
		assertEquals(sha1(this.email), parentJson.getAsString(VisEntityViewedResumesOfTheMonth.Fields.email));
		assertEquals(1, parentJson.fieldSet().size());
		assertFalse(group.toString().contains(this.email));
	}

	@Test
	public void theStoredViewKeepsTheDateAndTheGroupOfTheMonthButNoReadableValue() {
		String linkedin = this.linkedin("candidato");
		CcpJsonRepresentation view = this.save(linkedin, "Dev Java sênior");

		CcpJsonRepresentation stored = VisEntityViewedResume.ENTITY.getOneById(view);

		assertTrue(stored.getAsString(VisEntityViewedResume.Fields.date).matches(DATE_REGEX));
		assertEquals(Arrays.asList(this.groupIdOfTheMonth(this.currentMonth)), stored.getAsStringList(VisEntityViewedResume.Fields.monthGroup));
		assertFalse(stored.toString().contains(linkedin));
		assertFalse(stored.toString().contains(this.email));
	}

	@Test
	public void deletingTheViewKeepsTheHistoryOfTheMonth() {
		CcpJsonRepresentation view = this.save(this.linkedin("candidato"), "Dev Java sênior");

		VisEntityViewedResume.ENTITY.deleteAnyWhere(view);

		assertEquals(1, this.viewedResumesOf(this.currentMonth).size());
	}

	@Test
	public void theServiceAnswersTheViewsOfTheMonth() {
		String linkedin = this.linkedin("candidato");
		this.save(linkedin, "Dev Java sênior");
		CcpJsonRepresentation input = CcpOtherConstants.EMPTY_JSON
				.put(VisBusinessViewedResumesOfTheMonth.Fields.email, this.email)
				.put(VisBusinessViewedResumesOfTheMonth.Fields.month, this.currentMonth);

		CcpJsonRepresentation response = VisServiceRecruiterResumes.GetViewedResumesOfTheMonth.execute(input);

		List<String> texts = response.getAsStringList(VisEntityViewedResumesOfTheMonth.Fields.viewedResume);
		assertEquals(1, texts.size());
		assertTrue(texts.get(0).contains(linkedin));
	}

	@Test(expected = CcpJsonValidationError.class)
	public void aMonthOutOfTheFormatIsRefused() {
		this.viewsOf("2026-10");
	}

	@Test(expected = CcpJsonValidationError.class)
	public void aMonthThatDoesNotExistIsRefused() {
		this.viewsOf("202613");
	}
}
