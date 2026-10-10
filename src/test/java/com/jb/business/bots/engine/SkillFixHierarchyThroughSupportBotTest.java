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
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.utils.entity.CcpEntity;
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
import com.ccp.process.CcpProcessStatusDefault;
import com.jb.entities.JbEntityBot;
import com.jb.entities.JbEntityBotCommand;
import com.jb.entities.JbEntityBotCommandStep;
import com.jb.entities.JbEntityBotCommandStepEndMessage;
import com.jb.entities.JbEntityBotCommandStepSession;
import com.jb.entities.JbEntityPendingTickets;
import com.jn.business.messages.JnInstantMessageType;
import com.jn.entities.JnEntityEmailMessageSent;
import com.jn.entities.JnEntityInstantMessengerMessageSent;
import com.jn.entities.JnEntitySupportCancelledCommand;
import com.jn.entities.JnEntitySupportPendingCommand;
import com.jn.entities.decorators.ElasticsearchLocal;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;
import com.vis.business.skill.VisBusinessSkillFixHierarchyIgnoreUser;
import com.vis.business.skill.VisSkillWordsGroups;
import com.vis.entities.VisEntityCommandNotAllowedToUser;
import com.vis.entities.VisEntityGroupPositionsBySkills;
import com.vis.entities.VisEntitySkill;
import com.vis.entities.VisEntitySkillFixHierarchyFulfiled;
import com.vis.entities.VisEntitySkillFixHierarchyItemApproved;
import com.vis.entities.VisEntitySkillFixHierarchyItemPending;
import com.vis.entities.VisEntitySkillFixHierarchyPending;
import com.vis.json.fields.validation.VisJsonCommonsFields;
import com.vis.json.fields.validation.VisSkillFixHierarchyTypes;
import com.vis.json.fields.validation.VisUserRequestCommands;
import com.vis.messages.VisMessages;
import com.vis.services.VisServiceSkillFixHierarchy;
import com.vis.status.VisProcessStatusFixSkillHierarchy;

/**
 * Exercises the whole circuit of a skill hierarchy fix request: the user's request notifies the support bot
 * operator with {@code /fixSkillHierarchy <type> <email> <parent>}, the operator runs that command, reads the user's
 * justification and the pending items, decides them (all at once or one by one, always with a justification),
 * the items and the request move to the approved/rejected entities and the user gets an email listing the
 * approved and the rejected items with the operator's justifications.
 *
 * <p>The database is the real Elasticsearch (the bot configuration is read from it); Telegram and the e-mail
 * provider are test doubles, as in {@link UnlockTokenThroughSupportBotTest}.
 */
public class SkillFixHierarchyThroughSupportBotTest {

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

	private static final String ONE_BY_ONE_USER = "revisao.um.a.um@teste.com";

	private static final String APPROVE_ALL_USER = "revisao.aprovar.tudo@teste.com";

	private static final String DECIDED_BEFORE_USER = "revisao.decididos.antes@teste.com";

	private static final String ALL_DECIDED_BEFORE_USER = "revisao.todos.decididos.antes@teste.com";

	private static final String IGNORED_USER = "usuario.ignorado@teste.com";

	private static final String UNIGNORED_USER = "usuario.nao.mais.ignorado@teste.com";

	private static final String BLOCKED_USER = "usuario.bloqueado.no.comando@teste.com";

	private static final String TWO_TYPES_USER = "associa.e.desassocia@teste.com";

	private static final String REJECT_ALL_USER = "revisao.rejeitar.tudo@teste.com";

	private static final String SKILL_PARENTS_USER = "revisao.altera.parents@teste.com";

	private static final String WITHDRAWN_REQUEST_USER = "desistiu.da.solicitacao@teste.com";

	private static final String ALREADY_REVIEWED_SERVICE_USER = "revisao.ja.atendida.pelo.servico@teste.com";

