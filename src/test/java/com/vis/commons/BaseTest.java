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
import com.jn.entities.JnEntityLoginAnswers;
import com.jn.json.fields.validation.JnJsonCommonsFields;

public class BaseTest {
	public final static CcpJsonRepresentation REQUEST_TO_LOGIN = CcpOtherConstants.EMPTY_JSON
			.put(JnJsonCommonsFields.userAgent, "Apache-HttpClient/4.5.4 (Java/17.0.9)")
			;

	public final static CcpJsonRepresentation ANSWERS_JSON = REQUEST_TO_LOGIN.put(JnEntityLoginAnswers.Fields.goal, "jobs").put(JnEntityLoginAnswers.Fields.channel, "linkedin");

	static {
		boolean localEnvironment = CcpRestApiUtils.isLocalEnvironment();	

		
		CcpDependencyInjection.loadAllDependencies(
				new CcpApacheMimeHttp(), 
				new CcpGsonJsonHandler(), 
				new CcpElasticSearchCrud(),
				new CcpMindrotPasswordHandler(),
				new CcpElasticSearchDbRequest(),
				localEnvironment ? CcpLocalInstances.bucket : new CcpGcpFileBucket(),
			    localEnvironment ? CcpLocalCacheInstances.map : new CcpGcpMemCache(),
	    		localEnvironment ? CcpLocalInstances.email : new CcpSendGridEmailSender(),
				localEnvironment ? CcpLocalInstances.syncMensageriaListener : new CcpGcpPubSubMensageriaSender()
				);	
		
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler(), new CcpElasticSearchCrud(),
				new CcpElasticSearchDbRequest(), new CcpApacheMimeHttp(),
				new CcpElasticSerchDbBulk()); 
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
