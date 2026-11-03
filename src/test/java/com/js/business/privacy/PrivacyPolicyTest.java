package com.js.business.privacy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.json.validations.global.engine.CcpJsonValidationError;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.implementations.password.mindrot.CcpMindrotPasswordHandler;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.js.services.JsServicePrivacyPolicy;

/**
 * The privacy policy read by {@code GET /privacy-policy}: the text of each language seeded in
 * {@code JnEntitySystemMessage}, the English one for a language with no text of its own, and the refusal of an unknown
 * language.
 */
public class PrivacyPolicyTest {

	static {
		CcpDependencyInjection.removeAllDependencies();
		CcpDependencyInjection.loadAllDependencies(
				CcpLocalInstances.syncMensageriaListener,
				CcpElasticSearchDbRequest.INSTANCE,
				CcpMindrotPasswordHandler.INSTANCE,
				CcpLocalCacheInstances.mock,
				CcpElasticSerchDbBulk.INSTANCE,
				CcpElasticSearchCrud.INSTANCE,
				CcpElasticSearchQueryExecutor.INSTANCE,
				CcpGsonJsonHandler.INSTANCE,
				CcpApacheMimeHttp.INSTANCE
		);
	}

	/** Saves the texts of the policy again, so that the index follows what is declared in java. */
	@BeforeClass
	public static void resaveThePrivacyPolicy() {
		List<CcpBulkItem> seeds = JsPrivacyPolicyMessages.getSeeds();
		for (CcpBulkItem seed : seeds) {
			seed.entity.save(seed.json);
		}
	}

	private String getPrivacyPolicy(String language) {
		CcpJsonRepresentation request = CcpOtherConstants.EMPTY_JSON.put(JnJsonCommonsFields.language, language);
		CcpJsonRepresentation response = JsServicePrivacyPolicy.GetPrivacyPolicy.execute(request);
		String privacyPolicy = response.getAsString(JnJsonCommonsFields.message);
		return privacyPolicy;
	}

	@Test
	public void portugueseComesInPortuguese() {
		String privacyPolicy = this.getPrivacyPolicy("portuguese");
		assertTrue(privacyPolicy.startsWith("# Política de privacidade\n## 1. Quem somos\n"));
		assertTrue(privacyPolicy.contains("privacidade@onias.site"));
	}

	@Test
	public void englishComesInEnglish() {
		String privacyPolicy = this.getPrivacyPolicy("english");
		assertTrue(privacyPolicy.startsWith("# Privacy policy\n## 1. Who we are\n"));
	}

	@Test
	public void spanishFallsBackToEnglish() {
		String spanish = this.getPrivacyPolicy("spanish");
		String english = this.getPrivacyPolicy("english");
		assertEquals(english, spanish);
	}

	@Test(expected = CcpJsonValidationError.class)
	public void unknownLanguageIsRefused() {
		this.getPrivacyPolicy("klingon");
	}
}
