package com.ccp.implementations.http.request.log.gcp.logging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.Instant;

import org.junit.Test;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.http.CcpHttpMethods;
import com.ccp.especifications.http.CcpHttpRequestLog;
import com.google.cloud.logging.HttpRequest;
import com.google.cloud.logging.LogEntry;
import com.google.cloud.logging.Payload.StringPayload;

/**
 * The Cloud Logging implementation of {@code CcpHttpRequestLog} without going to GCP: the provider, the filter sent to
 * Cloud Logging and the copy of each entry to JSON. The real read needs credentials of the production project and
 * stays out of the suite.
 */
public class CcpGcpHttpRequestLogTest {

	enum Fields implements CcpJsonFieldName {
		jobsnowJs {
			public String getValue() {
				return "jobsnow-js";
			}
		},
		loginPath {
			public String getValue() {
				return "/login/([^/?]+)/token/language/[^/?]+$";
			}
		},
		expectedFilter {
			public String getValue() {
				return "resource.type=\"cloud_run_revision\" AND log_id(\"run.googleapis.com/requests\")"
						+ " AND resource.labels.service_name=\"jobsnow-js\" AND httpRequest.requestMethod=\"POST\""
						+ " AND httpRequest.status=200 AND httpRequest.requestUrl=~\"/login/([^/?]+)/token/language/[^/?]+$\""
						+ " AND timestamp>=\"2026-11-11T08:00:00Z\"";
			}
		},
		startTime {
			public String getValue() {
				return "2026-11-11T08:00:00Z";
			}
		},
		requestUrl {
			public String getValue() {
				return "https://api.onias.site/login/someone%40example.com/token/language/portuguese";
			}
		},
		userAgent {
			public String getValue() {
				return "Mozilla/5.0";
			}
		},
		remoteIp {
			public String getValue() {
				return "203.0.113.7";
			}
		},
		payload
	}

	@Test
	public void providerReturnsTheCloudLoggingImplementation() {
		CcpHttpRequestLog instance = CcpGcpHttpRequestLog.INSTANCE.getInstance();
		assertNotNull(instance);
		assertTrue(instance instanceof GcpLoggingHttpRequestLog);
	}

	@Test
	public void filterNamesServiceMethodStatusUrlAndMoment() {
		String startTimeText = Fields.startTime.getValue();
		Instant startTime = Instant.parse(startTimeText);
		long startTimeInMillis = startTime.toEpochMilli();
		String serviceName = Fields.jobsnowJs.getValue();
		String pathRegex = Fields.loginPath.getValue();

		String filter = GcpLoggingHttpRequestLog.INSTANCE.getFilter(serviceName, CcpHttpMethods.POST, pathRegex, 200, startTimeInMillis);

		String expectedFilter = Fields.expectedFilter.getValue();
		assertEquals(expectedFilter, filter);
	}

	@Test
	public void entryWithTheRequestBecomesJsonWithEveryField() {
		Instant timestamp = Instant.now();
		HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder();
		String url = Fields.requestUrl.getValue();
		String userAgent = Fields.userAgent.getValue();
		String ip = Fields.remoteIp.getValue();
		HttpRequest httpRequest = httpRequestBuilder.setRequestUrl(url).setUserAgent(userAgent).setRemoteIp(ip).build();
		LogEntry entry = this.newEntry(timestamp, httpRequest);

		CcpJsonRepresentation json = GcpLoggingHttpRequestLog.INSTANCE.toJson(entry);

		long timestampInMillis = timestamp.toEpochMilli();
		Long jsonTimestamp = json.getAsLongNumber(CcpHttpRequestLog.Fields.timestamp);
		assertEquals(timestampInMillis, jsonTimestamp.longValue());
		String jsonUrl = json.getAsString(CcpHttpRequestLog.Fields.url);
		assertEquals(url, jsonUrl);
		String jsonUserAgent = json.getAsString(CcpHttpRequestLog.Fields.userAgent);
		assertEquals(userAgent, jsonUserAgent);
		String jsonIp = json.getAsString(CcpHttpRequestLog.Fields.ip);
		assertEquals(ip, jsonIp);
	}

	@Test
	public void entryWithoutTheRequestKeepsOnlyTheMoment() {
		Instant timestamp = Instant.now();
		LogEntry.Builder builder = this.newBuilder(timestamp);
		LogEntry entry = builder.build();

		CcpJsonRepresentation json = GcpLoggingHttpRequestLog.INSTANCE.toJson(entry);

		boolean hasTimestamp = json.containsAllFields(CcpHttpRequestLog.Fields.timestamp);
		assertTrue(hasTimestamp);
		boolean hasUrl = json.containsAllFields(CcpHttpRequestLog.Fields.url);
		assertFalse(hasUrl);
	}

	@Test
	public void requestWithoutUserAgentAndIpKeepsOnlyTheUrl() {
		Instant timestamp = Instant.now();
		HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder();
		String url = Fields.requestUrl.getValue();
		HttpRequest httpRequest = httpRequestBuilder.setRequestUrl(url).build();
		LogEntry entry = this.newEntry(timestamp, httpRequest);

		CcpJsonRepresentation json = GcpLoggingHttpRequestLog.INSTANCE.toJson(entry);

		boolean hasUrl = json.containsAllFields(CcpHttpRequestLog.Fields.url);
		assertTrue(hasUrl);
		boolean hasUserAgent = json.containsAllFields(CcpHttpRequestLog.Fields.userAgent);
		assertFalse(hasUserAgent);
		boolean hasIp = json.containsAllFields(CcpHttpRequestLog.Fields.ip);
		assertFalse(hasIp);
	}

	private LogEntry newEntry(Instant timestamp, HttpRequest httpRequest) {
		LogEntry.Builder builder = this.newBuilder(timestamp);
		LogEntry entry = builder.setHttpRequest(httpRequest).build();
		return entry;
	}

	private LogEntry.Builder newBuilder(Instant timestamp) {
		String payloadText = Fields.payload.getValue();
		StringPayload payload = StringPayload.of(payloadText);
		LogEntry.Builder builder = LogEntry.newBuilder(payload);
		LogEntry.Builder withTimestamp = builder.setTimestamp(timestamp);
		return withTimestamp;
	}
}
