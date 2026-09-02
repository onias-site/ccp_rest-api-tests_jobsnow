package com.jb.instant.messenger.reader;

import java.util.List;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.http.CcpHttpBodyBinary;
import com.ccp.especifications.http.CcpHttpBodyText;
import com.ccp.especifications.http.CcpHttpMethods;
import com.ccp.especifications.http.CcpHttpRequester;
import com.ccp.especifications.http.CcpHttpResponse;

class FakeHttpRequester implements CcpHttpRequester {

	private final int httpStatus;
	private final String[] respostas;
	private int chamadas = 0;
	String lastRequest = "";

	FakeHttpRequester(int httpStatus, String... respostas) {
		this.httpStatus = httpStatus;
		this.respostas = respostas;
	}

	public CcpHttpResponse executeHttpRequest(String url, CcpHttpMethods method, CcpJsonRepresentation headers, String body) {

		this.lastRequest = body;

		int ultima = this.respostas.length - 1;
		int indice = this.chamadas > ultima ? ultima : this.chamadas;

		this.chamadas++;

		CcpHttpResponse response = new CcpHttpResponse(this.respostas[indice], this.httpStatus, "");
		return response;
	}

	public CcpHttpResponse executeMultiPartHttpRequest(String url, CcpHttpMethods method, CcpJsonRepresentation headers, List<CcpHttpBodyText> bodyTexts, List<CcpHttpBodyBinary> bodyBinaries) {
		CcpHttpResponse response = this.executeHttpRequest(url, method, headers, "");
		return response;
	}
}
