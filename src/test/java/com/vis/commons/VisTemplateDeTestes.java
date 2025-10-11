package com.vis.commons;

import com.ccp.constantes.CcpOtherConstants;
import com.ccp.decorators.CcpFileDecorator;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.decorators.CcpTimeDecorator;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.bulk.CcpEntityBulkOperationType;
import com.ccp.especifications.db.utils.CcpDbRequester;
import com.ccp.especifications.http.CcpHttpHandler;
import com.ccp.especifications.http.CcpHttpMethods;
import com.ccp.especifications.http.CcpHttpResponse;
import com.ccp.especifications.http.CcpHttpResponseType;
import com.ccp.especifications.mensageria.receiver.CcpBusiness;
import com.ccp.flow.CcpErrorFlowDisturb;
import com.ccp.flow.CcpTreeFlow;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.implementations.password.mindrot.CcpMindrotPasswordHandler;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.ccp.process.CcpProcessStatus;
import com.jn.business.login.JnBusinessSendUserToken;
import com.jn.db.bulk.JnExecuteBulkOperation;
import com.jn.entities.JnEntityEmailMessageSent;
import com.jn.entities.JnEntityLoginAnswers;
import com.jn.entities.JnEntityLoginEmail;
import com.jn.entities.JnEntityLoginPassword;
import com.jn.entities.JnEntityLoginPasswordAttempts;
import com.jn.entities.JnEntityLoginSessionConflict;
import com.jn.entities.JnEntityLoginToken;
import com.jn.entities.JnEntityLoginTokenAttempts;
import com.jn.status.login.JnProcessStatusCreateLoginEmail;
import com.jn.status.login.JnProcessStatusExecuteLogin;
public abstract class VisTemplateDeTestes {
	enum JsonFieldNames implements CcpJsonFieldName{
		message, statusName, x, url, method, actualStatus, expectedStatus, request, headers, response, timestamp, language
	}
	protected final String ENDPOINT_URL = "http://localhost:8081/";

	static {
		CcpDependencyInjection.loadAllDependencies(
				CcpLocalInstances.mensageriaSender,
				new CcpElasticSearchDbRequest(), 
				new CcpMindrotPasswordHandler(), 
				CcpLocalCacheInstances.mock,
				new CcpElasticSerchDbBulk(),
				new CcpElasticSearchCrud(),
				new CcpGsonJsonHandler(), 
				new CcpApacheMimeHttp(),  
				CcpLocalInstances.email
				);
		
		String pathToCreateEntityScript = "documentation\\database\\elasticsearch\\scripts\\entities\\create";
		String pathToJavaClasses = "..\\vis-business-commons\\src\\main\\java\\com\\vis\\commons\\entities";
		String mappingJnEntitiesErrors = "c:\\logs\\mappingJnEntitiesErrors.json";
		String insertErrors = "c:\\logs\\insertErrors.json";
		CcpDbRequester database = CcpDependencyInjection.getDependency(CcpDbRequester.class);
		database.createTables(pathToCreateEntityScript, pathToJavaClasses, mappingJnEntitiesErrors, insertErrors);
	}
	
	public final Object getInnerClass(){
		class InnerClass{
			
		}
		return new InnerClass();
	}
	
	
	protected abstract CcpHttpMethods getMethod();

	protected CcpJsonRepresentation getHeaders() {
		return CcpOtherConstants.EMPTY_JSON;
	}

	protected CcpJsonRepresentation testarEndpoint(String uri, String scenarioName, CcpProcessStatus expectedStatus) {
		CcpJsonRepresentation testarEndpoint = this.getJsonResponseFromEndpoint(expectedStatus, scenarioName, CcpOtherConstants.EMPTY_JSON, uri);
		return testarEndpoint;
	}

	protected CcpJsonRepresentation getJsonResponseFromEndpoint(CcpProcessStatus status, String scenarioName, CcpJsonRepresentation body, String uri) {

		CcpJsonRepresentation headers = this.getHeaders();

		CcpJsonRepresentation executeHttpRequest = this.getJsonResponseFromEndpoint(status, scenarioName, body, uri, headers);

		return executeHttpRequest;
	}


