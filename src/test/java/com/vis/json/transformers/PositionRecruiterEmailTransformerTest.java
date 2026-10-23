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
 * Finding 47, corrected in 2026-10-22: the transformer of the e-mail of the position puts in {@code domain} the part of
 * the e-mail after the {@code @} ({@code onias@jobsnow.com} has the domain {@code jobsnow.com}). Until then it put the
 * part before it, and read the e-mail from {@code recruiter}, a field the position does not have, so the domain came
 * empty and the hash was the one of an empty text.
 */
public class PositionRecruiterEmailTransformerTest {

	static {
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE);
	}

	private CcpJsonRepresentation transform(String email) {
		CcpJsonRepresentation position = CcpOtherConstants.EMPTY_JSON.put(VisJsonCommonsFields.email, email);
		CcpJsonRepresentation transformed = VisJsonTransformerPutEmailHashAndDomainRecruiter.INSTANCE.execute(position);
		return transformed;
	}

	@Test
	public void theDomainIsThePartOfTheEmailAfterTheAt() {
		assertEquals("jobsnow.com", this.transform("onias@jobsnow.com").getAsString(VisJsonCommonsFields.domain));
		assertEquals("rh.empresa.com.br", this.transform("maria@rh.empresa.com.br").getAsString(VisJsonCommonsFields.domain));
	}

	@Test
	public void theRecruiterIsTheHashOfTheEmailAndTheEmailIsKept() {
		CcpJsonRepresentation transformed = this.transform("onias@jobsnow.com");
		String expectedHash = new CcpStringDecorator("onias@jobsnow.com").hash().asString(CcpHashAlgorithm.SHA1);

		assertEquals(expectedHash, transformed.getAsString(VisJsonCommonsFields.recruiter));
		assertEquals("onias@jobsnow.com", transformed.getAsString(VisJsonCommonsFields.email));
		assertEquals("onias@jobsnow.com", transformed.getAsString(new CcpFieldName("originalRecruiter")));
	}

	@Test
	public void aJsonWithNoEmailGoesThroughUntouched() {
		CcpJsonRepresentation position = CcpOtherConstants.EMPTY_JSON.put(VisJsonCommonsFields.title, "Developer");

		CcpJsonRepresentation transformed = VisJsonTransformerPutEmailHashAndDomainRecruiter.INSTANCE.execute(position);

		assertEquals(position.fieldSet(), transformed.fieldSet());
	}
}
