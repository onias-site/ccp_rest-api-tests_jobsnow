package com.vis.json.transformers;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.hash.CcpHashAlgorithm;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.vis.json.fields.validation.VisJsonCommonsFields;

/**
 * The transformer of the {@code recruiter} of the position (since 2026-11-21 the primary key of the position): the
 * e-mail of the recruiter becomes its SHA-1 hash, the original goes on in {@code originalRecruiter}, and a recruiter
 * already hashed is not hashed again.
 */
public class PositionRecruiterEmailTransformerTest {

	static {
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE);
	}

	private CcpJsonRepresentation transform(String recruiter) {
		CcpJsonRepresentation position = CcpOtherConstants.EMPTY_JSON.put(VisJsonCommonsFields.recruiter, recruiter);
		CcpJsonRepresentation transformed = VisJsonTransformerRecruiterHash.INSTANCE.execute(position);
		return transformed;
	}

	@Test
	public void theRecruiterIsTheHashOfTheEmailAndTheEmailIsKept() {
		CcpJsonRepresentation transformed = this.transform("onias@jobsnow.com");
		String expectedHash = new CcpStringDecorator("onias@jobsnow.com").hash().asString(CcpHashAlgorithm.SHA1);

		assertEquals(expectedHash, transformed.getAsString(VisJsonCommonsFields.recruiter));
		assertEquals("onias@jobsnow.com", transformed.getAsString(new CcpFieldName("originalRecruiter")));
	}

	@Test
	public void aRecruiterAlreadyHashedIsNotHashedAgain() {
		String hash = new CcpStringDecorator("onias@jobsnow.com").hash().asString(CcpHashAlgorithm.SHA1);

		CcpJsonRepresentation transformed = this.transform(hash);

		assertEquals(hash, transformed.getAsString(VisJsonCommonsFields.recruiter));
	}

	@Test
	public void aJsonWithNoRecruiterGoesThroughUntouched() {
		CcpJsonRepresentation position = CcpOtherConstants.EMPTY_JSON.put(VisJsonCommonsFields.title, "Developer");

		CcpJsonRepresentation transformed = VisJsonTransformerRecruiterHash.INSTANCE.execute(position);

		assertEquals(position.fieldSet(), transformed.fieldSet());
	}
}
