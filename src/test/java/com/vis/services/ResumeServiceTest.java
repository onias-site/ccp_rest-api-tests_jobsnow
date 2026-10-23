package com.vis.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;

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
		CcpInstanceProvider<CcpInstantMessenger> telegram = () -> SilentInstantMessenger.INSTANCE;
		CcpInstanceProvider<CcpEmailSender> email = () -> SilentEmailSender.INSTANCE;

		CcpDependencyInjection.removeAllDependencies();
		CcpDependencyInjection.loadAllDependencies(
				CcpLocalInstances.syncMensageriaListener,
				CcpLocalInstances.bucket,
				CcpElasticSearchQueryExecutor.INSTANCE,
				CcpElasticSearchDbRequest.INSTANCE,
				CcpMindrotPasswordHandler.INSTANCE,
				CcpLocalCacheInstances.mock,
				CcpElasticSerchDbBulk.INSTANCE,
				CcpElasticSearchCrud.INSTANCE,
				CcpGsonJsonHandler.INSTANCE,
				CcpApacheMimeHttp.INSTANCE,
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

	/** The answers carry only the fields of the resume: until 2026-10-14 they echoed the session values. */
	@Test
	public void theAnswersDoNotEchoTheSessionValues() {
		CcpJsonRepresentation sessionValues = CcpOtherConstants.EMPTY_JSON
				.put(new CcpFieldName("sessionToken"), "ABCDEFGH")
				.put(new CcpFieldName("ip"), "127.0.0.1")
				.put(new CcpFieldName("userAgent"), "test");
		List<CcpJsonRepresentation> answers = Arrays.asList(
				VisServiceResume.Save.execute(this.resume().mergeWithAnotherJson(sessionValues)),
				VisServiceResume.ChangeStatus.execute(this.key().mergeWithAnotherJson(sessionValues)),
				VisServiceResume.Delete.execute(this.key().mergeWithAnotherJson(sessionValues)));
		for (CcpJsonRepresentation answer : answers) {
			assertEquals(this.email, answer.getAsString(VisEntityResume.Fields.email));
			for (String sessionField : sessionValues.fieldSet()) {
				assertFalse(answer.toString(), answer.fieldSet().contains(sessionField));
			}
		}
	}

	/** The reading carries only what the screen uses: until 2026-10-15 it sent the internal hash keys and the hashed e-mail (R05-2). */
	@Test
	public void theReadingDoesNotCarryTheInternalFields() {
		VisServiceResume.Save.execute(this.resume());
		List<String> entityNames = Arrays.asList("vis_resume", "vis_inactive_resume");
		for (String entityName : entityNames) {
			CcpJsonRepresentation data = VisServiceResume.GetData.execute(this.key());
			CcpJsonRepresentation storedRecord = data.getInnerJson(new CcpFieldName(entityName));
			assertFalse(data.toString(), storedRecord.isEmpty());
			assertFalse(storedRecord.toString(), storedRecord.containsField(VisEntityResume.Fields.hash));
			assertFalse(storedRecord.toString(), storedRecord.containsField(VisEntityResume.Fields.email));
			assertEquals("developer", storedRecord.getAsString(VisEntityResume.Fields.desiredJob));
			VisServiceResume.ChangeStatus.execute(this.key());
		}
		VisServiceResume.Delete.execute(this.key());
	}

	/** The home office goes alone, as on the screen: until 2026-10-14 the backend accepted it with regions. */
	@Test
	public void homeOfficeWithRegionsIsRefused() {
		CcpJsonRepresentation homeOfficeWithRegions = this.resume().put(VisEntityResume.Fields.ddd, Arrays.asList(10, 11));
		try {
			VisServiceResume.Save.execute(homeOfficeWithRegions);
			fail("home office with regions was accepted");
		} catch (CcpJsonValidationError e) {
		}
		CcpJsonRepresentation homeOffice = this.resume().put(VisEntityResume.Fields.ddd, Arrays.asList(10));
		VisServiceResume.Save.execute(homeOffice);
		VisServiceResume.Delete.execute(this.key());
	}

	/** A LinkedIn profile ending with a hyphen is the address cut at the line break of a PDF: refused since 2026-10-14. */
	@Test
	public void linkedinProfileEndingWithAHyphenIsRefused() {
		CcpJsonRepresentation cutAddress = this.resume().put(VisEntityResume.Fields.linkedinAddress, "https://www.linkedin.com/in/andre-");
		try {
			VisServiceResume.Save.execute(cutAddress);
			fail("a linkedin profile ending with a hyphen was accepted");
		} catch (CcpJsonValidationError e) {
		}
		CcpJsonRepresentation hyphenInTheMiddle = this.resume().put(VisEntityResume.Fields.linkedinAddress, "https://www.linkedin.com/in/andre-silva/");
		VisServiceResume.Save.execute(hyphenInTheMiddle);
		VisServiceResume.Delete.execute(this.key());
	}

	/** The company typed out of the base comes back with the spelling of the user: until 2026-10-14 in capitals. */
	@Test
	public void companyOutOfTheBaseKeepsTheTypedSpelling() {
		CcpJsonRepresentation search = CcpOtherConstants.EMPTY_JSON.put(VisServiceCompany.FieldsToSearchCompaniesByTheirFirstThreeInitials.search, "Zyxwv Clinica Pet");
		CcpJsonRepresentation suggestions = VisServiceCompany.SearchCompaniesByTheirFirstThreeInitials.execute(search);
		assertEquals(Arrays.asList("Zyxwv Clinica Pet"), suggestions.getAsStringList(new CcpFieldName("companies")));
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
