package com.ccp.json.validations.global.annotations;

/**
 * Group with both ends of the CLT salary range, mirroring {@code CltSalaryRange} of the position entity: whoever
 * gives one end must give the other.
 */
public enum CltRangeGroup {

	minClt,
	maxClt,
	;
}
