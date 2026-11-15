package com.vis.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.enums.CcpEntityExpurgableOptions;
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
import com.jn.entities.JnEntityGrouper;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.utils.JnGroupers;
import com.vis.business.recruiter.resumes.VisBusinessEditViewedResume;
import com.vis.business.recruiter.resumes.VisBusinessViewedResumesOfTheMonth;
import com.vis.business.recruiter.resumes.VisRecruiterResumesFields;
import com.vis.services.VisServiceRecruiterResumes;

/**
 * Proves the actions of the screen of the viewed resumes (since 2026-11-15): "Editar detalhes" saves the alias and the
 * comment on the side where the view is (a view of the twin stays in the twin) and keeps the date of the last view;
 * "Negativar CV" moves the view to the twin and "Positivar CV" brings it back, the list of each tab following the move.
 */
public class ViewedResumeEditAndMovesTest {

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

	private final String email = "recrutador" + this.now + "@empresa.com.br";

	private final String currentMonth = CcpEntityExpurgableOptions.monthly.getFormattedDate();

	private final List<CcpJsonRepresentation> savedViews = new ArrayList<>();

	private final CcpEntity twinEntity = VisEntityViewedResume.ENTITY.getTwinEntity();

	private String linkedin(String name) {
		return "https://www.linkedin.com/in/" + name + "-" + this.now;
	}

	private CcpJsonRepresentation save(String linkedin, String alias) {
		CcpJsonRepresentation view = CcpOtherConstants.EMPTY_JSON
				.put(VisEntityViewedResume.Fields.linkedin, linkedin)
				.put(VisEntityViewedResume.Fields.email, this.email)
				.put(VisEntityViewedResume.Fields.alias, alias);
		VisEntityViewedResume.ENTITY.save(view);
		this.savedViews.add(view);
		return view;
	}

	private CcpJsonRepresentation viewsOfTheMonth() {
		CcpJsonRepresentation input = CcpOtherConstants.EMPTY_JSON
				.put(VisBusinessViewedResumesOfTheMonth.Fields.email, this.email)
				.put(VisBusinessViewedResumesOfTheMonth.Fields.month, this.currentMonth);
		return VisBusinessViewedResumesOfTheMonth.INSTANCE.execute(input);
	}

	private List<CcpJsonRepresentation> viewed() {
		return parse(this.viewsOfTheMonth().getAsStringList(VisEntityViewedResumesOfTheMonth.Fields.viewedResume));
	}

	private List<CcpJsonRepresentation> unwanted() {
		return parse(this.viewsOfTheMonth().getAsStringList(VisRecruiterResumesFields.unwantedResume));
	}

	private static List<CcpJsonRepresentation> parse(List<String> texts) {
		List<CcpJsonRepresentation> views = new ArrayList<>();
		for (String text : texts) {
			views.add(new CcpJsonRepresentation(text));
		}
		return views;
	}

	/** The hash of the LinkedIn of the only view of a list. */
	private static String onlyHash(List<CcpJsonRepresentation> views) {
		assertEquals(1, views.size());
		return views.get(0).getAsString(VisRecruiterResumesFields.linkedinHash);
	}

	private CcpJsonRepresentation request(String linkedinHash) {
		return CcpOtherConstants.EMPTY_JSON
				.put(VisBusinessEditViewedResume.Fields.email, this.email)
				.put(VisBusinessEditViewedResume.Fields.month, this.currentMonth)
				.put(VisBusinessEditViewedResume.Fields.linkedinHash, linkedinHash);
	}

	private void edit(String linkedinHash, String alias, String comment) {
		CcpJsonRepresentation request = this.request(linkedinHash)
				.put(VisBusinessEditViewedResume.Fields.alias, alias)
				.put(VisBusinessEditViewedResume.Fields.comment, comment);
		VisServiceRecruiterResumes.EditViewedResume.execute(request.content);
	}

	private static String sha1(String text) {
		return new CcpStringDecorator(text).hash().asString(CcpHashAlgorithm.SHA1);
	}

