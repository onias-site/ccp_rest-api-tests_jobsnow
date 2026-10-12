package com.ccp.random;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


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
				new CcpMindrotPasswordHandler(),
				new CcpElasticSerchDbBulk(),
				CcpLocalCacheInstances.map,
				new CcpElasticSearchCrud(),
				new CcpGsonJsonHandler(),
				new CcpTelegramInstantMessenger(),
				new CcpApacheMimeHttp()
				);
	}

	// in the order the recreation must run: vis seeds message templates into jn indices, so jn goes first
	private static final String[] COST_CENTERS = { "jn", "vis", "jb" };

	/**
	 * Recreates the entities of the cost centers given as arguments ({@code jn}, {@code vis}, {@code jb}), always in
	 * the order of {@link #COST_CENTERS} whatever the order typed; without arguments, recreates all three. Until
	 * 2026-10-11 a checkbox dialog asked which ones, and nothing ran without someone at the screen.
	 * @param args the cost centers to recreate; none means all of them
	 */
	public static void main(String[] args) {
		List<String> chosenCostCenters = chooseCostCenters(args);
		for (String costCenter : chosenCostCenters) {
			createEntities(costCenter);
		}
		System.out.println("CcpCreateEntities: recreated " + chosenCostCenters);
	}

	/**
	 * The cost centers to recreate, in the order of {@link #COST_CENTERS}: the ones given, or all of them when none
	 * is given. Recreating jn alone erases the records vis seeds into jn indices (templates and sending parameters),
	 * so asking for jn without vis prints a warning.
	 * @throws IllegalArgumentException when an argument is not a cost center
	 */
	private static List<String> chooseCostCenters(String[] args) {
		List<String> allCostCenters = Arrays.asList(COST_CENTERS);
		boolean noArguments = args.length == 0;
		if (noArguments) {
			return allCostCenters;
		}
		List<String> givenCostCenters = Arrays.asList(args);
		for (String given : givenCostCenters) {
			boolean unknown = false == allCostCenters.contains(given);
			if (unknown) {
				throw new IllegalArgumentException("Unknown cost center '" + given + "'; use any of " + allCostCenters + ", or nothing for all of them");
			}
		}
		List<String> chosenCostCenters = new ArrayList<>();
		for (String costCenter : COST_CENTERS) {
			boolean chosen = givenCostCenters.contains(costCenter);
			if (chosen) {
				chosenCostCenters.add(costCenter);
			}
		}
		boolean jnWithoutVis = chosenCostCenters.contains("jn") && false == chosenCostCenters.contains("vis");
		if (jnWithoutVis) {
			System.out.println("CcpCreateEntities: recreating jn without vis erases the message templates vis seeds into jn.");
		}
		return chosenCostCenters;
	}

	static void createEntities(String systemName) {
		String pathToCreateEntityScript = "documentation\\" + systemName + "\\database\\elasticsearch\\scripts\\entities\\create";
		String pathToJavaClasses = "..\\" + systemName + "_business_jobsnow\\src\\main\\java\\com\\" + systemName + "\\entities";
		String mappingJnEntitiesErrors = "c:\\logs\\"
				+ systemName
				+ "\\mappingJnEntitiesErrors.json";
		String insertResults = "c:\\logs\\"
				+ systemName
				+ "\\insertResults.json";
		CcpDbRequester database = CcpDependencyInjection.getDependency(CcpDbRequester.class);
		database.createTables(pathToCreateEntityScript, pathToJavaClasses, mappingJnEntitiesErrors, insertResults);
	}
}
