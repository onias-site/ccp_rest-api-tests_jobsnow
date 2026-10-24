package com.js.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpTimeDecorator;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.json.fields.validation.CcpJsonCommonsFields;
import com.jn.entities.JnEntityDisposableRecord;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/**
 * The two businesses that answer the login services with the data of the token: {@link JsonTransformer} opens the copy
 * of a disposable record ({@code jn_disposable_record}: the record under {@code json}, plus when it was saved and when
 * it expires, written as {@code dd/MM/yyyy - HH:mm}), and {@link LoadDataAboutToken} applies it to the copy found under
 * {@code _entities}, answering only that data with the {@code email} and the {@code sessionToken} of the request.
 */
public class LoadDataAboutTokenTest {

	static {
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE);
	}

	private static final long SAVED_AT = 1790000000000L;

	private static final long EXPIRES_AT = SAVED_AT + 3_600_000L;

	private static String formatted(long time) {
		CcpTimeDecorator timeDecorator = new CcpTimeDecorator(time);
		String formattedTime = timeDecorator.getFormattedDateTime("dd/MM/yyyy - HH:mm");
		return formattedTime;
	}

	/** The copy of a disposable record whose data is a token. */
	private CcpJsonRepresentation disposableCopy() {
		CcpJsonRepresentation data = CcpOtherConstants.EMPTY_JSON.put(JnJsonCommonsFields.token, "HASH-DO-TOKEN");
		CcpJsonRepresentation copy = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonCommonsFields.json, data)
				.put(JnJsonCommonsFields.timestamp, EXPIRES_AT)
				.put(JnEntityDisposableRecord.Fields.trueTimestamp, SAVED_AT)
				.put(JnEntityDisposableRecord.Fields.format, "ddMMyyyyHH");
		return copy;
	}

	@Test
	public void theCopyIsOpenedWithItsSavingAndExpirationDates() {
		CcpJsonRepresentation opened = JsonTransformer.INSTANCE.execute(this.disposableCopy());

		assertEquals("HASH-DO-TOKEN", opened.getAsString(JnJsonCommonsFields.token));
		assertEquals(formatted(SAVED_AT), opened.getAsString(JnJsonCommonsFields.dateItWasSaved));
		assertEquals(formatted(EXPIRES_AT), opened.getAsString(JnJsonCommonsFields.expirationDate));
		assertEquals(EXPIRES_AT, opened.getAsLongNumber(JnJsonCommonsFields.timestamp).longValue());
	}

	@Test
	public void theAnswerIsTheDataOfTheTokenWithTheEmailAndTheSessionToken() {
		CcpJsonRepresentation request = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonCommonsFields.email, "dados.do.token@teste.com")
				.put(CcpJsonCommonsFields.sessionToken, "SESSAO")
				.put(JnJsonCommonsFields.password, "nao-pode-voltar")
				.addToItem(CcpEntity.JsonFieldNames._entities, JnEntityDisposableRecord.ENTITY, this.disposableCopy());

		CcpJsonRepresentation answer = LoadDataAboutToken.INSTANCE.execute(request);

		assertEquals("dados.do.token@teste.com", answer.getAsString(JnJsonCommonsFields.email));
		assertEquals("SESSAO", answer.getAsString(CcpJsonCommonsFields.sessionToken));
		assertEquals("HASH-DO-TOKEN", answer.getAsString(JnJsonCommonsFields.token));
		assertEquals(formatted(EXPIRES_AT), answer.getAsString(JnJsonCommonsFields.expirationDate));
		// nothing else of the request goes back: neither the password nor the records read by the flow
		assertFalse(answer.toString(), answer.containsField(JnJsonCommonsFields.password));
		assertFalse(answer.toString(), answer.containsField(CcpEntity.JsonFieldNames._entities));
	}

	@Test
	public void withoutTheCopyOnlyTheEmailAndTheSessionTokenGoBack() {
		CcpJsonRepresentation request = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonCommonsFields.email, "sem.copia@teste.com")
				.put(CcpJsonCommonsFields.sessionToken, "SESSAO")
				.put(JnJsonCommonsFields.password, "nao-pode-voltar");

		CcpJsonRepresentation answer = LoadDataAboutToken.INSTANCE.execute(request);

		CcpJsonRepresentation expected = CcpOtherConstants.EMPTY_JSON
				.put(JnJsonCommonsFields.email, "sem.copia@teste.com")
				.put(CcpJsonCommonsFields.sessionToken, "SESSAO");
		assertEquals(expected.content, answer.content);
	}
}
