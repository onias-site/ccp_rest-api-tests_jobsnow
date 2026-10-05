package com.ccp.setup.broken;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsValidator;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityFactory;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldPrimaryKey;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;

/**
 * Entity used only by the database setup test: it has no creation script, which is an unexpected setup failure.
 */
@CcpEntityFieldsValidator(classReferenceWithTheFields = JnEntitySetupProbeWithoutScript.Fields.class)
public class JnEntitySetupProbeWithoutScript implements CcpEntityConfigurator {

	/** The entity {@code jn_setup_probe_without_script}. */
	public static final CcpEntity ENTITY = new CcpEntityFactory(JnEntitySetupProbeWithoutScript.class).entityInstance;

	/** The fields of the entity. */
	public static enum Fields implements CcpJsonFieldName {
		/** The key. */
		@CcpEntityFieldPrimaryKey
		@CcpJsonFieldTypeString
		code
	}
}
