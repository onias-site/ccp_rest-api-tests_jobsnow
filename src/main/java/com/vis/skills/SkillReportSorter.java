package com.vis.skills;

import java.util.Comparator;

import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpStringDecorator;

/**
 * Comparator of {@link SkillManager}: compares the skills of the report over the fields in order: numbers descending,
 * booleans true first, texts ascending.
 */
class SkillReportSorter implements Comparator<CcpJsonRepresentation> {

	/** The fields, in order of precedence. */
	private final String[] fields;

	/**
	 * Keeps the fields.
	 * @param fields the fields, in order of precedence
	 */
	SkillReportSorter(String... fields) {
		this.fields = fields;
	}

	/**
	 * Compares two skills of the report.
	 * @param a a skill
	 * @param b another skill
	 * @return the comparison
	 */
	public int compare(CcpJsonRepresentation a, CcpJsonRepresentation b) {
		for (String field : this.fields) {
			CcpStringDecorator firstFieldValue = a.getAsStringDecorator(new CcpFieldName(field));

			if(firstFieldValue.isLongNumber()) {
				Integer secondNumber = b.getAsIntegerNumber(new CcpFieldName(field));
				Integer firstNumber = a.getAsIntegerNumber(new CcpFieldName(field));
				int difference = secondNumber - firstNumber;
				if(difference == 0) {
					continue;
				}
				return difference;
			}

			var firstString = a.getAsString(new CcpFieldName(field));
			var secondString = b.getAsString(new CcpFieldName(field));

			if(firstFieldValue.isBoolean()) {
				int comparison = secondString.compareTo(firstString);
				if(comparison == 0) {
					continue;
				}
				return comparison;
			}
			int comparison = firstString.compareTo(secondString);
			if(comparison == 0) {
				continue;
			}
			return comparison;
		}

		return 0;
	}
}
