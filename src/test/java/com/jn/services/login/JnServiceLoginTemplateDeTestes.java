package com.jn.services.login;

import java.util.Map;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.decorators.CcpTimeDecorator;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.utils.CcpDbRequester;
import com.ccp.flow.CcpErrorFlowDisturb;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.implementations.password.mindrot.CcpMindrotPasswordHandler;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.ccp.process.CcpProcessStatus;
import com.ccp.process.CcpProcessStatusDefault;
import com.jn.rest.api.commons.VariaveisParaTeste;
import com.jn.services.JnServiceLogin;

public abstract class JnServiceLoginTemplateDeTestes {
	public final JnServiceLogin service;
	
	public JnServiceLoginTemplateDeTestes() {
		this.service = JnServiceLogin.valueOf(this.getClass().getSimpleName());
	}

	enum JsonFieldNames implements com.ccp.decorators.CcpJsonRepresentation.CcpJsonFieldName {
		actualStatus, expectedStatus, servico, request, response, timestamp
	}

	static {
		CcpDependencyInjection.loadAllDependencies(
				CcpLocalInstances.mensageriaSender,
				new CcpElasticSearchDbRequest(),
				new CcpMindrotPasswordHandler(),
				CcpLocalCacheInstances.mock,
				new CcpElasticSerchDbBulk(),
				new CcpElasticSearchCrud(),
				new CcpGsonJsonHandler(),
				CcpLocalInstances.email,
				new CcpApacheMimeHttp()
		);
		String pathToCreateEntityScript = "documentation\\jn\\database\\elasticsearch\\scripts\\entities\\create";
		String pathToJavaClasses = "..\\jn_business_jobsnow\\src\\main\\java\\com\\jn\\entities";
		String mappingJnEntitiesErrors = "c:\\logs\\mappingJnEntitiesErrors.json";
		String insertErrors = "c:\\logs\\insertErrors.json";
		CcpDbRequester database = CcpDependencyInjection.getDependency(CcpDbRequester.class);
		database.createTables(pathToCreateEntityScript, pathToJavaClasses, mappingJnEntitiesErrors, insertErrors);
	}

	protected CcpJsonRepresentation execute(CcpJsonRepresentation json, CcpProcessStatus expectedStatus) {
		int statusAtual;
		CcpJsonRepresentation resposta;
		try {
			Map<String, Object> result = this.service.execute(json.content);
			statusAtual = CcpProcessStatusDefault.OK.asNumber();
			resposta = new CcpJsonRepresentation(result);
		} catch (CcpErrorFlowDisturb e) {
			statusAtual = e.status.asNumber();
			resposta = e.json;
		}
		this.log(json, resposta, expectedStatus, statusAtual);
		expectedStatus.verifyStatus(statusAtual, ""); 
		return resposta;
	}

	private void log( CcpJsonRepresentation request, CcpJsonRepresentation response, CcpProcessStatus expectedStatus, int statusAtual) {
		String date = new CcpTimeDecorator().getFormattedDateTime("dd/MM/yyyy HH:mm:ss");
		CcpJsonRepresentation log = CcpOtherConstants.EMPTY_JSON
				.put(JsonFieldNames.servico, this.getClass().getSimpleName())
				.put(JsonFieldNames.expectedStatus, expectedStatus.asNumber())
				.put(JsonFieldNames.actualStatus, statusAtual)
				.put(JsonFieldNames.request, request)
				.put(JsonFieldNames.response, response)
				.put(JsonFieldNames.timestamp, date);
		String testName = this.getClass().getSimpleName();
		CcpJsonRepresentation res = log.getInnerJson(() -> "response").removeFields(() -> "flow");
		CcpJsonRepresentation put = log.put(() -> "response", res);
		String asPrettyJson = put.asPrettyJson();
		new CcpStringDecorator("c:\\logs\\jn\\services\\").folder().createNewFolderIfNotExists(testName)
				.writeInTheFile(expectedStatus + ".json", asPrettyJson);
	}

	protected VariaveisParaTeste comEmailInvalido() {
		return new VariaveisParaTeste(VariaveisParaTeste.INVALID_EMAIL);
	}
}
