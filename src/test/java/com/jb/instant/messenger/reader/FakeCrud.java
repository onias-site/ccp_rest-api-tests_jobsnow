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

	private static final Map<String, CcpJsonRepresentation> records = new HashMap<>();

	/**
	 * Empties the database. The map is static, so it survives the change of instance and makes a test see what the
	 * previous one saved: whoever uses this double must clear it between one test and another.
	 */
	static void clear() {
		records.clear();
	}

	public CcpJsonRepresentation getOneById(String entityName, String id) {

		String key = this.getKey(entityName, id);

		boolean recordNotFound = false == records.containsKey(key);

		if (recordNotFound) {
			throw new CcpErrorBulkEntityRecordNotFound(entityName, id);
		}

		CcpJsonRepresentation record = records.get(key);
		return record;
	}

	public CcpJsonRepresentation save(String entityName, CcpJsonRepresentation json, String id) {
		String key = this.getKey(entityName, id);
		boolean inserted = false == records.containsKey(key);
		records.put(key, json);
		CcpJsonRepresentation response = json.put(JsonFieldNames.inserted, inserted);
		return response;
	}

	public boolean isInsertedDocument(CcpJsonRepresentation saveResponse) {
		boolean inserted = saveResponse.getAsBoolean(JsonFieldNames.inserted);
		return inserted;
	}

	public boolean exists(String entityName, String id) {
		boolean exists = records.containsKey(this.getKey(entityName, id));
		return exists;
	}

	public boolean delete(String entityName, String id) {
		CcpJsonRepresentation removido = records.remove(this.getKey(entityName, id));
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
