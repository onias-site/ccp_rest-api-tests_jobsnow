package com.js.business.bots.login.token;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;

import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.bulk.CcpBulkEntityOperationType;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.jn.entities.JnEntityBotCommand;
import com.jn.entities.JnEntityBotCommandStep;
import com.jn.entities.JnEntityInstantMessengerTemplateMessage;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;

/**
 * The seeds of the command {@code solveLoginTokenTicket}: the command in the support bot, its step run by the class name
 * of {@link JsSupportLoginToken}, and one notice template per ticket type, all as {@code create}.
 */
public class JsSupportLoginTokenBotConfigurationTest {

	static {
		// the templates are versionable: their bulk items read the current record
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE,
				CcpApacheMimeHttp.INSTANCE,
				CcpElasticSearchDbRequest.INSTANCE,
				CcpElasticSearchCrud.INSTANCE,
				CcpLocalCacheInstances.mock);
	}

	private final List<CcpBulkItem> seeds = JsSupportLoginTokenBotConfiguration.getSeeds();

	private List<CcpBulkItem> seedsInto(String entityName) {
		return this.seeds.stream().filter(seed -> seed.entity.getEntityMetaData().entityName.equals(entityName)).collect(Collectors.toList());
	}

	@Test
	public void everySeedIsACreate() {
		for (CcpBulkItem seed : this.seeds) {
			assertEquals(seed.toString(), CcpBulkEntityOperationType.create, seed.operation);
		}
	}

	@Test
	public void theCommandIsSeededInTheSupportBot() {
		String entityName = JnEntityBotCommand.ENTITY.getEntityMetaData().entityName;
		List<CcpBulkItem> commands = this.seedsInto(entityName);
		assertEquals(1, commands.size());
		CcpBulkItem command = commands.get(0);
		assertEquals("solveLoginTokenTicket", command.json.getAsString(JnJsonInstantMessengerFields.commandName));
		assertEquals("support", command.json.getAsString(JnJsonInstantMessengerFields.botName));
	}

	@Test
	public void theStepRunsTheLoginTokenSupportByClassName() {
		String entityName = JnEntityBotCommandStep.ENTITY.getEntityMetaData().entityName;
		List<CcpBulkItem> steps = this.seedsInto(entityName);
		assertEquals(1, steps.size());
		String stepAsText = steps.get(0).json.toString();
		assertTrue(stepAsText, stepAsText.contains(JsSupportLoginToken.class.getName()));
	}

	@Test
	public void theNoticeTemplatesOfEveryTicketTypeAreSeeded() {
		String entityName = JnEntityInstantMessengerTemplateMessage.ENTITY.getEntityMetaData().entityName;
		Set<String> seeded = this.seedsInto(entityName).stream()
				.map(seed -> seed.json.getAsString(JnJsonCommonsFields.templateId)).collect(Collectors.toSet());
		for (JsSupportLoginTokenTypes type : JsSupportLoginTokenTypes.values()) {
			String templateId = type.sender.getName();
			assertTrue(type.name(), seeded.contains(templateId));
		}
	}
}
