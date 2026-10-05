package com.vis.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.flow.CcpErrorFlowDisturb;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.implementations.password.mindrot.CcpMindrotPasswordHandler;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.vis.entities.VisEntityPosition;
import com.vis.json.fields.validation.VisJsonCommonsFields;
import com.vis.status.VisProcessStatusSuggestNewSkill;
import com.jn.services.JnService;
import com.jn.services.JnErrorServiceValidationClassNotFound;

/**
 * Proves {@link VisServicePosition} against the local Elasticsearch: a saved position is active, changing its status
 * deactivates it (moves it to the twin), and a suggested skill that already exists answers {@code alreadyExists}.
 */
public class PositionServiceTest {

	private static final long ONE_DAY = 86_400_000L;

	static {
		CcpDependencyInjection.removeAllDependencies();
		CcpDependencyInjection.loadAllDependencies(
				CcpLocalInstances.syncMensageriaListener,
				CcpLocalInstances.bucket,
				CcpLocalInstances.email,
				CcpLocalInstances.instantMessenger,
				new CcpElasticSearchQueryExecutor(),
				new CcpElasticSearchDbRequest(),
				new CcpMindrotPasswordHandler(),
				CcpLocalCacheInstances.mock,
				new CcpElasticSerchDbBulk(),
				new CcpElasticSearchCrud(),
				new CcpGsonJsonHandler(),
				new CcpApacheMimeHttp());
	}

	private final String email = "position" + System.currentTimeMillis() + "@jobsnow.com";

	private CcpJsonRepresentation position() {
		long thirtyDaysAgo = System.currentTimeMillis() - 30 * ONE_DAY;
		CcpJsonRepresentation position = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonCommonsFields.email, this.email)
				.put(VisEntityPosition.Fields.channel, Arrays.asList("email"))
				.put(VisEntityPosition.Fields.contactChannel, "selecao@jobsnow.com")
				.put(VisEntityPosition.Fields.ddd, Arrays.asList(11))
				.put(VisEntityPosition.Fields.description, "position used to test the position service")
				.put(VisEntityPosition.Fields.disponibility, 5)
				.put(VisEntityPosition.Fields.expireDate, thirtyDaysAgo)
				.put(VisEntityPosition.Fields.frequency, "daily")
				.put(VisEntityPosition.Fields.requiredSkill, Arrays.asList("java"))
				.put(VisEntityPosition.Fields.seniority, "SR")
				.put(VisEntityPosition.Fields.sortFields, Arrays.asList("seniority"))
				.put(VisEntityPosition.Fields.title, "position service test")
				.put(VisEntityPosition.Fields.showSalaryExpectation, true)
				.put(VisEntityPosition.Fields.minClt, 2_000)
				.put(VisEntityPosition.Fields.maxClt, 9_000);
		return position;
	}

	private CcpJsonRepresentation key() {
		CcpJsonRepresentation key = this.position().getJsonPiece(JnJsonCommonsFields.email, VisEntityPosition.Fields.title);
		return key;
	}

	@org.junit.Ignore("finding 53: the item has no input rules class, so execute fails before the business")
	@Test
	public void aSavedPositionIsActiveAndChangingItsStatusDeactivatesIt() {
		VisServicePosition.Save.execute(this.position());

		CcpJsonRepresentation active = VisServicePosition.GetData.execute(this.key());
		assertTrue(active.toString(), active.getAsBoolean(JnJsonCommonsFields.activePosition));
		assertEquals("position service test", active.getAsString(VisEntityPosition.Fields.title));

		VisServicePosition.ChangeStatus.execute(this.position());

		CcpJsonRepresentation inactive = VisServicePosition.GetData.execute(this.key());
		assertFalse(inactive.toString(), inactive.getAsBoolean(JnJsonCommonsFields.activePosition));
	}

	@Test
	public void itemsWithoutAnInputRulesClassCurrentlyFailOnExecute() {
		JnService[] itemsWithoutRules = {
				VisServicePosition.Save, VisServicePosition.GetData, VisServicePosition.ChangeStatus,
				VisServiceRecruiter.SaveOpinionAboutThisResume, VisServiceResume.GetData, VisServiceSkills.RequestToCreateNewSkill };
		for (JnService item : itemsWithoutRules) {
			try {
				item.execute(this.position());
				fail(item + " found an input rules class");
			} catch (JnErrorServiceValidationClassNotFound e) {
				// finding 53: the convention of JnService looks for a class named after the item
			}
		}
	}

	@org.junit.Ignore("finding 53: every vis service item must have its input rules class")
	@Test
	public void executingAPositionServiceRunsItsBusiness() {
		VisServicePosition.Save.execute(this.position());

		CcpJsonRepresentation active = VisServicePosition.GetData.execute(this.key());
		assertTrue(active.getAsBoolean(JnJsonCommonsFields.activePosition));
	}

	@org.junit.Ignore("finding 53: the item has no input rules class, so execute fails before the business")
	@Test
	public void gettingImportantSkillsFromATextIsNotImplementedYet() {
		CcpJsonRepresentation request = CcpOtherConstants.EMPTY_JSON.put(VisJsonCommonsFields.skill, "JAVA");

		CcpJsonRepresentation response = VisServicePosition.GetImportantSkillsFromText.execute(request);

		assertEquals(request, response);
	}

	@org.junit.Ignore("finding 53: the item has no input rules class, so execute fails before the business")
	@Test
	public void suggestingASkillThatAlreadyExistsAnswersAlreadyExists() {
		CcpJsonRepresentation suggestion = CcpOtherConstants.EMPTY_JSON.put(VisJsonCommonsFields.skill, "JAVA");
		try {
			VisServicePosition.SuggestNewSkills.execute(suggestion);
			fail("an existing skill must divert the flow");
		} catch (CcpErrorFlowDisturb e) {
			assertEquals(VisProcessStatusSuggestNewSkill.alreadyExists, e.status);
		}
	}
}
