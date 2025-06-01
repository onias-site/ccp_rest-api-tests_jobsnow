package com.ccp.performance;

import com.ccp.constantes.CcpOtherConstants;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.implementations.password.mindrot.CcpMindrotPasswordHandler;
import com.jn.entities.JnEntityJobsnowError;

public class PocHashPerformance {
	static{
		CcpDependencyInjection.loadAllDependencies(new CcpApacheMimeHttp(), new CcpGsonJsonHandler(), new CcpElasticSearchDbRequest());
	}
	public static void main(String[] args) {
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler(), new CcpElasticSearchCrud(),
				new CcpElasticSearchDbRequest(), new CcpApacheMimeHttp(), new CcpMindrotPasswordHandler(),
				new CcpElasticSerchDbBulk());

		Object exists = JnEntityJobsnowError.ENTITY.calculateId(CcpOtherConstants.EMPTY_JSON.put(JnEntityJobsnowError.Fields.type.name(), "org.springframework.web.HttpRequestMethodNotSupportedException"));
		System.out.println("" + exists);
		
	}
	
}
