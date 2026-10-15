package com.vis.entities;

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
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.jn.entities.JnEntityInstantMessengerParametersToSend;
import com.jn.entities.JnEntityInstantMessengerTemplateMessage;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.json.fields.validation.JnJsonInstantMessengerFields;
import com.vis.messages.VisMessages;

/**
 * The seeds of {@link VisEntityResumeLinkedin}: the notice to the support operator that a LinkedIn address already in the
 * resume of another e-mail was saved again (template in Portuguese and English, with the address and the new e-mail),
 * sent to the support chat with ten tries.
 */
public class VisEntityResumeLinkedinSeedsTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler(),
				new com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp(),
				new com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest(),
				new com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud(),
				com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances.mock);
	}

	private final List<CcpBulkItem> seeds = new VisEntityResumeLinkedin().getFirstRecordsToInsert();

	private final String templateId = VisMessages.VisNotifySupportAboutSharedLinkedin.class.getName();

	private List<CcpJsonRepresentation> recordsOf(CcpEntity entity) {
		String entityName = entity.getEntityMetaData().entityName;
		List<CcpJsonRepresentation> records = this.seeds.stream()
				.filter(seed -> seed.entity.getEntityMetaData().entityName.equals(entityName))
				.map(seed -> seed.json).collect(Collectors.toList());
		assertFalse(entityName, records.isEmpty());
		return records;
	}

	@Test
	public void everySeedIsACreate() {
		for (CcpBulkItem seed : this.seeds) {
			assertEquals(CcpBulkEntityOperationType.create, seed.operation);
		}
	}

	@Test
	public void theNoticeComesInPortugueseAndEnglishWithTheAddressAndTheEmail() {
		List<CcpJsonRepresentation> templates = this.recordsOf(JnEntityInstantMessengerTemplateMessage.ENTITY);
		List<String> languages = templates.stream().map(template -> template.getAsString(JnJsonCommonsFields.language)).sorted().collect(Collectors.toList());
		assertEquals(Arrays.asList("english", "portuguese"), languages);
		for (CcpJsonRepresentation template : templates) {
			assertEquals(this.templateId, template.getAsString(JnJsonCommonsFields.templateId));
			String message = template.getAsString(JnJsonCommonsFields.message);
			assertTrue(message, message.contains("{" + VisEntityResumeLinkedin.Fields.linkedinAddress + "}"));
			assertTrue(message, message.contains("{" + JnJsonCommonsFields.email + "}"));
		}
	}

	@Test
	public void theNoticeGoesToTheSupportChatWithTenTries() {
		List<CcpJsonRepresentation> parameters = this.recordsOf(JnEntityInstantMessengerParametersToSend.ENTITY);
		assertEquals(1, parameters.size());
		CcpJsonRepresentation parameter = parameters.get(0);
		assertEquals(this.templateId, parameter.getAsString(JnJsonCommonsFields.templateId));
		assertEquals("support", parameter.getAsString(JnJsonInstantMessengerFields.botName));
		assertEquals(751717896L, (long) parameter.getAsLongNumber(JnJsonInstantMessengerFields.chatId));
		CcpJsonRepresentation moreParameters = parameter.getInnerJson(JnJsonCommonsFields.moreParameters);
		assertEquals(10, (int) moreParameters.getAsIntegerNumber(JnJsonCommonsFields.maxTriesToSendMessage));
	}
}
