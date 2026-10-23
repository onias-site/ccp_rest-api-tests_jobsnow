package com.ccp.random;

import java.util.Comparator;
import java.util.Set;

import com.ccp.decorators.CcpFieldName;

/** Comparator of {@link CcpRandomScripts#saveGroupedCompanies()}: the initials of the largest groups of companies come first. */
class RandomScriptsGroupSizeComparator implements Comparator<String> {

	/** The single instance. */
	public static final RandomScriptsGroupSizeComparator INSTANCE = new RandomScriptsGroupSizeComparator();

	private RandomScriptsGroupSizeComparator() {}

	public int compare(String a, String b) {
		Set<String> set1 = CcpRandomScripts.groupedCompanies.getAsObject(new CcpFieldName(a));
		Set<String> set2 = CcpRandomScripts.groupedCompanies.getAsObject(new CcpFieldName(b));
		return set2.size() - set1.size();
	}
}
