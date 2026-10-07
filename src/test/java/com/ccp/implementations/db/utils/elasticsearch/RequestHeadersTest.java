package com.ccp.implementations.db.utils.elasticsearch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import org.junit.After;
import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.utils.CcpDbRequester;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.fields.CcpErrorDbUtilsIncorrectEntityFields;
import com.ccp.especifications.http.CcpHttpBodyBinary;
import com.ccp.especifications.http.CcpHttpBodyText;
import com.ccp.especifications.http.CcpHttpMethods;
import com.ccp.especifications.http.CcpHttpRequester;
import com.ccp.especifications.http.CcpHttpResponse;
import com.ccp.especifications.http.CcpHttpResponseType;
import com.ccp.implementations.http.apache.mime.CcpApacheMimeHttp;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.jn.entities.JnEntityAsyncTask;

/**
 * Proves the requests and the setup messages of {@link ElasticSearchDbRequester} (finding 31): until 2026-10-07 the
 * connection details, address of the database included, were sent as the HTTP headers of every request, and the error
 * of a mapping that is not strict showed the value of {@code dynamic} where the entity name should be.
 */
public class RequestHeadersTest {

	static {
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	/** Keeps the URL and the headers of the last request and answers an empty JSON. */
	static class CapturingRequester implements CcpHttpRequester {
		String lastUrl = "";
		CcpJsonRepresentation lastHeaders = CcpOtherConstants.EMPTY_JSON;

		public CcpHttpResponse executeHttpRequest(String url, CcpHttpMethods method, CcpJsonRepresentation headers, String body) {
			this.lastUrl = url;
			this.lastHeaders = headers;
			return new CcpHttpResponse("{}", 200, "");
		}

		public CcpHttpResponse executeMultiPartHttpRequest(String url, CcpHttpMethods method, CcpJsonRepresentation headers, List<CcpHttpBodyText> bodyTexts, List<CcpHttpBodyBinary> bodyBinaries) {
			return this.executeHttpRequest(url, method, headers, "");
		}
	}

	@After
	public void restoreTheRealHttp() {
		CcpDependencyInjection.loadAllDependencies(new CcpApacheMimeHttp());
	}

	@Test
	public void theAddressOfTheDatabaseIsNotSentAsAHeader() {
		CapturingRequester requester = new CapturingRequester();
		CcpDependencyInjection.loadAllDependencies(() -> requester);
		CcpDbRequester db = new CcpElasticSearchDbRequest().getInstance();
		String dbUrl = db.getConnectionDetails().getAsString(new CcpFieldName("DB_URL"));

		db.executeHttpRequest("headers", "/_cluster/health", CcpHttpMethods.GET, 200, CcpOtherConstants.EMPTY_JSON, CcpHttpResponseType.singleRecord);

		assertTrue(requester.lastUrl, requester.lastUrl.startsWith(dbUrl));
		assertFalse(requester.lastHeaders.toString(), requester.lastHeaders.containsField(new CcpFieldName("DB_URL")));
		assertEquals("application/json", requester.lastHeaders.getAsString(new CcpFieldName("Content-Type")));
		assertEquals("application/json", requester.lastHeaders.getAsString(new CcpFieldName("Accept")));
		assertTrue(requester.lastHeaders.containsField(new CcpFieldName("Authorization")));
	}

	@Test
	public void theMappingThatIsNotStrictNamesTheEntityAndTheValueFound() throws Exception {
		CcpEntity entity = JnEntityAsyncTask.ENTITY;
		String entityName = entity.getEntityMetaData().entityName;
		File scriptsFolder = Files.createTempDirectory("ccp_strict_test").toFile();
		File script = new File(scriptsFolder, entityName);
		Files.write(script.toPath(), "{\"mappings\":{\"dynamic\":\"true\",\"properties\":{}}}".getBytes(StandardCharsets.UTF_8));
		Object db = new CcpElasticSearchDbRequest().getInstance();
		Method validateEntityFields = db.getClass().getDeclaredMethod("validateEntityFields", CcpEntity.class, String.class, String.class);
		validateEntityFields.setAccessible(true);
		try {
			validateEntityFields.invoke(db, entity, scriptsFolder.getAbsolutePath(), "JnEntityAsyncTask");
			fail("the mapping is not strict");
		} catch (InvocationTargetException e) {
			Throwable cause = e.getCause();
			assertTrue(String.valueOf(cause), cause instanceof CcpErrorDbUtilsIncorrectEntityFields);
			String message = cause.getMessage();
			assertTrue(message, message.startsWith("The entity '" + entityName + "'"));
			assertTrue(message, message.contains("'true'"));
		} finally {
			script.delete();
			scriptsFolder.delete();
		}
	}
}
