package com.jn.business.bots;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;

import com.ccp.business.CcpBusiness;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpReflectionConstructorDecorator;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.jb.entities.JbEntityBot;
import com.jn.entities.JnEntityBotCommand;
import com.jn.entities.JnEntityBotCommandName;
import com.jn.entities.JnEntityBotCommandStep;
import com.jn.entities.JnEntitySystemMessage;
import com.jn.entities.subfields.JnNextStepFields;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;
import com.jn.messages.JnSystemMessage;
import com.jn.utils.JnLanguage;
import com.vis.business.bots.VisCommandNamesInPortuguese;
import com.vis.business.bots.VisSupportBotCommands;
import com.vis.business.bots.ignored.users.VisSupportIgnoredUsersMessages;
import com.vis.business.bots.skill.hierarchy.VisSupportSkillFixHierarchyMessages;
import com.vis.business.bots.skill.suggestion.VisSupportSkillSuggestionMessages;
import com.vis.entities.VisEntityCommandNotAllowedToUser;
import com.vis.entities.VisEntitySkillFixHierarchyPending;
import com.vis.entities.VisEntitySkillPending;

/**
 * Since 2026-10-15 the bot runs the step of a command by the class name seeded in {@code jn_bot_command_step}, and the
 * cost center that owns the command (jb or vis) seeds it: the compiler no longer checks those names. This test does, over
 * the seeds of every cost center together, as the bot reads them: every engine is a {@code CcpBusiness} the bot can
 * instantiate, every step a flow goes to exists, every command starts in a step of its own name, no command is seeded
 * twice, and every text of the vis conversations is seeded in Portuguese and English.
 */
public class BotConfigurationSeedsIntegrityTest {

	static {
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE,
				com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp.INSTANCE,
				com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest.INSTANCE,
				com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud.INSTANCE,
				com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances.mock);
	}

	/** The seeds of every entity that adds commands to the bots. */
	private final List<CcpBulkItem> allSeeds = this.loadAllSeeds();

	private List<CcpBulkItem> loadAllSeeds() {
		List<CcpBulkItem> seeds = new ArrayList<>();
		seeds.addAll(JbEntityBot.INSTANCE.getFirstRecordsToInsert());
		seeds.addAll(VisEntitySkillPending.INSTANCE.getFirstRecordsToInsert());
		seeds.addAll(VisEntitySkillFixHierarchyPending.INSTANCE.getFirstRecordsToInsert());
		seeds.addAll(VisEntityCommandNotAllowedToUser.INSTANCE.getFirstRecordsToInsert());
		return seeds;
	}

	private List<CcpJsonRepresentation> recordsOf(CcpEntity entity) {
		String entityName = entity.getEntityMetaData().entityName;
		List<CcpJsonRepresentation> records = this.allSeeds.stream()
				.filter(seed -> seed.entity.getEntityMetaData().entityName.equals(entityName))
				.map(seed -> seed.json).collect(Collectors.toList());
		assertFalse(entityName, records.isEmpty());
		return records;
	}

	private Set<String> stepNames() {
		Set<String> stepNames = this.recordsOf(JnEntityBotCommandStep.ENTITY).stream()
				.map(step -> step.getAsString(JnJsonInstantMessengerFields.stepName)).collect(Collectors.toSet());
		return stepNames;
	}

	/** Instantiated as {@code BotCommandStep.loadEngine} does it. */
	@Test
	public void everyEngineIsABusinessTheBotCanInstantiate() {
		for (CcpJsonRepresentation step : this.recordsOf(JnEntityBotCommandStep.ENTITY)) {
			String engineName = step.getAsString(JnEntityBotCommandStep.Fields.engine);
			CcpReflectionConstructorDecorator reflection = new CcpStringDecorator(engineName).reflection();
			assertTrue(engineName, reflection.thisClassExists());
			Object instance = reflection.newInstance();
			assertTrue(engineName, instance instanceof CcpBusiness);
		}
	}

	@Test
	public void everyStepThatAFlowGoesToExists() {
		Set<String> stepNames = this.stepNames();
		for (CcpJsonRepresentation step : this.recordsOf(JnEntityBotCommandStep.ENTITY)) {
			String stepName = step.getAsString(JnJsonInstantMessengerFields.stepName);
			boolean hasNextStep = step.containsField(JnEntityBotCommandStep.Fields.nextStep);
			if (hasNextStep) {
				String nextStep = step.getAsString(JnEntityBotCommandStep.Fields.nextStep);
				assertTrue(stepName + " -> " + nextStep, stepNames.contains(nextStep));
			}
			for (CcpJsonRepresentation flow : step.getAsJsonList(JnEntityBotCommandStep.Fields.stepFlow)) {
				boolean flowHasNextStep = flow.containsField(JnNextStepFields.nextStep);
				if (flowHasNextStep) {
					String flowNextStep = flow.getAsString(JnNextStepFields.nextStep);
					assertTrue(stepName + " (flow) -> " + flowNextStep, stepNames.contains(flowNextStep));
				}
			}
		}
	}

	@Test
	public void everyCommandStartsInAStepOfItsOwnNameAndIsSeededOnce() {
		Set<String> stepNames = this.stepNames();
		List<String> commandNames = this.recordsOf(JnEntityBotCommand.ENTITY).stream()
				.map(command -> command.getAsString(JnJsonInstantMessengerFields.commandName)).collect(Collectors.toList());
		Set<String> distinctCommandNames = new HashSet<>(commandNames);
		assertEquals("a command seeded twice: " + commandNames, commandNames.size(), distinctCommandNames.size());
		for (String commandName : commandNames) {
			assertTrue(commandName, stepNames.contains(commandName));
		}
	}

	@Test
	public void everyVisCommandHasItsNameInPortuguese() {
		Set<String> commandsWithAName = this.recordsOf(JnEntityBotCommandName.ENTITY).stream()
				.map(name -> name.getAsString(JnJsonInstantMessengerFields.commandName)).collect(Collectors.toSet());
		for (VisSupportBotCommands command : VisSupportBotCommands.values()) {
			String commandName = command.name();
			assertTrue(commandName, commandsWithAName.contains(commandName));
			VisCommandNamesInPortuguese nameInPortuguese = VisCommandNamesInPortuguese.valueOf(commandName);
			assertFalse(commandName, nameInPortuguese.getValue().isEmpty());
		}
	}

	@Test
	public void everyTextOfTheVisConversationsIsSeededInPortugueseAndEnglish() {
		Set<String> seededTexts = this.recordsOf(JnEntitySystemMessage.ENTITY).stream()
				.map(text -> text.getAsString(JnEntitySystemMessage.Fields.systemMessageName) + "/" + text.getAsString(JnJsonCommonsFields.language))
				.collect(Collectors.toSet());
		List<JnSystemMessage> texts = new ArrayList<>();
		texts.addAll(java.util.Arrays.asList(VisSupportSkillSuggestionMessages.values()));
		texts.addAll(java.util.Arrays.asList(VisSupportSkillFixHierarchyMessages.values()));
		texts.addAll(java.util.Arrays.asList(VisSupportIgnoredUsersMessages.values()));
		for (JnSystemMessage text : texts) {
			String name = text.getSystemMessageName();
			assertTrue(name + " in Portuguese", seededTexts.contains(name + "/" + JnLanguage.portuguese.name()));
			assertTrue(name + " in English", seededTexts.contains(name + "/" + JnLanguage.english.name()));
		}
	}
}
