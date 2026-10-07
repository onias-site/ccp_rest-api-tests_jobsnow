package com.jn.business.login.solve.token;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.jn.entities.JnEntityLoginEmail;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.utils.JnLanguage;
import com.jn.utils.JnSystemProperties;

/**
 * Proves that the reset of a login token by the support answers the language of the user (finding 41): until 2026-10-07
 * it was always Portuguese, so the new token of a user who signed up in English went in Portuguese.
 */
public class ResetLanguageTest {

	static {
		CcpDependencyInjection.loadAllDependencies(
				CcpLocalInstances.syncMensageriaListener,
				new CcpElasticSearchQueryExecutor(),
				new CcpElasticSearchDbRequest(),
				CcpLocalCacheInstances.mock,
				new CcpElasticSerchDbBulk(),
				new CcpElasticSearchCrud(),
				new CcpGsonJsonHandler(),
				new CcpApacheMimeHttp());
	}

	private final String email = "reset.language." + System.nanoTime() + "@jobsnow.com";

	private final CcpJsonRepresentation loginEmail = CcpOtherConstants.EMPTY_JSON.put(JnJsonCommonsFields.email, this.email);

	/** The session of the support conversation, which is in Portuguese. */
	private final CcpJsonRepresentation supportSession = this.loginEmail.put(JnJsonCommonsFields.language, JnLanguage.portuguese.name());

	@After
	public void deleteTheLoginEmail() {
		JnEntityLoginEmail.ENTITY.delete(this.loginEmail);
	}

	private String languageOfTheReset() {
		CcpJsonRepresentation reset = JnBusinessResetLoginToken.INSTANCE.execute(this.supportSession);
		String language = reset.getAsString(JnJsonCommonsFields.language);
		return language;
	}

	@Test
	public void theResetAnswersTheLanguageRecordedForTheUser() {
		JnEntityLoginEmail.ENTITY.save(this.loginEmail.put(JnJsonCommonsFields.language, JnLanguage.english.name()));

		assertEquals(JnLanguage.english.name(), this.languageOfTheReset());
	}

	@Test
	public void aLoginEmailWithoutLanguageFallsBackToTheLanguageOfTheSystem() {
		JnEntityLoginEmail.ENTITY.save(this.loginEmail);

		assertEquals(JnSystemProperties.INSTANCE.supportLanguage(), this.languageOfTheReset());
	}

	@Test
	public void anEmailWithoutLoginRecordFallsBackToTheLanguageOfTheSystem() {
		assertEquals(JnSystemProperties.INSTANCE.supportLanguage(), this.languageOfTheReset());
	}
}
