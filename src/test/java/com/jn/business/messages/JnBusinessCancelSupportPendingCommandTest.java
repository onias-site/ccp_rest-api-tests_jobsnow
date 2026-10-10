package com.jn.business.messages;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
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

/**
 * The edges of {@link JnBusinessCancelSupportPendingCommand}; the cancellation itself, from the withdrawal in the
 * screen to the list of the operator, is exercised by {@code SkillSuggestionThroughSupportBotTest} and
 * {@code SkillFixHierarchyThroughSupportBotTest}.
 */
public class JnBusinessCancelSupportPendingCommandTest {

	@BeforeClass
	public static void loadDependencies() {
		CcpDependencyInjection.removeAllDependencies();
		CcpDependencyInjection.loadAllDependencies(
				CcpLocalInstances.syncMensageriaListener,
				new CcpElasticSearchDbRequest(),
				new CcpMindrotPasswordHandler(),
				CcpLocalCacheInstances.mock,
				new CcpElasticSerchDbBulk(),
				new CcpElasticSearchCrud(),
				new CcpElasticSearchQueryExecutor(),
				new CcpGsonJsonHandler(),
				new CcpApacheMimeHttp()
		);
	}

	/** A template that sends nothing to an instant messenger sent no command, so there is no ticket to cancel. */
	@Test
	public void aTemplateWithoutInstantMessageCancelsNothing() {
		CcpJsonRepresentation values = CcpOtherConstants.EMPTY_JSON.put(JnJsonCommonsFields.email, "sem.ticket@teste.com");
		CcpJsonRepresentation valuesWithTemplateId = values.put(JnJsonCommonsFields.templateId, "com.jn.template.que.nao.existe");

		CcpJsonRepresentation result = JnBusinessCancelSupportPendingCommand.INSTANCE.execute(valuesWithTemplateId);

		assertEquals(valuesWithTemplateId.content, result.content);
	}

	@Test(expected = CcpNullParameterException.class)
	public void cancelTemplateIdNullTest() {
		JnBusinessCancelSupportPendingCommand.INSTANCE.cancel(null, CcpOtherConstants.EMPTY_JSON);
	}

	@Test(expected = CcpNullParameterException.class)
	public void cancelValuesNullTest() {
		JnBusinessCancelSupportPendingCommand.INSTANCE.cancel("com.jn.template.que.nao.existe", null);
	}
}
