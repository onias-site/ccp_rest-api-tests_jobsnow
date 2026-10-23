package com.ccp.implementations.db.query.elasticsearch;

import java.util.List;
import java.util.function.Consumer;

import com.ccp.decorators.CcpJsonRepresentation;

/** Consumer of the pages of a scroll of {@link QueryExecutorAgainstElasticsearchTest}: keeps the size of each page and its documents. */
class PageCollector implements Consumer<List<CcpJsonRepresentation>> {

	/** Receives the size of each page. */
	private final List<Integer> pageSizes;

	/** Receives the documents. */
	private final List<CcpJsonRepresentation> documents;

	PageCollector(List<Integer> pageSizes, List<CcpJsonRepresentation> documents) {
		this.pageSizes = pageSizes;
		this.documents = documents;
	}

	public void accept(List<CcpJsonRepresentation> page) {
		this.pageSizes.add(page.size());
		this.documents.addAll(page);
	}
}
