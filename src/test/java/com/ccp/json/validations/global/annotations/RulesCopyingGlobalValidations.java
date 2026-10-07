package com.ccp.json.validations.global.annotations;

import com.ccp.decorators.CcpJsonFieldName;

/**
 * Rules class that copies the global validations of {@link RulesRequiresAtLeastOneOneGroup} (the JSON must bring
 * {@code maxClt} or {@code maxPj}) instead of declaring its own.
 */
@CcpJsonCopyGlobalValidationsFrom(RulesRequiresAtLeastOneOneGroup.class)
public enum RulesCopyingGlobalValidations implements CcpJsonFieldName {

	maxClt,
	maxPj,
	titulo,
	;
}
