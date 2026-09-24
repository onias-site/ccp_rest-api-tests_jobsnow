package com.jb.instant.messenger.reader;

import java.util.HashMap;
import java.util.Map;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.bulk.CcpErrorBulkEntityRecordNotFound;
import com.ccp.especifications.db.crud.CcpCrud;
import com.ccp.especifications.db.crud.CcpUnionAllExecutor;

class FakeCrud implements CcpCrud {

	enum JsonFieldNames implements CcpJsonFieldName {
		inserted
	}

	private static final Map<String, CcpJsonRepresentation> registros = new HashMap<>();

	/**
	 * Esvazia o banco. O mapa é estático, então sobrevive à troca de instância e faz um teste enxergar o
	 * que o anterior gravou — quem usa este dublê precisa limpá-lo entre um teste e outro.
	 */
	static void limpar() {
		registros.clear();
	}

	public CcpJsonRepresentation getOneById(String entityName, String id) {

		String key = this.getKey(entityName, id);

		boolean registroNaoEncontrado = false == registros.containsKey(key);

		if (registroNaoEncontrado) {
			throw new CcpErrorBulkEntityRecordNotFound(entityName, id);
		}

		CcpJsonRepresentation registro = registros.get(key);
		return registro;
	}

	public CcpJsonRepresentation save(String entityName, CcpJsonRepresentation json, String id) {
		String key = this.getKey(entityName, id);
		boolean inserted = false == registros.containsKey(key);
		registros.put(key, json);
		CcpJsonRepresentation response = json.put(JsonFieldNames.inserted, inserted);
		return response;
	}

	public boolean isInsertedDocument(CcpJsonRepresentation saveResponse) {
		boolean inserted = saveResponse.getAsBoolean(JsonFieldNames.inserted);
		return inserted;
	}

	public boolean exists(String entityName, String id) {
		boolean exists = registros.containsKey(this.getKey(entityName, id));
		return exists;
	}

	public boolean delete(String entityName, String id) {
		CcpJsonRepresentation removido = registros.remove(this.getKey(entityName, id));
		boolean deleted = removido != null;
		return deleted;
	}

	public CcpUnionAllExecutor getUnionAllExecutor() {
		throw new UnsupportedOperationException();
	}

	private String getKey(String entityName, String id) {
		String key = entityName + "." + id;
		return key;
	}
}
