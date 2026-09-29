package com.jb.business.bots.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.dependency.injection.CcpInstanceProvider;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.email.CcpEmailSender;
import com.ccp.especifications.instant.messenger.CcpInstantMessenger;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.implementations.password.mindrot.CcpMindrotPasswordHandler;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.jb.entities.JbEntityBotCommandStepSession;
import com.jn.business.messages.JnInstantMessageType;
import com.jn.entities.JnEntityEmailMessageSent;
import com.jn.entities.JnEntityInstantMessengerMessageSent;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;
import com.vis.entities.VisEntitySkillFixHierarchyApproved;
import com.vis.entities.VisEntitySkillFixHierarchyItemApproved;
import com.vis.entities.VisEntitySkillFixHierarchyItemPending;
import com.vis.entities.VisEntitySkillFixHierarchyPending;
import com.vis.entities.VisEntitySkillFixHierarchyRejected;
import com.vis.json.fields.validation.VisSkillFixHierarchyTypes;
import com.vis.messages.VisMessages;

/**
 * Exercises the whole circuit of a skill hierarchy fix request: the user's request notifies the support bot
 * operator with {@code /fixSkillHierarchy <parent> <email>}, the operator runs that command, reads the user's
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
				new CcpGsonJsonHandler(),
				new CcpApacheMimeHttp(),
				telegram,
				email
		);
	}

	private static final Long SUPPORT_CHAT = 751717896L;

	private static final String ONE_BY_ONE_USER = "revisao.um.a.um@teste.com";

	private static final String APPROVE_ALL_USER = "revisao.aprovar.tudo@teste.com";

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
		assertEquals("/fixSkillHierarchy java " + ONE_BY_ONE_USER, notice);

		String request = this.operatorTypes("/fixSkillHierarchy java " + ONE_BY_ONE_USER);
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
		assertTrue(VisEntitySkillFixHierarchyApproved.ENTITY.exists(request1));

		CcpJsonRepresentation email = this.emailSentTo(ONE_BY_ONE_USER, VisMessages.VisNotifyUserAboutAprovedSkillHierarchy.class);
		String body = email.getAsString(JnJsonCommonsFields.message);
		assertTrue(body, body.contains("associação com o termo java"));
		assertTrue(body, body.contains("Itens aprovados:</p><ul><li><b>spring</b>: spring é framework java</li></ul>"));
		assertTrue(body, body.contains("Itens reprovados:</p><ul><li><b>hibernate</b>: hibernate já está associado a jpa</li></ul>"));
	}

	@Test
	public void operatorApprovesEverything() {

		this.openRequest(APPROVE_ALL_USER, VisSkillFixHierarchyTypes.remove, "python", "Django não é python puro", "django", "flask");

		this.operatorTypes("/fixSkillHierarchy python " + APPROVE_ALL_USER);
		String summary = this.operatorTypes("aprovar os dois são frameworks e não linguagem");
		assertTrue(summary, summary.contains("Aprovados: django, flask"));
		assertTrue(summary, summary.contains("Reprovados: -"));

		CcpJsonRepresentation django = this.item(APPROVE_ALL_USER, VisSkillFixHierarchyTypes.remove, "python", "django");
		CcpJsonRepresentation flask = this.item(APPROVE_ALL_USER, VisSkillFixHierarchyTypes.remove, "python", "flask");
		assertTrue(VisEntitySkillFixHierarchyItemApproved.ENTITY.exists(django));
		assertTrue(VisEntitySkillFixHierarchyItemApproved.ENTITY.exists(flask));

		CcpJsonRepresentation email = this.emailSentTo(APPROVE_ALL_USER, VisMessages.VisNotifyUserAboutAprovedSkillHierarchy.class);
		String body = email.getAsString(JnJsonCommonsFields.message);
		assertTrue(body, body.contains("desassociação com o termo python"));
		assertTrue(body, body.contains("<li><b>django</b>: os dois são frameworks e não linguagem</li>"));
		assertFalse(body, body.contains("Itens reprovados"));

		// once decided, the request is no longer pending for the operator
		String again = this.operatorTypes("/fixSkillHierarchy python " + APPROVE_ALL_USER);
		assertTrue(again, again.startsWith("Não há itens pendentes"));
	}

	@Test
	public void requestThatDoesNotExist() {
		String answer = this.operatorTypes("/fixSkillHierarchy termoinexistente ninguem@teste.com");
		assertEquals("Não há itens pendentes de ajuste na hierarquia de conhecimentos para o e-mail 'ninguem@teste.com' e o termo 'termoinexistente'", answer);
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

		CcpJsonRepresentation request = this.request(email, type, parent);
		List<String> skillList = Arrays.asList(skills);
		CcpJsonRepresentation newRequest = request
				.put(VisEntitySkillFixHierarchyPending.Fields.description, description)
				.put(VisEntitySkillFixHierarchyPending.Fields.skill, skillList);

		// the pending entity writes through the messaging, which validates the whole record, even to delete it
		VisEntitySkillFixHierarchyPending.ENTITY.delete(newRequest);
		VisEntitySkillFixHierarchyApproved.ENTITY.delete(request);
		VisEntitySkillFixHierarchyRejected.ENTITY.delete(request);

		for (String skill : skills) {
			CcpJsonRepresentation item = this.item(email, type, parent, skill);
			VisEntitySkillFixHierarchyItemPending.ENTITY.deleteAnyWhere(item);
			VisEntitySkillFixHierarchyItemApproved.ENTITY.delete(item);
		}

		List<Class<?>> templates = Arrays.asList(
				VisMessages.VisNotifySupportAndUserAboutPendingSkillHierarchyRequest.class,
				VisMessages.VisNotifyUserAboutAprovedSkillHierarchy.class,
				VisMessages.VisNotifyUserAboutRejectedSkillHierarchy.class);

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
				.put(JnJsonCommonsFields.message, "/fixSkillHierarchy " + parent + " " + email);
		JnEntityInstantMessengerMessageSent.ENTITY.delete(sentNotice);

		VisEntitySkillFixHierarchyPending.ENTITY.save(newRequest);
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