	private CcpJsonRepresentation groupKey() {
		CcpJsonRepresentation key = CcpOtherConstants.EMPTY_JSON
				.put(VisEntityViewedResumesOfTheMonth.Fields.email, sha1(this.email))
				.put(JnJsonCommonsFields.disposablePeriod, this.currentMonth);
		String parentId = VisEntityViewedResumesOfTheMonth.ENTITY.calculateId(key);
		String parentName = VisEntityViewedResumesOfTheMonth.ENTITY.getEntityMetaData().entityName;
		String childName = VisEntityViewedResume.ENTITY.getEntityMetaData().entityName;
		return JnGroupers.getGroupKey(parentName, childName, parentId);
	}

	@After
	public void removeTheViewsAndTheGroup() {
		for (CcpJsonRepresentation view : this.savedViews) {
			VisEntityViewedResume.ENTITY.deleteAnyWhere(view);
		}
		JnEntityGrouper.ENTITY.deleteAnyWhere(this.groupKey());
	}

	@Test
	public void theEditOfAWantedViewChangesAliasAndCommentAndKeepsTheDate() throws InterruptedException {
		CcpJsonRepresentation view = this.save(this.linkedin("candidato"), "Apelido antigo");
		CcpJsonRepresentation listed = this.viewed().get(0);
		String linkedinHash = listed.getAsString(VisRecruiterResumesFields.linkedinHash);
		String dateOfTheView = listed.getAsString(VisEntityViewedResume.Fields.date);
		Thread.sleep(1100);

		this.edit(linkedinHash, "Apelido novo", "Bom perfil, salário acima do teto");

		List<CcpJsonRepresentation> viewed = this.viewed();
		assertEquals(1, viewed.size());
		CcpJsonRepresentation edited = viewed.get(0);
		assertEquals("Apelido novo", edited.getAsString(VisEntityViewedResume.Fields.alias));
		assertEquals("Bom perfil, salário acima do teto", edited.getAsString(VisEntityViewedResume.Fields.comment));
		assertEquals("the edit is not a view", dateOfTheView, edited.getAsString(VisEntityViewedResume.Fields.date));
		assertTrue(this.unwanted().isEmpty());
		assertTrue(VisEntityViewedResume.ENTITY.exists(view));
		assertFalse(this.twinEntity.exists(view));
		CcpJsonRepresentation stored = VisEntityViewedResume.ENTITY.getOneById(view);
		assertEquals("Apelido novo", stored.getAsString(VisEntityViewedResume.Fields.alias));
		assertEquals(dateOfTheView, stored.getAsString(VisEntityViewedResume.Fields.date));
	}

	@Test
	public void theEditOfAnUnwantedViewKeepsItInTheTwin() {
		CcpJsonRepresentation view = this.save(this.linkedin("candidato"), "Apelido antigo");
		VisEntityViewedResume.ENTITY.delete(view);
		String linkedinHash = onlyHash(this.unwanted());

		this.edit(linkedinHash, "Apelido no indesejado", "Não atende ao perfil da vaga");

		assertTrue("the save of the twin keeps the view there", this.twinEntity.exists(view));
		assertFalse(VisEntityViewedResume.ENTITY.exists(view));
		assertTrue(this.viewed().isEmpty());
		List<CcpJsonRepresentation> unwanted = this.unwanted();
		assertEquals(1, unwanted.size());
		assertEquals("Apelido no indesejado", unwanted.get(0).getAsString(VisEntityViewedResume.Fields.alias));
		assertEquals("Não atende ao perfil da vaga", unwanted.get(0).getAsString(VisEntityViewedResume.Fields.comment));
		CcpJsonRepresentation stored = this.twinEntity.getOneById(view);
		assertEquals("Apelido no indesejado", stored.getAsString(VisEntityViewedResume.Fields.alias));
	}

	@Test
	public void theEditWithoutCommentKeepsTheCommentSaved() {
		this.save(this.linkedin("candidato"), "Apelido antigo");
		String linkedinHash = onlyHash(this.viewed());
		this.edit(linkedinHash, "Apelido novo", "Comentário que fica salvo");

		CcpJsonRepresentation request = this.request(linkedinHash).put(VisBusinessEditViewedResume.Fields.alias, "Outro apelido");
		VisServiceRecruiterResumes.EditViewedResume.execute(request.content);

		CcpJsonRepresentation edited = this.viewed().get(0);
		assertEquals("Outro apelido", edited.getAsString(VisEntityViewedResume.Fields.alias));
		assertEquals("Comentário que fica salvo", edited.getAsString(VisEntityViewedResume.Fields.comment));
	}

