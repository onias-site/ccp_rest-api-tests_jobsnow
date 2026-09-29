package com.jn.entities.decorators;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;

/**
 * Raw access to the local Elasticsearch, <b>outside</b> the decorator chain. The decorator tests need
 * it for two things the entity itself cannot do for them: look at the document exactly as it was
 * stored (to prove a transformation) and touch the database without going through the decorator (to
 * prove that the read came from the cache or from the disposable copy, and not from the index).
 */
public class ElasticsearchLocal {

	private static final String BASE_URL = "http://localhost:9200/";

	private static final HttpClient client = HttpClient.newHttpClient();

	/** The stored document, or an empty json when the index does not have that id. */
	public static CcpJsonRepresentation document(String index, String id) {
		HttpResponse<String> response = sendRequest("GET", index + "/_doc/" + id, "");
		boolean notFound = response.statusCode() == 404;
		if(notFound) {
			return CcpOtherConstants.EMPTY_JSON;
		}
		CcpJsonRepresentation body = new CcpJsonRepresentation(response.body());
		CcpJsonRepresentation document = body.getInnerJson(TestFields._source);
		return document;
	}

	public static void delete(String index, String id) {
		sendRequest("DELETE", index + "/_doc/" + id + "?refresh=true", "");
	}

	/** Saves the document with the given id without going through any decorator. */
	public static void save(String index, String id, CcpJsonRepresentation document) {
		sendRequest("PUT", index + "/_doc/" + id + "?refresh=true", document.asUgglyJson());
	}

	/** Changes fields of the document without going through any decorator. */
	public static void update(String index, String id, CcpJsonRepresentation fields) {
		String body = "{\"doc\":" + fields.asUgglyJson() + "}";
		sendRequest("POST", index + "/_update/" + id + "?refresh=true", body);
	}

	/** How many documents of the index match all the field/value pairs (exact terms). */
	public static long count(String index, String... fieldsAndValues) {
		sendRequest("POST", index + "/_refresh", "");
		StringBuilder terms = new StringBuilder();
		for (int k = 0; k < fieldsAndValues.length; k += 2) {
			if(k > 0) {
				terms.append(",");
			}
			String value = fieldsAndValues[k + 1].replace("\\", "\\\\").replace("\"", "\\\"");
			terms.append("{\"term\":{\"").append(fieldsAndValues[k]).append("\":\"").append(value).append("\"}}");
		}
		String body = "{\"query\":{\"bool\":{\"filter\":[" + terms + "]}}}";
		HttpResponse<String> response = sendRequest("POST", index + "/_count", body);
		CcpJsonRepresentation json = new CcpJsonRepresentation(response.body());
		Long matchCount = json.getAsLongNumber(TestFields.count);
		return matchCount;
	}

	private static HttpResponse<String> sendRequest(String method, String path, String body) {
		try {
			HttpRequest.BodyPublisher publisher = body.isEmpty() ? BodyPublishers.noBody() : BodyPublishers.ofString(body);
			URI uri = URI.create(BASE_URL + path);
			HttpRequest request = HttpRequest.newBuilder(uri)
					.header("Content-Type", "application/json")
					.method(method, publisher)
					.build();
			HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
			return response;
		} catch (Exception e) {
			throw new ElasticsearchLocalUnavailable(path, e);
		}
	}

	enum TestFields implements CcpJsonFieldName {
		_source, count
	}

	@SuppressWarnings("serial")
	static class ElasticsearchLocalUnavailable extends RuntimeException {
		ElasticsearchLocalUnavailable(String path, Exception cause) {
			super("Local Elasticsearch did not respond at " + path, cause);
		}
	}
}
