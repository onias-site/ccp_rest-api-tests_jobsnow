package com.ccp.setup.probes;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsValidator;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityFactory;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldPrimaryKey;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;

/**
 * Entity used only by the database setup test: {@code jn_setup_probe_plain}, without twin and without seeds.
 */
@CcpEntityFieldsValidator(classReferenceWithTheFields = JnEntitySetupProbePlain.Fields.class)
public class JnEntitySetupProbePlain implements CcpEntityConfigurator {

	/** The entity {@code jn_setup_probe_plain}. */
	public static final CcpEntity ENTITY = new CcpEntityFactory(JnEntitySetupProbePlain.class).entityInstance;

	/** The fields of the entity. */
	public static enum Fields implements CcpJsonFieldName {
		/** The key. */
		@CcpEntityFieldPrimaryKey
		@CcpJsonFieldTypeString
		code
	}
}