	protected CcpJsonRepresentation getJsonResponseFromEndpoint(CcpProcessStatus status, String scenarioName, CcpJsonRepresentation body, String uri,
			CcpJsonRepresentation headers) {
		CcpHttpMethods method = this.getMethod();
		int expectedStatus = status.asNumber();
		CcpHttpHandler http = new CcpHttpHandler(expectedStatus, CcpOtherConstants.DO_NOTHING);
		String path = this.ENDPOINT_URL + uri;
		String name = this.getClass().getName();
		String asUgglyJson = body.asUgglyJson(); 

		CcpHttpResponse response = http.ccpHttp.executeHttpRequest(path, method, headers, asUgglyJson);

		CcpJsonRepresentation executeHttpRequest = http.executeHttpRequest(name, path, method, headers, asUgglyJson, CcpHttpResponseType.singleRecord, response);

		int actualStatus = response.httpStatus;

		this.logRequestAndResponse(path, method, status, scenarioName, actualStatus, body, headers, executeHttpRequest);
		String message = executeHttpRequest.isInnerJson(JsonFieldNames.message) == false ? executeHttpRequest.getAsString(JsonFieldNames.message) : 
			executeHttpRequest.getValueFromPath("", JsonFieldNames.message, JsonFieldNames.statusName);
		status.verifyStatusNames(actualStatus, message);
		return executeHttpRequest;
	}

	private <V> void logRequestAndResponse(String url, CcpHttpMethods method, CcpProcessStatus status, String scenarioName, int actualStatus,
			CcpJsonRepresentation body, CcpJsonRepresentation headers, V executeHttpRequest) {

		CcpJsonRepresentation md = CcpOtherConstants.EMPTY_JSON.put(JsonFieldNames.x, executeHttpRequest);

		if (executeHttpRequest instanceof CcpJsonRepresentation json) {
			md = json;
		}

		String date = new CcpTimeDecorator().getFormattedDateTime("dd/MM/yyyy HH:mm:ss");

		int expectedStatus = status.asNumber();
		CcpJsonRepresentation put = CcpOtherConstants.EMPTY_JSON.put(JsonFieldNames.url, url).put(JsonFieldNames.method, method).put(JsonFieldNames.actualStatus, actualStatus)
				.put(JsonFieldNames.expectedStatus, expectedStatus).put(JsonFieldNames.headers, headers).put(JsonFieldNames.request, body).put(JsonFieldNames.response, md)
				.put(JsonFieldNames.timestamp, date);
		String asPrettyJson = put.asPrettyJson();

		String testName = this.getClass().getSimpleName();
		new CcpStringDecorator("c:\\logs\\vis\\logs\\").folder().createNewFolderIfNotExists(testName)
				.writeInTheFile(scenarioName + ".json", asPrettyJson);
	}
	
 
	public CcpJsonRepresentation getJsonFile(String path) {
		CcpStringDecorator ccpStringDecorator =	new CcpStringDecorator(path);
		CcpFileDecorator file = ccpStringDecorator.file();
		CcpJsonRepresentation json = file.asSingleJson();
		return json;

	}
	
	public CcpJsonRepresentation executeThisFlow(
			CcpBusiness first
			, CcpJsonRepresentation flow
			, CcpJsonRepresentation json
			) {
		
		
		try {
			CcpJsonRepresentation apply = first.apply(json);
			return apply;
		} catch (CcpErrorFlowDisturb e) {
			CcpBusiness nextFlow = flow.getAsObject(e.status);
			nextFlow.apply(json);
			CcpJsonRepresentation executeThisFlow = this.executeThisFlow(first, flow, json);
			return executeThisFlow;
		}
	}

