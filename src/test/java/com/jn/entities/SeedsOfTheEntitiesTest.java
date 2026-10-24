package com.jn.entities;

import com.js.entities.JsEntityContactUs;
import com.jn.entities.JnEntityWriteEventSubscribers;
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
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.jb.entities.JbEntityBot;
import com.jb.entities.JbEntityBotAllowedUser;
import com.jb.entities.JbEntityBotExplanation;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;
import com.jn.utils.JnLanguage;
import com.js.business.login.JsBusinessValidateLogin;
import com.js.entities.JsEntityLoginSessionValidation;
import com.js.entities.JsEntityLoginTokenRequestUnlock;
import com.jn.entities.JnEntityCommandNotAllowedToUser;
import com.vis.entities.VisEntitySkillFixHierarchyPending;
import com.vis.entities.VisEntitySkillPending;

/**
 * Proves the initial loads ({@code getFirstRecordsToInsert}) of the jn and jb entities: every seed is a {@code create}
 * item, and each one carries the configuration its javadoc describes.
 */
public class SeedsOfTheEntitiesTest {

	static {
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE,
				com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp.INSTANCE,
				com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest.INSTANCE,
				com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud.INSTANCE,
				com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances.mock);
	}

	private List<CcpBulkItem> seedsOf(CcpEntityConfigurator configurator) {
		List<CcpBulkItem> seeds = configurator.getFirstRecordsToInsert();
		assertFalse(configurator.getClass().getSimpleName(), seeds.isEmpty());
		for (CcpBulkItem seed : seeds) {
			// the only seed that is not a create is the one that appends a subscriber to a write event
			boolean scripted = seed.operation != CcpBulkEntityOperationType.create;
			if(scripted) {
				assertEquals(JnEntityWriteEventSubscribers.ENTITY.getEntityMetaData().entityName, seed.entity.getEntityMetaData().entityName);
				continue;
			}
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
		List<CcpJsonRepresentation> seeds = this.jsonsOf(JnEntitySystemMessage.INSTANCE);

		CcpJsonRepresentation domains = seeds.get(0);
		assertEquals(JnLanguage.portuguese.name(), domains.getAsString(JnJsonCommonsFields.language));
		String text = domains.getAsStringList(JnJsonCommonsFields.message).toString();
		assertTrue(text, text.contains("gmail.com"));
		assertTrue(text, text.contains("hotmail.com"));
	}

	@Test
	public void theSupportBotSettingsSendToTheSupportChatWithTenTries() {
		List<CcpJsonRepresentation> seeds = this.jsonsOf(JnEntityInstantMessengerParametersToSend.INSTANCE);

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
		List<String> languages = this.jsonsOf(JnEntityEmailTemplateMessage.INSTANCE).stream()
				.map(seed -> seed.getAsString(JnJsonCommonsFields.language)).distinct().sorted().collect(Collectors.toList());

		assertEquals(Arrays.asList("english", "portuguese"), languages);
	}

	@Test
	public void theEmailParametersAreSeeded() {
		for (CcpJsonRepresentation seed : this.jsonsOf(JnEntityEmailParametersToSend.INSTANCE)) {
			assertFalse(seed.toString(), seed.getAsString(JnJsonCommonsFields.templateId).isEmpty());
		}
	}

	@Test
	public void theSupportBotSeedsAreLoaded() {
		CcpJsonRepresentation allowed = this.jsonsOf(JbEntityBotAllowedUser.INSTANCE).get(0);
		assertEquals(Arrays.asList("751717896"), allowed.getAsStringList(JbEntityBotAllowedUser.Fields.allowedUser).stream()
				.map(id -> "" + Double.valueOf(id).longValue()).collect(Collectors.toList()));

		List<String> bots = this.jsonsOf(JbEntityBotExplanation.INSTANCE).stream()
				.map(seed -> seed.getAsString(JnJsonInstantMessengerFields.botName)).distinct().sorted().collect(Collectors.toList());
		assertEquals(Arrays.asList("support", "user"), bots);

		// since 2026-10-15 the bot entities are in jn and have no seeds of their own: jb seeds its commands through
		// JbEntityBot and, since 2026-10-22, js seeds the command of the login tokens through JsEntityLoginTokenRequestUnlock
		JsEntityLoginTokenRequestUnlock jsUnlock = JsEntityLoginTokenRequestUnlock.INSTANCE;
		List<String> names = this.seedsInto(jsUnlock, JnEntityBotCommandName.ENTITY).stream()
				.map(seed -> seed.getAsString(JnJsonInstantMessengerFields.message)).collect(Collectors.toList());
		assertEquals(Arrays.asList("solucionarTicketsDeTokenDeLogin"), names);

		List<String> explained = this.seedsInto(jsUnlock, JnEntityBotCommandExplanation.ENTITY).stream()
				.map(seed -> seed.getAsString(JnJsonInstantMessengerFields.commandName)).distinct().collect(Collectors.toList());
		assertEquals(Arrays.asList("solveLoginTokenTicket"), explained);
	}

	/**
	 * jb, js and vis seed only their own commands, all in the support bot: jb imports neither vis (2026-10-15) nor js
	 * (2026-10-22), and the bot finds the commands of all of them by botName.
	 */
	@Test
	public void eachCostCenterSeedsItsOwnCommandsOfTheSupportBot() {
		List<String> jbCommands = this.commandsSeededBy(JbEntityBot.INSTANCE);
		assertEquals(Arrays.asList("pendingTickets"), jbCommands);

		List<String> jsCommands = new java.util.ArrayList<>();
		jsCommands.addAll(this.commandsSeededBy(JsEntityLoginTokenRequestUnlock.INSTANCE));
		jsCommands.addAll(this.commandsSeededBy(JsEntityContactUs.INSTANCE));
		jsCommands.sort(null);
		assertEquals(Arrays.asList("answerContactUs", "ignoredContactUs", "solveLoginTokenTicket"), jsCommands);

		List<String> visCommands = new java.util.ArrayList<>();
		visCommands.addAll(this.commandsSeededBy(VisEntitySkillPending.INSTANCE));
		visCommands.addAll(this.commandsSeededBy(VisEntitySkillFixHierarchyPending.INSTANCE));
		visCommands.addAll(this.commandsSeededBy(JnEntityCommandNotAllowedToUser.INSTANCE));
		visCommands.sort(null);
		assertEquals(Arrays.asList("allowCommandToUser", "fixSkillHierarchy", "ignoredUsers", "reviewSkillSuggestion"), visCommands);
	}

	/** js seeds the validator of the session that the vis API runs by class name, without importing js (2026-10-22). */
	@Test
	public void jsSeedsTheValidatorOfTheSession() {
		List<CcpJsonRepresentation> validators = this.seedsInto(JsEntityLoginSessionValidation.INSTANCE, JnEntitySessionValidator.ENTITY);
		assertEquals(1, validators.size());
		CcpJsonRepresentation validator = validators.get(0);
		assertEquals("userSession", validator.getAsString(JnEntitySessionValidator.Fields.validatorName));
		assertEquals(JsBusinessValidateLogin.class.getName(), validator.getAsString(JnEntitySessionValidator.Fields.business));
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
