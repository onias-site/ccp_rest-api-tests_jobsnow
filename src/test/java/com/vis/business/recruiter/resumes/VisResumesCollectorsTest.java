package com.vis.business.recruiter.resumes;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.vis.entities.VisEntityResume;
import com.vis.entities.VisEntityViewedResume;

/**
 * The pieces of the daily e-mail to the recruiters that read the history and the views:
 * {@link VisResumesBySortKeys}, {@link VisHistoryKeyCollector}, {@link VisHistoryResumeDatesCollector},
 * {@link VisHistoryLastViewCollector} and {@link VisViewedResumeAliasCollector}.
 */
public class VisResumesCollectorsTest {

	static {
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE);
	}

	private static final String SEPARATOR = "|";

	/** A row of {@code jn_versionable}: the record as text and the time of the change. */
	private static CcpJsonRepresentation historyRow(CcpJsonRepresentation record, long timestamp) {
		return CcpOtherConstants.EMPTY_JSON
				.put(JnJsonCommonsFields.json, record.asUgglyJson())
				.put(JnJsonCommonsFields.timestamp, timestamp);
	}

	private static CcpJsonRepresentation view(String recruiterHash, String linkedinHash) {
		return CcpOtherConstants.EMPTY_JSON
				.put(VisEntityViewedResume.Fields.email, recruiterHash)
				.put(VisEntityViewedResume.Fields.linkedin, linkedinHash);
	}

	private static CcpJsonRepresentation withSortKeys(Object... keys) {
		return CcpOtherConstants.EMPTY_JSON.put(VisRecruiterResumesFields.sortKeys, List.of(keys));
	}

	@Test
	public void theResumesAreOrderedKeyByKey() {
		List<CcpJsonRepresentation> resumes = new ArrayList<>(List.of(withSortKeys(2, 1), withSortKeys(1, 9), withSortKeys(2, 0.5), withSortKeys(1, 9)));

		resumes.sort(VisResumesBySortKeys.INSTANCE);

		assertEquals(List.of(1, 9), resumes.get(0).getAsObjectList(VisRecruiterResumesFields.sortKeys));
		assertEquals(0, VisResumesBySortKeys.INSTANCE.compare(resumes.get(0), resumes.get(1)));
		assertEquals(List.of(2, 0.5), resumes.get(2).getAsObjectList(VisRecruiterResumesFields.sortKeys));
		assertEquals(List.of(2, 1), resumes.get(3).getAsObjectList(VisRecruiterResumesFields.sortKeys));
	}

	@Test
	public void theKeyOfTheRecordOfEachRowIsKeptOnce() {
		Set<String> keys = new LinkedHashSet<>();
		VisHistoryKeyCollector collector = new VisHistoryKeyCollector(keys, VisEntityResume.Fields.email);
		CcpJsonRepresentation resume = CcpOtherConstants.EMPTY_JSON.put(VisEntityResume.Fields.email, "hashA");

		collector.accept(historyRow(resume, 1L));
		collector.accept(historyRow(resume, 2L));
		collector.accept(historyRow(CcpOtherConstants.EMPTY_JSON.put(VisEntityResume.Fields.email, "hashB"), 3L));

		assertEquals(List.of("hashA", "hashB"), new ArrayList<>(keys));
	}

	@Test
	public void theOldestAndTheNewestChangeOfEachResumeAreKept() {
		Map<String, long[]> dates = new HashMap<>();
		VisHistoryResumeDatesCollector collector = new VisHistoryResumeDatesCollector(dates);
		CcpJsonRepresentation resume = CcpOtherConstants.EMPTY_JSON.put(VisEntityResume.Fields.email, "hashA");

		collector.accept(historyRow(resume, 50L));
		collector.accept(historyRow(resume, 10L));
		collector.accept(historyRow(resume, 30L));

		assertArrayEquals(new long[] {10L, 50L}, dates.get("hashA"));
	}

	@Test
	public void theLastViewOfEachPairAndOfEachRecruiterIsKept() {
		Map<String, Long> lastViewByPair = new HashMap<>();
		Map<String, Long> lastViewByRecruiter = new HashMap<>();
		VisHistoryLastViewCollector collector = new VisHistoryLastViewCollector(lastViewByPair, lastViewByRecruiter, SEPARATOR);

		collector.accept(historyRow(view("recruiter1", "linkedinA"), 20L));
		collector.accept(historyRow(view("recruiter1", "linkedinA"), 10L));
		collector.accept(historyRow(view("recruiter1", "linkedinB"), 40L));

		assertEquals(Long.valueOf(20L), lastViewByPair.get("recruiter1|linkedinA"));
		assertEquals(Long.valueOf(40L), lastViewByPair.get("recruiter1|linkedinB"));
		assertEquals(Long.valueOf(40L), lastViewByRecruiter.get("recruiter1"));
	}

	@Test
	public void theAliasOfEachPairIsKept() {
		Map<String, String> aliasByPair = new HashMap<>();
		VisViewedResumeAliasCollector collector = new VisViewedResumeAliasCollector(aliasByPair, SEPARATOR);

		collector.accept(view("recruiter1", "linkedinA").put(VisEntityViewedResume.Fields.alias, "Dev Java"));

		assertEquals("Dev Java", aliasByPair.get("recruiter1|linkedinA"));
		assertTrue(aliasByPair.size() == 1);
	}
}
