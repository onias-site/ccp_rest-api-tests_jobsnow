package com.ccp.especifications.db.utils.entity.decorators.engine;

import com.vis.business.position.VisPositionOfTheRecruiter;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.dependency.injection.CcpInstanceProvider;
import com.ccp.especifications.email.CcpEmailProvider;
import com.ccp.especifications.instant.messenger.CcpInstantMessenger;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.implementations.password.mindrot.CcpMindrotPasswordHandler;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.vis.entities.VisEntityPosition;

/**
 * The save of the position against the real Elasticsearch (2026-11-21): the {@code @CcpBeforeSaveFromMainEntity} of
 * {@code VisEntityPosition} ({@code VisBusinessPutPositionDefaultValues}) writes the default values of the absent
 * fields, keeps the informed ones, and the save fills the {@code hash} list of the position.
 */
public class PositionSaveDefaultValuesTest {

	static {
		CcpInstanceProvider<CcpInstantMessenger> telegram = () -> SilentInstantMessenger.INSTANCE;
		CcpInstanceProvider<CcpEmailProvider> email = () -> SilentEmailSender.INSTANCE;

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

	private final String recruiter = "position" + System.currentTimeMillis() + "@jobsnow.com";

	private String daysAhead(int days) {
		long instant = System.currentTimeMillis() + days * 24L * 60L * 60L * 1000L;
		String instantAsText = String.valueOf(instant);
		return instantAsText;
	}

	private CcpJsonRepresentation position(String title) {
		CcpJsonRepresentation salaryRange = new CcpJsonRepresentation("{\"min\":2000,\"max\":9000}");
		CcpJsonRepresentation expiration = CcpOtherConstants.EMPTY_JSON.put(new CcpFieldName("min"), this.daysAhead(1)).put(new CcpFieldName("max"), this.daysAhead(30));
		CcpJsonRepresentation sort = new CcpJsonRepresentation("{\"type\":\"desc\",\"field\":[\"clt\"]}");
		CcpJsonRepresentation contact = new CcpJsonRepresentation("{\"type\":\"email\",\"value\":\"selecao@jobsnow.com\"}");
		CcpJsonRepresentation java = new CcpJsonRepresentation("{\"skill\":\"JAVA\",\"word\":\"JAVA\",\"type\":\"skillsFromResume\",\"category\":\"skillsFromText\"}");
		String description = "position used to test the default values, with a description long enough to pass the minimum of one hundred characters";
		CcpJsonRepresentation position = CcpOtherConstants.EMPTY_JSON
				.put(VisEntityPosition.Fields.recruiter, this.recruiter)
				.put(VisEntityPosition.Fields.yearMonth, VisPositionOfTheRecruiter.getCurrentYearMonth())
				.put(VisEntityPosition.Fields.title, title)
				.put(VisEntityPosition.Fields.resumeType, 1)
				.put(VisEntityPosition.Fields.clt, salaryRange)
				.put(VisEntityPosition.Fields.ddd, Arrays.asList(11))
				.put(VisEntityPosition.Fields.description, description)
				.put(VisEntityPosition.Fields.disponibility, 5)
				.put(VisEntityPosition.Fields.expiration, expiration)
				.put(VisEntityPosition.Fields.seniority, List.of("senior"))
				.put(VisEntityPosition.Fields.skill, Arrays.asList(java))
				.put(VisEntityPosition.Fields.sort, Arrays.asList(sort))
				.put(VisEntityPosition.Fields.contact, Arrays.asList(contact))
				.put(VisEntityPosition.Fields.temporallyJobTime, 0);
		return position;
	}

	@Test
	public void theSaveWritesTheDefaultValuesAndTheHashes() {
		CcpJsonRepresentation position = this.position("position with the default values");
		try {
			VisEntityPosition.ENTITY.save(position);

			CcpJsonRepresentation saved = VisEntityPosition.ENTITY.getOneById(position);
			assertTrue(saved.getAsBoolean(VisEntityPosition.Fields.mustSendInGroups));
			assertEquals("daily", saved.getAsString(VisEntityPosition.Fields.frequencyType));
			List<String> hashes = saved.getAsStringList(VisEntityPosition.Fields.hash);
			assertFalse("the save did not fill the hashes", hashes.isEmpty());
		} finally {
			VisEntityPosition.ENTITY.deleteAnyWhere(position);
		}
	}

	@Test
	public void theInformedValuesAreKept() {
		CcpJsonRepresentation position = this.position("position with informed values")
				.put(VisEntityPosition.Fields.mustSendInGroups, false)
				.put(VisEntityPosition.Fields.frequencyType, "weekly");
		try {
			VisEntityPosition.ENTITY.save(position);

			CcpJsonRepresentation saved = VisEntityPosition.ENTITY.getOneById(position);
			assertFalse(saved.getAsBoolean(VisEntityPosition.Fields.mustSendInGroups));
			assertEquals("weekly", saved.getAsString(VisEntityPosition.Fields.frequencyType));
		} finally {
			VisEntityPosition.ENTITY.deleteAnyWhere(position);
		}
	}
}
