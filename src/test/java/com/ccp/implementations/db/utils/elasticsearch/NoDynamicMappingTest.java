package com.ccp.implementations.db.utils.elasticsearch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.utils.entity.fields.CcpErrorDbUtilsIncorrectEntityFields;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

/**
 * Rule NDM (2026-11-20): no mapping has {@code "dynamic": "true"}, at any depth, because every key written in such an
 * object became a new field and the mapping grew with the data. {@link ElasticSearchDbRequester#validateNoDynamicField}
 * rejects it; an object kept whole is {@code "type": "object", "enabled": false}. Every script of the entities follows
 * the rule.
 */
public class NoDynamicMappingTest {

	static {
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE);
	}

	private static final String SCRIPTS_ROOT = "documentation";

	private static final String CREATE_FOLDER = "/database/elasticsearch/scripts/entities/create";

	private final ElasticSearchDbRequester requester = new ElasticSearchDbRequester();

	private String messageOf(String properties) {
		CcpJsonRepresentation propertiesJson = new CcpJsonRepresentation(properties);
		try {
			this.requester.validateNoDynamicField(propertiesJson, "test_entity", "");
		} catch (CcpErrorDbUtilsIncorrectEntityFields e) {
			return e.getMessage();
		}
		return "";
	}

	@Test
	public void aFieldWithDynamicTrueIsRejected() {
		String message = this.messageOf("{\"json\": {\"dynamic\": \"true\", \"properties\": {}}, \"email\": {\"type\": \"keyword\"}}");
		assertTrue(message, message.contains("'json'"));
		assertTrue(message, message.contains("test_entity"));
		assertTrue("the message says what to do instead: " + message, message.contains("\"enabled\": false"));
	}

	@Test
	public void dynamicTrueAsABooleanIsRejectedToo() {
		String message = this.messageOf("{\"headers\": {\"dynamic\": true, \"properties\": {}}}");
		assertTrue(message, message.contains("'headers'"));
	}

	@Test
	public void aDynamicTrueInsideAnObjectIsRejectedWithItsPath() {
		String message = this.messageOf("{\"outer\": {\"properties\": {\"inner\": {\"dynamic\": \"true\", \"properties\": {}}}}}");
		assertTrue(message, message.contains("'outer.inner'"));
	}

	@Test
	public void anObjectKeptWholeAndAStrictObjectAreAccepted() {
		String message = this.messageOf("{\"json\": {\"type\": \"object\", \"enabled\": false}, "
				+ "\"outer\": {\"dynamic\": \"strict\", \"properties\": {\"inner\": {\"type\": \"keyword\"}}}, \"email\": {\"type\": \"keyword\"}}");
		assertEquals("", message);
	}

	@Test
	public void noScriptOfTheEntitiesHasDynamicTrue() throws IOException {
		List<String> rejected = new ArrayList<>();
		int checked = 0;
		try (Stream<Path> centers = Files.list(Paths.get(SCRIPTS_ROOT))) {
			for (Path center : (Iterable<Path>) centers::iterator) {
				Path createFolder = Paths.get(center.toString() + CREATE_FOLDER);
				if(false == Files.isDirectory(createFolder)) {
					continue;
				}
				try (Stream<Path> scripts = Files.list(createFolder)) {
					for (Path script : (Iterable<Path>) scripts::iterator) {
						String content = new String(Files.readAllBytes(script), StandardCharsets.UTF_8);
						CcpJsonRepresentation scriptJson = new CcpJsonRepresentation(content);
						CcpJsonRepresentation mappings = scriptJson.getInnerJson(ElasticSearchDbRequesterSpecialWords.JsonFieldNames.mappings);
						CcpJsonRepresentation properties = mappings.getInnerJson(ElasticSearchDbRequesterSpecialWords.JsonFieldNames.properties);
						checked++;
						try {
							this.requester.validateNoDynamicField(properties, script.getFileName().toString(), "");
						} catch (CcpErrorDbUtilsIncorrectEntityFields e) {
							rejected.add(e.getMessage());
						}
					}
				}
			}
		}
		assertTrue("the scripts of the entities were found: " + checked, checked > 50);
		if(false == rejected.isEmpty()) {
			fail(String.join("\n", rejected));
		}
	}
}
