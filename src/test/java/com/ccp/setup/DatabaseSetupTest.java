package com.ccp.setup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.utils.CcpDbRequester;
import com.ccp.implementations.db.bulk.elasticsearch.CcpElasticSerchDbBulk;
import com.ccp.implementations.db.crud.elasticsearch.CcpElasticSearchCrud;
import com.ccp.implementations.db.query.elasticsearch.CcpElasticSearchQueryExecutor;
import com.ccp.implementations.db.utils.elasticsearch.CcpElasticSearchDbRequest;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.ccp.local.testings.implementations.CcpLocalInstances;
import com.ccp.local.testings.implementations.cache.CcpLocalCacheInstances;

/**
 * Proves the database setup ({@code CcpDbRequester.createTables}) against the local Elasticsearch, isolated from the
 * real indices: it reads only the probe entities of {@code com.ccp.setup.probes}, whose scripts are written to a
 * temporary folder, and touches only the {@code jn_setup_probe_*} indices, all of them dropped at the end.
 * <ul>
 * <li>each entity is recreated (deleted and created again from its script), and so is its twin;</li>
 * <li>the seeds of the entities are inserted;</li>
 * <li>an auxiliary type in the folder is skipped;</li>
 * <li>a script that does not match the fields of the entity is reported in the mapping errors file, and the entity is
 * not created;</li>
 * <li>an unexpected failure (an entity without script) interrupts the setup.</li>
 * </ul>
 */
public class DatabaseSetupTest {

	private static final String[] PROBE_INDICES = { "jn_setup_probe_plain", "jn_setup_probe_twin", "jn_setup_probe_twin_copy",
			"jn_setup_probe_wrong", "jn_setup_probe_without_script" };

	private static final HttpClient HTTP = HttpClient.newHttpClient();

	private static final String STRICT_SCRIPT = "{\"mappings\":{\"dynamic\":\"strict\",\"properties\":{\"code\":{\"type\":\"keyword\"}}}}";

	private static final String SCRIPT_WITH_AN_EXTRA_FIELD = "{\"mappings\":{\"dynamic\":\"strict\",\"properties\":{\"code\":{\"type\":\"keyword\"},\"extra\":{\"type\":\"keyword\"}}}}";

	private static Path folder;

	static {
		CcpDependencyInjection.loadAllDependencies(
				CcpLocalInstances.syncMensageriaListener,
				new CcpElasticSearchQueryExecutor(),
				new CcpElasticSearchDbRequest(),
				CcpLocalCacheInstances.mock,
				new CcpElasticSerchDbBulk(),
				new CcpElasticSearchCrud(),
				new CcpGsonJsonHandler(),
				new CcpApacheMimeHttp());
	}

	private static int status(String method, String path) throws Exception {
		HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:9200/" + path))
				.method(method, HttpRequest.BodyPublishers.noBody()).build();
		int status = HTTP.send(request, HttpResponse.BodyHandlers.discarding()).statusCode();
		return status;
	}

	private static String body(String path) throws Exception {
		HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:9200/" + path)).GET().build();
		String body = HTTP.send(request, HttpResponse.BodyHandlers.ofString()).body();
		return body;
	}

	@BeforeClass
	public static void writeTheScripts() throws Exception {
		folder = Files.createTempDirectory("ccp_setup_test");
		Files.writeString(folder.resolve("jn_setup_probe_plain"), STRICT_SCRIPT);
		Files.writeString(folder.resolve("jn_setup_probe_twin"), STRICT_SCRIPT);
		Files.writeString(folder.resolve("jn_setup_probe_wrong"), SCRIPT_WITH_AN_EXTRA_FIELD);
		for (String index : PROBE_INDICES) {
			status("DELETE", index);
		}
	}

	@AfterClass
	public static void dropTheProbeIndices() throws Exception {
		for (String index : PROBE_INDICES) {
			status("DELETE", index);
		}
	}

	private String sourceFolder(String packageFolder) {
		String path = new File("src/test/java/com/ccp/setup/" + packageFolder).getAbsolutePath();
		return path;
	}

	private void runTheSetup(String packageFolder) {
		CcpDbRequester database = CcpDependencyInjection.getDependency(CcpDbRequester.class);
		database.createTables(folder.toString(), this.sourceFolder(packageFolder),
				folder.resolve("mapping-errors.txt").toString(), folder.resolve("insert-results.txt").toString());
	}

	@Test
	public void theEntitiesOfTheFolderAreRecreatedWithTheirTwinsAndSeeds() throws Exception {
		this.runTheSetup("probes");

		assertEquals(200, status("GET", "jn_setup_probe_plain"));
		assertEquals(200, status("GET", "jn_setup_probe_twin"));
		assertEquals(200, status("GET", "jn_setup_probe_twin_copy"));
		assertEquals("a script that does not match the entity creates nothing", 404, status("GET", "jn_setup_probe_wrong"));

		status("POST", "jn_setup_probe_twin/_refresh");
		assertTrue(body("jn_setup_probe_twin/_count"), body("jn_setup_probe_twin/_count").contains("\"count\":1"));

		String mappingErrors = Files.readString(folder.resolve("mapping-errors.txt"));
		assertTrue(mappingErrors, mappingErrors.contains("jn_setup_probe_wrong"));
		assertTrue(mappingErrors, mappingErrors.contains("extra"));
		assertTrue(Files.exists(folder.resolve("insert-results.txt")));
	}

	@Test
	public void runningTheSetupAgainStartsTheIndicesEmpty() throws Exception {
		this.runTheSetup("probes");
		HttpRequest addDocument = HttpRequest.newBuilder(URI.create("http://localhost:9200/jn_setup_probe_plain/_doc/1?refresh=true"))
				.header("Content-Type", "application/json").PUT(HttpRequest.BodyPublishers.ofString("{\"code\":\"x\"}")).build();
		HTTP.send(addDocument, HttpResponse.BodyHandlers.discarding());
		assertTrue(body("jn_setup_probe_plain/_count").contains("\"count\":1"));

		this.runTheSetup("probes");

		status("POST", "jn_setup_probe_plain/_refresh");
		assertTrue(body("jn_setup_probe_plain/_count"), body("jn_setup_probe_plain/_count").contains("\"count\":0"));
	}

	@Test
	public void anEntityWithoutScriptInterruptsTheSetup() {
		try {
			this.runTheSetup("broken");
			fail("the entity has no script");
		} catch (RuntimeException e) {
			assertTrue(e.getClass().getName(), e.getClass().getName().endsWith("CcpErrorElasticSearchDbSetupUnexpected"));
		}
	}
}
