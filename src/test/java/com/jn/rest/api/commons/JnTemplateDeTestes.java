package com.jn.rest.api.commons;

import java.util.function.Function;

import com.ccp.constantes.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.decorators.CcpTimeDecorator;
import com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.utils.CcpDbRequester;
import com.ccp.especifications.http.CcpHttpHandler;
import com.ccp.especifications.http.CcpHttpMethods;
import com.ccp.especifications.http.CcpHttpResponse;
import com.ccp.especifications.http.CcpHttpResponseTransform;
import com.ccp.especifications.http.CcpHttpResponseType;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.implementations.password.mindrot.CcpMindrotPasswordHandler;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.ccp.process.CcpProcessStatus;


public abstract class JnTemplateDeTestes {
	enum JsonFieldNames implements CcpJsonFieldName{
		x, timestamp, response, request, headers, expectedStatus, actualStatus, method, url, message
	}
	protected final String ENDPOINT_URL = "http://localhost:8080/";

	static {
		CcpDependencyInjection.loadAllDependencies(
				new CcpElasticSearchDbRequest(), 
				new CcpMindrotPasswordHandler(),
				CcpLocalCacheInstances.mock,
				new CcpElasticSearchCrud(),
				new CcpGsonJsonHandler(), 
				new CcpApacheMimeHttp(), 
				new CcpElasticSerchDbBulk()
				);
		
		String pathToCreateEntityScript = "documentation\\jn\\database\\elasticsearch\\scripts\\entities\\create";
		String pathToJavaClasses = "..\\jn_business_jobsnow\\src\\main\\java\\com\\jn\\entities";
		String mappingJnEntitiesErrors = "c:\\logs\\mappingJnEntitiesErrors.json";
		String insertErrors = "c:\\logs\\insertErrors.json";
		CcpDbRequester database = CcpDependencyInjection.getDependency(CcpDbRequester.class);
		database.createTables(pathToCreateEntityScript, pathToJavaClasses, mappingJnEntitiesErrors, insertErrors);
	}


	public static void main(String[] args) {
		
	}
	
	protected abstract CcpHttpMethods getMethod();

	protected CcpJsonRepresentation getHeaders() {
		return CcpOtherConstants.EMPTY_JSON;
	}

	protected CcpJsonRepresentation testarEndpoint(String uri, CcpProcessStatus expectedStatus) {
		CcpJsonRepresentation testarEndpoint = this.testarEndpoint(expectedStatus, CcpOtherConstants.EMPTY_JSON, uri,
				CcpHttpResponseType.singleRecord);
		return testarEndpoint;
	}

	protected CcpJsonRepresentation testarEndpoint(CcpProcessStatus scenarioName, CcpJsonRepresentation body, String uri,
			CcpHttpResponseTransform<CcpJsonRepresentation> transformer) {

		CcpHttpMethods method = this.getMethod();
		CcpJsonRepresentation headers = this.getHeaders();

		int expectedStatus = scenarioName.asNumber();
		CcpHttpHandler http = new CcpHttpHandler(expectedStatus, CcpOtherConstants.DO_NOTHING);
		String path = this.ENDPOINT_URL + uri;
		String name = this.getClass().getName();
		String asUgglyJson = body.asUgglyJson();

		CcpHttpResponse response = http.ccpHttp.executeHttpRequest(path, method, headers, asUgglyJson);

		CcpJsonRepresentation responseFromEndpoint = http.executeHttpRequest(name, path, method, headers, asUgglyJson, transformer, response);

		int actualStatus = response.httpStatus;

		this.logRequestAndResponse(path, method, scenarioName, actualStatus, body, headers, responseFromEndpoint);

		String message = response.asSingleJson().getAsString(JsonFieldNames.message);
		scenarioName.verifyStatus(actualStatus, message);

		return responseFromEndpoint;
	}

	private <V> void logRequestAndResponse(String url, CcpHttpMethods method, CcpProcessStatus status, int actualStatus,
			CcpJsonRepresentation body, CcpJsonRepresentation headers, V executeHttpRequest) {

		CcpJsonRepresentation md = CcpOtherConstants.EMPTY_JSON.put(JsonFieldNames.x, executeHttpRequest);

		if (executeHttpRequest instanceof CcpJsonRepresentation json) {
			md = json;
		}

		String date = new CcpTimeDecorator().getFormattedDateTime("dd/MM/yyyy HH:mm:ss");

		int expectedStatus = status.asNumber();
		CcpJsonRepresentation put = CcpOtherConstants.EMPTY_JSON
				.put(JsonFieldNames.url, url)
				.put(JsonFieldNames.method, method)
				.put(JsonFieldNames.actualStatus, actualStatus)
				.put(JsonFieldNames.expectedStatus, expectedStatus)
				.put(JsonFieldNames.headers, headers)
				.put(JsonFieldNames.request, body)
				.put(JsonFieldNames.response, md)
				.put(JsonFieldNames.timestamp, date);
		String asPrettyJson = put.asPrettyJson();

		String testName = this.getClass().getSimpleName();
		new CcpStringDecorator("c:\\logs\\jn\\").folder().createNewFolderIfNotExists(testName)
				.writeInTheFile(status + ".json", asPrettyJson);
	}
	
	public final String execute(VariaveisParaTeste variaveisParaTeste, CcpProcessStatus expectedStatus) {
		String execute = this.execute(variaveisParaTeste, expectedStatus, x -> "");
		return execute; 
	}
	
	public abstract String execute(VariaveisParaTeste variaveisParaTeste, CcpProcessStatus expectedStatus, Function<VariaveisParaTeste, String> producer); 

}
