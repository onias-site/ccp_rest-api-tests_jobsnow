package com.vis.business.recruiter.resumes;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.vis.entities.VisEntityRecruiter;

/**
 * {@link VisResumesToTheRecruiterSender} skips, without sending, the recruiter out of a restricted run, the recruiter
 * who stopped viewing resumes and the recruiter with no resume to receive. The sending itself (the list of the screen
 * "see all" and the e-mail) is proved by {@link ResumesToRecruitersTest}, whose runs go through this class.
 */
public class VisResumesToTheRecruiterSenderTest {

	static {
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE);
	}

	private static final String RECRUITER = "recrutador@empresa.com.br";

	private static final long VIEWS_SINCE = 1_000L;

	private final int[] emailsSent = {0};

	private final CcpJsonRepresentation recruiter = CcpOtherConstants.EMPTY_JSON.put(VisEntityRecruiter.Fields.email, RECRUITER);

	private VisResumesToTheRecruiterSender sender(List<String> restrictedTo, Map<String, Long> lastViewByRecruiter, List<CcpJsonRepresentation> resumes) {
		return new VisResumesToTheRecruiterSender(restrictedTo, lastViewByRecruiter, VIEWS_SINCE, resumes, new HashMap<>(), new HashMap<>(), "http://localhost", this.emailsSent);
	}

	@Test
	public void aRecruiterOutOfARestrictedRunReceivesNothing() {
		this.sender(List.of("another@empresa.com.br"), new HashMap<>(), List.of()).accept(this.recruiter);

		assertEquals(0, this.emailsSent[0]);
	}

	@Test
	public void aRecruiterWhoStoppedViewingReceivesNothing() {
		Map<String, Long> lastViewByRecruiter = new HashMap<>();
		lastViewByRecruiter.put(VisBusinessSendResumesToRecruiters.getEmailHash(RECRUITER), VIEWS_SINCE - 1);

		this.sender(List.of(), lastViewByRecruiter, List.of()).accept(this.recruiter);

		assertEquals(0, this.emailsSent[0]);
	}

	@Test
	public void aRecruiterWithoutResumesReceivesNothing() {
		this.sender(List.of(RECRUITER), new HashMap<>(), List.of()).accept(this.recruiter);

		assertEquals(0, this.emailsSent[0]);
	}
}
