package com.vis.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.hash.CcpHashAlgorithm;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.implementations.password.mindrot.CcpMindrotPasswordHandler;
import com.ccp.json.validations.global.engine.CcpJsonValidationError;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;

/**
 * Proves the resumes viewed by the recruiters ({@code vis_viewed_resume}): the LinkedIn and the e-mail are stored only
 * as SHA-1 hashes, and the alias is required, with 5 to 200 characters.
 */
public class ViewedResumeTest {

	static {
		CcpDependencyInjection.removeAllDependencies();
		CcpDependencyInjection.loadAllDependencies(
				CcpLocalInstances.syncMensageriaListener,
				CcpLocalInstances.bucket,
				CcpLocalInstances.email,
				CcpElasticSearchQueryExecutor.INSTANCE,
				CcpElasticSearchDbRequest.INSTANCE,
				CcpMindrotPasswordHandler.INSTANCE,
				CcpLocalCacheInstances.mock,
				CcpElasticSerchDbBulk.INSTANCE,
				CcpElasticSearchCrud.INSTANCE,
				CcpGsonJsonHandler.INSTANCE,
				CcpApacheMimeHttp.INSTANCE
		);
	}

	private final long now = System.currentTimeMillis();

	private final String linkedin = "https://www.linkedin.com/in/candidato-" + this.now;

	private final String email = "recrutador" + this.now + "@empresa.com.br";

	private CcpJsonRepresentation viewedResume(String alias) {
		return CcpOtherConstants.EMPTY_JSON
				.put(VisEntityViewedResume.Fields.linkedin, this.linkedin)
				.put(VisEntityViewedResume.Fields.email, this.email)
				.put(VisEntityViewedResume.Fields.alias, alias);
	}

	private static String sha1(String text) {
		return new CcpStringDecorator(text).hash().asString(CcpHashAlgorithm.SHA1);
	}

	@Test
	public void theLinkedinAndTheEmailAreStoredOnlyAsHashes() {
		CcpJsonRepresentation viewedResume = this.viewedResume("Dev Java sênior");
		VisEntityViewedResume.ENTITY.save(viewedResume);
		try {
			CcpJsonRepresentation stored = VisEntityViewedResume.ENTITY.getOneById(viewedResume);
			assertEquals(sha1(this.linkedin), stored.getAsString(VisEntityViewedResume.Fields.linkedin));
			assertEquals(sha1(this.email), stored.getAsString(VisEntityViewedResume.Fields.email));
			assertEquals("Dev Java sênior", stored.getAsString(VisEntityViewedResume.Fields.alias));
			String storedText = stored.toString();
			assertFalse(storedText.contains(this.linkedin));
			assertFalse(storedText.contains(this.email));
		} finally {
			VisEntityViewedResume.ENTITY.deleteAnyWhere(viewedResume);
		}
		assertFalse(VisEntityViewedResume.ENTITY.exists(viewedResume));
	}

	@Test
	public void theRecruiterCanRenameTheResume() {
		VisEntityViewedResume.ENTITY.save(this.viewedResume("Primeiro apelido"));
		VisEntityViewedResume.ENTITY.save(this.viewedResume("Segundo apelido"));
		CcpJsonRepresentation viewedResume = this.viewedResume("Segundo apelido");
		try {
			CcpJsonRepresentation stored = VisEntityViewedResume.ENTITY.getOneById(viewedResume);
			assertEquals("Segundo apelido", stored.getAsString(VisEntityViewedResume.Fields.alias));
			assertTrue(VisEntityViewedResume.ENTITY.exists(viewedResume));
		} finally {
			VisEntityViewedResume.ENTITY.deleteAnyWhere(viewedResume);
		}
	}

	@Test(expected = CcpJsonValidationError.class)
	public void anAliasShorterThanFiveCharactersIsRefused() {
		VisEntityViewedResume.ENTITY.save(this.viewedResume("Dev"));
	}

	@Test(expected = CcpJsonValidationError.class)
	public void anAliasLongerThanTwoHundredCharactersIsRefused() {
		VisEntityViewedResume.ENTITY.save(this.viewedResume("x".repeat(201)));
	}

	@Test(expected = CcpJsonValidationError.class)
	public void aViewedResumeWithoutAliasIsRefused() {
		CcpJsonRepresentation withoutAlias = this.viewedResume("Qualquer").removeFields(VisEntityViewedResume.Fields.alias);
		VisEntityViewedResume.ENTITY.save(withoutAlias);
	}

	@Test(expected = CcpJsonValidationError.class)
	public void anAddressThatIsNotALinkedinProfileIsRefused() {
		CcpJsonRepresentation notLinkedin = this.viewedResume("Qualquer").put(VisEntityViewedResume.Fields.linkedin, "https://example.com/perfil");
		VisEntityViewedResume.ENTITY.save(notLinkedin);
	}
}
