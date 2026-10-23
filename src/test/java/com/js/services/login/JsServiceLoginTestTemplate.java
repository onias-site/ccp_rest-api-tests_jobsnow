package com.js.services.login;

import com.ccp.business.CcpBusiness;
import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.decorators.CcpTimeDecorator;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.flow.CcpErrorFlowDisturb;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.instant.messenger.telegram.CcpTelegramInstantMessenger;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.implementations.password.mindrot.CcpMindrotPasswordHandler;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.ccp.process.CcpProcessStatus;
import com.ccp.process.CcpProcessStatusDefault;
import com.jn.rest.api.commons.TestVariables;
import com.js.services.JsServiceLogin;
import com.ccp.decorators.CcpJsonFieldName;

public abstract class JsServiceLoginTestTemplate {
	public final CcpBusiness service;
	
	/** The service is the item of {@code JsServiceLogin} named after the test class. */
	public JsServiceLoginTestTemplate() {
		this.service = JsServiceLogin.valueOf(this.getClass().getSimpleName());
	}

	/**
	 * For a login business that is not an item of {@code JsServiceLogin} (such as {@code JsBusinessValidateLogin}).
	 * @param service the business under test
	 */
	protected JsServiceLoginTestTemplate(CcpBusiness service) {
		this.service = service;
	}

	enum JsonFieldNames implements CcpJsonFieldName {
		actualStatus, expectedStatus, servico, request, response, timestamp
	}

	static {
		CcpDependencyInjection.removeAllDependencies();
		
		CcpDependencyInjection.loadAllDependencies(
				CcpLocalInstances.syncMensageriaListener,
				CcpElasticSearchDbRequest.INSTANCE,
				CcpMindrotPasswordHandler.INSTANCE,
				CcpLocalCacheInstances.mock,
				CcpElasticSerchDbBulk.INSTANCE,
				CcpElasticSearchCrud.INSTANCE,
				CcpGsonJsonHandler.INSTANCE,
				CcpLocalInstances.email,
				CcpTelegramInstantMessenger.INSTANCE,
				CcpApacheMimeHttp.INSTANCE
		);
	}

	protected CcpJsonRepresentation execute(CcpJsonRepresentation json, CcpProcessStatus expectedStatus) {
		int actualStatus;
		CcpJsonRepresentation response;
		try {
			response = this.service.execute(json);
			actualStatus = CcpProcessStatusDefault.OK.asNumber();
		} catch (CcpErrorFlowDisturb e) {
			actualStatus = e.status.asNumber();
			response = e.json;
		}
		this.log(json, response, expectedStatus, actualStatus);
		expectedStatus.verifyStatus(actualStatus, ""); 
		return response;
	}

	private void log( CcpJsonRepresentation request, CcpJsonRepresentation response, CcpProcessStatus expectedStatus, int actualStatus) {
		String date = new CcpTimeDecorator().getFormattedDateTime("dd/MM/yyyy HH:mm:ss");
		CcpJsonRepresentation log = CcpOtherConstants.EMPTY_JSON
				.put(JsonFieldNames.servico, this.getClass().getSimpleName())
				.put(JsonFieldNames.expectedStatus, expectedStatus.asNumber())
				.put(JsonFieldNames.actualStatus, actualStatus)
				.put(JsonFieldNames.request, request)
				.put(JsonFieldNames.response, response)
				.put(JsonFieldNames.timestamp, date);
		String testName = this.getClass().getSimpleName();
		CcpJsonRepresentation responseWithoutFlow = log.getInnerJson(() -> "response").removeFields(() -> "flow");
		CcpJsonRepresentation logWithCleanResponse = log.put(() -> "response", responseWithoutFlow);
		String logContent = logWithCleanResponse.asPrettyJson();
		new CcpStringDecorator("c:\\logs\\jn\\services\\").folder().createNewFolderIfNotExists(testName)
				.writeInTheFile(expectedStatus + ".json", logContent);
	}

	protected TestVariables withInvalidEmail() {
		return new TestVariables(TestVariables.INVALID_EMAIL);
	}
}
