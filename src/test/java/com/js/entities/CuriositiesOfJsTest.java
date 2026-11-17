package com.js.entities;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpTimeDecorator;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.dependency.injection.CcpInstanceProvider;
import com.ccp.especifications.db.utils.entity.decorators.engine.SilentInstantMessenger;
import com.ccp.especifications.email.CcpEmailProvider;
import com.ccp.especifications.instant.messenger.CcpInstantMessenger;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.implementations.password.mindrot.CcpMindrotPasswordHandler;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.jn.business.grouper.JnBusinessGetGrouperCharts;
import com.jn.db.bulk.JnExecuteBulkOperation;
import com.jn.json.fields.validation.JnGrouperChartsScreens;
import com.jn.utils.JnDeleteKeysFromCache;
import com.js.business.grouper.JsBusinessCuriositiesCharts;
import com.js.json.fields.validation.JsContactUsTypes;
import com.vis.entities.RecordingEmailSender;

/**
 * 2026-11-14: the charts of js in the screen Curiosities, read with the ones of vis by id through the providers seeded
 * in {@code jn_grouper_charts_providers}: the new users by day and by month, their goals and channels, and the tickets
 * ever opened by type, which the answer of the ticket does not subtract (a history) and a reopening counts again.
 */
public class CuriositiesOfJsTest {
	static {
		CcpInstanceProvider<CcpInstantMessenger> telegram = () -> SilentInstantMessenger.INSTANCE;
		CcpInstanceProvider<CcpEmailProvider> email = () -> RecordingEmailSender.INSTANCE;
		CcpDependencyInjection.removeAllDependencies();
		CcpDependencyInjection.loadAllDependencies(
				CcpLocalInstances.syncMensageriaListener,
				CcpLocalInstances.bucket,
				CcpElasticSearchQueryExecutor.INSTANCE,
				CcpElasticSearchDbRequest.INSTANCE,
				CcpMindrotPasswordHandler.INSTANCE,
				CcpLocalCacheInstances.mock,
				CcpElasticSerchDbBulk.INSTANCE,
				CcpElasticSearchCrud.INSTANCE,
				CcpGsonJsonHandler.INSTANCE,
				CcpApacheMimeHttp.INSTANCE,
				telegram,
				email
		);
		JnExecuteBulkOperation.INSTANCE.executeBulk(JsEntityContactUs.INSTANCE.getFirstRecordsToInsert(), JnDeleteKeysFromCache.INSTANCE);
	}

	private final String email = "curiosities" + System.currentTimeMillis() + "@jobsnow.com";

	private CcpJsonRepresentation ticket() {
		String type = JsContactUsTypes.makeSuggestion.name();
		return CcpOtherConstants.EMPTY_JSON
				.put(JsEntityContactUs.Fields.email, this.email)
				.put(JsEntityContactUs.Fields.type, type)
				.put(JsEntityContactUs.Fields.title, "Uma sugestao")
				.put(JsEntityContactUs.Fields.description, "Uma sugestao para a tela de curiosidades");
	}

	private CcpJsonRepresentation answers() {
		return CcpOtherConstants.EMPTY_JSON
				.put(JsEntityLoginAnswers.Fields.email, this.email)
				.put(JsEntityLoginAnswers.Fields.goal, "recruiting")
				.put(JsEntityLoginAnswers.Fields.channel, "others");
	}

	private static long curiosity(JsBusinessCuriositiesCharts.Fields chart, String key) {
		String screen = JnGrouperChartsScreens.curiosities.name();
		CcpJsonRepresentation input = CcpOtherConstants.EMPTY_JSON.put(JnBusinessGetGrouperCharts.Fields.screen, screen);
		CcpJsonRepresentation charts = JnBusinessGetGrouperCharts.INSTANCE.execute(input);
		CcpJsonRepresentation slices = charts.containsAllFields(chart) ? charts.getInnerJson(chart) : CcpOtherConstants.EMPTY_JSON;
		CcpFieldName keyField = new CcpFieldName(key);
		return slices.containsAllFields(keyField) ? slices.getAsLongNumber(keyField) : 0L;
	}

