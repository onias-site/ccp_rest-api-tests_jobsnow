package com.ccp.random;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import com.ccp.decorators.CcpFileDecorator;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.random.CcpRandomScripts.JsonFieldNames;

/** Consumer of {@link CcpRandomScripts#extracted()}: lists, for each field of the mapping of a script, the entity classes that have it. */
class RandomScriptsFieldsByEntityCollector implements Consumer<CcpFileDecorator> {

	/** Receives the entity classes of each field. */
	private final Map<String, List<String>> map;

	RandomScriptsFieldsByEntityCollector(Map<String, List<String>> map) {
		this.map = map;
	}

	public void accept(CcpFileDecorator file) {
		String fileName = file.getName();
		String javaClassName = "JnEntity" + new CcpStringDecorator(fileName).text().toCamelCase().toString();
		CcpJsonRepresentation json = file.asSingleJson().getInnerJsonFromPath(JsonFieldNames.mappings, JsonFieldNames.properties);
		Set<String> fields = json.fieldSet();
		for (String field : fields) {
			List<String> javaClassesName = this.map.getOrDefault(field, new ArrayList<>());
			javaClassesName.add(javaClassName);
			this.map.put(field, javaClassesName);
		}
	}
}
