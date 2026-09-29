package com.vis.rest.api.resume.validations;

import java.util.stream.Collectors;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.crud.CcpGetEntityId;
import com.ccp.especifications.db.utils.entity.CcpEntityOperationType;
import com.ccp.especifications.http.CcpHttpMethods;
import com.ccp.process.CcpProcessStatusDefault;
import com.jn.business.messages.JnMessages.JnNotifyUserAboutLoginToken;
import com.jn.entities.JnEntityAsyncTask;
import com.jn.entities.JnEntityEmailMessageSent;
import com.jn.entities.JnEntityLoginAnswers;
import com.jn.entities.JnEntityLoginEmail;
import com.jn.entities.JnEntityLoginPassword;
import com.jn.entities.JnEntityLoginToken;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.jn.status.login.JnProcessStatusCreateLoginEmail;
import com.jn.status.login.JnProcessStatusExecuteLogin;
import com.jn.utils.JnDeleteKeysFromCache;
import com.vis.commons.VisTestTemplate;
import com.vis.rest.api.resume.status.SaveResumeStatus;

public class ValidationsEndpointsCreateResume  extends VisTestTemplate{
	enum JsonFieldNames implements CcpJsonFieldName{
		sessionToken
	}

	private final String email = "onias85@gmail.com";
	private final String uri = "resume/" + this.email + "/language/portuguese";
	
	public String getUri() {
		return this.uri;
	}

	//@Test
	public void testarEmailInvalido() {
		String scenarioName = new Object(){}.getClass().getEnclosingMethod().getName();
		CcpJsonRepresentation body = super.getJsonFile("documentation/tests/resume/curriculoComArquivoInvalido.json");
		super.getJsonResponseFromEndpoint(CcpProcessStatusDefault.BAD_REQUEST, scenarioName, body, this.uri.replace("@", ""));
	}

	//@Test
	public void testarRequisicaoSemTokenDeSessao() {
		String scenarioName = new Object() {}.getClass().getEnclosingMethod().getName();
		CcpJsonRepresentation body = super.getJsonFile("documentation/tests/resume/curriculoComArquivoInvalido.json");
		super.getJsonResponseFromEndpoint(JnProcessStatusExecuteLogin.missingSessionToken, scenarioName, body, this.uri, CcpOtherConstants.EMPTY_JSON);
	}
	
	
	//@Test
	public void testarRequisicaoComTokenFalso() {
		String scenarioName = new Object() {}.getClass().getEnclosingMethod().getName();
		CcpJsonRepresentation body = super.getJsonFile("documentation/tests/resume/curriculoComArquivoInvalido.json");
		CcpJsonRepresentation bodyWithFakeSessionToken = body.put(JsonFieldNames.sessionToken, "tokenFalsoSafadoQualquer");
		super.getJsonResponseFromEndpoint(JnProcessStatusExecuteLogin.invalidSession, scenarioName, bodyWithFakeSessionToken, this.uri);

	}

	protected CcpJsonRepresentation getHeaders() {
		return super.getHeaders().put(JsonFieldNames.sessionToken, "NFDP8DV9987EVMBW1H3N56OEGYMFZB");
	}
	
	//@Test
	public void missingPasswordRegistration() {
		String scenarioName = new Object() {}.getClass().getEnclosingMethod().getName();
		this.getJsonResponseFromEndpoint(JnProcessStatusExecuteLogin.missingSavePassword, scenarioName, this.pathToJsonFile, CcpEntityOperationType.delete.getOperationCallback(JnEntityLoginPassword.ENTITY));
	}

