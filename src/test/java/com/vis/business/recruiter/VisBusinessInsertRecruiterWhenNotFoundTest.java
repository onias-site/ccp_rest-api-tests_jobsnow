package com.vis.business.recruiter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.utils.entity.CcpEntity;
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

/**
 * {@link VisBusinessInsertRecruiterWhenNotFound} saves the recruiter only when the steps before it found no record
 * (nothing under {@code _entities}); either way it hands over the json it received.
 */
public class VisBusinessInsertRecruiterWhenNotFoundTest {

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

	private CcpJsonRepresentation recruiterKey(String email) {
		return CcpOtherConstants.EMPTY_JSON.put(VisEntityRecruiter.Fields.email, email);
	}

	private CcpJsonRepresentation recruiter(String email) {
		CcpJsonRepresentation input = this.recruiterKey(email).put(VisEntityRecruiter.Fields.source, "login");
		return VisBusinessPrepareRecruiter.INSTANCE.execute(input);
	}

	@Test
	public void withNoRecordFoundTheRecruiterIsSaved() {
		String email = "inserir" + this.now + "@recrutadora.com.br";
		CcpJsonRepresentation stepsOutput = CcpOtherConstants.EMPTY_JSON.put(VisEntityRecruiter.Fields.email, email);

		CcpJsonRepresentation result = new VisBusinessInsertRecruiterWhenNotFound(this.recruiter(email)).execute(stepsOutput);

		assertEquals(stepsOutput, result);
		CcpJsonRepresentation saved = VisEntityRecruiter.ENTITY.getOneById(this.recruiterKey(email));
		assertEquals("login", saved.getAsString(VisEntityRecruiter.Fields.source));
		VisEntityRecruiter.ENTITY.delete(this.recruiterKey(email));
	}

	@Test
	public void withARecordFoundNothingIsSaved() {
		String email = "existente" + this.now + "@recrutadora.com.br";
		CcpJsonRepresentation stepsOutput = CcpOtherConstants.EMPTY_JSON
				.put(VisEntityRecruiter.Fields.email, email)
				.addToItem(CcpEntity.JsonFieldNames._entities, VisEntityRecruiter.ENTITY, this.recruiter(email));

		CcpJsonRepresentation result = new VisBusinessInsertRecruiterWhenNotFound(this.recruiter(email)).execute(stepsOutput);

		assertEquals(stepsOutput, result);
		assertTrue(result.containsField(CcpEntity.JsonFieldNames._entities));
		assertFalse(VisEntityRecruiter.ENTITY.exists(this.recruiterKey(email)));
	}
}
