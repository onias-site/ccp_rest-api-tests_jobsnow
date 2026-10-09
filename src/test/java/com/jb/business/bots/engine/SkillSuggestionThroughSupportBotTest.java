package com.jb.business.bots.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.dependency.injection.CcpInstanceProvider;
import com.ccp.especifications.cache.CcpCacheDecorator;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator;
import com.ccp.especifications.email.CcpEmailSender;
import com.ccp.especifications.instant.messenger.CcpInstantMessenger;
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
import com.ccp.process.CcpProcessStatus;
import com.ccp.process.CcpProcessStatusDefault;
import com.jb.entities.JbEntityBot;
import com.jb.entities.JbEntityBotCommand;
import com.jb.entities.JbEntityBotCommandStep;
import com.jb.entities.JbEntityBotCommandStepEndMessage;
import com.jb.entities.JbEntityBotCommandStepSession;
import com.jb.entities.JbEntityPendingTickets;
import com.jn.entities.JnEntitySupportPendingCommand;
import com.jn.business.messages.JnInstantMessageType;
import com.jn.entities.JnEntityEmailMessageSent;
import com.jn.entities.JnEntityInstantMessengerMessageSent;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;
import com.vis.entities.VisEntityCommandNotAllowedToUser;
import com.vis.entities.VisEntityGroupPositionsBySkills;
import com.vis.entities.VisEntitySkill;
import com.vis.entities.VisEntitySkillApproved;
import com.vis.entities.VisEntitySkillFixHierarchyPending;
import com.vis.entities.VisEntitySkillPending;
import com.vis.entities.VisEntitySkillRejected;
import com.vis.json.fields.validation.VisJsonCommonsFields;
import com.vis.json.fields.validation.VisSkillFixHierarchyTypes;
import com.vis.json.fields.validation.VisUserRequestCommands;
import com.vis.messages.VisMessages;
import com.vis.services.VisServiceSkillFixHierarchy;
import com.vis.services.VisServiceSkillSuggestion;
import com.vis.status.VisProcessStatusFixSkillHierarchy;
import com.vis.status.VisProcessStatusSuggestSkill;

/**
 * Exercises the whole circuit of a skill suggestion: the candidate's suggestion notifies the support bot operator
 * with {@code /reviewSkillSuggestion <email> <skill>}, the operator runs that command, reads the skill, its synonyms
 * and the candidate's justification, and approves it, rejects it (always with a justification) or ignores the
 * candidate; the suggestion moves to the approved or rejected entity, an approved skill enters {@link VisEntitySkill}
 * and the candidate gets an email with the operator's justification.
 *
 * <p>The database is the real Elasticsearch (the bot configuration is read from it); Telegram and the e-mail
 * provider are test doubles, as in {@link SkillFixHierarchyThroughSupportBotTest}.
 */
public class SkillSuggestionThroughSupportBotTest {

	private static final FakeInstantMessenger TELEGRAM = new FakeInstantMessenger();

	private static final FakeEmailSender EMAIL_INBOX = new FakeEmailSender();