	//@Test
	public void salvarCurriculoComArquivoInvalido() {
		
		String scenarioName = new Object() {}.getClass().getEnclosingMethod().getName();
		
		CcpJsonRepresentation jsonDeRetornoDoTeste = this
				.getJsonResponseFromEndpoint(CcpProcessStatusDefault.OK, scenarioName, "documentation/vis/tests/resume/curriculoComArquivoInvalido.json")
				.put(JnJsonCommonsFields.subjectType, JnNotifyUserAboutLoginToken.class.getName())
				;
		
		 CcpJsonRepresentation result = new CcpGetEntityId(jsonDeRetornoDoTeste)
			.toBeginProcedureAnd()
			.ifThisIdIsNotPresentInEntity(JnEntityAsyncTask.ENTITY).returnStatus(SaveResumeStatus.didNotRegisterMessaging).and()
			.ifThisIdIsNotPresentInEntity(JnEntityEmailMessageSent.ENTITY).returnStatus(SaveResumeStatus.didNotSendEmail)
			.andFinallyReturningTheseFields(jsonDeRetornoDoTeste.fieldSet().stream().map(x -> (CcpJsonFieldName)() -> x).collect(Collectors.toSet()))
			.endThisProcedureRetrievingTheResultingData(new CcpFieldName(new Object(){}.getClass().getEnclosingMethod().getName()), CcpOtherConstants.DO_NOTHING, CcpOtherConstants.DO_NOTHING, JnDeleteKeysFromCache.INSTANCE)
			;
		 
		 System.out.println(result);
	}
	
	private String pathToJsonFile = "documentation/tests/resume/curriculoParaSalvar.json";
	
	
	//@Test
	public void salvarCurriculoComArquivoValido() {
		String scenarioName = new Object() {}.getClass().getEnclosingMethod().getName();

		CcpJsonRepresentation responseFromEndpoint = this.getJsonResponseFromEndpoint(CcpProcessStatusDefault.CREATED, scenarioName, this.pathToJsonFile);
		
		 new CcpGetEntityId(responseFromEndpoint)
			.toBeginProcedureAnd()
			.ifThisIdIsNotPresentInEntity(JnEntityAsyncTask.ENTITY).returnStatus(SaveResumeStatus.didNotRegisterMessaging).and()
			.ifThisIdIsNotPresentInEntity(JnEntityEmailMessageSent.ENTITY).returnStatus(SaveResumeStatus.didNotSendEmail)
			.andFinallyReturningTheseFields(new CcpFieldName("x"))
			;
	}
	

	
	//@Test
	public void faltandoCadastrarPreRegistro() {
		String scenarioName = new Object() {}.getClass().getEnclosingMethod().getName();
		this.getJsonResponseFromEndpoint(JnProcessStatusCreateLoginEmail.missingSaveAnswers, scenarioName, this.pathToJsonFile, 
				CcpEntityOperationType.delete.getOperationCallback(JnEntityLoginAnswers.ENTITY)
				);
		
	}
	
	
	//@Test
	public void lockedPassword() {
		String scenarioName = new Object() {}.getClass().getEnclosingMethod().getName();
		this.getJsonResponseFromEndpoint(JnProcessStatusExecuteLogin.lockedPassword, scenarioName, this.pathToJsonFile, CcpEntityOperationType.save.getOperationCallback(JnEntityLoginPassword.ENTITY.getTwinEntity()));
		
	}

	
	//@Test
	public void lockedToken() {
		String scenarioName = new Object() {}.getClass().getEnclosingMethod().getName();
		this.getJsonResponseFromEndpoint(JnProcessStatusExecuteLogin.lockedToken, scenarioName, this.pathToJsonFile, CcpEntityOperationType.save.getOperationCallback(JnEntityLoginToken.ENTITY.getTwinEntity()));
		
	}

	
	//@Test
	public void faltandoCadastrarEmail() {
		String scenarioName = new Object() {}.getClass().getEnclosingMethod().getName();
		this.getJsonResponseFromEndpoint(JnProcessStatusExecuteLogin.missingSavingEmail, scenarioName, this.pathToJsonFile, CcpEntityOperationType.delete.getOperationCallback(JnEntityLoginEmail.ENTITY));

	}
	
	protected CcpHttpMethods getMethod() {
		return CcpHttpMethods.POST;
	}
	
}
