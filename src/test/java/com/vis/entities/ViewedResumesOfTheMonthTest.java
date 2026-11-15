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
import com.vis.business.recruiter.resumes.VisRecruiterResumesFields;
import com.vis.services.VisServiceRecruiterResumes;

/**
 * Proves the views of the month of a recruiter: every save of {@code vis_viewed_resume} keeps, in the group of the
 * recruiter in the current month ({@code jn_grouper}, parent {@code vis_viewed_resumes_of_the_month}), one JSON text per
 * resume with the LinkedIn as it came, the alias and the date of the last view; {@code VisBusinessViewedResumesOfTheMonth}
 * reads it by id from the e-mail and the month and answers the hash of the LinkedIn in its place, never the LinkedIn.
 * Since 2026-11-15 the group follows the sides of the entity: a view moved to the twin goes to the group of the same
 * month in {@code jn_inactive_grouper}, answered in {@code unwantedResume} (the tab "Indesejados"). Also since
 * 2026-11-15 the month {@code all} (the option "Listar todos") reads the group of every view of the recruiter, whatever
 * the month (parent {@code vis_viewed_resumes_of_the_recruiter}), kept by the same saves.
 */
public class ViewedResumesOfTheMonthTest {

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

	private static final String DATE_REGEX = "^[0-9]{2}/[0-9]{2}/[0-9]{4} [0-9]{2}:[0-9]{2}:[0-9]{2}$";

	private final long now = System.currentTimeMillis();

	private final String email = "recrutador" + this.now + "@empresa.com.br";

	private final String currentMonth = CcpEntityExpurgableOptions.monthly.getFormattedDate();

	private static final String ALL_MONTHS = "all";

	/** A date of a past month, given to the save as the date of the view (the decorator keeps a date given by the caller). */
	private static final String DATE_OF_A_PAST_MONTH = "15/10/2025 10:00:00";