	protected final CcpJsonRepresentation createLogin(CcpBusiness... whatToNext) {
		
		CcpJsonRepresentation sessionValuesToTest = this.getSessionValuesToTest();
		
		CcpJsonRepresentation jsonWithSubjectType = sessionValuesToTest.put(JnEntityEmailMessageSent.Fields.subjectType, JnBusinessSendUserToken.class.getName());
		
		JnExecuteBulkOperation.INSTANCE.executeBulk(
				jsonWithSubjectType 
				,CcpEntityBulkOperationType.delete 
				,JnEntityEmailMessageSent.ENTITY
				,JnEntityLoginPassword.ENTITY
				,JnEntityLoginSessionConflict.ENTITY
				,JnEntityLoginToken.ENTITY
				,JnEntityLoginEmail.ENTITY
				,JnEntityLoginPasswordAttempts.ENTITY
				,JnEntityLoginAnswers.ENTITY
				,JnEntityLoginPassword.ENTITY.getTwinEntity()
				,JnEntityLoginToken.ENTITY.getTwinEntity()
				,JnEntityLoginTokenAttempts.ENTITY
				);
		
		CcpJsonRepresentation endThisStatement = CcpTreeFlow
		.beginThisStatement()
		.tryToExecuteTheGivenFinalTargetProcess(LoginActions.ExecuteLogin).usingTheGivenJson(sessionValuesToTest)
		.butIfThisExecutionReturns(JnProcessStatusExecuteLogin.missingSavingEmail).thenExecuteTheGivenProcesses(LoginActions.CreateLoginEmail)
		.and()
		.ifThisExecutionReturns(JnProcessStatusCreateLoginEmail.missingSavePassword).thenExecuteTheGivenProcesses(
				LoginActions.SaveAnswers, LoginActions.CreateLoginToken, LoginActions.readTokenFromReceivedEmail, 
				LoginActions.SavePassword, LoginActions.renameTokenField, LoginActions.ExecuteLogout)
		.and()
		.ifThisExecutionReturns(JnProcessStatusCreateLoginEmail.missingSaveAnswers).thenExecuteTheGivenProcesses(LoginActions.SaveAnswers)
		.and()
		.endThisStatement(whatToNext);
		
		return endThisStatement; 
	}
	//LATER melhorar a intuitividade dos bounds no retorno do json

	protected final CcpJsonRepresentation getJsonResponseFromEndpoint(CcpProcessStatus processStatus, String scenarioName,
			String pathToJsonFile, CcpBusiness... whatToNext) {
		CcpJsonRepresentation jsonFile = this.getJsonFile(pathToJsonFile);
		CcpJsonRepresentation loginData = this.createLogin(whatToNext);
		CcpJsonRepresentation body = loginData.putAll(jsonFile);
		CcpJsonRepresentation headers = loginData;
		
		String uri = this.getUri();
		CcpJsonRepresentation responseFromEndpoint = this.getJsonResponseFromEndpoint(processStatus, scenarioName, body, uri, headers);
		JnEntityLoginEmail.ENTITY.delete(body);
		JnEntityLoginPassword.ENTITY.delete(body);
		JnEntityLoginPasswordAttempts.ENTITY.delete(body);
		JnEntityLoginAnswers.ENTITY.delete(body);
		JnEntityLoginPassword.ENTITY.getTwinEntity().delete(body);
		JnEntityLoginTokenAttempts.ENTITY.delete(body);
		JnEntityLoginToken.ENTITY.getTwinEntity().delete(body);
		JnEntityLoginToken.ENTITY.delete(body);
		
		return responseFromEndpoint;
	}

	protected abstract String getUri();

	protected final CcpJsonRepresentation getSessionValuesToTest() {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(JnEntityLoginToken.Fields.email, "onias85@gmail.com")
				.put(JnEntityLoginToken.Fields.userAgent, "Apache-HttpClient/4.5.4 (Java/17.0.9)")
				.put(JsonFieldNames.language, "portuguese")
				.put(JnEntityLoginToken.Fields.ip, "127.0.0.1")
				;

		return json;
	}

}
