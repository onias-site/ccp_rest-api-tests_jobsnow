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
import com.jb.entities.JbEntityBotAllowedUser;
import com.jb.entities.JbEntityBotCommandExplanation;
import com.jb.entities.JbEntityBotCommandName;
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

		CcpJsonRepresentation name = this.jsonsOf(new JbEntityBotCommandName()).get(0);
		assertEquals("solucionarTicketsDeTokenDeLogin", name.getAsString(JnJsonInstantMessengerFields.message));

		CcpJsonRepresentation explanation = this.jsonsOf(new JbEntityBotCommandExplanation()).get(0);
		assertEquals("solveLoginTokenTicket", explanation.getAsString(JnJsonInstantMessengerFields.commandName));
	}
}
