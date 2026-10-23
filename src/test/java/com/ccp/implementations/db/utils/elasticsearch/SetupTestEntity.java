package com.ccp.implementations.db.utils.elasticsearch;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsValidator;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldPrimaryKey;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;

/** Entity of {@link ElasticSearchEntitySetupFromJavaFileTest}: it has no creation script, so its setup fails before touching the database. */
@CcpEntityFieldsValidator(SetupTestEntity.Fields.class)
public class SetupTestEntity implements CcpEntityConfigurator {
	/** The single instance: the class keeps no state. */
	public final static SetupTestEntity INSTANCE = new SetupTestEntity();

	private SetupTestEntity() {}

	public static enum Fields implements CcpJsonFieldName {
		@CcpEntityFieldPrimaryKey
		@CcpJsonFieldTypeString(maxLength = 10)
		name,
	}
}