	/**
	 * Saves the bot, its commands, their steps, their end messages and the message templates of the requests again so
	 * that the index follows what is declared in java (the create operation of the initial records does not overwrite
	 * what is already there).
	 */
	@BeforeClass
	public static void resaveBotConfiguration() {
		List<CcpEntityConfigurator> configurators = Arrays.asList(new JbEntityBot(), new JbEntityBotCommand(), new JbEntityBotCommandStep(), new JbEntityBotCommandStepEndMessage(), new VisEntitySkillFixHierarchyPending());
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

	@Test
	public void operatorDecidesOneByOne() {

		this.openRequest(ONE_BY_ONE_USER, VisSkillFixHierarchyTypes.add, "java", "Uso spring e hibernate em todo projeto java", "spring", "hibernate");

		String notice = TELEGRAM.lastMessageFor(SUPPORT_CHAT).getAsString(JnJsonCommonsFields.message);
		assertEquals("/fixSkillHierarchy add " + ONE_BY_ONE_USER + " java", notice);

		String request = this.operatorTypes("/fixSkillHierarchy add " + ONE_BY_ONE_USER + " java");
		assertTrue(request, request.contains("Uso spring e hibernate em todo projeto java"));
		assertTrue(request, request.contains("spring, hibernate"));
		assertTrue(request, request.contains("um a um"));

		String firstItem = this.operatorTypes("um a um");
		assertTrue(firstItem, firstItem.startsWith("Item 1 de 2: spring"));

		String withoutJustification = this.operatorTypes("aprovar");
		assertTrue(withoutJustification, withoutJustification.startsWith("Não entendi a resposta"));
		assertTrue(withoutJustification, withoutJustification.contains("Item 1 de 2: spring"));

		String secondItem = this.operatorTypes("aprovar spring é framework java");
		assertTrue(secondItem, secondItem.startsWith("Item 2 de 2: hibernate"));

		String summary = this.operatorTypes("rejeitar hibernate já está associado a jpa");
		assertTrue(summary, summary.startsWith("Revisão concluída para " + ONE_BY_ONE_USER + " / java."));
		assertTrue(summary, summary.contains("Aprovados: spring"));
		assertTrue(summary, summary.contains("Reprovados: hibernate"));

		CcpJsonRepresentation spring = this.item(ONE_BY_ONE_USER, VisSkillFixHierarchyTypes.add, "java", "spring");
		CcpJsonRepresentation hibernate = this.item(ONE_BY_ONE_USER, VisSkillFixHierarchyTypes.add, "java", "hibernate");
		CcpEntity rejectedItems = VisEntitySkillFixHierarchyItemPending.ENTITY.getTwinEntity();

		assertFalse(VisEntitySkillFixHierarchyItemPending.ENTITY.exists(spring));
		assertFalse(VisEntitySkillFixHierarchyItemPending.ENTITY.exists(hibernate));
		assertTrue(VisEntitySkillFixHierarchyItemApproved.ENTITY.exists(spring));
		assertTrue(rejectedItems.exists(hibernate));

		CcpJsonRepresentation request1 = this.request(ONE_BY_ONE_USER, VisSkillFixHierarchyTypes.add, "java");
		assertFalse(VisEntitySkillFixHierarchyPending.ENTITY.exists(request1));
		assertTrue(VisEntitySkillFixHierarchyFulfiled.ENTITY.exists(request1));

		// the "Motivo" panel of the screen shows the explanation: the decisions are written in the language of the
		// user (the language of the system, Portuguese, while the language of the user is not stored)
		CcpJsonRepresentation fulfiled = VisEntitySkillFixHierarchyFulfiled.ENTITY.getOneById(request1);
		String explanation = fulfiled.getAsString(JnJsonCommonsFields.explanation);
		assertTrue(explanation, explanation.contains("spring (aprovado): spring é framework java"));
		assertTrue(explanation, explanation.contains("hibernate (reprovado): hibernate já está associado a jpa"));
		assertFalse(explanation, explanation.contains("approved"));

		CcpJsonRepresentation email = this.emailSentTo(ONE_BY_ONE_USER, VisMessages.VisNotifyUserAboutFulfiledSkillHierarchy.class);
		String body = email.getAsString(JnJsonCommonsFields.message);
		assertTrue(body, body.contains("associação com o termo java"));
		assertTrue(body, body.contains("Itens aprovados:</p><ul><li><b>spring</b>: spring é framework java</li></ul>"));
		assertTrue(body, body.contains("Itens reprovados:</p><ul><li><b>hibernate</b>: hibernate já está associado a jpa</li></ul>"));
	}

	@Test
	public void operatorApprovesEverything() {

		this.openRequest(APPROVE_ALL_USER, VisSkillFixHierarchyTypes.remove, "python", "Django não é python puro", "django", "flask");

		this.operatorTypes("/fixSkillHierarchy remove " + APPROVE_ALL_USER + " python");
		String summary = this.operatorTypes("aprovar os dois são frameworks e não linguagem");
		assertTrue(summary, summary.contains("Aprovados: django, flask"));
		assertTrue(summary, summary.contains("Reprovados: -"));

		CcpJsonRepresentation django = this.item(APPROVE_ALL_USER, VisSkillFixHierarchyTypes.remove, "python", "django");
		CcpJsonRepresentation flask = this.item(APPROVE_ALL_USER, VisSkillFixHierarchyTypes.remove, "python", "flask");
		assertTrue(VisEntitySkillFixHierarchyItemApproved.ENTITY.exists(django));
		assertTrue(VisEntitySkillFixHierarchyItemApproved.ENTITY.exists(flask));

		CcpJsonRepresentation email = this.emailSentTo(APPROVE_ALL_USER, VisMessages.VisNotifyUserAboutFulfiledSkillHierarchy.class);
		String body = email.getAsString(JnJsonCommonsFields.message);
		assertTrue(body, body.contains("desassociação com o termo python"));
		assertTrue(body, body.contains("<li><b>django</b>: os dois são frameworks e não linguagem</li>"));
		assertFalse(body, body.contains("Itens reprovados"));

		// once decided, the request is no longer pending for the operator
		String again = this.operatorTypes("/fixSkillHierarchy remove " + APPROVE_ALL_USER + " python");
		assertTrue(again, again.startsWith("Não há itens pendentes"));
	}

	/**
	 * An approved association adds the parent to the {@code parent} array of the skill in {@link VisEntitySkill}; a
	 * rejected one leaves the skill as it was. The lookup the resume screen reads changes too, in the item of the skill
	 * and in the item of its synonym (another group, since the synonym starts with other letters).
	 */
	@Test
	public void approvedAssociationAddsTheParentToTheSkill() {

		this.saveSkill("TESTE.SKILL.ASSOCIADA", "TESTE.PAI.ANTIGO");
		this.saveSkill("TESTE.SKILL.REJEITADA", "TESTE.PAI.ANTIGO");
		this.addSynonym("TESTE.SKILL.ASSOCIADA", "XTESTE.SINONIMO.ASSOCIADA");
		this.putInTheWordsGroup("TESTE.SKILL.ASSOCIADA", "TESTE.SKILL.ASSOCIADA", "TESTE.PAI.ANTIGO");
		this.putInTheWordsGroup("TESTE.SKILL.ASSOCIADA", "XTESTE.SINONIMO.ASSOCIADA", "TESTE.PAI.ANTIGO");
		this.putInTheWordsGroup("TESTE.SKILL.REJEITADA", "TESTE.SKILL.REJEITADA", "TESTE.PAI.ANTIGO");

		try {
			this.openRequest(SKILL_PARENTS_USER, VisSkillFixHierarchyTypes.add, "TESTE.PAI", "As duas dependem de TESTE.PAI", "TESTE.SKILL.ASSOCIADA", "TESTE.SKILL.REJEITADA");

			this.operatorTypes("/fixSkillHierarchy add " + SKILL_PARENTS_USER + " TESTE.PAI");
			this.operatorTypes("um a um");
			this.operatorTypes("aprovar depende mesmo");
			String summary = this.operatorTypes("rejeitar não depende");
			assertTrue(summary, summary.contains("Aprovados: TESTE.SKILL.ASSOCIADA"));

			assertEquals(Arrays.asList("TESTE.PAI.ANTIGO", "TESTE.PAI"), this.skillParents("TESTE.SKILL.ASSOCIADA"));
			assertEquals(Arrays.asList("TESTE.PAI.ANTIGO"), this.skillParents("TESTE.SKILL.REJEITADA"));

			assertEquals(Arrays.asList("TESTE.PAI.ANTIGO", "TESTE.PAI"), this.wordParents("TESTE.SKILL.ASSOCIADA"));
			assertEquals(Arrays.asList("TESTE.PAI.ANTIGO", "TESTE.PAI"), this.wordParents("XTESTE.SINONIMO.ASSOCIADA"));
			assertEquals(Arrays.asList("TESTE.PAI.ANTIGO"), this.wordParents("TESTE.SKILL.REJEITADA"));
		} finally {
			this.deleteSkill("TESTE.SKILL.ASSOCIADA");
			this.deleteSkill("TESTE.SKILL.REJEITADA");
			this.removeFromTheWordsGroup("TESTE.SKILL.ASSOCIADA", "XTESTE.SINONIMO.ASSOCIADA", "TESTE.SKILL.REJEITADA");
		}
	}

	/**
	 * A parent with spaces (FRONT END, HTTP SERVER) goes last in the command, which takes the rest of the text: up to
	 * 2026-10-08 it went first and the bot read the first word as the parent and the second as the type.
	 */
	@Test
	public void operatorReviewsAParentWithSpaces() {

		this.openRequest(SKILL_PARENTS_USER, VisSkillFixHierarchyTypes.add, "TESTE PAI COM ESPACO", "Depende do termo com espaço", "TESTE.SKILL.COM.ESPACO");

		String notice = TELEGRAM.lastMessageFor(SUPPORT_CHAT).getAsString(JnJsonCommonsFields.message);
		assertEquals("/fixSkillHierarchy add " + SKILL_PARENTS_USER + " TESTE PAI COM ESPACO", notice);

		String request = this.operatorTypes(notice);
		assertTrue(request, request.startsWith("Solicitação de associação de " + SKILL_PARENTS_USER + " para o termo TESTE PAI COM ESPACO"));
		assertTrue(request, request.contains("Itens pendentes: TESTE.SKILL.COM.ESPACO"));

		String summary = this.operatorTypes("aprovar depende mesmo");
		assertTrue(summary, summary.startsWith("Revisão concluída para " + SKILL_PARENTS_USER + " / TESTE PAI COM ESPACO."));
		CcpJsonRepresentation requestKey = this.request(SKILL_PARENTS_USER, VisSkillFixHierarchyTypes.add, "TESTE PAI COM ESPACO");
		assertTrue(VisEntitySkillFixHierarchyFulfiled.ENTITY.exists(requestKey));
	}

	private void addSynonym(String skill, String synonym) {
		CcpJsonRepresentation skillKey = CcpOtherConstants.EMPTY_JSON.put(VisEntitySkill.Fields.skill, skill);
		CcpJsonRepresentation skillRecord = VisEntitySkill.ENTITY.getOneById(skillKey);
		VisEntitySkill.ENTITY.save(skillRecord.put(VisEntitySkill.Fields.synonym, Arrays.asList(synonym)));
	}

	/**
	 * Puts the word in the lookup with the given parents, as the initial load does for the known skills.
	 */
	private void putInTheWordsGroup(String skill, String word, String... parents) {
		this.removeFromTheWordsGroup(word);
		VisSkillWordsGroups.addWord(skill, word);
		for (String parent : parents) {
			VisSkillWordsGroups.changeParents(skill, Arrays.asList(word), wordParents -> wordParents.add(parent));
		}
	}

	private List<String> wordParents(String word) {
		List<CcpJsonRepresentation> items = this.wordsGroupItems(word);
		CcpJsonRepresentation item = items.stream().filter(x -> word.equals(x.getAsString(VisJsonCommonsFields.word))).findFirst().get();
		List<String> parents = item.getAsStringList(VisJsonCommonsFields.parent);
		return parents;
	}

	private List<CcpJsonRepresentation> wordsGroupItems(String word) {
		CcpJsonRepresentation groupKey = CcpOtherConstants.EMPTY_JSON.put(VisEntityGroupPositionsBySkills.Fields.firstTwoInitials, word.substring(0, 2));
		CcpJsonRepresentation group = VisEntityGroupPositionsBySkills.ENTITY.getEntityMetaData().getOneByIdOrHandleItIfThisIdWasNotFound(groupKey, notFound -> CcpOtherConstants.EMPTY_JSON);
		List<CcpJsonRepresentation> items = group.getAsJsonList(VisEntityGroupPositionsBySkills.Fields.skill);
		return items;
	}

	/**
	 * Removes the test words from the lookup, so that they do not stay in the real lookup.
	 */
	private void removeFromTheWordsGroup(String... words) {
		for (String word : words) {
			CcpJsonRepresentation groupKey = CcpOtherConstants.EMPTY_JSON.put(VisEntityGroupPositionsBySkills.Fields.firstTwoInitials, word.substring(0, 2));
			List<CcpJsonRepresentation> items = this.wordsGroupItems(word);
			List<CcpJsonRepresentation> otherItems = items.stream().filter(item -> false == word.equals(item.getAsString(VisJsonCommonsFields.word))).collect(Collectors.toList());
			boolean nothingToRemove = otherItems.size() == items.size();
			if(nothingToRemove) {
				continue;
			}
			boolean groupWouldBeEmpty = otherItems.isEmpty();
			if(groupWouldBeEmpty) {
				VisEntityGroupPositionsBySkills.ENTITY.delete(groupKey);
				continue;
			}
			VisEntityGroupPositionsBySkills.ENTITY.save(groupKey.put(VisEntityGroupPositionsBySkills.Fields.skill, otherItems));
		}
	}

	/**
	 * An approved dissociation removes the parent from the {@code parent} array of the skill in
	 * {@link VisEntitySkill}, keeping the other parents.
	 */
	@Test
	public void approvedDissociationRemovesTheParentFromTheSkill() {

		this.saveSkill("TESTE.SKILL.DESASSOCIADA", "TESTE.PAI", "TESTE.OUTRO.PAI");

		try {
			this.openRequest(SKILL_PARENTS_USER, VisSkillFixHierarchyTypes.remove, "TESTE.PAI", "Não depende de TESTE.PAI", "TESTE.SKILL.DESASSOCIADA");

			this.operatorTypes("/fixSkillHierarchy remove " + SKILL_PARENTS_USER + " TESTE.PAI");
			String summary = this.operatorTypes("aprovar não depende mesmo");
			assertTrue(summary, summary.contains("Aprovados: TESTE.SKILL.DESASSOCIADA"));

			assertEquals(Arrays.asList("TESTE.OUTRO.PAI"), this.skillParents("TESTE.SKILL.DESASSOCIADA"));
		} finally {
			this.deleteSkill("TESTE.SKILL.DESASSOCIADA");
		}
	}

	private void saveSkill(String skill, String... parents) {
		CcpJsonRepresentation skillRecord = CcpOtherConstants.EMPTY_JSON
				.put(VisEntitySkill.Fields.skill, skill)
				.put(VisEntitySkill.Fields.parent, Arrays.asList(parents))
				.put(VisEntitySkill.Fields.ranking, 999999);
		VisEntitySkill.ENTITY.save(skillRecord);
	}

	private List<String> skillParents(String skill) {
		CcpJsonRepresentation skillKey = CcpOtherConstants.EMPTY_JSON.put(VisEntitySkill.Fields.skill, skill);
		CcpJsonRepresentation skillRecord = VisEntitySkill.ENTITY.getOneById(skillKey);
		List<String> parents = skillRecord.getAsStringList(VisEntitySkill.Fields.parent);
		return parents;
	}

	private void deleteSkill(String skill) {
		CcpJsonRepresentation skillKey = CcpOtherConstants.EMPTY_JSON.put(VisEntitySkill.Fields.skill, skill);
		VisEntitySkill.ENTITY.delete(skillKey);
	}

	/**
	 * Rejecting everything also fulfils the request: it leaves the pending entity for the fulfiled one, the items
	 * go to the rejected items (the twin of the pending items) and the user gets the review email.
	 */
	@Test
	public void operatorRejectsEverything() {

		this.openRequest(REJECT_ALL_USER, VisSkillFixHierarchyTypes.add, "kotlin", "Ktor e Exposed são de kotlin", "ktor", "exposed");

		this.operatorTypes("/fixSkillHierarchy add " + REJECT_ALL_USER + " kotlin");
		String summary = this.operatorTypes("rejeitar já estão associados a kotlin");
		assertTrue(summary, summary.contains("Aprovados: -"));
		assertTrue(summary, summary.contains("Reprovados: ktor, exposed"));

		CcpEntity rejectedItems = VisEntitySkillFixHierarchyItemPending.ENTITY.getTwinEntity();
		CcpJsonRepresentation ktor = this.item(REJECT_ALL_USER, VisSkillFixHierarchyTypes.add, "kotlin", "ktor");
		CcpJsonRepresentation exposed = this.item(REJECT_ALL_USER, VisSkillFixHierarchyTypes.add, "kotlin", "exposed");
		assertFalse(VisEntitySkillFixHierarchyItemPending.ENTITY.exists(ktor));
		assertFalse(VisEntitySkillFixHierarchyItemApproved.ENTITY.exists(ktor));
		assertTrue(rejectedItems.exists(ktor));
		assertTrue(rejectedItems.exists(exposed));

		CcpJsonRepresentation requestKey = this.request(REJECT_ALL_USER, VisSkillFixHierarchyTypes.add, "kotlin");
		assertFalse(VisEntitySkillFixHierarchyPending.ENTITY.exists(requestKey));
		assertTrue(VisEntitySkillFixHierarchyFulfiled.ENTITY.exists(requestKey));

		CcpJsonRepresentation email = this.emailSentTo(REJECT_ALL_USER, VisMessages.VisNotifyUserAboutFulfiledSkillHierarchy.class);
		String body = email.getAsString(JnJsonCommonsFields.message);
		assertTrue(body, body.contains("<li><b>ktor</b>: já estão associados a kotlin</li>"));
		assertFalse(body, body.contains("Itens aprovados"));
	}

	/**
	 * A new request that repeats skills decided in an earlier review: the email about the pending request keeps
	 * every skill, the operator is asked only about the new one, and the feedback to the user carries the earlier
	 * decisions as they were (spring approved, hibernate rejected) together with the new one.
	 */
	@Test
	public void itemsDecidedBeforeAreNotAskedAgain() {

		this.clearItems(DECIDED_BEFORE_USER, VisSkillFixHierarchyTypes.add, "kotlin", "ktor");
		this.openRequest(DECIDED_BEFORE_USER, VisSkillFixHierarchyTypes.add, "kotlin", "Uso spring e hibernate com kotlin", "spring", "hibernate");
		this.operatorTypes("/fixSkillHierarchy add " + DECIDED_BEFORE_USER + " kotlin");
		this.operatorTypes("um a um");
		this.operatorTypes("aprovar spring roda em kotlin");
		this.operatorTypes("rejeitar hibernate não tem relação");

		EMAIL_INBOX.sentEmails.clear();
		this.sendRequestAgain(DECIDED_BEFORE_USER, VisSkillFixHierarchyTypes.add, "kotlin", "Agora com ktor também", "spring", "hibernate", "ktor");

		CcpJsonRepresentation pendingEmail = this.emailSentTo(DECIDED_BEFORE_USER, VisMessages.VisNotifySupportAndUserAboutPendingSkillHierarchyRequest.class);
		String pendingBody = pendingEmail.getAsString(JnJsonCommonsFields.message);
		assertTrue(pendingBody, pendingBody.contains("spring, hibernate, ktor"));

		String request = this.operatorTypes("/fixSkillHierarchy add " + DECIDED_BEFORE_USER + " kotlin");
		assertTrue(request, request.contains("Itens pendentes: ktor"));
		assertTrue(request, request.contains("Já aprovados anteriormente (não serão perguntados): spring"));
		assertTrue(request, request.contains("Já reprovados anteriormente (não serão perguntados): hibernate"));

		String firstItem = this.operatorTypes("um a um");
		assertTrue(firstItem, firstItem.startsWith("Item 1 de 1: ktor"));

		String summary = this.operatorTypes("aprovar ktor é framework kotlin");
		assertTrue(summary, summary.contains("Aprovados: spring, ktor"));
		assertTrue(summary, summary.contains("Reprovados: hibernate"));

		CcpJsonRepresentation spring = this.item(DECIDED_BEFORE_USER, VisSkillFixHierarchyTypes.add, "kotlin", "spring");
		CcpJsonRepresentation hibernate = this.item(DECIDED_BEFORE_USER, VisSkillFixHierarchyTypes.add, "kotlin", "hibernate");
		CcpJsonRepresentation ktor = this.item(DECIDED_BEFORE_USER, VisSkillFixHierarchyTypes.add, "kotlin", "ktor");
		CcpEntity rejectedItems = VisEntitySkillFixHierarchyItemPending.ENTITY.getTwinEntity();
		assertTrue(VisEntitySkillFixHierarchyItemApproved.ENTITY.exists(spring));
		assertTrue(rejectedItems.exists(hibernate));
		assertTrue(VisEntitySkillFixHierarchyItemApproved.ENTITY.exists(ktor));
		assertFalse(VisEntitySkillFixHierarchyItemPending.ENTITY.exists(ktor));

		CcpJsonRepresentation email = this.emailSentTo(DECIDED_BEFORE_USER, VisMessages.VisNotifyUserAboutFulfiledSkillHierarchy.class);
		String body = email.getAsString(JnJsonCommonsFields.message);
		assertTrue(body, body.contains("<li><b>spring</b>: item já aprovado em revisão anterior</li>"));
		assertTrue(body, body.contains("<li><b>ktor</b>: ktor é framework kotlin</li>"));
		assertTrue(body, body.contains("Itens reprovados:</p><ul><li><b>hibernate</b>: item já reprovado em revisão anterior</li></ul>"));
	}

	/**
	 * A new request made only of skills reviewed before (one approved, one rejected): the before save of the
	 * pending entity refuses it, so it is not saved, the operator is not notified and the user gets an email
	 * telling that every skill was already handled in earlier requests.
	 */
	@Test
	public void requestWithEveryItemReviewedBeforeIsRefused() {

		this.openRequest(ALL_DECIDED_BEFORE_USER, VisSkillFixHierarchyTypes.remove, "scala", "Akka não é scala puro", "akka", "play");
		this.operatorTypes("/fixSkillHierarchy remove " + ALL_DECIDED_BEFORE_USER + " scala");
		this.operatorTypes("um a um");
		this.operatorTypes("aprovar akka é biblioteca");
		this.operatorTypes("rejeitar play depende de scala");

		EMAIL_INBOX.sentEmails.clear();
		TELEGRAM.sentMessages.clear();
		this.sendRequestAgain(ALL_DECIDED_BEFORE_USER, VisSkillFixHierarchyTypes.remove, "scala", "Pedindo de novo a mesma coisa", "akka", "play");

		CcpJsonRepresentation request = this.request(ALL_DECIDED_BEFORE_USER, VisSkillFixHierarchyTypes.remove, "scala");
		assertFalse(VisEntitySkillFixHierarchyPending.ENTITY.exists(request));

		CcpJsonRepresentation email = this.emailSentTo(ALL_DECIDED_BEFORE_USER, VisMessages.VisNotifyUserAboutAlreadyReviewedSkillHierarchy.class);
		String body = email.getAsString(JnJsonCommonsFields.message);
		assertTrue(body, body.contains("desassociação entre os termos akka, play e scala"));
		assertTrue(body, body.contains("já foram atendidos em solicitações anteriores"));
		assertFalse(EMAIL_INBOX.sentEmails.toString(), this.anyEmailSentTo(ALL_DECIDED_BEFORE_USER, VisMessages.VisNotifySupportAndUserAboutPendingSkillHierarchyRequest.class));
		assertTrue(TELEGRAM.sentMessages.toString(), TELEGRAM.sentMessages.isEmpty());

		String answer = this.operatorTypes("/fixSkillHierarchy remove " + ALL_DECIDED_BEFORE_USER + " scala");
		assertTrue(answer, answer.startsWith("Não há itens pendentes"));
	}

	/**
	 * The same refusal through the service the screen calls: the answer is {@code alreadyReviewed} (208), so the screen
	 * tells the user the request is complete and an email is on its way, instead of a 200 that reads as "sent for
	 * review"; nothing stays pending and the email goes out.
	 */
	@Test
	public void theServiceAnswersThatTheRequestWasAlreadyReviewed() {

		this.openRequest(ALREADY_REVIEWED_SERVICE_USER, VisSkillFixHierarchyTypes.add, "groovy", "Grails é framework groovy", "grails");
		this.operatorTypes("/fixSkillHierarchy add " + ALREADY_REVIEWED_SERVICE_USER + " groovy");
		this.operatorTypes("aprovar grails roda sobre groovy");

		EMAIL_INBOX.sentEmails.clear();
		CcpJsonRepresentation sameRequest = this.clearRequest(ALREADY_REVIEWED_SERVICE_USER, VisSkillFixHierarchyTypes.add, "groovy", "Pedindo de novo", "grails");
		CcpErrorFlowDisturb answer = this.refusalOf(sameRequest);

		assertEquals(VisProcessStatusFixSkillHierarchy.alreadyReviewed, answer.status);
		assertEquals(208, VisProcessStatusFixSkillHierarchy.alreadyReviewed.asNumber());
		CcpJsonRepresentation request = this.request(ALREADY_REVIEWED_SERVICE_USER, VisSkillFixHierarchyTypes.add, "groovy");
		assertFalse(VisEntitySkillFixHierarchyPending.ENTITY.exists(request));
		assertTrue(EMAIL_INBOX.sentEmails.toString(), this.anyEmailSentTo(ALREADY_REVIEWED_SERVICE_USER, VisMessages.VisNotifyUserAboutAlreadyReviewedSkillHierarchy.class));
	}

	/**
	 * The operator ignores a user who only plays with the requests: the intention is confirmed (a "no" goes back
	 * to the options), the user is recorded in vis_command_not_allowed_to_user with the request, the request and
	 * its items are discarded without any email, and the next request of the user reaches nobody.
	 */
	@Test
	public void operatorIgnoresTheUser() {

		CcpJsonRepresentation ignoredUser = CcpOtherConstants.EMPTY_JSON
				.put(VisEntityCommandNotAllowedToUser.Fields.email, IGNORED_USER)
				.put(VisEntityCommandNotAllowedToUser.Fields.commandName, VisUserRequestCommands.fixSkillHierarchy);
		VisEntityCommandNotAllowedToUser.ENTITY.delete(ignoredUser);

		this.openRequest(IGNORED_USER, VisSkillFixHierarchyTypes.add, "rust", "Palavrão e bobagem sem sentido", "blablabla", "blebleble");

		String request = this.operatorTypes("/fixSkillHierarchy add " + IGNORED_USER + " rust");
		assertTrue(request, request.contains("• ignorar"));

		String confirmation = this.operatorTypes("ignorar");
		assertTrue(confirmation, confirmation.startsWith("Confirma que o usuário " + IGNORED_USER + " será ignorado"));

		String notUnderstood = this.operatorTypes("talvez");
		assertTrue(notUnderstood, notUnderstood.startsWith("Não entendi a resposta."));
		assertTrue(notUnderstood, notUnderstood.contains("Confirma que o usuário"));

		String canceled = this.operatorTypes("não");
		assertTrue(canceled, canceled.startsWith("O usuário não será ignorado."));
		assertTrue(canceled, canceled.contains("• um a um"));
		assertFalse(VisEntityCommandNotAllowedToUser.ENTITY.exists(ignoredUser));

		this.operatorTypes("ignorar");
		String ignored = this.operatorTypes("sim");
		assertTrue(ignored, ignored.startsWith("O usuário " + IGNORED_USER + " foi ignorado pelo suporte, em todos os comandos."));

		assertTrue(VisEntityCommandNotAllowedToUser.ENTITY.exists(ignoredUser));
		CcpJsonRepresentation record = VisEntityCommandNotAllowedToUser.ENTITY.getOneById(ignoredUser);
		CcpJsonRepresentation description = record.getInnerJson(VisEntityCommandNotAllowedToUser.Fields.description);
		assertEquals("rust", description.getAsString(VisEntitySkillFixHierarchyPending.Fields.parent));
		String requests = description.getAsJsonList(VisBusinessSkillFixHierarchyIgnoreUser.JsonFieldNames.requests).toString();
		assertTrue(requests, requests.contains("Palavrão e bobagem sem sentido"));
		assertTrue(requests, requests.contains("blablabla"));

		CcpJsonRepresentation pendingRequest = this.request(IGNORED_USER, VisSkillFixHierarchyTypes.add, "rust");
		assertFalse(VisEntitySkillFixHierarchyPending.ENTITY.exists(pendingRequest));
		assertFalse(VisEntitySkillFixHierarchyFulfiled.ENTITY.exists(pendingRequest));

		CcpEntity rejectedItems = VisEntitySkillFixHierarchyItemPending.ENTITY.getTwinEntity();
		for (String skill : Arrays.asList("blablabla", "blebleble")) {
			CcpJsonRepresentation item = this.item(IGNORED_USER, VisSkillFixHierarchyTypes.add, "rust", skill);
			assertFalse(VisEntitySkillFixHierarchyItemPending.ENTITY.exists(item));
			assertFalse(rejectedItems.exists(item));
			assertFalse(VisEntitySkillFixHierarchyItemApproved.ENTITY.exists(item));
		}

		assertTrue(EMAIL_INBOX.sentEmails.toString(), EMAIL_INBOX.sentEmails.size() == 1);

		// the next request of the ignored user is refused with userNotAllowed (403), neither saved nor notified
		TELEGRAM.sentMessages.clear();
		EMAIL_INBOX.sentEmails.clear();
		CcpJsonRepresentation newRequestKey = this.request(IGNORED_USER, VisSkillFixHierarchyTypes.remove, "rust");
		CcpJsonRepresentation newRequest = this.newRequest(newRequestKey, "Mais uma bobagem qualquer", "xpto");
		CcpErrorFlowDisturb refusal = this.refusalOf(newRequest);
		assertEquals(VisProcessStatusFixSkillHierarchy.userNotAllowed, refusal.status);
		assertEquals(403, refusal.status.asNumber());

		assertFalse(VisEntitySkillFixHierarchyPending.ENTITY.exists(newRequestKey));
		assertTrue(TELEGRAM.sentMessages.toString(), TELEGRAM.sentMessages.isEmpty());
		assertTrue(EMAIL_INBOX.sentEmails.toString(), EMAIL_INBOX.sentEmails.isEmpty());
	}

	/**
	 * The operator undoes the decision with {@code /allowCommandToUser <email>}: the record leaves
	 * vis_command_not_allowed_to_user and the next request of the user is saved and reaches the operator again.
	 * Asking it again for the same user tells the operator that the user is not ignored.
	 */
	@Test
	public void operatorStopsIgnoringTheUser() {

		CcpJsonRepresentation ignoredUserKey = CcpOtherConstants.EMPTY_JSON
				.put(VisEntityCommandNotAllowedToUser.Fields.email, UNIGNORED_USER)
				.put(VisEntityCommandNotAllowedToUser.Fields.commandName, VisUserRequestCommands.fixSkillHierarchy);
		CcpJsonRepresentation ignoredDescription = CcpOtherConstants.EMPTY_JSON.put(VisEntitySkillFixHierarchyPending.Fields.parent, "go");
		CcpJsonRepresentation ignoredUser = ignoredUserKey.put(VisEntityCommandNotAllowedToUser.Fields.description, ignoredDescription);
		VisEntityCommandNotAllowedToUser.ENTITY.save(ignoredUser);

		CcpJsonRepresentation requestKey = this.request(UNIGNORED_USER, VisSkillFixHierarchyTypes.add, "go");
		CcpJsonRepresentation request = this.newRequest(requestKey, "Gin é framework web em go", "gin");
		CcpErrorFlowDisturb refusal = this.refusalOf(request);
		assertEquals(VisProcessStatusFixSkillHierarchy.userNotAllowed, refusal.status);

		String allowed = this.operatorTypes("/allowCommandToUser " + UNIGNORED_USER);
		assertEquals("O usuário " + UNIGNORED_USER + " não é mais ignorado pelo suporte: as próximas solicitações dele, em todos os comandos, voltarão a chegar ao suporte.", allowed);
		assertFalse(VisEntityCommandNotAllowedToUser.ENTITY.exists(ignoredUserKey));

		// the record is kept in the twin, for control and tracking
		CcpEntity reallowedUsers = VisEntityCommandNotAllowedToUser.ENTITY.getTwinEntity();
		assertTrue(reallowedUsers.exists(ignoredUserKey));
		CcpJsonRepresentation reallowedUser = reallowedUsers.getOneById(ignoredUserKey);
		CcpJsonRepresentation reallowedDescription = reallowedUser.getInnerJson(VisEntityCommandNotAllowedToUser.Fields.description);
		assertEquals("go", reallowedDescription.getAsString(VisEntitySkillFixHierarchyPending.Fields.parent));

		String notIgnored = this.operatorTypes("/allowCommandToUser " + UNIGNORED_USER);
		assertEquals("O usuário " + UNIGNORED_USER + " não está sendo ignorado pelo suporte", notIgnored);

		// the request goes through again: cleans what a previous run left and sends it through the service
		this.clearItems(UNIGNORED_USER, VisSkillFixHierarchyTypes.add, "go", "gin");
		this.clearRequest(UNIGNORED_USER, VisSkillFixHierarchyTypes.add, "go", "Gin é framework web em go", "gin");
		TELEGRAM.sentMessages.clear();
		VisServiceSkillFixHierarchy.FixSkillHierarchy.execute(request);

		assertTrue(VisEntitySkillFixHierarchyPending.ENTITY.exists(requestKey));
		String notice = TELEGRAM.lastMessageFor(SUPPORT_CHAT).getAsString(JnJsonCommonsFields.message);
		assertEquals("/fixSkillHierarchy add " + UNIGNORED_USER + " go", notice);
	}

	/**
	 * The command refuses to review a request of an ignored user, telling the operator how to stop ignoring the user.
	 * The request that was pending when the user was ignored no longer exists: ignoring discards every pending request
	 * of the user, so even after the user is allowed again there is nothing to review.
	 */
	@Test
	public void operatorCannotReviewAnIgnoredUser() {

		this.openRequest(BLOCKED_USER, VisSkillFixHierarchyTypes.add, "elixir", "Phoenix é framework web em elixir", "phoenix");
		// ignoring finds the pending requests by a query, which sees a write only after a refresh
		ElasticsearchLocal.refresh("vis_skill_fix_hierarchy_pending");

		CcpJsonRepresentation ignoredUserKey = CcpOtherConstants.EMPTY_JSON
				.put(VisEntityCommandNotAllowedToUser.Fields.email, BLOCKED_USER)
				.put(VisEntityCommandNotAllowedToUser.Fields.commandName, VisUserRequestCommands.fixSkillHierarchy);
		CcpJsonRepresentation ignoredDescription = CcpOtherConstants.EMPTY_JSON.put(VisEntitySkillFixHierarchyPending.Fields.parent, "outro termo");
		CcpJsonRepresentation ignoredUser = ignoredUserKey.put(VisEntityCommandNotAllowedToUser.Fields.description, ignoredDescription);
		VisEntityCommandNotAllowedToUser.ENTITY.save(ignoredUser);

		String refused = this.operatorTypes("/fixSkillHierarchy add " + BLOCKED_USER + " elixir");
		// the bot names the command in the operator's language (Portuguese by default); the canonical name still works
		String allowCommandInPortuguese = JbCommandNamesInPortuguese.allowCommandToUser.getValue();
		assertEquals("O usuário " + BLOCKED_USER + " está sendo ignorado pelo suporte e as solicitações dele não são atendidas em nenhum comando. "
				+ "Para voltar a atendê-lo, use /" + allowCommandInPortuguese + " " + BLOCKED_USER, refused);

		// since 2026-10-10 ignoring the user discards every request of the user still pending, as if it had never existed
		CcpJsonRepresentation requestKey = this.request(BLOCKED_USER, VisSkillFixHierarchyTypes.add, "elixir");
		CcpJsonRepresentation item = this.item(BLOCKED_USER, VisSkillFixHierarchyTypes.add, "elixir", "phoenix");
		assertFalse(VisEntitySkillFixHierarchyPending.ENTITY.exists(requestKey));
		assertFalse(VisEntitySkillFixHierarchyItemPending.ENTITY.exists(item));

		this.operatorTypes("/allowCommandToUser " + BLOCKED_USER);
		String request = this.operatorTypes("/fixSkillHierarchy add " + BLOCKED_USER + " elixir");
		assertFalse(request, request.contains("Itens pendentes: phoenix"));
	}

	private CcpJsonRepresentation newRequest(CcpJsonRepresentation requestKey, String description, String... skills) {
		CcpJsonRepresentation requestWithDescription = requestKey.put(VisEntitySkillFixHierarchyPending.Fields.description, description);
		CcpJsonRepresentation newRequest = requestWithDescription.put(VisEntitySkillFixHierarchyPending.Fields.skill, Arrays.asList(skills));
		return newRequest;
	}

	private CcpErrorFlowDisturb refusalOf(CcpJsonRepresentation request) {
		try {
			VisServiceSkillFixHierarchy.FixSkillHierarchy.execute(request);
		} catch (CcpErrorFlowDisturb refusal) {
			return refusal;
		}
		throw new AssertionError("The request of the ignored user was accepted: " + request);
	}

	/**
	 * The candidate withdraws a request that is still pending: the request leaves the pending entity and its ticket
	 * leaves the operator's tickets (the jn inbox at once; a cancellation is recorded for the list of the bot).
	 */
	@Test
	public void candidateWithdrawsAPendingRequest() {

		this.openRequest(WITHDRAWN_REQUEST_USER, VisSkillFixHierarchyTypes.add, "rust", "Tokio é runtime assíncrono de rust", "tokio");
		String ticket = "/fixSkillHierarchy add " + WITHDRAWN_REQUEST_USER + " rust";

		CcpJsonRepresentation requestKey = this.request(WITHDRAWN_REQUEST_USER, VisSkillFixHierarchyTypes.add, "rust");
		assertTrue(VisEntitySkillFixHierarchyPending.ENTITY.exists(requestKey));
		assertTrue(this.isPendingTicket(ticket));

		VisServiceSkillFixHierarchy.DeleteSkillFixHierarchy.execute(requestKey);

		assertFalse(VisEntitySkillFixHierarchyPending.ENTITY.exists(requestKey));
		assertFalse(this.isPendingTicket(ticket));
		assertTrue(JnEntitySupportCancelledCommand.ENTITY.exists(this.cancelledCommand(ticket)));
	}

	/**
	 * The candidate withdraws a request whose ticket {@code /pendingTickets} already moved to the list of the bot: the
	 * next listing applies the cancellation and the ticket leaves the list.
	 */
	@Test
	public void candidateWithdrawsARequestAlreadyInTheOperatorList() {

		this.openRequest(WITHDRAWN_REQUEST_USER, VisSkillFixHierarchyTypes.remove, "rust", "Serde não é rust puro", "serde");
		String ticket = "/fixSkillHierarchy remove " + WITHDRAWN_REQUEST_USER + " rust";
		this.listTheTickets();
		assertTrue(JbEntityPendingTickets.ENTITY.exists(this.listedTicket(ticket)));

		CcpJsonRepresentation requestKey = this.request(WITHDRAWN_REQUEST_USER, VisSkillFixHierarchyTypes.remove, "rust");
		VisServiceSkillFixHierarchy.DeleteSkillFixHierarchy.execute(requestKey);
		this.listTheTickets();

		assertFalse(this.isPendingTicket(ticket));
		assertFalse(JnEntitySupportCancelledCommand.ENTITY.exists(this.cancelledCommand(ticket)));
	}

	/**
	 * Runs {@code /pendingTickets}, which moves the inbox to the list of the bot and applies the cancellations, and
	 * leaves the list. Both are read by queries, which see a write only after a refresh.
	 */
	private void listTheTickets() {
		ElasticsearchLocal.refresh("jn_support_pending_command", "jb_pending_tickets", "jn_support_cancelled_command");
		this.operatorTypes("/pendingTickets");
		this.clearTheBotSession();
	}

	/**
	 * Tells whether the command is still a ticket of the operator: in the jn inbox or in the jb list.
	 */
	private boolean isPendingTicket(String command) {
		CcpJsonRepresentation pendingCommand = CcpOtherConstants.EMPTY_JSON
				.put(JnEntitySupportPendingCommand.Fields.botName, JbBotType.support)
				.put(JnEntitySupportPendingCommand.Fields.chatId, SUPPORT_CHAT)
				.put(JnEntitySupportPendingCommand.Fields.command, command);
		boolean inTheInbox = JnEntitySupportPendingCommand.ENTITY.exists(pendingCommand);
		boolean inTheList = JbEntityPendingTickets.ENTITY.exists(this.listedTicket(command));
		boolean pending = inTheInbox || inTheList;
		return pending;
	}

	private CcpJsonRepresentation listedTicket(String command) {
		CcpJsonRepresentation listedTicket = CcpOtherConstants.EMPTY_JSON
				.put(JbEntityPendingTickets.Fields.botName, JbBotType.support)
				.put(JbEntityPendingTickets.Fields.chatId, SUPPORT_CHAT)
				.put(JbEntityPendingTickets.Fields.ticket, command);
		return listedTicket;
	}

	private CcpJsonRepresentation cancelledCommand(String command) {
		CcpJsonRepresentation cancelledCommand = CcpOtherConstants.EMPTY_JSON
				.put(JnEntitySupportCancelledCommand.Fields.botName, JbBotType.support)
				.put(JnEntitySupportCancelledCommand.Fields.chatId, SUPPORT_CHAT)
				.put(JnEntitySupportCancelledCommand.Fields.command, command);
		return cancelledCommand;
	}

	/**
	 * Withdrawing a request that is not pending interrupts the flow with {@code NOT_FOUND} (404).
	 */
	@Test
	public void candidateWithdrawsARequestThatDoesNotExist() {

		CcpJsonRepresentation requestKey = this.request("ninguem@teste.com", VisSkillFixHierarchyTypes.add, "termoinexistente");

		try {
			VisServiceSkillFixHierarchy.DeleteSkillFixHierarchy.execute(requestKey);
		} catch (CcpErrorFlowDisturb notFound) {
			assertEquals(CcpProcessStatusDefault.NOT_FOUND, notFound.status);
			return;
		}
		throw new AssertionError("The withdrawal of a request that does not exist was accepted: " + requestKey);
	}

	private static final String CONSULTING_USER = "consulta.da.solicitacao@teste.com";

	private CcpJsonRepresentation consult(String email, VisSkillFixHierarchyTypes type, String parent) {
		CcpJsonRepresentation requestKey = this.request(email, type, parent);
		CcpJsonRepresentation suggestion = VisServiceSkillFixHierarchy.GetSkillFixHierarchy.execute(requestKey);
		return suggestion;
	}

	/**
	 * The candidate sees the pending request, with {@code status} {@code pending}; once the operator reviews it, the
	 * same consultation answers {@code fulfiled}; without a request the answer is empty.
	 */
	@Test
	public void candidateConsultsTheRequestThroughItsLife() {

		this.openRequest(CONSULTING_USER, VisSkillFixHierarchyTypes.add, "kotlin", "Ktor é framework de kotlin", "ktor");

		CcpJsonRepresentation pending = this.consult(CONSULTING_USER, VisSkillFixHierarchyTypes.add, "kotlin");
		assertEquals(pending.toString(), "pending", pending.getAsString(new com.ccp.decorators.CcpFieldName("status")));
		assertEquals(Arrays.asList("ktor"), pending.getAsStringList(VisEntitySkillFixHierarchyPending.Fields.skill));

		this.operatorTypes("/fixSkillHierarchy add " + CONSULTING_USER + " kotlin");
		this.operatorTypes("aprovar ktor roda sobre kotlin");

		CcpJsonRepresentation fulfiled = this.consult(CONSULTING_USER, VisSkillFixHierarchyTypes.add, "kotlin");
		assertEquals(fulfiled.toString(), "fulfiled", fulfiled.getAsString(new com.ccp.decorators.CcpFieldName("status")));

		CcpJsonRepresentation nothing = this.consult(CONSULTING_USER, VisSkillFixHierarchyTypes.remove, "kotlin");
		assertTrue(nothing.toString(), nothing.isEmpty());
	}

	/**
	 * A second request for the same e-mail, parent and type while the first one is pending is refused with
	 * {@code CONFLICT} (409).
	 */
	@Test
	public void aSecondPendingRequestIsAConflict() {

		this.openRequest(CONSULTING_USER, VisSkillFixHierarchyTypes.remove, "scala", "Akka não é scala", "akka");

		CcpJsonRepresentation requestKey = this.request(CONSULTING_USER, VisSkillFixHierarchyTypes.remove, "scala");
		CcpJsonRepresentation sameRequest = this.newRequest(requestKey, "Akka não é scala", "akka");
		try {
			VisServiceSkillFixHierarchy.FixSkillHierarchy.execute(sameRequest);
		} catch (CcpErrorFlowDisturb conflict) {
			assertEquals(CcpProcessStatusDefault.CONFLICT, conflict.status);
			return;
		}
		throw new AssertionError("The second pending request was accepted: " + sameRequest);
	}

	@Test
	public void requestThatDoesNotExist() {
		String answer = this.operatorTypes("/fixSkillHierarchy add ninguem@teste.com termoinexistente");
		assertEquals("Não há itens pendentes de ajuste (add) na hierarquia de conhecimentos para o e-mail 'ninguem@teste.com' e o termo 'termoinexistente'", answer);
	}

	@Test
	public void typeThatDoesNotExist() {
		String answer = this.operatorTypes("/fixSkillHierarchy associar " + ONE_BY_ONE_USER + " java");
		assertEquals("Não há itens pendentes de ajuste (associar) na hierarquia de conhecimentos para o e-mail '" + ONE_BY_ONE_USER + "' e o termo 'java'", answer);
	}

	/**
	 * The user associates and dissociates skills of the same parent: each request notifies the operator with the
	 * command of its own type, and each command shows and decides only the request of its type.
	 */
	@Test
	public void eachTypeIsReviewedByItsOwnCommand() {

		this.openRequest(TWO_TYPES_USER, VisSkillFixHierarchyTypes.add, "php", "Laravel é framework php", "laravel");
		String addNotice = TELEGRAM.lastMessageFor(SUPPORT_CHAT).getAsString(JnJsonCommonsFields.message);
		assertEquals("/fixSkillHierarchy add " + TWO_TYPES_USER + " php", addNotice);

		this.openRequest(TWO_TYPES_USER, VisSkillFixHierarchyTypes.remove, "php", "Wordpress é produto, não depende de php", "wordpress");
		String removeNotice = TELEGRAM.lastMessageFor(SUPPORT_CHAT).getAsString(JnJsonCommonsFields.message);
		assertEquals("/fixSkillHierarchy remove " + TWO_TYPES_USER + " php", removeNotice);

		String removeRequest = this.operatorTypes("/fixSkillHierarchy remove " + TWO_TYPES_USER + " php");
		assertTrue(removeRequest, removeRequest.startsWith("Solicitação de desassociação de " + TWO_TYPES_USER + " para o termo php"));
		assertTrue(removeRequest, removeRequest.contains("Itens pendentes: wordpress"));
		assertFalse(removeRequest, removeRequest.contains("laravel"));

		String summary = this.operatorTypes("aprovar wordpress é um produto");
		assertTrue(summary, summary.contains("Aprovados: wordpress"));

		CcpJsonRepresentation removeRequestKey = this.request(TWO_TYPES_USER, VisSkillFixHierarchyTypes.remove, "php");
		CcpJsonRepresentation addRequestKey = this.request(TWO_TYPES_USER, VisSkillFixHierarchyTypes.add, "php");
		assertTrue(VisEntitySkillFixHierarchyFulfiled.ENTITY.exists(removeRequestKey));
		assertTrue(VisEntitySkillFixHierarchyPending.ENTITY.exists(addRequestKey));

		String addRequest = this.operatorTypes("/fixSkillHierarchy add " + TWO_TYPES_USER + " php");
		assertTrue(addRequest, addRequest.startsWith("Solicitação de associação de " + TWO_TYPES_USER + " para o termo php"));
		assertTrue(addRequest, addRequest.contains("Itens pendentes: laravel"));
		assertFalse(addRequest, addRequest.contains("wordpress"));
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
	 * Leaves only the new request: removes whatever a previous run left in the request and item entities, the
	 * "already sent" records of the notices (their keys are fixed, and a repeated notice is refused, which
	 * interrupts the save) and then saves the request, which notifies the operator and creates the items.
	 */
	private void openRequest(String email, VisSkillFixHierarchyTypes type, String parent, String description, String... skills) {
		this.clearItems(email, type, parent, skills);
		this.sendRequestAgain(email, type, parent, description, skills);
	}

	private void clearItems(String email, VisSkillFixHierarchyTypes type, String parent, String... skills) {
		for (String skill : skills) {
			CcpJsonRepresentation item = this.item(email, type, parent, skill);
			VisEntitySkillFixHierarchyItemPending.ENTITY.deleteAnyWhere(item);
			VisEntitySkillFixHierarchyItemApproved.ENTITY.delete(item);
		}
	}

	/**
	 * Same as {@link #openRequest} but keeps the items: those already decided stay approved or rejected.
	 */
	private void sendRequestAgain(String email, VisSkillFixHierarchyTypes type, String parent, String description, String... skills) {
		CcpJsonRepresentation newRequest = this.clearRequest(email, type, parent, description, skills);
		VisEntitySkillFixHierarchyPending.ENTITY.save(newRequest);
	}

	/**
	 * Removes the request from the request entities and the "already sent" records of its notices, returning the
	 * complete request, ready to be saved.
	 */
	private CcpJsonRepresentation clearRequest(String email, VisSkillFixHierarchyTypes type, String parent, String description, String... skills) {

		CcpJsonRepresentation request = this.request(email, type, parent);
		CcpJsonRepresentation newRequest = this.newRequest(request, description, skills);

		// the pending entity writes through the messaging, which validates the whole record, even to delete it
		VisEntitySkillFixHierarchyPending.ENTITY.delete(newRequest);
		VisEntitySkillFixHierarchyFulfiled.ENTITY.delete(request);

		List<Class<?>> templates = Arrays.asList(
				VisMessages.VisNotifySupportAndUserAboutPendingSkillHierarchyRequest.class,
				VisMessages.VisNotifyUserAboutFulfiledSkillHierarchy.class,
				VisMessages.VisNotifyUserAboutAlreadyReviewedSkillHierarchy.class);

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
				.put(JnJsonCommonsFields.message, "/fixSkillHierarchy " + type + " " + email + " " + parent);
		JnEntityInstantMessengerMessageSent.ENTITY.delete(sentNotice);

		return newRequest;
	}

	private CcpJsonRepresentation request(String email, VisSkillFixHierarchyTypes type, String parent) {
		CcpJsonRepresentation request = CcpOtherConstants.EMPTY_JSON
				.put(VisEntitySkillFixHierarchyPending.Fields.email, email)
				.put(VisEntitySkillFixHierarchyPending.Fields.parent, parent)
				.put(VisEntitySkillFixHierarchyPending.Fields.type, type);
		return request;
	}

	private CcpJsonRepresentation item(String email, VisSkillFixHierarchyTypes type, String parent, String skill) {
		CcpJsonRepresentation request = this.request(email, type, parent);
		CcpJsonRepresentation item = request.put(VisEntitySkillFixHierarchyItemPending.Fields.skill, skill);
		return item;
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
