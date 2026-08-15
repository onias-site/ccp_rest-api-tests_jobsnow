package com.jn.messages;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.ccp.aop.CcpNullParameterException;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.jn.business.http.JnBusinessSendHttpRequest;
import com.jn.business.messages.JnMessageSenderExceptionHandler;
import com.jn.entities.JnEntityJobsnowError;

public class JnSendMessageToUserFluentTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	static class NoopBusiness extends JnBusinessSendHttpRequest {
		NoopBusiness() {
			super(json -> json, JnMessageSenderExceptionHandler.THROWS);
		}
	}

	private JnSendMessageToUser.JnCreateStep freshCreateStep() {
		return new JnSendMessageToUser().createStep();
	}

	// ── JnAndWithTheParametersEntity ─────────────────────────────────────────

	@Test
	public void andWithTheParametersEntityBuiltTest() {
		JnSendMessageToUser.JnAndWithTheParametersEntity r = freshCreateStep()
				.withTheProcess(new NoopBusiness())
				.andWithTheParametersEntity(JnEntityJobsnowError.ENTITY);
		assertNotNull(r);
	}

	@Test(expected = CcpNullParameterException.class)
	public void andWithTheParametersEntityAndWithTemplateNullTest() {
		freshCreateStep()
				.withTheProcess(new NoopBusiness())
				.andWithTheParametersEntity(JnEntityJobsnowError.ENTITY)
				.andWithTheTemplateEntity(null);
	}

	// ── JnAndWithTheTemplateEntity ───────────────────────────────────────────

	@Test
	public void andWithTheTemplateEntityBuiltTest() {
		JnSendMessageToUser.JnAndWithTheTemplateEntity t = freshCreateStep()
				.withTheProcess(new NoopBusiness())
				.andWithTheParametersEntity(JnEntityJobsnowError.ENTITY)
				.andWithTheTemplateEntity(JnEntityJobsnowError.ENTITY);
		assertNotNull(t);
	}

	@Test(expected = CcpNullParameterException.class)
	public void andWithTheTemplateEntityAndCreateAnotherStepBlockNullTest() {
		freshCreateStep()
				.withTheProcess(new NoopBusiness())
				.andWithTheParametersEntity(JnEntityJobsnowError.ENTITY)
				.andWithTheTemplateEntity(JnEntityJobsnowError.ENTITY)
				.andCreateAnotherStep(null, JnEntityJobsnowError.ENTITY);
	}

	@Test(expected = CcpNullParameterException.class)
	public void andWithTheTemplateEntityAndCreateAnotherStepAlreadySentNullTest() {
		freshCreateStep()
				.withTheProcess(new NoopBusiness())
				.andWithTheParametersEntity(JnEntityJobsnowError.ENTITY)
				.andWithTheTemplateEntity(JnEntityJobsnowError.ENTITY)
				.andCreateAnotherStep(JnEntityJobsnowError.ENTITY, null);
	}

	@Test(expected = CcpNullParameterException.class)
	public void andWithTheTemplateEntitySoWithAllAddedStepsBlockNullTest() {
		freshCreateStep()
				.withTheProcess(new NoopBusiness())
				.andWithTheParametersEntity(JnEntityJobsnowError.ENTITY)
				.andWithTheTemplateEntity(JnEntityJobsnowError.ENTITY)
				.soWithAllAddedStepsAnd(null, JnEntityJobsnowError.ENTITY);
	}

	@Test(expected = CcpNullParameterException.class)
	public void andWithTheTemplateEntitySoWithAllAddedStepsAlreadySentNullTest() {
		freshCreateStep()
				.withTheProcess(new NoopBusiness())
				.andWithTheParametersEntity(JnEntityJobsnowError.ENTITY)
				.andWithTheTemplateEntity(JnEntityJobsnowError.ENTITY)
				.soWithAllAddedStepsAnd(JnEntityJobsnowError.ENTITY, null);
	}

	// ── JnSoWithAllAddedStepsAnd ─────────────────────────────────────────────

	private JnSendMessageToUser.JnSoWithAllAddedStepsAnd freshSoStep() {
		return freshCreateStep()
				.withTheProcess(new NoopBusiness())
				.andWithTheParametersEntity(JnEntityJobsnowError.ENTITY)
				.andWithTheTemplateEntity(JnEntityJobsnowError.ENTITY)
				.soWithAllAddedStepsAnd(JnEntityJobsnowError.ENTITY, JnEntityJobsnowError.ENTITY);
	}

	@Test
	public void soWithAllAddedStepsAndBuiltTest() {
		assertNotNull(freshSoStep());
	}

	@Test(expected = CcpNullParameterException.class)
	public void soWithAllAddedStepsAndWithTemplateIdNullTest() {
		freshSoStep().withTheTemplateEntity(null);
	}

	// ── JnWithTheTemplateId ──────────────────────────────────────────────────

	@Test
	public void withTheTemplateIdBuiltTest() {
		JnSendMessageToUser.JnWithTheTemplateId t = freshSoStep().withTheTemplateEntity("tid");
		assertNotNull(t);
	}

	@Test(expected = CcpNullParameterException.class)
	public void withTheTemplateIdAndWithEntityToBlockNullTest() {
		freshSoStep().withTheTemplateEntity("tid").andWithTheEntityToBlockMessageResend(null);
	}

	// ── JnAndWithTheEntityToBlockMessageResend ───────────────────────────────

	@Test
	public void andWithTheEntityToBlockBuiltTest() {
		JnSendMessageToUser.JnAndWithTheEntityToBlockMessageResend r = freshSoStep()
				.withTheTemplateEntity("tid")
				.andWithTheEntityToBlockMessageResend(JnEntityJobsnowError.ENTITY);
		assertNotNull(r);
	}

	@Test(expected = CcpNullParameterException.class)
	public void andWithTheEntityToBlockAndWithMessageValuesNullTest() {
		freshSoStep()
				.withTheTemplateEntity("tid")
				.andWithTheEntityToBlockMessageResend(JnEntityJobsnowError.ENTITY)
				.andWithTheMessageValuesFromJson(null);
	}

	// ── JnAndWithTheJsonValues ───────────────────────────────────────────────

	@Test
	public void andWithTheJsonValuesBuiltTest() {
		JnSendMessageToUser.JnAndWithTheJsonValues r = freshSoStep()
				.withTheTemplateEntity("tid")
				.andWithTheEntityToBlockMessageResend(JnEntityJobsnowError.ENTITY)
				.andWithTheMessageValuesFromJson(CcpOtherConstants.EMPTY_JSON);
		assertNotNull(r);
	}

	@Test(expected = CcpNullParameterException.class)
	public void andWithTheJsonValuesAndWithSupportLanguageNullTest() {
		freshSoStep()
				.withTheTemplateEntity("tid")
				.andWithTheEntityToBlockMessageResend(JnEntityJobsnowError.ENTITY)
				.andWithTheMessageValuesFromJson(CcpOtherConstants.EMPTY_JSON)
				.andWithTheSupportLanguage(null);
	}

	// ── JnAndWithTheSupportLanguage ──────────────────────────────────────────

	@Test
	public void andWithTheSupportLanguageBuiltTest() {
		JnSendMessageToUser.JnAndWithTheSupportLanguage r = freshSoStep()
				.withTheTemplateEntity("tid")
				.andWithTheEntityToBlockMessageResend(JnEntityJobsnowError.ENTITY)
				.andWithTheMessageValuesFromJson(CcpOtherConstants.EMPTY_JSON)
				.andWithTheSupportLanguage("portuguese");
		assertNotNull(r);
	}

	// sendAllMessages() precisa de CcpCrud DI real — coberto pelos testes de integração
}