	static {
		CcpInstanceProvider<CcpInstantMessenger> telegram = () -> TELEGRAM;
		CcpInstanceProvider<CcpEmailSender> email = () -> EMAIL_INBOX;

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
				new CcpApacheMimeHttp(),
				telegram,
				email
		);
	}

	private static final Long SUPPORT_CHAT = 751717896L;

	private static final String APPROVED_USER = "sugestao.aprovada@teste.com";

	private static final String REJECTED_USER = "sugestao.rejeitada@teste.com";

	private static final String IGNORED_USER = "sugestao.usuario.ignorado@teste.com";

	private static final String WITHDRAWN_USER = "sugestao.retirada@teste.com";

	private static final String SERVICE_USER = "sugestao.servico@teste.com";

	/**
	 * Saves the bot, its commands, their steps, their end messages and the message templates of the suggestion again
	 * so that the index follows what is declared in java (the create operation of the initial records does not
	 * overwrite what is already there).
	 */
	@BeforeClass
	public static void resaveBotConfiguration() {
		List<CcpEntityConfigurator> configurators = Arrays.asList(new JbEntityBot(), new JbEntityBotCommand(), new JbEntityBotCommandStep(), new JbEntityBotCommandStepEndMessage(), new VisEntitySkillPending());
		for (CcpEntityConfigurator configurator : configurators) {
			List<CcpBulkItem> records = configurator.getFirstRecordsToInsert();
			for (CcpBulkItem record : records) {
				record.entity.save(record.json);
			}
		}
	}

	@Before
	public void clearTheBotSession() {
		CcpJsonRepresentation session = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonInstantMessengerFields.botName, JbBotType.support)
				.put(JnJsonInstantMessengerFields.chatId, SUPPORT_CHAT);
		JbEntityBotCommandStepSession.ENTITY.delete(session);
		TELEGRAM.sentMessages.clear();
		EMAIL_INBOX.sentEmails.clear();
	}

	/**
	 * The operator approves a skill with spaces in its name: the suggestion goes to the approved entity, the skill
	 * enters {@link VisEntitySkill} with the suggested synonyms and the candidate gets the approval email.
	 */
	@Test
	public void operatorApprovesTheSkill() {

		String skill = "TESTE SKILL APROVADA";
		this.deleteSkill(skill);

		try {
			this.openSuggestion(APPROVED_USER, skill, "Uso esta ferramenta em todos os projetos citados", "TESTE SINONIMO A", "TESTE SINONIMO B");

			String notice = TELEGRAM.lastMessageFor(SUPPORT_CHAT).getAsString(JnJsonCommonsFields.message);
			assertEquals("/reviewSkillSuggestion " + APPROVED_USER + " " + skill, notice);

			// the notice to the operator is a command, so it enters the inbox of /pendingTickets
			String ticket = "/reviewSkillSuggestion " + APPROVED_USER + " " + skill;
			assertTrue(this.isPendingTicket(ticket));

			String request = this.operatorTypes("/reviewSkillSuggestion " + APPROVED_USER + " " + skill);
			assertTrue(request, request.contains("Habilidade: " + skill));
			assertTrue(request, request.contains("Sinônimos: TESTE SINONIMO A, TESTE SINONIMO B"));
			assertTrue(request, request.contains("Uso esta ferramenta em todos os projetos citados"));
			assertTrue(request, request.contains("aprovar <justificativa>"));

			String tooShort = this.operatorTypes("aprovar ok");
			assertTrue(tooShort, tooShort.startsWith("Não entendi a resposta"));

			String finished = this.operatorTypes("aprovar ferramenta relevante para o mercado");
			assertTrue(finished, finished.startsWith("A habilidade " + skill + " sugerida por " + APPROVED_USER + " foi aprovada"));
			// the session ended, so the ticket left /pendingTickets
			assertFalse(this.isPendingTicket(ticket));

			CcpJsonRepresentation suggestionKey = this.suggestion(APPROVED_USER, skill);
			assertFalse(VisEntitySkillPending.ENTITY.exists(suggestionKey));
			assertTrue(VisEntitySkillApproved.ENTITY.exists(suggestionKey));

			CcpJsonRepresentation approved = VisEntitySkillApproved.ENTITY.getOneById(suggestionKey);
			assertEquals("ferramenta relevante para o mercado", approved.getAsString(JnJsonCommonsFields.explanation));

			CcpJsonRepresentation skillKey = CcpOtherConstants.EMPTY_JSON.put(VisEntitySkill.Fields.skill, skill);
			assertTrue(VisEntitySkill.ENTITY.exists(skillKey));
			CcpJsonRepresentation skillRecord = VisEntitySkill.ENTITY.getOneById(skillKey);
			assertEquals(Arrays.asList("TESTE SINONIMO A", "TESTE SINONIMO B"), skillRecord.getAsStringList(VisEntitySkill.Fields.synonym));
			assertTrue(skillRecord.toString(), skillRecord.getAsLongNumber(VisEntitySkill.Fields.ranking) > 0);

			CcpJsonRepresentation email = this.emailSentTo(APPROVED_USER, VisMessages.VisNotifyUserAboutAprovedSkill.class);
			String body = email.getAsString(JnJsonCommonsFields.message);
			assertTrue(body, body.contains("a habilidade " + skill + " que você sugeriu foi aprovada"));
			assertTrue(body, body.contains("ferramenta relevante para o mercado"));

			// once decided, the suggestion is no longer pending for the operator
			String again = this.operatorTypes("/reviewSkillSuggestion " + APPROVED_USER + " " + skill);
			assertTrue(again, again.startsWith("Não há sugestão pendente"));

			// the skill and its synonyms enter the lookup used to read skills from a resume, so the approved skill is
			// found in resumes and its synonyms can no longer be suggested as new skills
			assertEquals(0, VisEntityGroupPositionsBySkills.getWordStatus(skill));
			assertEquals(0, VisEntityGroupPositionsBySkills.getWordStatus("TESTE SINONIMO B"));
			CcpJsonRepresentation synonymSuggestion = this.newSuggestion(this.suggestion(SERVICE_USER, "TESTE SINONIMO B"), "O sinônimo da skill aprovada como skill nova");
			CcpProcessStatus synonymRefused = this.statusOf(() -> VisServiceSkillSuggestion.SuggestSkill.execute(synonymSuggestion));
			assertEquals(VisProcessStatusSuggestSkill.skillAlreadyExists, synonymRefused);
		} finally {
			this.deleteSkill(skill);
			this.removeFromTheWordsGroup(skill, "TESTE SINONIMO A", "TESTE SINONIMO B");
		}
	}

	/**
	 * A word the system already knows as a synonym of a skill (WEM, synonym of VCM) or as a skill (PRIMEFACES) is
	 * refused with {@code skillAlreadyExists} (412), even when the call does not come from the screen, which already
	 * refuses it; a word that is only a piece of a skill (PRIMEFACE) is not a known word and is accepted.
	 */
	@Test
	public void serviceRefusesWordsKnownAsSynonyms() {

		for (String knownWord : Arrays.asList("WEM", "PRIMEFACES")) {
			CcpJsonRepresentation suggestionKey = this.suggestion(SERVICE_USER, knownWord);
			CcpJsonRepresentation knownSuggestion = this.newSuggestion(suggestionKey, "Esta palavra já é conhecida pelo sistema");
			CcpProcessStatus refusal = this.statusOf(() -> VisServiceSkillSuggestion.SuggestSkill.execute(knownSuggestion));
			assertEquals(knownWord, VisProcessStatusSuggestSkill.skillAlreadyExists, refusal);
			assertFalse(knownWord, VisEntitySkillPending.ENTITY.exists(suggestionKey));
		}

		this.openSuggestion(SERVICE_USER, "PRIMEFACE", "Só um pedaço de PRIMEFACES, que o servidor aceita");
		assertTrue(VisEntitySkillPending.ENTITY.exists(this.suggestion(SERVICE_USER, "PRIMEFACE")));
		VisServiceSkillSuggestion.DeleteSkillSuggestion.execute(this.suggestion(SERVICE_USER, "PRIMEFACE"));
	}

	/**
	 * Removes the test words from the groups of the lookup, so that they do not stay in the real lookup.
	 */
	private void removeFromTheWordsGroup(String... words) {
		for (String word : words) {
			CcpJsonRepresentation groupKey = CcpOtherConstants.EMPTY_JSON.put(VisEntityGroupPositionsBySkills.Fields.firstTwoInitials, word.substring(0, 2));
			CcpJsonRepresentation group = VisEntityGroupPositionsBySkills.ENTITY.getEntityMetaData().getOneByIdOrHandleItIfThisIdWasNotFound(groupKey, notFound -> CcpOtherConstants.EMPTY_JSON);
			List<CcpJsonRepresentation> items = group.getAsJsonList(VisEntityGroupPositionsBySkills.Fields.skill);
			List<CcpJsonRepresentation> otherItems = items.stream().filter(item -> false == word.equals(item.getAsString(VisJsonCommonsFields.word))).collect(Collectors.toList());
			boolean nothingToRemove = otherItems.size() == items.size();
			if(nothingToRemove) {
				continue;
			}
			VisEntityGroupPositionsBySkills.ENTITY.save(groupKey.put(VisEntityGroupPositionsBySkills.Fields.skill, otherItems));
			new CcpCacheDecorator(VisEntityGroupPositionsBySkills.ENTITY.calculateId(groupKey)).delete();
		}
	}

	/**
	 * The operator rejects the skill: the suggestion goes to the rejected entity, the skill does not enter
	 * {@link VisEntitySkill} and the candidate gets the rejection email, with the justification escaped.
	 */
	@Test
	public void operatorRejectsTheSkill() {

		String skill = "TESTE SKILL REJEITADA";
		this.openSuggestion(REJECTED_USER, skill, "Acho que esta habilidade deveria constar");

		this.operatorTypes("/reviewSkillSuggestion " + REJECTED_USER + " " + skill);
		String finished = this.operatorTypes("rejeitar não é uma <habilidade> técnica");
		assertTrue(finished, finished.contains("foi rejeitada"));

		CcpJsonRepresentation suggestionKey = this.suggestion(REJECTED_USER, skill);
		assertFalse(VisEntitySkillPending.ENTITY.exists(suggestionKey));
		assertTrue(VisEntitySkillRejected.ENTITY.exists(suggestionKey));

		CcpJsonRepresentation skillKey = CcpOtherConstants.EMPTY_JSON.put(VisEntitySkill.Fields.skill, skill);
		assertFalse(VisEntitySkill.ENTITY.exists(skillKey));

		CcpJsonRepresentation email = this.emailSentTo(REJECTED_USER, VisMessages.VisNotifyUserAboutRejectedSkill.class);
		String body = email.getAsString(JnJsonCommonsFields.message);
		assertTrue(body, body.contains("não foi aprovada"));
		assertTrue(body, body.contains("não é uma &lt;habilidade&gt; técnica"));

		CcpJsonRepresentation found = VisServiceSkillSuggestion.GetSkillSuggestion.execute(suggestionKey);
		assertEquals("rejected", found.getAsString(JnJsonCommonsFields.status));
		assertEquals("não é uma <habilidade> técnica", found.getAsString(JnJsonCommonsFields.explanation));

		// the rejection is final: suggesting the same skill again is refused and nothing becomes pending
		CcpJsonRepresentation sameSuggestion = this.newSuggestion(suggestionKey, "Insistindo depois de rejeitada");
		CcpProcessStatus refusal = this.statusOf(() -> VisServiceSkillSuggestion.SuggestSkill.execute(sameSuggestion));
		assertEquals(VisProcessStatusSuggestSkill.alreadyRejected, refusal);
		assertEquals(410, refusal.asNumber());
		assertFalse(VisEntitySkillPending.ENTITY.exists(suggestionKey));
	}

	/**
	 * The operator asks to ignore the candidate, gives up, asks again and confirms: the suggestion is discarded
	 * without any email, the next suggestion of the candidate is refused with {@code userNotAllowed} (403) and the
	 * command itself refuses to review the candidate until {@code allowCommandToUser}.
	 */
	@Test
	public void operatorIgnoresTheCandidate() {

		String skill = "TESTE SKILL IGNORADA";
		this.allowCandidate(IGNORED_USER);
		this.openSuggestion(IGNORED_USER, skill, "Sugestão sem sentido para testar o ignorar");

		this.operatorTypes("/reviewSkillSuggestion " + IGNORED_USER + " " + skill);
		String confirmation = this.operatorTypes("ignorar");
		assertTrue(confirmation, confirmation.startsWith("Confirma que o usuário " + IGNORED_USER + " será ignorado pelo suporte, em todos os comandos?"));

		String canceled = this.operatorTypes("não");
		assertTrue(canceled, canceled.startsWith("O usuário não será ignorado."));

		this.operatorTypes("ignorar");
		String ignored = this.operatorTypes("sim");
		assertTrue(ignored, ignored.startsWith("O usuário " + IGNORED_USER + " foi ignorado"));
		assertFalse(this.isPendingTicket("/reviewSkillSuggestion " + IGNORED_USER + " " + skill));

		CcpJsonRepresentation suggestionKey = this.suggestion(IGNORED_USER, skill);
		assertFalse(VisEntitySkillPending.ENTITY.exists(suggestionKey));
		assertFalse(VisEntitySkillRejected.ENTITY.exists(suggestionKey));
		assertFalse(this.anyEmailSentTo(IGNORED_USER, VisMessages.VisNotifyUserAboutRejectedSkill.class));

		CcpJsonRepresentation newSuggestion = this.newSuggestion(suggestionKey, "Tentando de novo depois de ignorado");
		CcpProcessStatus refusal = this.statusOf(() -> VisServiceSkillSuggestion.SuggestSkill.execute(newSuggestion));
		assertEquals(VisProcessStatusSuggestSkill.userNotAllowed, refusal);
		assertFalse(VisEntitySkillPending.ENTITY.exists(suggestionKey));

		// the ignoring is global: a skill hierarchy fix request of the same candidate is refused too
		CcpJsonRepresentation hierarchyRequest = CcpOtherConstants.EMPTY_JSON
				.put(VisEntitySkillFixHierarchyPending.Fields.email, IGNORED_USER)
				.put(VisEntitySkillFixHierarchyPending.Fields.parent, "TESTE.PAI")
				.put(VisEntitySkillFixHierarchyPending.Fields.type, VisSkillFixHierarchyTypes.add)
				.put(VisEntitySkillFixHierarchyPending.Fields.description, "Pedido de ajuste de quem foi ignorado")
				.put(VisEntitySkillFixHierarchyPending.Fields.skill, Arrays.asList("TESTE.SKILL"));
		CcpProcessStatus hierarchyRefusal = this.statusOf(() -> VisServiceSkillFixHierarchy.FixSkillHierarchy.execute(hierarchyRequest));
		assertEquals(VisProcessStatusFixSkillHierarchy.userNotAllowed, hierarchyRefusal);

		String notAllowed = this.operatorTypes("/reviewSkillSuggestion " + IGNORED_USER + " " + skill);
		assertTrue(notAllowed, notAllowed.contains("/allowCommandToUser " + IGNORED_USER));

		this.operatorTypes("/allowCommandToUser " + IGNORED_USER);
		VisServiceSkillSuggestion.SuggestSkill.execute(newSuggestion);
		assertTrue(VisEntitySkillPending.ENTITY.exists(suggestionKey));
	}

	/**
	 * The candidate withdraws the suggestion while the operator is reviewing it: the decision ends the session telling
	 * that it is no longer pending, and nothing is moved.
	 */
	@Test
	public void candidateWithdrawsDuringTheReview() {

		String skill = "TESTE SKILL RETIRADA";
		this.openSuggestion(WITHDRAWN_USER, skill, "Vou desistir durante a revisão");

		this.operatorTypes("/reviewSkillSuggestion " + WITHDRAWN_USER + " " + skill);

		CcpJsonRepresentation suggestionKey = this.suggestion(WITHDRAWN_USER, skill);
		VisServiceSkillSuggestion.DeleteSkillSuggestion.execute(suggestionKey);

		String finished = this.operatorTypes("aprovar parecia uma boa habilidade");
		assertTrue(finished, finished.contains("não está mais pendente"));
		// withdrawing in the screen does not touch the ticket; the operator's session ending (even without a decision) does
		assertFalse(this.isPendingTicket("/reviewSkillSuggestion " + WITHDRAWN_USER + " " + skill));
		assertFalse(VisEntitySkillApproved.ENTITY.exists(suggestionKey));

		CcpProcessStatus notFound = this.statusOf(() -> VisServiceSkillSuggestion.DeleteSkillSuggestion.execute(suggestionKey));
		assertEquals(CcpProcessStatusDefault.NOT_FOUND, notFound);
	}

	/**
	 * The service refuses a skill the system already knows (412) and a second pending suggestion of the same skill
	 * by the same candidate (409), and the lookup tells the status of a pending suggestion.
	 */
	@Test
	public void serviceRefusesKnownSkillsAndRepeatedSuggestions() {

		String knownSkill = "TESTE SKILL CONHECIDA";
		CcpJsonRepresentation knownSkillRecord = CcpOtherConstants.EMPTY_JSON
				.put(VisEntitySkill.Fields.skill, knownSkill)
				.put(VisEntitySkill.Fields.ranking, 999999);
		VisEntitySkill.ENTITY.save(knownSkillRecord);

		try {
			CcpJsonRepresentation knownSuggestion = this.newSuggestion(this.suggestion(SERVICE_USER, knownSkill), "Esta habilidade já existe no sistema");
			CcpProcessStatus alreadyExists = this.statusOf(() -> VisServiceSkillSuggestion.SuggestSkill.execute(knownSuggestion));
			assertEquals(VisProcessStatusSuggestSkill.skillAlreadyExists, alreadyExists);
		} finally {
			this.deleteSkill(knownSkill);
		}

		String skill = "TESTE SKILL REPETIDA";
		this.openSuggestion(SERVICE_USER, skill, "Primeira sugestão desta habilidade");
		CcpJsonRepresentation suggestionKey = this.suggestion(SERVICE_USER, skill);

		CcpJsonRepresentation found = VisServiceSkillSuggestion.GetSkillSuggestion.execute(suggestionKey);
		assertEquals("pending", found.getAsString(JnJsonCommonsFields.status));

		CcpJsonRepresentation repeated = this.newSuggestion(suggestionKey, "Segunda sugestão desta habilidade");
		CcpProcessStatus conflict = this.statusOf(() -> VisServiceSkillSuggestion.SuggestSkill.execute(repeated));
		assertEquals(CcpProcessStatusDefault.CONFLICT, conflict);
	}

	private CcpProcessStatus statusOf(Runnable call) {
		try {
			call.run();
		} catch (CcpErrorFlowDisturb refusal) {
			return refusal.status;
		}
		throw new AssertionError("The call was accepted");
	}

	private String operatorTypes(String text) {
		CcpJsonRepresentation message = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonInstantMessengerFields.botName, JbBotType.support)
				.put(JnJsonInstantMessengerFields.chatId, SUPPORT_CHAT)
				.put(JnJsonCommonsFields.message, text)
				.put(JbBotEngine.Fields.message_id, 1);
		JbBotType.support.getBot().execute(message);
		CcpJsonRepresentation reply = TELEGRAM.lastMessageFor(SUPPORT_CHAT);
		String replyText = reply.getAsString(JnJsonCommonsFields.message);
		return replyText;
	}

	/**
	 * Leaves only the new suggestion: removes whatever a previous run left in the suggestion entities and the
	 * "already sent" records of the notices, then saves the suggestion, which notifies the candidate and the operator.
	 */
	private void openSuggestion(String email, String skill, String description, String... synonyms) {
		CcpJsonRepresentation suggestionKey = this.suggestion(email, skill);
		CcpJsonRepresentation newSuggestion = this.newSuggestion(suggestionKey, description, synonyms);

		// the pending entity writes through the messaging, which validates the whole record, even to delete it
		VisEntitySkillPending.ENTITY.delete(newSuggestion);
		VisEntitySkillApproved.ENTITY.delete(suggestionKey);
		VisEntitySkillRejected.ENTITY.delete(suggestionKey);

		List<Class<?>> templates = Arrays.asList(
				VisMessages.VisNotifySupportAndUserAboutPendingSkillRequest.class,
				VisMessages.VisNotifyUserAboutAprovedSkill.class,
				VisMessages.VisNotifyUserAboutRejectedSkill.class);

		for (Class<?> template : templates) {
			CcpJsonRepresentation sentEmail = CcpOtherConstants.EMPTY_JSON
					.put(JnJsonCommonsFields.subjectType, template.getName())
					.put(JnJsonCommonsFields.email, email);
			JnEntityEmailMessageSent.ENTITY.delete(sentEmail);
		}

		double chatIdAsTheDatabaseReturnsIt = SUPPORT_CHAT.doubleValue();
		CcpJsonRepresentation sentNotice = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonInstantMessengerFields.botName, JbBotType.support)
				.put(JnJsonInstantMessengerFields.chatId, chatIdAsTheDatabaseReturnsIt)
				.put(JnJsonInstantMessengerFields.instantMessageType, JnInstantMessageType.text)
				.put(JnJsonCommonsFields.message, "/reviewSkillSuggestion " + email + " " + skill);
		JnEntityInstantMessengerMessageSent.ENTITY.delete(sentNotice);

		VisEntitySkillPending.ENTITY.save(newSuggestion);
	}

	/**
	 * Tells whether the command is still a ticket of the operator: in the jn inbox, where the sending of the notice
	 * records it, or in the jb list, where {@code /pendingTickets} moves it.
	 */
	private boolean isPendingTicket(String command) {
		CcpJsonRepresentation pendingCommand = CcpOtherConstants.EMPTY_JSON
				.put(JnEntitySupportPendingCommand.Fields.botName, JbBotType.support)
				.put(JnEntitySupportPendingCommand.Fields.chatId, SUPPORT_CHAT)
				.put(JnEntitySupportPendingCommand.Fields.command, command);
		CcpJsonRepresentation pendingTicket = CcpOtherConstants.EMPTY_JSON
				.put(JbEntityPendingTickets.Fields.botName, JbBotType.support)
				.put(JbEntityPendingTickets.Fields.chatId, SUPPORT_CHAT)
				.put(JbEntityPendingTickets.Fields.ticket, command);
		boolean inTheInbox = JnEntitySupportPendingCommand.ENTITY.exists(pendingCommand);
		boolean inTheList = JbEntityPendingTickets.ENTITY.exists(pendingTicket);
		boolean pending = inTheInbox || inTheList;
		return pending;
	}

	private void allowCandidate(String email) {
		CcpJsonRepresentation ignoredUser = CcpOtherConstants.EMPTY_JSON
				.put(VisEntityCommandNotAllowedToUser.Fields.email, email)
				.put(VisEntityCommandNotAllowedToUser.Fields.commandName, VisUserRequestCommands.reviewSkillSuggestion);
		VisEntityCommandNotAllowedToUser.ENTITY.delete(ignoredUser);
	}

	private void deleteSkill(String skill) {
		CcpJsonRepresentation skillKey = CcpOtherConstants.EMPTY_JSON.put(VisEntitySkill.Fields.skill, skill);
		VisEntitySkill.ENTITY.delete(skillKey);
	}

	private CcpJsonRepresentation suggestion(String email, String skill) {
		CcpJsonRepresentation suggestion = CcpOtherConstants.EMPTY_JSON
				.put(VisEntitySkillPending.Fields.email, email)
				.put(VisEntitySkillPending.Fields.skill, skill);
		return suggestion;
	}

	private CcpJsonRepresentation newSuggestion(CcpJsonRepresentation suggestionKey, String description, String... synonyms) {
		CcpJsonRepresentation suggestionWithDescription = suggestionKey.put(VisEntitySkillPending.Fields.description, description);
		CcpJsonRepresentation newSuggestion = suggestionWithDescription.put(VisEntitySkillPending.Fields.synonym, Arrays.asList(synonyms));
		return newSuggestion;
	}

	private boolean anyEmailSentTo(String recipient, Class<?> template) {
		try {
			this.emailSentTo(recipient, template);
			return true;
		} catch (AssertionError e) {
			return false;
		}
	}

	private CcpJsonRepresentation emailSentTo(String recipient, Class<?> template) {
		String templateId = template.getName();

		for (CcpJsonRepresentation sentEmail : EMAIL_INBOX.sentEmails) {
			String sentTemplateId = sentEmail.getAsString(JnJsonCommonsFields.templateId);
			List<String> recipients = sentEmail.getAsStringList(JnJsonCommonsFields.email);
			boolean isThisEmail = sentTemplateId.equals(templateId) && recipients.contains(recipient);

			if(isThisEmail) {
				return sentEmail;
			}
		}
		throw new AssertionError("No email was sent to " + recipient + " with the template " + templateId + ". Sent: " + EMAIL_INBOX.sentEmails);
	}
}