	private static final String PAST_MONTH = "202510";

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
		return parse(texts);
	}

	private List<CcpJsonRepresentation> unwantedResumesOf(String month) {
		List<String> texts = this.viewsOf(month).getAsStringList(VisRecruiterResumesFields.unwantedResume);
		return parse(texts);
	}

	private static List<CcpJsonRepresentation> parse(List<String> texts) {
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

	/** The id of the group of every view of the recruiter, calculated as the writer does it: the SHA-1 of the e-mail only. */
	private String groupIdOfTheRecruiter() {
		CcpJsonRepresentation key = CcpOtherConstants.EMPTY_JSON.put(VisEntityViewedResumesOfTheRecruiter.Fields.email, sha1(this.email));
		return VisEntityViewedResumesOfTheRecruiter.ENTITY.calculateId(key);
	}

	private CcpJsonRepresentation groupKeyOfTheRecruiter() {
		String parentName = VisEntityViewedResumesOfTheRecruiter.ENTITY.getEntityMetaData().entityName;
		String childName = VisEntityViewedResume.ENTITY.getEntityMetaData().entityName;
		return JnGroupers.getGroupKey(parentName, childName, this.groupIdOfTheRecruiter());
	}

	private CcpJsonRepresentation groupKeyOfThePastMonth() {
		String parentName = VisEntityViewedResumesOfTheMonth.ENTITY.getEntityMetaData().entityName;
		String childName = VisEntityViewedResume.ENTITY.getEntityMetaData().entityName;
		return JnGroupers.getGroupKey(parentName, childName, this.groupIdOfTheMonth(PAST_MONTH));
	}

	private CcpJsonRepresentation saveInThePastMonth(String linkedin, String alias) {
		CcpJsonRepresentation view = this.view(linkedin, alias).put(VisEntityViewedResume.Fields.date, DATE_OF_A_PAST_MONTH);
		VisEntityViewedResume.ENTITY.save(view);
		this.savedViews.add(view);
		return view;
	}

	@After
	public void removeTheViewsAndTheGroup() {
		for (CcpJsonRepresentation view : this.savedViews) {
			VisEntityViewedResume.ENTITY.deleteAnyWhere(view);
		}
		JnEntityGrouper.ENTITY.deleteAnyWhere(this.groupKey());
		JnEntityGrouper.ENTITY.deleteAnyWhere(this.groupKeyOfTheRecruiter());
		JnEntityGrouper.ENTITY.deleteAnyWhere(this.groupKeyOfThePastMonth());
	}

	@Test
	public void everyMonthListsTheViewsOfEveryMonth() {
		this.save(this.linkedin("deste-mes"), "Deste mês");
		this.saveInThePastMonth(this.linkedin("mes-passado"), "Do mês passado");

		List<CcpJsonRepresentation> currentMonthViews = this.viewedResumesOf(this.currentMonth);
		List<CcpJsonRepresentation> pastMonthViews = this.viewedResumesOf(PAST_MONTH);
		List<CcpJsonRepresentation> everyView = this.viewedResumesOf(ALL_MONTHS);

		assertEquals(1, currentMonthViews.size());
		assertEquals("Deste mês", currentMonthViews.get(0).getAsString(VisEntityViewedResume.Fields.alias));
		assertEquals(1, pastMonthViews.size());
		assertEquals("Do mês passado", pastMonthViews.get(0).getAsString(VisEntityViewedResume.Fields.alias));
		assertEquals(2, everyView.size());
		assertEquals(ALL_MONTHS, this.viewsOf(ALL_MONTHS).getAsString(VisBusinessViewedResumesOfTheMonth.Fields.month));
	}

	@Test
	public void everyMonthKeepsOneItemPerResumeWithTheLastView() {
		String linkedin = this.linkedin("candidato");
		this.saveInThePastMonth(linkedin, "Primeira visualização");
		this.save(linkedin, "Visualização deste mês");

		List<CcpJsonRepresentation> everyView = this.viewedResumesOf(ALL_MONTHS);

		assertEquals(1, everyView.size());
		CcpJsonRepresentation view = everyView.get(0);
		assertEquals("Visualização deste mês", view.getAsString(VisEntityViewedResume.Fields.alias));
		assertFalse("the date of the last view", DATE_OF_A_PAST_MONTH.equals(view.getAsString(VisEntityViewedResume.Fields.date)));
		assertFalse("the LinkedIn never leaves in the list", view.toString().contains(linkedin));
	}

	@Test
	public void aViewMadeUnwantedInAPastMonthStaysInTheUnwantedOfEveryMonth() {
		CcpJsonRepresentation view = this.saveInThePastMonth(this.linkedin("indesejado"), "Indesejado");
		this.save(this.linkedin("desejado"), "Desejado");

		VisEntityViewedResume.ENTITY.delete(view);

		assertTrue("the current month does not see the view of the past month", this.unwantedResumesOf(this.currentMonth).isEmpty());
		List<CcpJsonRepresentation> unwanted = this.unwantedResumesOf(ALL_MONTHS);
		assertEquals(1, unwanted.size());
		assertEquals("Indesejado", unwanted.get(0).getAsString(VisEntityViewedResume.Fields.alias));
		List<CcpJsonRepresentation> viewed = this.viewedResumesOf(ALL_MONTHS);
		assertEquals(1, viewed.size());
		assertEquals("Desejado", viewed.get(0).getAsString(VisEntityViewedResume.Fields.alias));
	}

	@Test
	public void theGroupOfEveryMonthKeepsOnlyTheHashOfTheEmail() {
		this.save(this.linkedin("candidato"), "Dev Java sênior");

		CcpJsonRepresentation group = JnEntityGrouper.ENTITY.getOneById(this.groupKeyOfTheRecruiter());

		CcpJsonRepresentation parentJson = group.getInnerJson(JnEntityGrouper.Fields.parentJson);
		assertEquals(sha1(this.email), parentJson.getAsString(VisEntityViewedResumesOfTheRecruiter.Fields.email));
		assertEquals("only the e-mail: no period", 1, parentJson.fieldSet().size());
		assertFalse(group.toString().contains(this.email));
	}

	@Test(expected = CcpJsonValidationError.class)
	public void aWordOtherThanAllIsRefused() {
		this.viewsOf("todos");
	}

	@Test
	public void theViewIsListedWithTheHashOfTheLinkedinTheAliasAndTheDate() {
		String linkedin = this.linkedin("candidato");
		this.save(linkedin, "Dev Java sênior");

		List<CcpJsonRepresentation> views = this.viewedResumesOf(this.currentMonth);

		assertEquals(1, views.size());
		CcpJsonRepresentation view = views.get(0);
		assertFalse("the LinkedIn never leaves in the list", view.containsAllFields(VisEntityViewedResume.Fields.linkedin));
		assertFalse(view.toString().contains(linkedin));
		assertFalse(view.getAsString(VisRecruiterResumesFields.linkedinHash).isEmpty());
		assertEquals("Dev Java sênior", view.getAsString(VisEntityViewedResume.Fields.alias));
		assertTrue(view.getAsString(VisEntityViewedResume.Fields.date).matches(DATE_REGEX));
		assertEquals("only linkedinHash, alias and date", 3, view.fieldSet().size());
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
		List<String> linkedinHashes = new ArrayList<>();
		for (CcpJsonRepresentation view : views) {
			linkedinHashes.add(view.getAsString(VisRecruiterResumesFields.linkedinHash));
		}
		assertEquals("one hash per LinkedIn", 2, new java.util.HashSet<>(linkedinHashes).size());
	}

	@Test
	public void anotherMonthHasNoView() {
		this.save(this.linkedin("candidato"), "Dev Java sênior");

		CcpJsonRepresentation views = this.viewsOf("200001");

		assertEquals("200001", views.getAsString(VisBusinessViewedResumesOfTheMonth.Fields.month));
		assertTrue(views.getAsStringList(VisEntityViewedResumesOfTheMonth.Fields.viewedResume).isEmpty());
		assertTrue(views.getAsStringList(VisRecruiterResumesFields.unwantedResume).isEmpty());
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
	public void theStoredViewKeepsTheDateButNoParentIdNorReadableValue() {
		String linkedin = this.linkedin("candidato");
		CcpJsonRepresentation view = this.save(linkedin, "Dev Java sênior");

		CcpJsonRepresentation stored = VisEntityViewedResume.ENTITY.getOneById(view);

		assertTrue(stored.getAsString(VisEntityViewedResume.Fields.date).matches(DATE_REGEX));
		// oneToMany: the parents come from the view itself, no field keeps their ids
		assertFalse(stored.fieldSet().contains("monthGroup"));
		assertFalse(stored.toString().contains(linkedin));
		assertFalse(stored.toString().contains(this.email));
	}

	@Test
	public void theViewMovedToTheTwinLeavesTheViewedListAndEntersTheUnwantedOne() {
		String linkedin = this.linkedin("candidato");
		CcpJsonRepresentation view = this.save(linkedin, "Dev Java sênior");
		CcpJsonRepresentation before = this.viewedResumesOf(this.currentMonth).get(0);

		VisEntityViewedResume.ENTITY.delete(view.put(VisEntityViewedResume.Fields.comment, "Comentário da inativação"));

		assertTrue(this.viewedResumesOf(this.currentMonth).isEmpty());
		List<CcpJsonRepresentation> unwanted = this.unwantedResumesOf(this.currentMonth);
		assertEquals(1, unwanted.size());
		CcpJsonRepresentation moved = unwanted.get(0);
		assertEquals(before.getAsString(VisRecruiterResumesFields.linkedinHash), moved.getAsString(VisRecruiterResumesFields.linkedinHash));
		assertEquals("Dev Java sênior", moved.getAsString(VisEntityViewedResume.Fields.alias));
		assertEquals("the moved view keeps the date of the last view", before.getAsString(VisEntityViewedResume.Fields.date), moved.getAsString(VisEntityViewedResume.Fields.date));
		assertEquals("Comentário da inativação", moved.getAsString(VisEntityViewedResume.Fields.comment));
		assertFalse("the LinkedIn never leaves in the list", moved.toString().contains(linkedin));
		assertFalse("only the fields the main group had, plus the comment", moved.containsAllFields(VisEntityViewedResume.Fields.insertionDate));
	}

	@Test
	public void theViewSavedAgainLeavesTheUnwantedListAndReturnsToTheViewedOne() {
		String linkedin = this.linkedin("candidato");
		CcpJsonRepresentation view = this.save(linkedin, "Dev Java sênior");
		VisEntityViewedResume.ENTITY.delete(view);

		this.save(linkedin, "Dev Java de novo");

		assertTrue(this.unwantedResumesOf(this.currentMonth).isEmpty());
		List<CcpJsonRepresentation> viewed = this.viewedResumesOf(this.currentMonth);
		assertEquals(1, viewed.size());
		assertEquals("Dev Java de novo", viewed.get(0).getAsString(VisEntityViewedResume.Fields.alias));
	}

	@Test
	public void theViewReactivatedFromTheTwinReturnsToTheViewedList() {
		CcpJsonRepresentation view = this.save(this.linkedin("candidato"), "Dev Java sênior");
		VisEntityViewedResume.ENTITY.delete(view);

		VisEntityViewedResume.ENTITY.getTwinEntity().delete(view);

		assertTrue(this.unwantedResumesOf(this.currentMonth).isEmpty());
		assertEquals(1, this.viewedResumesOf(this.currentMonth).size());
	}

	@Test
	public void theViewDeletedFromBothSidesLeavesBothLists() {
		CcpJsonRepresentation active = this.save(this.linkedin("ativo"), "Ativo");
		CcpJsonRepresentation inactive = this.save(this.linkedin("inativo"), "Inativo");
		VisEntityViewedResume.ENTITY.delete(inactive);

		VisEntityViewedResume.ENTITY.deleteAnyWhere(active);
		VisEntityViewedResume.ENTITY.deleteAnyWhere(inactive);

		assertTrue(this.viewedResumesOf(this.currentMonth).isEmpty());
		assertTrue(this.unwantedResumesOf(this.currentMonth).isEmpty());
	}

	@Test
	public void eachListHasOnlyTheViewsOfItsSide() {
		this.save(this.linkedin("primeiro"), "Primeiro");
		CcpJsonRepresentation second = this.save(this.linkedin("segundo"), "Segundo");
		this.save(this.linkedin("terceiro"), "Terceiro");

		VisEntityViewedResume.ENTITY.delete(second);

		assertEquals(2, this.viewedResumesOf(this.currentMonth).size());
		List<CcpJsonRepresentation> unwanted = this.unwantedResumesOf(this.currentMonth);
		assertEquals(1, unwanted.size());
		assertEquals("Segundo", unwanted.get(0).getAsString(VisEntityViewedResume.Fields.alias));
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
		assertFalse("the LinkedIn never leaves in the list", texts.get(0).contains(linkedin));
		assertTrue(texts.get(0).contains(VisRecruiterResumesFields.linkedinHash.name()));
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
