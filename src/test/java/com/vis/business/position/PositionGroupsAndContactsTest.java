package com.vis.business.position;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.json.validations.global.engine.CcpJsonValidationError;
import com.ccp.json.validations.global.engine.CcpJsonValidatorEngine;
import com.vis.entities.VisEntityPosition;

/**
 * The rules between the publishing in the groups and the contacts of a position (since 2026-11-25), both in the entity
 * ({@code VisEntityPosition.Fields}) and in the input of the screen ({@code VisPositionOfTheRecruiterFields}): the
 * contacts are required only when the position goes to the groups of instant messages (the default), and a position
 * out of the groups that receives no resumes is refused, since it would go nowhere.
 */
public class PositionGroupsAndContactsTest {

	static {
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE);
	}

	private static final List<Class<?>> VALIDATION_CLASSES = Arrays.asList(VisEntityPosition.Fields.class, VisPositionOfTheRecruiterFields.class);

	private CcpJsonRepresentation position() {
		long now = System.currentTimeMillis();
		CcpJsonRepresentation salaryRange = new CcpJsonRepresentation("{\"min\":2000,\"max\":9000}");
		CcpJsonRepresentation expiration = CcpOtherConstants.EMPTY_JSON
				.put(new CcpFieldName("min"), String.valueOf(now + 86_400_000L))
				.put(new CcpFieldName("max"), String.valueOf(now + 30 * 86_400_000L));
		CcpJsonRepresentation sort = new CcpJsonRepresentation("{\"type\":\"desc\",\"field\":[\"clt\"]}");
		String description = "position used to test the rules of the groups and the contacts, with a description long enough to pass the minimum";
		return CcpOtherConstants.EMPTY_JSON
				.put(VisPositionOfTheRecruiterFields.email, "groups" + now + "@jobsnow.com")
				.put(VisEntityPosition.Fields.recruiter, "groups" + now + "@jobsnow.com")
				.put(VisEntityPosition.Fields.yearMonth, VisPositionOfTheRecruiter.getCurrentYearMonth())
				.put(VisEntityPosition.Fields.title, "position of the groups")
				.put(VisEntityPosition.Fields.resumeType, 1)
				.put(VisEntityPosition.Fields.clt, salaryRange)
				.put(VisEntityPosition.Fields.ddd, Arrays.asList(11))
				.put(VisEntityPosition.Fields.description, description)
				.put(VisEntityPosition.Fields.disponibility, 5)
				.put(VisEntityPosition.Fields.expiration, expiration)
				.put(VisEntityPosition.Fields.seniority, List.of("senior"))
				.put(VisEntityPosition.Fields.sort, Arrays.asList(sort))
				.put(VisEntityPosition.Fields.temporallyJobTime, 0)
				.put(VisEntityPosition.Fields.resumesEmail, "rh@jobsnow.com");
	}

	private CcpJsonRepresentation withContact(CcpJsonRepresentation position) {
		CcpJsonRepresentation contact = new CcpJsonRepresentation("{\"type\":\"email\",\"value\":\"selecao@jobsnow.com\"}");
		return position.put(VisEntityPosition.Fields.contact, Arrays.asList(contact));
	}

	private static void valid(String reason, CcpJsonRepresentation position) {
		for (Class<?> validationClass : VALIDATION_CLASSES) {
			CcpJsonValidatorEngine.INSTANCE.validateJson(validationClass, position, "PositionGroupsAndContactsTest");
		}
		assertTrue(reason, true);
	}

	private static void refused(String reason, CcpJsonRepresentation position) {
		for (Class<?> validationClass : VALIDATION_CLASSES) {
			try {
				CcpJsonValidatorEngine.INSTANCE.validateJson(validationClass, position, "PositionGroupsAndContactsTest");
				fail(reason + " (" + validationClass.getSimpleName() + ")");
			} catch (CcpJsonValidationError expected) {
			}
		}
	}

	@Test
	public void theContactsAreRequiredOnlyInTheGroups() {
		valid("in the groups, with contacts", this.withContact(this.position()).put(VisEntityPosition.Fields.mustSendInGroups, true));
		refused("in the groups, without contacts", this.position().put(VisEntityPosition.Fields.mustSendInGroups, true));
		refused("in the groups by default, without contacts", this.position());
		valid("out of the groups, without contacts", this.position().put(VisEntityPosition.Fields.mustSendInGroups, false).put(VisEntityPosition.Fields.frequencyType, "daily"));
		valid("out of the groups, with contacts", this.withContact(this.position()).put(VisEntityPosition.Fields.mustSendInGroups, false));
	}

	@Test
	public void outOfTheGroupsTheResumesMustBeReceived() {
		CcpJsonRepresentation outOfTheGroups = this.position().put(VisEntityPosition.Fields.mustSendInGroups, false);
		refused("out of the groups and receiving no resumes goes nowhere", outOfTheGroups.put(VisEntityPosition.Fields.frequencyType, "none"));
		valid("out of the groups, the candidates send their resumes", outOfTheGroups.put(VisEntityPosition.Fields.frequencyType, "byTheCandidates"));
		valid("out of the groups, the default frequency receives resumes", outOfTheGroups);
		valid("in the groups, no resumes is allowed", this.withContact(this.position()).put(VisEntityPosition.Fields.frequencyType, "none"));
	}
}
