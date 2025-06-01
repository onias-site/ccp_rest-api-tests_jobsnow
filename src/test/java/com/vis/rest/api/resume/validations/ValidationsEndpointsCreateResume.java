package com.vis.rest.api.resume.validations;

import org.junit.Test;

import com.ccp.constantes.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.crud.CcpGetEntityId;
import com.ccp.especifications.db.utils.CcpEntityCrudOperationType;
import com.ccp.http.CcpHttpMethods;
import com.ccp.process.CcpProcessStatusDefault;
import com.jn.entities.JnEntityAsyncTask;
import com.jn.entities.JnEntityEmailMessageSent;
import com.jn.entities.JnEntityLoginAnswers;
import com.jn.entities.JnEntityLoginEmail;
import com.jn.entities.JnEntityLoginPassword;
import com.jn.entities.JnEntityLoginToken;
import com.jn.status.login.JnProcessStatusCreateLoginEmail;
import com.jn.status.login.JnProcessStatusExecuteLogin;
import com.jn.utils.JnDeleteKeysFromCache;
import com.vis.commons.VisTemplateDeTestes;
import com.vis.rest.api.resume.status.SaveResumeStatus;

public class ValidationsEndpointsCreateResume  extends VisTemplateDeTestes{

	private final String email = "onias85@gmail.com";
	private final String uri = "resume/" + this.email + "/language/portuguese";
	
	public String getUri() {
		return this.uri;
	}

	@Test
	public void testarEmailInvalido() {
		String scenarioName = new Object(){}.getClass().getEnclosingMethod().getName();
		CcpJsonRepresentation body = super.getJsonFile("documentation/tests/resume/curriculoComArquivoInvalido.json");
		super.getJsonResponseFromEndpoint(CcpProcessStatusDefault.BAD_REQUEST, scenarioName, body, this.uri.replace("@", ""));
	}

	@Test
	public void testarRequisicaoSemTokenDeSessao() {
		String scenarioName = new Object() {}.getClass().getEnclosingMethod().getName();
		CcpJsonRepresentation body = super.getJsonFile("documentation/tests/resume/curriculoComArquivoInvalido.json");
		super.getJsonResponseFromEndpoint(JnProcessStatusExecuteLogin.missingSessionToken, scenarioName, body, this.uri, CcpOtherConstants.EMPTY_JSON);
	}
	
	
	@Test
	public void testarRequisicaoComTokenFalso() {
		String scenarioName = new Object() {}.getClass().getEnclosingMethod().getName();
		CcpJsonRepresentation body = super.getJsonFile("documentation/tests/resume/curriculoComArquivoInvalido.json");
		CcpJsonRepresentation bodyWithFakeSessionToken = body.put("sessionToken", "tokenFalsoSafadoQualquer");
		super.getJsonResponseFromEndpoint(JnProcessStatusExecuteLogin.invalidSession, scenarioName, bodyWithFakeSessionToken, this.uri);

	}

	protected CcpJsonRepresentation getHeaders() {
		return super.getHeaders().put("sessionToken", "NFDP8DV9987EVMBW1H3N56OEGYMFZB");
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void faltandoCadastrarSenha() {
		String scenarioName = new Object() {}.getClass().getEnclosingMethod().getName();
		this.getJsonResponseFromEndpoint(JnProcessStatusExecuteLogin.missingSavePassword, scenarioName, this.pathToJsonFile, JnEntityLoginPassword.ENTITY.getOperationCallback(CcpEntityCrudOperationType.delete));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void salvarCurriculoComArquivoInvalido() {
		
		String scenarioName = new Object() {}.getClass().getEnclosingMethod().getName();
		
		CcpJsonRepresentation jsonDeRetornoDoTeste = this.getJsonResponseFromEndpoint(CcpProcessStatusDefault.SUCCESS, scenarioName, "documentation/tests/resume/curriculoComArquivoInvalido.json");
		
		 CcpJsonRepresentation result = new CcpGetEntityId(jsonDeRetornoDoTeste)
			.toBeginProcedureAnd()
			.ifThisIdIsNotPresentInEntity(JnEntityAsyncTask.ENTITY).returnStatus(SaveResumeStatus.naoCadastrouMensageria).and()
			.ifThisIdIsNotPresentInEntity(JnEntityEmailMessageSent.ENTITY).returnStatus(SaveResumeStatus.naoEnviouEmail)
			.andFinallyReturningTheseFields(jsonDeRetornoDoTeste.fieldSet())
			.endThisProcedureRetrievingTheResultingData(new Object(){}.getClass().getEnclosingMethod().getName(), CcpOtherConstants.DO_NOTHING, JnDeleteKeysFromCache.INSTANCE)
			;
		 
		 System.out.println(result);
	}
	
	private String pathToJsonFile = "documentation/tests/resume/curriculoParaSalvar.json";
	
	@SuppressWarnings("unchecked")
	@Test
	public void salvarCurriculoComArquivoValido() {
		String scenarioName = new Object() {}.getClass().getEnclosingMethod().getName();

		CcpJsonRepresentation responseFromEndpoint = this.getJsonResponseFromEndpoint(CcpProcessStatusDefault.CREATED, scenarioName, this.pathToJsonFile);
		
		 new CcpGetEntityId(responseFromEndpoint)
			.toBeginProcedureAnd()
			.ifThisIdIsNotPresentInEntity(JnEntityAsyncTask.ENTITY).returnStatus(SaveResumeStatus.naoCadastrouMensageria).and()
			.ifThisIdIsNotPresentInEntity(JnEntityEmailMessageSent.ENTITY).returnStatus(SaveResumeStatus.naoEnviouEmail)
			.andFinallyReturningTheseFields("x")
			;
	}
	

	@SuppressWarnings("unchecked")
	@Test
	public void faltandoCadastrarPreRegistro() {
		String scenarioName = new Object() {}.getClass().getEnclosingMethod().getName();
		this.getJsonResponseFromEndpoint(JnProcessStatusCreateLoginEmail.missingSaveAnswers, scenarioName, this.pathToJsonFile, 
				JnEntityLoginAnswers.ENTITY.getOperationCallback(CcpEntityCrudOperationType.delete)
				);
		
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void senhaBloqueada() {
		String scenarioName = new Object() {}.getClass().getEnclosingMethod().getName();
		this.getJsonResponseFromEndpoint(JnProcessStatusExecuteLogin.lockedPassword, scenarioName, this.pathToJsonFile, JnEntityLoginPassword.ENTITY.getTwinEntity().getOperationCallback(CcpEntityCrudOperationType.save));
		
	}

	@SuppressWarnings("unchecked")
	@Test
	public void tokenBloqueado() {
		String scenarioName = new Object() {}.getClass().getEnclosingMethod().getName();
		this.getJsonResponseFromEndpoint(JnProcessStatusExecuteLogin.lockedToken, scenarioName, this.pathToJsonFile, JnEntityLoginToken.ENTITY.getTwinEntity().getOperationCallback(CcpEntityCrudOperationType.save));
		
	}

	@SuppressWarnings("unchecked")
	@Test
	public void faltandoCadastrarEmail() {
		String scenarioName = new Object() {}.getClass().getEnclosingMethod().getName();
		this.getJsonResponseFromEndpoint(JnProcessStatusExecuteLogin.missingSavingEmail, scenarioName, this.pathToJsonFile, JnEntityLoginEmail.ENTITY.getOperationCallback(CcpEntityCrudOperationType.delete));

	}
	
	protected CcpHttpMethods getMethod() {
		return CcpHttpMethods.POST;
	}
	
}