	@After
	public void deleteTheRecords() {
		JsEntityContactUs.ENTITY.deleteAnyWhere(this.ticket());
		JsEntityLoginAnswers.ENTITY.delete(this.answers());
	}

	@Test
	public void theTicketsEverOpenedAreCountedByType() {
		String suggestion = JsContactUsTypes.makeSuggestion.name();
		long before = curiosity(JsBusinessCuriositiesCharts.Fields.contactUsTypes, suggestion);

		JsEntityContactUs.ENTITY.save(this.ticket());
		assertEquals("the ticket opened", before + 1, curiosity(JsBusinessCuriositiesCharts.Fields.contactUsTypes, suggestion));

		JsEntityContactUs.ENTITY.save(this.ticket());
		assertEquals("a new save of the open ticket is not a new ticket", before + 1, curiosity(JsBusinessCuriositiesCharts.Fields.contactUsTypes, suggestion));

		CcpJsonRepresentation answered = this.ticket().put(JsEntityContactUs.Fields.supportResponse, "Obrigado pela sugestao");
		JsEntityContactUs.ENTITY.delete(answered);
		assertEquals("the answer does not subtract", before + 1, curiosity(JsBusinessCuriositiesCharts.Fields.contactUsTypes, suggestion));

		JsEntityContactUs.ENTITY.save(this.ticket());
		assertEquals("a ticket opened again counts again", before + 2, curiosity(JsBusinessCuriositiesCharts.Fields.contactUsTypes, suggestion));
	}

	@Test
	public void theNewUsersAreCountedByDayMonthGoalAndChannel() {
		String today = new CcpTimeDecorator().getFormattedDateTime("yyyyMMdd");
		String thisMonth = today.substring(0, 6);
		long todayBefore = curiosity(JsBusinessCuriositiesCharts.Fields.newUsersByDay, today);
		long monthBefore = curiosity(JsBusinessCuriositiesCharts.Fields.newUsersByMonth, thisMonth);
		long recruitingBefore = curiosity(JsBusinessCuriositiesCharts.Fields.goals, "recruiting");
		long othersBefore = curiosity(JsBusinessCuriositiesCharts.Fields.channels, "others");

		JsEntityLoginAnswers.ENTITY.save(this.answers());
		assertEquals(todayBefore + 1, curiosity(JsBusinessCuriositiesCharts.Fields.newUsersByDay, today));
		assertEquals(monthBefore + 1, curiosity(JsBusinessCuriositiesCharts.Fields.newUsersByMonth, thisMonth));
		assertEquals(recruitingBefore + 1, curiosity(JsBusinessCuriositiesCharts.Fields.goals, "recruiting"));
		assertEquals(othersBefore + 1, curiosity(JsBusinessCuriositiesCharts.Fields.channels, "others"));
	}

	/** 2026-11-17: the channel crossed with the goal; a save that changes the goal moves the count, the delete subtracts it. */
	@Test
	public void theChannelIsCrossedWithTheGoal() {
		String othersRecruiting = "others|recruiting";
		String othersJobs = "others|jobs";
		long recruitingBefore = curiosity(JsBusinessCuriositiesCharts.Fields.channelGoals, othersRecruiting);
		long jobsBefore = curiosity(JsBusinessCuriositiesCharts.Fields.channelGoals, othersJobs);

		JsEntityLoginAnswers.ENTITY.save(this.answers());
		assertEquals("the new user", recruitingBefore + 1, curiosity(JsBusinessCuriositiesCharts.Fields.channelGoals, othersRecruiting));

		JsEntityLoginAnswers.ENTITY.save(this.answers().put(JsEntityLoginAnswers.Fields.goal, "jobs"));
		assertEquals("the goal changed: it left the old key", recruitingBefore, curiosity(JsBusinessCuriositiesCharts.Fields.channelGoals, othersRecruiting));
		assertEquals("and went to the new one", jobsBefore + 1, curiosity(JsBusinessCuriositiesCharts.Fields.channelGoals, othersJobs));

		JsEntityLoginAnswers.ENTITY.delete(this.answers());
		assertEquals("the delete subtracts", jobsBefore, curiosity(JsBusinessCuriositiesCharts.Fields.channelGoals, othersJobs));
	}
}
