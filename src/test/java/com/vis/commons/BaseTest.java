package com.vis.commons;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpFileDecorator;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.cache.gcp.memcache.CcpGcpMemCache;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.email.sendgrid.CcpSendGridEmailSender;
import com.ccp.implementations.file.bucket.gcp.CcpGcpFileBucket;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.implementations.mensageria.sender.gcp.pubsub.CcpGcpPubSubMensageriaSender;
import com.ccp.implementations.password.mindrot.CcpMindrotPasswordHandler;
import com.ccp.json.validations.global.engine.CcpJsonValidationError;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.ccp.rest.api.utils.CcpRestApiUtils;
import com.js.entities.JsEntityLoginAnswers;
import com.jn.json.fields.validation.JnJsonCommonsFields;

public class BaseTest {
	/** The single instance: the class keeps no state. */
	public final static BaseTest INSTANCE = new BaseTest();

	private BaseTest() {}

	public final static CcpJsonRepresentation REQUEST_TO_LOGIN = CcpOtherConstants.EMPTY_JSON
			.put(JnJsonCommonsFields.userAgent, "Apache-HttpClient/4.5.4 (Java/17.0.9)")
			;

	public final static CcpJsonRepresentation ANSWERS_JSON = REQUEST_TO_LOGIN.put(JsEntityLoginAnswers.Fields.goal, "jobs").put(JsEntityLoginAnswers.Fields.channel, "linkedin");

	static {
		boolean localEnvironment = CcpRestApiUtils.isLocalEnvironment();	

		
		CcpDependencyInjection.loadAllDependencies(
				CcpApacheMimeHttp.INSTANCE, 
				CcpGsonJsonHandler.INSTANCE, 
				CcpElasticSearchCrud.INSTANCE,
				CcpMindrotPasswordHandler.INSTANCE,
				CcpElasticSearchDbRequest.INSTANCE,
				localEnvironment ? CcpLocalInstances.bucket : CcpGcpFileBucket.INSTANCE,
			    localEnvironment ? CcpLocalCacheInstances.map : CcpGcpMemCache.INSTANCE,
	    		localEnvironment ? CcpLocalInstances.email : CcpSendGridEmailSender.INSTANCE,
				localEnvironment ? CcpLocalInstances.syncMensageriaListener : CcpGcpPubSubMensageriaSender.INSTANCE
				);	
		
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE, CcpElasticSearchCrud.INSTANCE,
				CcpElasticSearchDbRequest.INSTANCE, CcpApacheMimeHttp.INSTANCE,
				CcpElasticSerchDbBulk.INSTANCE); 
	}
	
	protected void saveErrors(CcpFileDecorator file, CcpJsonValidationError e) {
		String path = file.getPath();
		this.saveErrors(path, e);
		throw new VisErrorJsonFileIsInvalid(file, e);
	}
	
	protected void saveErrors(String path, CcpJsonValidationError e) {
		String errorsFilePath = path.replace(".json", "_errors.json");
		String message = e.getMessage();
		CcpFileDecorator errorsFile = new CcpStringDecorator(errorsFilePath).file().reset();
		errorsFile.append(message);
	}
	
	protected CcpJsonRepresentation getJson (String filePath) {
		CcpStringDecorator filePathDecorator = new CcpStringDecorator(filePath);
		CcpFileDecorator file = filePathDecorator.file();
		CcpJsonRepresentation json = file.asSingleJson();
		return json;
	}

	/**
	 * Exception thrown after the validation errors of a JSON file have already been written to disk, to
	 * stop the test indicating which file is invalid.
	 */
	@SuppressWarnings("serial")
	public static class VisErrorJsonFileIsInvalid extends RuntimeException {
		/**
		 * Builds the message stating which file is invalid and chains the validation errors as the cause.
		 * @param file the JSON file that failed validation
		 * @param cause the validation errors found
		 */
		private VisErrorJsonFileIsInvalid(CcpFileDecorator file, CcpJsonValidationError cause) {
			super("The json file '" + file.getPath() + "' is invalid", cause);
		}
	}

}
