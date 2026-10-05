package com.ccp.json.validations.global.annotations;

/**
 * Group with both ends of the PJ salary range, mirroring {@code PjSalaryRange} of the position entity: whoever
 * gives one end must give the other.
 */
public enum PjRangeGroup {

	minPj,
	maxPj,
	;
}
