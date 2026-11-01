package com.vis.business.telegram;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.bulk.CcpBulkEntityOperationType;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.jn.entities.JnEntityDisposableRecord;
import com.jn.json.fields.validation.JnJsonCommonsFields;
import com.vis.entities.VisEntityTelegramPosition;

/** A position is written under the day of its message, not under the day of the load. */
public class VisTelegramPositionPeriodTest {

	@BeforeClass
	public static void loadDependencies() {
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE);
	}

	private static CcpJsonRepresentation position(String period) {
		return CcpOtherConstants.EMPTY_JSON
				.put(VisEntityTelegramPosition.Fields.contact, "rh@empresa.com")
				.put(VisEntityTelegramPosition.Fields.skill, "JAVA_PHP")
				.put(VisEntityTelegramPosition.Fields.seniority, "pleno")
				.put(VisEntityTelegramPosition.Fields.ddd, 11)
				.put(VisEntityTelegramPosition.Fields.yearMonth, "202104")
				.put(JnJsonCommonsFields.disposablePeriod, period);
	}

	@Test
	public void theSamePositionInAnotherDayIsAnotherRecord() {
		assertNotEquals(VisEntityTelegramPosition.ENTITY.calculateId(position("29042021")), VisEntityTelegramPosition.ENTITY.calculateId(position("30042021")));
	}

	@Test
	public void theSamePositionInTheSameDayIsTheSameRecord() {
		assertEquals(VisEntityTelegramPosition.ENTITY.calculateId(position("29042021")), VisEntityTelegramPosition.ENTITY.calculateId(position("29042021")));
	}

	@Test
	public void theBulkWritesUnderTheDayOfTheMessageAndItsCopyIsBornExpired() {
		CcpJsonRepresentation position = position("29042021");
		List<CcpBulkItem> items = VisEntityTelegramPosition.ENTITY.toBulkItems(position, CcpBulkEntityOperationType.create);
		assertEquals(2, items.size());
		String expectedId = VisEntityTelegramPosition.ENTITY.calculateId(position);
		assertEquals(expectedId, items.get(0).id);
		CcpJsonRepresentation copy = items.get(1).json;
		Long expiration = copy.getAsLongNumber(JnJsonCommonsFields.timestamp);
		assertEquals(true, expiration < System.currentTimeMillis());
		assertEquals(JnEntityDisposableRecord.ENTITY.getEntityMetaData().entityName, items.get(1).entity.getEntityMetaData().entityName);
	}
}
