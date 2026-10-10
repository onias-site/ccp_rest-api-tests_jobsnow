package com.jn.entities.decorators;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.especifications.db.utils.entity.CcpEntity;
import com.jn.entities.JnEntityLoginEmail;
import com.jn.entities.JnEntityLoginSessionConflict;
import com.jn.entities.JnEntityVersionable;
import com.jn.json.fields.validation.JnJsonCommonsFields;

/**
 * {@code transferDataTo} and {@code copyDataTo}. Until 2026-09-27 no entity transferred or copied:
 * the implementations received {@code CcpEntity...}, did not override the interface method, and every
 * call fell into the default that returns {@code false}. Both execution paths are covered: the
 * innermost layer (entity without a bulk decorator) and the default delegator (here, the disposable one).
 */
public class DataTransferAndCopyTest extends EntityDecoratorTestTemplate {

	protected CcpEntity entityUnderTest() {
		return JnEntityLoginEmail.ENTITY;
	}

	protected CcpJsonRepresentation validRecord() {
		return this.com(JnJsonCommonsFields.email, this.email);
	}

	@Test
	public void transferThroughInnerLayer() {
		CcpEntity source = JnEntityLoginEmail.ENTITY;
		CcpEntity target = JnEntityLoginSessionConflict.ENTITY;
		CcpJsonRepresentation record = this.validRecord();

		source.save(record);
		boolean transferred = source.transferDataTo(record, target);

		assertTrue("transferDataTo returned false with the source saved", transferred);
		assertFalse("transfer left the record in the source", source.exists(record));
		assertTrue("transfer did not save in the target", target.exists(record));
	}

	@Test
	public void copyThroughInnerLayer() {
		CcpEntity source = JnEntityLoginEmail.ENTITY;
		CcpEntity target = JnEntityLoginSessionConflict.ENTITY;
		CcpJsonRepresentation record = this.validRecord();

		source.save(record);
		boolean copied = source.copyDataTo(record, target);

		assertTrue("copyDataTo returned false with the source saved", copied);
		assertTrue("copy removed the record from the source", source.exists(record));
		assertTrue("copy did not save in the target", target.exists(record));
	}

	@Test
	public void transferThroughDefaultDelegator() {
		CcpEntity source = JnEntityLoginSessionConflict.ENTITY;
		CcpEntity target = JnEntityLoginEmail.ENTITY;
		CcpJsonRepresentation record = this.validRecord();

		source.save(record);
		boolean transferred = source.transferDataTo(record, target);

		assertTrue("transferDataTo returned false with the source saved", transferred);
		assertFalse("transfer left the record in the disposable source", source.exists(record));
		assertTrue("transfer did not save in the target", target.exists(record));
	}

	@Test
	public void readingADisposableAnyWhereFindsTheRecordInItsOwnEntity() {
		CcpEntity disposable = JnEntityLoginSessionConflict.ENTITY;
		CcpJsonRepresentation record = this.validRecord();
		disposable.save(record);

		CcpJsonRepresentation everywhere = disposable.getOneByIdAnyWhere(record);

		String entityName = disposable.getEntityMetaData().entityName;
		assertTrue(everywhere.toString(), everywhere.containsAllFields(new com.ccp.decorators.CcpFieldName(entityName)));
		disposable.delete(record);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void aVersionableEntityDoesNotReadAnyWhere() {
		com.jn.entities.JnEntityEmailParametersToSend.ENTITY.getOneByIdAnyWhere(this.validRecord());
	}

	@Test
	public void missingSourceCreatesNothingInTarget() {
		CcpEntity source = JnEntityLoginEmail.ENTITY;
		CcpEntity target = JnEntityLoginSessionConflict.ENTITY;
		CcpJsonRepresentation record = this.validRecord();

		boolean transferred = source.transferDataTo(record, target);

		assertFalse("transferDataTo returned true without a record in the source", transferred);
		assertFalse("transfer from a missing source created a record in the target", target.exists(record));
	}

	/** The record is put into the read-only entity from outside, because it refuses to save through the API. */
	@Test
	public void readOnlyEntityDoesNotTransfer() {
		CcpEntity source = JnEntityVersionable.ENTITY;
		CcpJsonRepresentation record = this.com(JnJsonCommonsFields.timestamp, this.unique)
				.put(JnJsonCommonsFields.operation, "create")
				.put(JnJsonCommonsFields.date, "27/09/2026 10:00:00.000")
				.put(JnJsonCommonsFields.entity, "jn_login_email")
				.put(JnJsonCommonsFields.id, "{\"email\":\"" + this.unique + "\"}")
				.put(JnJsonCommonsFields.json, this.com(JnJsonCommonsFields.email, this.email).asUgglyJson());
		String id = source.calculateId(source.getHandledJson(record));
		ElasticsearchLocal.save("jn_versionable", id, record);

		boolean transferred = source.transferDataTo(record, JnEntityLoginEmail.ENTITY);

		assertFalse("read-only entity transferred", transferred);
		assertTrue("read-only entity lost the record", source.exists(record));
		ElasticsearchLocal.delete("jn_versionable", id);
	}
}
