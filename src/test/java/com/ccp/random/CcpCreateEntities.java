package com.ccp.random;

import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.utils.CcpDbRequester;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.instant.messenger.telegram.CcpTelegramInstantMessenger;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.implementations.password.mindrot.CcpMindrotPasswordHandler;
import com.ccp.implementations.text.extractor.apache.tika.CcpApacheTikaTextExtractor;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;

public class CcpCreateEntities {

	static {
		CcpDependencyInjection.loadAllDependencies(
				new CcpElasticSearchQueryExecutor(), 
				new CcpElasticSearchDbRequest(),
				CcpLocalInstances.syncMensageriaListener,
				CcpLocalInstances.bucket,
				CcpLocalInstances.email,
				new CcpApacheTikaTextExtractor(),
				new CcpMindrotPasswordHandler(),
				new CcpElasticSerchDbBulk(),
				CcpLocalCacheInstances.map,
				new CcpElasticSearchCrud(),
				new CcpGsonJsonHandler(),
				new CcpTelegramInstantMessenger(),
				new CcpApacheMimeHttp()
				);
	}

	public static void main(String[] args) {
		createEntities("jn");
		createEntities("jb");
	}

	static void createEntities(String systemName) {
		String pathToCreateEntityScript = "documentation\\" + systemName + "\\database\\elasticsearch\\scripts\\entities\\create";
		String pathToJavaClasses = "..\\" + systemName + "_business_jobsnow\\src\\main\\java\\com\\" + systemName + "\\entities";
		String mappingJnEntitiesErrors = "c:\\logs\\"
				+ systemName
				+ "\\mappingJnEntitiesErrors.json";
		String insertErrors = "c:\\logs\\"
				+ systemName
				+ "\\insertErrors.json";
		CcpDbRequester database = CcpDependencyInjection.getDependency(CcpDbRequester.class);
		database.createTables(pathToCreateEntityScript, pathToJavaClasses, mappingJnEntitiesErrors, insertErrors);
	}
}