	@Test
	public void theEditRefusesAnAliasTooShort() {
		this.save(this.linkedin("candidato"), "Apelido antigo");
		String linkedinHash = onlyHash(this.viewed());
		try {
			this.edit(linkedinHash, "abc", "Comentário qualquer aqui");
			fail("an alias of 3 characters is refused");
		} catch (CcpJsonValidationError expected) {
		}
		assertEquals("Apelido antigo", this.viewed().get(0).getAsString(VisEntityViewedResume.Fields.alias));
	}

	@Test
	public void markingAsUnwantedMovesTheViewToTheTwinWithItsDetails() {
		CcpJsonRepresentation view = this.save(this.linkedin("candidato"), "Apelido antigo");
		String linkedinHash = onlyHash(this.viewed());
		this.edit(linkedinHash, "Apelido antigo", "Comentário antes de negativar");

		VisServiceRecruiterResumes.MarkViewedResumeAsUnwanted.execute(this.request(linkedinHash).content);

		assertTrue(this.twinEntity.exists(view));
		assertFalse(VisEntityViewedResume.ENTITY.exists(view));
		assertTrue(this.viewed().isEmpty());
		List<CcpJsonRepresentation> unwanted = this.unwanted();
		assertEquals(linkedinHash, onlyHash(unwanted));
		assertEquals("Apelido antigo", unwanted.get(0).getAsString(VisEntityViewedResume.Fields.alias));
		assertEquals("Comentário antes de negativar", unwanted.get(0).getAsString(VisEntityViewedResume.Fields.comment));
	}

	@Test
	public void markingAsWantedBringsTheViewBackFromTheTwin() {
		CcpJsonRepresentation view = this.save(this.linkedin("candidato"), "Apelido antigo");
		VisEntityViewedResume.ENTITY.delete(view);
		String linkedinHash = onlyHash(this.unwanted());

		VisServiceRecruiterResumes.MarkViewedResumeAsWanted.execute(this.request(linkedinHash).content);

		assertTrue(VisEntityViewedResume.ENTITY.exists(view));
		assertFalse(this.twinEntity.exists(view));
		assertTrue(this.unwanted().isEmpty());
		assertEquals(linkedinHash, onlyHash(this.viewed()));
	}

	@Test
	public void markingAsUnwantedAViewAlreadyUnwantedChangesNothing() {
		CcpJsonRepresentation view = this.save(this.linkedin("candidato"), "Apelido antigo");
		VisEntityViewedResume.ENTITY.delete(view);
		String linkedinHash = onlyHash(this.unwanted());

		VisServiceRecruiterResumes.MarkViewedResumeAsUnwanted.execute(this.request(linkedinHash).content);

		assertTrue(this.twinEntity.exists(view));
		assertFalse(VisEntityViewedResume.ENTITY.exists(view));
		assertEquals(linkedinHash, onlyHash(this.unwanted()));
		assertTrue(this.viewed().isEmpty());
	}

	@Test
	public void aHashOfNoViewOfTheMonthIsNotFound() {
		this.save(this.linkedin("candidato"), "Apelido antigo");
		String unknownHash = sha1("https://www.linkedin.com/in/ninguem-" + this.now);
		try {
			VisServiceRecruiterResumes.MarkViewedResumeAsUnwanted.execute(this.request(unknownHash).content);
			fail("no view of the month has this hash");
		} catch (CcpErrorFlowDisturb expected) {
			assertEquals(CcpProcessStatusDefault.NOT_FOUND, expected.status);
		}
		try {
			this.edit(unknownHash, "Apelido novo", "Comentário qualquer aqui");
			fail("no view of the month has this hash");
		} catch (CcpErrorFlowDisturb expected) {
			assertEquals(CcpProcessStatusDefault.NOT_FOUND, expected.status);
		}
	}
}
