package com.ccp.setup.probes;

import java.util.List;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityFieldsValidator;
import com.ccp.especifications.db.utils.entity.decorators.annotations.CcpEntityTwin;
import com.ccp.especifications.db.utils.entity.decorators.engine.CcpEntityFactory;
import com.ccp.especifications.db.utils.entity.decorators.interfaces.CcpEntityConfigurator;
import com.ccp.especifications.db.utils.entity.fields.annotations.CcpEntityFieldPrimaryKey;
import com.ccp.json.validations.fields.annotations.type.CcpJsonFieldTypeString;
import com.jn.db.bulk.JnExecuteBulkOperation;
import com.jn.utils.JnDeleteKeysFromCache;

/**
 * Entity used only by the database setup test: {@code jn_setup_probe_twin}, with the twin
 * {@code jn_setup_probe_twin_copy} and one seed.
 */
@CcpEntityFieldsValidator(classReferenceWithTheFields = JnEntitySetupProbeTwin.Fields.class)
@CcpEntityTwin(
		twinEntityName = "jn_setup_probe_twin_copy",
		bulkExecutorClass = JnExecuteBulkOperation.class,
		functionToDeleteKeysInTheCacheClass = JnDeleteKeysFromCache.class
		)
public class JnEntitySetupProbeTwin implements CcpEntityConfigurator {

	/** The entity {@code jn_setup_probe_twin}. */
	public static final CcpEntity ENTITY = new CcpEntityFactory(JnEntitySetupProbeTwin.class).entityInstance;

	/** The fields of the entity. */
	public static enum Fields implements CcpJsonFieldName {
		/** The key. */
		@CcpEntityFieldPrimaryKey
		@CcpJsonFieldTypeString
		code
	}

	/**
	 * One seed, {@code code = seed}.
	 * @return the seed
	 */
	public List<CcpBulkItem> getFirstRecordsToInsert() {
		List<CcpBulkItem> seeds = this.toCreateBulkItems(ENTITY, CcpOtherConstants.EMPTY_JSON.put(Fields.code, "seed"));
		return seeds;
	}
}
