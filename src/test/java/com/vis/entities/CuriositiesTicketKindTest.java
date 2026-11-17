package com.vis.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.dependency.injection.CcpInstanceProvider;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityMetaData;
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
import com.jn.utils.JnGroupers;
import com.vis.json.transformers.VisInvalidLinkedinTicketKindKeys;
import com.vis.json.transformers.VisSkillFixHierarchyTicketKindKeys;
import com.vis.json.transformers.VisSkillPendingTicketKindKeys;

/**
 * 2026-11-17: the tickets of vis (requests of hierarchy, suggestions of skill and reports of invalid LinkedIn) counted by
 * kind, a history, and merged into the chart of tickets of the screen Curiosities with the tickets of the contact us.
 */
public class CuriositiesTicketKindTest {
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
	}

	private static final String INVALID_LINKEDIN = "invalidLinkedin";

	private final long now = System.currentTimeMillis();

	private final String reporterEmail = "ticketkind" + this.now + "@empresa" + this.now + ".com.br";

	private final String linkedinAddress = "https://www.linkedin.com/in/ticketkind" + this.now;

	private CcpJsonRepresentation reportKey() {
		return CcpOtherConstants.EMPTY_JSON
				.put(VisEntityInvalidLinkedinReport.Fields.email, this.reporterEmail)
				.put(VisEntityInvalidLinkedinReport.Fields.linkedinAddress, this.linkedinAddress);
	}

	private CcpJsonRepresentation report() {
		return this.reportKey().put(VisEntityInvalidLinkedinReport.Fields.resumeEmailSha1, "sha1doticketkind" + this.now);
	}

	@After
	public void tearDown() {
		VisEntityInvalidLinkedinReport.ENTITY.deleteAnyWhere(this.reportKey());
	}

	private static long countOf(CcpEntity childEntity, String key) {
		CcpEntityMetaData parentDetails = VisEntityVirtualTicketKindCounter.ENTITY.getEntityMetaData();
		CcpEntityMetaData childDetails = childEntity.getEntityMetaData();
		CcpJsonRepresentation combination = VisEntityVirtualTicketKindCounter.getCombination();
		String parentId = VisEntityVirtualTicketKindCounter.ENTITY.calculateId(combination);
		CcpJsonRepresentation groupKey = JnGroupers.getGroupKey(parentDetails.entityName, childDetails.entityName, parentId);
		List<List<String>> childrenOfEachGroup = JnGroupers.getChildrenOfEachGroup(Arrays.asList(groupKey));
		List<String> children = childrenOfEachGroup.get(0);
		if (children.isEmpty()) {
			return 0L;
		}
		CcpJsonRepresentation counters = new CcpJsonRepresentation(children.get(0));
		CcpFieldName keyField = new CcpFieldName(key);
		return counters.containsAllFields(keyField) ? counters.getAsLongNumber(keyField) : 0L;
	}

	private static long reports() {
		return countOf(VisEntityInvalidLinkedinReport.ENTITY, INVALID_LINKEDIN);
	}

	@Test
	public void eachTicketHasTheKindOfTheComboOfTheTickets() {
		CcpJsonRepresentation association = CcpOtherConstants.EMPTY_JSON.put(VisEntitySkillFixHierarchyPending.Fields.type, "add");
		CcpJsonRepresentation dissociation = CcpOtherConstants.EMPTY_JSON.put(VisEntitySkillFixHierarchyPending.Fields.type, "remove");
		assertEquals("hierarchyAdd", VisSkillFixHierarchyTicketKindKeys.INSTANCE.apply(association));
		assertEquals("hierarchyRemove", VisSkillFixHierarchyTicketKindKeys.INSTANCE.apply(dissociation));
		assertEquals("skillSuggestion", VisSkillPendingTicketKindKeys.INSTANCE.apply(CcpOtherConstants.EMPTY_JSON));
		assertEquals(INVALID_LINKEDIN, VisInvalidLinkedinTicketKindKeys.INSTANCE.apply(CcpOtherConstants.EMPTY_JSON));
	}

	@Test
	public void theTicketsAreAHistory() {
		long before = reports();

		VisEntityInvalidLinkedinReport.ENTITY.save(this.report());
		assertEquals("the ticket opened", before + 1, reports());

		VisEntityInvalidLinkedinReport.ENTITY.save(this.report());
		assertEquals("a new save of the open ticket is not a new ticket", before + 1, reports());

		CcpJsonRepresentation solved = this.report().put(VisEntityInvalidLinkedinReport.Fields.supportResponse, "Endereco corrigido pelo dono");
		VisEntityInvalidLinkedinReport.ENTITY.delete(solved);
		assertEquals("the answer does not subtract", before + 1, reports());
	}

	@Test
	public void theScreenCuriositiesMergesTheTicketsOfVisWithTheContactUs() {
		VisEntityInvalidLinkedinReport.ENTITY.save(this.report());

		CcpJsonRepresentation screen = CcpOtherConstants.EMPTY_JSON.put(JnBusinessGetGrouperCharts.Fields.screen, "curiosities");
		CcpJsonRepresentation charts = JnBusinessGetGrouperCharts.INSTANCE.execute(screen);
		CcpJsonRepresentation tickets = charts.getInnerJson(new CcpFieldName("contactUsTypes"));
		assertTrue(tickets.getAsLongNumber(new CcpFieldName(INVALID_LINKEDIN)) >= 1);
	}
}
