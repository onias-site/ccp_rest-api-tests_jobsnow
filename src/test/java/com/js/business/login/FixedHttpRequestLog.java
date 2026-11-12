package com.js.business.login;

import java.util.ArrayList;
import java.util.List;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.http.CcpHttpMethods;
import com.ccp.especifications.http.CcpHttpRequestLog;

/** A request log that answers the requests given by the test and keeps the arguments of each reading. */
public class FixedHttpRequestLog implements CcpHttpRequestLog {
	/** The single instance: the tests clear its state. */
	public final static FixedHttpRequestLog INSTANCE = new FixedHttpRequestLog();

	private FixedHttpRequestLog() {}

	/** The requests answered by every reading. */
	public final List<CcpJsonRepresentation> requests = new ArrayList<>();

	/** The service, method, path, status and start time of each reading, in that order. */
	public final List<List<Object>> readings = new ArrayList<>();

	/** Forgets the requests and the readings. */
	public void clear() {
		this.requests.clear();
		this.readings.clear();
	}

	public List<CcpJsonRepresentation> getRequestsSince(String serviceName, CcpHttpMethods method, String pathRegex, int httpStatus, long startTimeInMillis) {
		List<Object> reading = List.of(serviceName, method, pathRegex, httpStatus, startTimeInMillis);
		this.readings.add(reading);
		List<CcpJsonRepresentation> copy = new ArrayList<>(this.requests);
		return copy;
	}
}
