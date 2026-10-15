package com.jn.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.bulk.CcpBulkEntityOperationType;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.jb.entities.JbEntityBot;
import com.jb.entities.JbEntityBotAllowedUser;
import com.vis.entities.VisEntityCommandNotAllowedToUser;
import com.vis.entities.VisEntitySkillFixHierarchyPending;
import com.vis.entities.VisEntitySkillPending;
import com.jn.entities.JnEntityBotCommandExplanation;
import com.jn.entities.JnEntityBotCommandName;
import com.jb.entities.JbEntityBotExplanation;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;
import com.jn.utils.JnLanguage;

/**
 * Proves the initial loads ({@code getFirstRecordsToInsert}) of the jn and jb entities: every seed is a {@code create}
 * item, and each one carries the configuration its javadoc describes.
 */
public class SeedsOfTheEntitiesTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler(),
				new com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp(),
				new com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest(),
				new com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud(),
				com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances.mock);
	}

	private List<CcpBulkItem> seedsOf(CcpEntityConfigurator configurator) {
		List<CcpBulkItem> seeds = configurator.getFirstRecordsToInsert();
		assertFalse(configurator.getClass().getSimpleName(), seeds.isEmpty());
		for (CcpBulkItem seed : seeds) {
			assertEquals(CcpBulkEntityOperationType.create, seed.operation);
		}
		return seeds;
	}

	/**
	 * The seeds of the entity itself; the others are rows of its associated entities, such as the history of a
	 * versionable entity.
	 */
	private List<CcpJsonRepresentation> jsonsOf(CcpEntityConfigurator configurator) {
		try {
			Object entity = configurator.getClass().getField("ENTITY").get(null);
			String entityName = ((com.ccp.especifications.db.utils.entity.CcpEntity) entity).getEntityMetaData().entityName;
			List<CcpJsonRepresentation> jsons = this.seedsOf(configurator).stream()
					.filter(seed -> seed.entity.getEntityMetaData().entityName.equals(entityName))
					.map(seed -> seed.json).collect(Collectors.toList());
			assertFalse(entityName, jsons.isEmpty());
			return jsons;
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException(e);
		}
	}

	@Test
	public void theNonProfessionalDomainsAreSeededInPortuguese() {
		List<CcpJsonRepresentation> seeds = this.jsonsOf(new JnEntitySystemMessage());

		CcpJsonRepresentation domains = seeds.get(0);
		assertEquals(JnLanguage.portuguese.name(), domains.getAsString(JnJsonCommonsFields.language));
		String text = domains.getAsStringList(JnJsonCommonsFields.message).toString();
		assertTrue(text, text.contains("gmail.com"));
		assertTrue(text, text.contains("hotmail.com"));
	}

	@Test
	public void theSupportBotSettingsSendToTheSupportChatWithTenTries() {
		List<CcpJsonRepresentation> seeds = this.jsonsOf(new JnEntityInstantMessengerParametersToSend());

		for (CcpJsonRepresentation seed : seeds) {
			assertEquals(751717896L, (long) seed.getAsLongNumber(JnJsonInstantMessengerFields.chatId));
			assertEquals("support", seed.getAsString(JnJsonInstantMessengerFields.botName));
			CcpJsonRepresentation moreParameters = seed.getInnerJson(JnJsonCommonsFields.moreParameters);
			assertEquals(10, (int) moreParameters.getAsIntegerNumber(JnJsonCommonsFields.maxTriesToSendMessage));
			assertEquals(3000, (int) moreParameters.getAsIntegerNumber(JnJsonCommonsFields.sleepToSendMessage));
		}
	}

	@Test
	public void theEmailTemplatesComeInPortugueseAndEnglish() {
		List<String> languages = this.jsonsOf(new JnEntityEmailTemplateMessage()).stream()
				.map(seed -> seed.getAsString(JnJsonCommonsFields.language)).distinct().sorted().collect(Collectors.toList());

		assertEquals(Arrays.asList("english", "portuguese"), languages);
	}

	@Test
	public void theEmailParametersAreSeeded() {
		for (CcpJsonRepresentation seed : this.jsonsOf(new JnEntityEmailParametersToSend())) {
			assertFalse(seed.toString(), seed.getAsString(JnJsonCommonsFields.templateId).isEmpty());
		}
	}

	@Test
	public void theSupportBotSeedsAreLoaded() {
		CcpJsonRepresentation allowed = this.jsonsOf(new JbEntityBotAllowedUser()).get(0);
		assertEquals(Arrays.asList("751717896"), allowed.getAsStringList(JbEntityBotAllowedUser.Fields.allowedUser).stream()
				.map(id -> "" + Double.valueOf(id).longValue()).collect(Collectors.toList()));

		List<String> bots = this.jsonsOf(new JbEntityBotExplanation()).stream()
				.map(seed -> seed.getAsString(JnJsonInstantMessengerFields.botName)).distinct().sorted().collect(Collectors.toList());
		assertEquals(Arrays.asList("support", "user"), bots);

		// since 2026-10-15 the bot entities are in jn and have no seeds of their own: jb seeds its commands through JbEntityBot
		JbEntityBot jbBot = new JbEntityBot();
		CcpJsonRepresentation name = this.seedsInto(jbBot, JnEntityBotCommandName.ENTITY).get(0);
		assertEquals("solucionarTicketsDeTokenDeLogin", name.getAsString(JnJsonInstantMessengerFields.message));

		CcpJsonRepresentation explanation = this.seedsInto(jbBot, JnEntityBotCommandExplanation.ENTITY).get(0);
		assertEquals("solveLoginTokenTicket", explanation.getAsString(JnJsonInstantMessengerFields.commandName));
	}

	/**
	 * jb seeds only its own commands and vis seeds the four of its own, all in the support bot: jb no longer imports vis
	 * (2026-10-15), and the bot finds the commands of both by botName.
	 */
	@Test
	public void eachCostCenterSeedsItsOwnCommandsOfTheSupportBot() {
		List<String> jbCommands = this.commandsSeededBy(new JbEntityBot());
		assertEquals(Arrays.asList("pendingTickets", "solveLoginTokenTicket"), jbCommands);

		List<String> visCommands = new java.util.ArrayList<>();
		visCommands.addAll(this.commandsSeededBy(new VisEntitySkillPending()));
		visCommands.addAll(this.commandsSeededBy(new VisEntitySkillFixHierarchyPending()));
		visCommands.addAll(this.commandsSeededBy(new VisEntityCommandNotAllowedToUser()));
		visCommands.sort(null);
		assertEquals(Arrays.asList("allowCommandToUser", "fixSkillHierarchy", "ignoredUsers", "reviewSkillSuggestion"), visCommands);
	}

	/** The records that the seeds of the configurator write into another entity. */
	private List<CcpJsonRepresentation> seedsInto(CcpEntityConfigurator configurator, CcpEntity entity) {
		String entityName = entity.getEntityMetaData().entityName;
		List<CcpJsonRepresentation> jsons = this.seedsOf(configurator).stream()
				.filter(seed -> seed.entity.getEntityMetaData().entityName.equals(entityName))
				.map(seed -> seed.json).collect(Collectors.toList());
		assertFalse(entityName, jsons.isEmpty());
		return jsons;
	}

	/** The commands of the support bot that the configurator seeds, sorted. */
	private List<String> commandsSeededBy(CcpEntityConfigurator configurator) {
		List<CcpJsonRepresentation> commands = this.seedsInto(configurator, JnEntityBotCommand.ENTITY);
		for (CcpJsonRepresentation command : commands) {
			assertEquals(command.toString(), "support", command.getAsString(JnJsonInstantMessengerFields.botName));
		}
		return commands.stream().map(command -> command.getAsString(JnJsonInstantMessengerFields.commandName)).sorted().collect(Collectors.toList());
	}
}
