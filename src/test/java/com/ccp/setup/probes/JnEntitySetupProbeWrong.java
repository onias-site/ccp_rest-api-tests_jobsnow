package com.ccp.setup.probes;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsValidator;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityFactory;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldPrimaryKey;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;

/**
 * Entity used only by the database setup test: {@code jn_setup_probe_wrong}, whose script declares a field the
 * entity does not have, so the setup reports an incorrect mapping and does not create it.
 */
@CcpEntityFieldsValidator(classReferenceWithTheFields = JnEntitySetupProbeWrong.Fields.class)
public class JnEntitySetupProbeWrong implements CcpEntityConfigurator {

	/** The entity {@code jn_setup_probe_wrong}. */
	public static final CcpEntity ENTITY = new CcpEntityFactory(JnEntitySetupProbeWrong.class).entityInstance;

	/** The fields of the entity. */
	public static enum Fields implements CcpJsonFieldName {
		/** The key. */
		@CcpEntityFieldPrimaryKey
		@CcpJsonFieldTypeString
		code
	}
}
