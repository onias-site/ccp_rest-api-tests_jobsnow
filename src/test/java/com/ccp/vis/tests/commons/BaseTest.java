package com.ccp.vis.tests.commons;

import com.ccp.constantes.CcpOtherConstants;
import com.ccp.decorators.CcpFileDecorator;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.utils.CcpDbRequester;
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
import com.ccp.implementations.text.extractor.apache.tika.CcpApacheTikaTextExtractor;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;
import com.ccp.rest.api.utils.CcpRestApiUtils;
import com.ccp.validation.CcpJsonInvalid;
import com.jn.entities.JnEntityLoginAnswers;
import com.jn.entities.JnEntityLoginSessionValidation;
import com.jn.entities.JnEntityLoginToken;

public class BaseTest {
	//FIXME faltando validações do SimpleArray
	public final static CcpJsonRepresentation REQUEST_TO_LOGIN = CcpOtherConstants.EMPTY_JSON
			.put(JnEntityLoginSessionValidation.Fields.userAgent.name(), "Apache-HttpClient/4.5.4 (Java/17.0.9)")
			.put(JnEntityLoginToken.Fields.ip.name(), "localhost:8080")
			;

	public final static CcpJsonRepresentation ANSWERS_JSON =  REQUEST_TO_LOGIN.put(JnEntityLoginAnswers.Fields.goal.name(), "jobs").put(JnEntityLoginAnswers.Fields.channel.name(), "linkedin");

	static {
		boolean localEnvironment = CcpRestApiUtils.isLocalEnvironment();	

		
		CcpDependencyInjection.loadAllDependencies(
				new CcpApacheMimeHttp(), 
				new CcpGsonJsonHandler(), 
				new CcpElasticSearchCrud(),
				new CcpMindrotPasswordHandler(),
				new CcpElasticSearchDbRequest(),
				new CcpApacheTikaTextExtractor(),
				localEnvironment ? CcpLocalInstances.bucket.getLocalImplementation() : new CcpGcpFileBucket(),
			    localEnvironment ? CcpLocalCacheInstances.map.getLocalImplementation() : new CcpGcpMemCache(),
	    		localEnvironment ? CcpLocalInstances.email.getLocalImplementation() : new CcpSendGridEmailSender(),
				localEnvironment ? CcpLocalInstances.mensageriaSender.getLocalImplementation() : new CcpGcpPubSubMensageriaSender()
				);	
		
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler(), new CcpElasticSearchCrud(),
				new CcpElasticSearchDbRequest(), new CcpApacheMimeHttp(),
				new CcpElasticSerchDbBulk());
		
//		createTables();
	}
	static void createTables() {
		String pathToCreateEntityScript = "documentation\\database\\elasticsearch\\scripts\\entities\\create";
		String pathToJavaClasses = "..\\vis-business-commons\\src\\main\\java\\com\\vis\\commons\\entities";
		String mappingJnEntitiesErrors = "c:\\logs\\mappingJnEntitiesErrors.json";
		String insertErrors = "c:\\logs\\insertErrors.json";
		CcpDbRequester database = CcpDependencyInjection.getDependency(CcpDbRequester.class);
		database.createTables(pathToCreateEntityScript, pathToJavaClasses, mappingJnEntitiesErrors, insertErrors);
	}
	
	protected void saveErrors(CcpFileDecorator file, CcpJsonInvalid e) {
		String path = file.getPath();
		this.saveErrors(path, e);
		throw new RuntimeException(e);
	}
	
	protected void saveErrors(String path, CcpJsonInvalid e) {
		String replace = path.replace(".json", "_errors.json");
		String message = e.getMessage();
		CcpFileDecorator reset = new CcpStringDecorator(replace).file().reset();
		reset.append(message);
	}
	
	protected CcpJsonRepresentation getJson (String filePath) {
		CcpStringDecorator ccpStringDecorator = new CcpStringDecorator(filePath);
		CcpFileDecorator file = ccpStringDecorator.file();
		CcpJsonRepresentation json = file.asSingleJson();
		return json;
	}
	
}
