package com.ccp.random;

import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

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

	public static void main(String[] args) {
		List<String> chosenCostCenters = chooseCostCenters();
		boolean nothingChosen = chosenCostCenters.isEmpty();
		if (nothingChosen) {
			System.out.println("CcpCreateEntities: no cost center chosen, nothing was changed.");
			return;
		}
		for (String costCenter : chosenCostCenters) {
			createEntities(costCenter);
		}
	}

	/**
	 * Recreating the entities discards what the local Elasticsearch holds, so a single dialog lists the cost
	 * centers with every box unchecked: only the ones checked by hand are recreated, and closing or cancelling
	 * the dialog recreates none.
	 */
	private static List<String> chooseCostCenters() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(new JLabel("Choose the cost centers whose entities will be recreated in the local Elasticsearch:"));
		// recreating jn alone erases the records vis seeds into jn indices (templates and sending parameters)
		panel.add(new JLabel("Note: recreating jn also erases the message templates vis seeds into jn; check vis too to restore them."));
		List<JCheckBox> checkBoxes = new ArrayList<>();
		for (String costCenter : COST_CENTERS) {
			JCheckBox checkBox = new JCheckBox(costCenter);
			checkBoxes.add(checkBox);
			panel.add(checkBox);
		}
		Object[] options = { "Recreate checked", "Cancel" };
		int chosenOption = JOptionPane.showOptionDialog(null, panel, "CcpCreateEntities",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[1]);
		List<String> chosenCostCenters = new ArrayList<>();
		boolean notConfirmed = chosenOption != JOptionPane.OK_OPTION;
		if (notConfirmed) {
			return chosenCostCenters;
		}
		for (JCheckBox checkBox : checkBoxes) {
			boolean checked = checkBox.isSelected();
			if (checked) {
				String costCenter = checkBox.getText();
				chosenCostCenters.add(costCenter);
			}
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
