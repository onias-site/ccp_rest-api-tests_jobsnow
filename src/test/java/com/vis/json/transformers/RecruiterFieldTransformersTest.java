package com.vis.json.transformers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

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
import com.ccp.implementations.password.mindrot.CcpMindrotPasswordHandler;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.vis.entities.VisEntityRecruiter;
import com.vis.entities.VisEntityViewedResume;

/**
 * The field transformers of the recruiter and of the resume viewed by them: the domain taken from the e-mail, whether
 * the e-mail is professional (its domain is out of the non-professional domains seeded in {@code jn_system_message}),
 * and the LinkedIn kept only as a hash. A json without the source field goes through untouched.
 */
public class RecruiterFieldTransformersTest {

	static {
		CcpDependencyInjection.removeAllDependencies();
		CcpDependencyInjection.loadAllDependencies(
				CcpLocalInstances.syncMensageriaListener,
				CcpLocalInstances.bucket,
				CcpLocalInstances.instantMessenger,
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

	private static CcpJsonRepresentation recruiter(String email) {
		return CcpOtherConstants.EMPTY_JSON.put(VisEntityRecruiter.Fields.email, email);
	}

	@Test
	public void theDomainIsTheWholeTextAfterTheAt() {
		CcpJsonRepresentation transformed = VisJsonTransformerRecruiterDomain.INSTANCE.execute(recruiter("maria@rh.empresa.com.br"));

		assertEquals("rh.empresa.com.br", transformed.getAsString(VisEntityRecruiter.Fields.domain));
	}

	@Test
	public void aRecruiterWithoutEmailHasNoDomain() {
		CcpJsonRepresentation withoutEmail = CcpOtherConstants.EMPTY_JSON.put(VisEntityRecruiter.Fields.source, "test");

		CcpJsonRepresentation transformed = VisJsonTransformerRecruiterDomain.INSTANCE.execute(withoutEmail);

		assertSame(withoutEmail, transformed);
	}

	@Test
	public void theDomainTransformerNamesItsFieldAndCanBePartOfAKey() {
		VisJsonTransformerRecruiterDomain transformer = VisJsonTransformerRecruiterDomain.INSTANCE;

		assertEquals(VisEntityRecruiter.Fields.domain.name(), transformer.name());
		assertTrue(transformer.canBePrimaryKey());
	}

	@Test
	public void aCompanyEmailIsProfessional() {
		CcpJsonRepresentation transformed = VisJsonTransformerRecruiterProfessional.INSTANCE.execute(recruiter("maria@empresa.com.br"));

		assertTrue(transformed.getAsBoolean(VisEntityRecruiter.Fields.professional));
	}

	@Test
	public void aWebmailEmailIsNotProfessional() {
		CcpJsonRepresentation transformed = VisJsonTransformerRecruiterProfessional.INSTANCE.execute(recruiter("maria@hotmail.com"));

		assertFalse(transformed.getAsBoolean(VisEntityRecruiter.Fields.professional));
	}

	@Test
	public void aRecruiterWithoutEmailIsNotClassified() {
		CcpJsonRepresentation withoutEmail = CcpOtherConstants.EMPTY_JSON.put(VisEntityRecruiter.Fields.source, "test");

		CcpJsonRepresentation transformed = VisJsonTransformerRecruiterProfessional.INSTANCE.execute(withoutEmail);

		assertSame(withoutEmail, transformed);
	}

	@Test
	public void theProfessionalTransformerNamesItsFieldAndCanNotBePartOfAKey() {
		VisJsonTransformerRecruiterProfessional transformer = VisJsonTransformerRecruiterProfessional.INSTANCE;

		assertEquals(VisEntityRecruiter.Fields.professional.name(), transformer.name());
		assertFalse("it depends on a list kept in the database", transformer.canBePrimaryKey());
	}

	@Test
	public void theLinkedinIsKeptOnlyAsAStableHash() {
		String linkedin = "https://www.linkedin.com/in/alguem";
		CcpJsonRepresentation view = CcpOtherConstants.EMPTY_JSON.put(VisEntityViewedResume.Fields.linkedin, linkedin);
		VisJsonTransformerLinkedinHash transformer = VisJsonTransformerLinkedinHash.INSTANCE;

		String hash = transformer.execute(view).getAsString(VisEntityViewedResume.Fields.linkedin);
		String sameHash = transformer.execute(view).getAsString(VisEntityViewedResume.Fields.linkedin);

		assertNotEquals(linkedin, hash);
		assertTrue("a SHA-1 in hexadecimal: " + hash, hash.matches("-?[0-9a-f]{1,40}"));
		assertEquals("the hash is part of the primary key, so it never changes", hash, sameHash);
	}

	@Test
	public void aViewWithoutLinkedinGoesThroughUntouched() {
		CcpJsonRepresentation withoutLinkedin = CcpOtherConstants.EMPTY_JSON.put(VisEntityViewedResume.Fields.alias, "apelido");

		CcpJsonRepresentation transformed = VisJsonTransformerLinkedinHash.INSTANCE.execute(withoutLinkedin);

		assertSame(withoutLinkedin, transformed);
	}

	@Test
	public void theLinkedinTransformerNamesItsFieldAndCanBePartOfAKey() {
		VisJsonTransformerLinkedinHash transformer = VisJsonTransformerLinkedinHash.INSTANCE;

		assertEquals(VisEntityViewedResume.Fields.linkedin.name(), transformer.name());
		assertTrue(transformer.canBePrimaryKey());
	}
}
