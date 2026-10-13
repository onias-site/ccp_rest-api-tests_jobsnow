package com.vis.services;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.dependency.injection.CcpInstanceProvider;
import com.ccp.especifications.db.utils.entity.decorators.engine.SilentEmailSender;
import com.ccp.especifications.db.utils.entity.decorators.engine.SilentInstantMessenger;
import com.ccp.especifications.email.CcpEmailSender;
import com.ccp.especifications.instant.messenger.CcpInstantMessenger;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.implementations.password.mindrot.CcpMindrotPasswordHandler;
import com.ccp.flow.CcpErrorFlowDisturb;
import com.ccp.json.validations.global.engine.CcpJsonValidationError;
import com.ccp.process.CcpProcessStatusDefault;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.vis.entities.VisEntityResume;

/**
 * Proves the items of {@link VisServiceResume} that take only the key of the resume ({@code GetData}, {@code Delete},
 * {@code ChangeStatus}): they find their input rules and run their business, which up to 2026-10-12 failed with
 * {@code JnErrorServiceValidationClassNotFound} (finding 53).
 */
public class ResumeServiceTest {

	static {
		CcpInstanceProvider<CcpInstantMessenger> telegram = () -> new SilentInstantMessenger();
		CcpInstanceProvider<CcpEmailSender> email = () -> new SilentEmailSender();

		CcpDependencyInjection.removeAllDependencies();
		CcpDependencyInjection.loadAllDependencies(
				CcpLocalInstances.syncMensageriaListener,
				CcpLocalInstances.bucket,
				new CcpElasticSearchQueryExecutor(),
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

	private final String email = "service" + System.currentTimeMillis() + "@jobsnow.com";

	private CcpJsonRepresentation key() {
		return CcpOtherConstants.EMPTY_JSON.put(VisEntityResume.Fields.email, this.email);
	}

	private CcpJsonRepresentation resume() {
		return this.key()
				.put(VisEntityResume.Fields.ddd, Arrays.asList(11))
				.put(VisEntityResume.Fields.desiredJob, "developer")
				.put(VisEntityResume.Fields.disponibility, 5)
				.put(VisEntityResume.Fields.experience, 4)
				.put(VisEntityResume.Fields.linkedinAddress, "https://www.linkedin.com/in/onias")
				.put(VisEntityResume.Fields.resumeType, 2)
				.put(VisEntityResume.Fields.temporallyJobTime, 0)
				.put(VisEntityResume.Fields.pj, 9_000);
	}

	private boolean foundIn(CcpJsonRepresentation data, String entityName) {
		CcpJsonRepresentation inTheEntity = data.getInnerJson(new CcpFieldName(entityName));
		return false == inTheEntity.isEmpty();
	}

	@Test
	public void theItemsThatTakeTheKeyRunTheirBusiness() {
		VisServiceResume.Save.execute(this.resume());

		CcpJsonRepresentation active = VisServiceResume.GetData.execute(this.key());
		assertTrue(active.toString(), this.foundIn(active, "vis_resume"));

		// the change of status toggles: deactivates the active resume...
		VisServiceResume.ChangeStatus.execute(this.key());
		CcpJsonRepresentation inactive = VisServiceResume.GetData.execute(this.key());
		assertTrue(inactive.toString(), this.foundIn(inactive, "vis_inactive_resume"));
		assertFalse(inactive.toString(), this.foundIn(inactive, "vis_resume"));

		// ...and reactivates the inactive one
		VisServiceResume.ChangeStatus.execute(this.key());
		CcpJsonRepresentation reactivated = VisServiceResume.GetData.execute(this.key());
		assertTrue(reactivated.toString(), this.foundIn(reactivated, "vis_resume"));
		assertFalse(reactivated.toString(), this.foundIn(reactivated, "vis_inactive_resume"));

		// the delete removes it for good, active or inactive
		VisServiceResume.ChangeStatus.execute(this.key());
		VisServiceResume.Delete.execute(this.key());
		CcpJsonRepresentation afterDelete = VisServiceResume.GetData.execute(this.key());
		assertFalse(afterDelete.toString(), this.foundIn(afterDelete, "vis_resume"));
		assertFalse(afterDelete.toString(), this.foundIn(afterDelete, "vis_inactive_resume"));

		// with no resume left, there is no status to change
		try {
			VisServiceResume.ChangeStatus.execute(this.key());
			fail("the status of a resume that does not exist was changed");
		} catch (CcpErrorFlowDisturb e) {
			assertTrue(e.toString(), e.status.equals(CcpProcessStatusDefault.NOT_FOUND));
		}
	}

	@Test
	public void theKeyIsRequired() {
		try {
			VisServiceResume.GetData.execute(CcpOtherConstants.EMPTY_JSON);
			fail("a request without the e-mail was accepted");
		} catch (CcpJsonValidationError e) {
		}
	}
}
