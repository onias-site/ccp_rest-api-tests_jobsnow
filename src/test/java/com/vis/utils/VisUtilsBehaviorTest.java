package com.vis.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.bulk.CcpBulkItem;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.vis.entities.VisEntityBalance;
import com.vis.entities.VisEntityGroupPositionsByRecruiter;
import com.vis.entities.VisEntityPosition;
import com.vis.json.fields.validation.VisJsonCommonsFields;

/**
 * Proves the pure helpers of {@link VisUtils}. The ignored tests describe the documented behavior that the code does
 * not have yet; each one names its finding in the coverage campaign findings.
 */
public class VisUtilsBehaviorTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	private CcpJsonRepresentation fee(double value) {
		return CcpOtherConstants.EMPTY_JSON.put(VisJsonCommonsFields.fee, value);
	}

	private CcpJsonRepresentation balance(double value) {
		return CcpOtherConstants.EMPTY_JSON.put(VisEntityBalance.Fields.balance, value);
	}

	@Test
	public void aBalanceAboveTheCostOfEveryItemIsSufficient() {
		assertFalse(VisUtils.isInsufficientFunds(3, this.fee(10), this.balance(31)));
	}

	@Test
	public void aBalanceBelowTheCostOfEveryItemIsInsufficient() {
		assertTrue(VisUtils.isInsufficientFunds(3, this.fee(10), this.balance(29)));
	}

	@Test
	public void aBalanceEqualToTheCostIsCurrentlyTreatedAsInsufficient() {
		assertTrue(VisUtils.isInsufficientFunds(3, this.fee(10), this.balance(30)));
	}

	@Ignore("finding 45: a balance equal to the cost should be enough")
	@Test
	public void aBalanceEqualToTheCostShouldBeSufficient() {
		assertFalse(VisUtils.isInsufficientFunds(3, this.fee(10), this.balance(30)));
	}

	private List<CcpJsonRepresentation> records(int count) {
		List<CcpJsonRepresentation> records = new ArrayList<>();
		for (int index = 0; index < count; index++) {
			records.add(CcpOtherConstants.EMPTY_JSON.put(VisEntityPosition.Fields.title, "position " + index));
		}
		return records;
	}

	private List<CcpBulkItem> pages(int recordsCount) {
		CcpJsonRepresentation groupKey = CcpOtherConstants.EMPTY_JSON.put(VisJsonCommonsFields.email, "recruiter@company.com");
		List<CcpBulkItem> pages = VisUtils.getRecordsInPages(this.records(recordsCount), groupKey, VisEntityGroupPositionsByRecruiter.ENTITY);
		return pages;
	}

	private int recordsInThePages(List<CcpBulkItem> pages) {
		int total = 0;
		for (CcpBulkItem page : pages) {
			total += page.json.getAsJsonList(VisJsonCommonsFields.detail).size();
		}
		return total;
	}

	@Test
	public void eachPageCarriesItsPositionTheListSizeAndTheGroupKey() {
		List<CcpBulkItem> pages = this.pages(3);

		CcpJsonRepresentation firstPage = pages.get(0).json;
		assertEquals(0, (int) firstPage.getAsIntegerNumber(VisJsonCommonsFields.from));
		assertEquals(10, (int) firstPage.getAsIntegerNumber(VisJsonCommonsFields.listSize));
		assertEquals("recruiter@company.com", firstPage.getAsString(VisJsonCommonsFields.email));
		assertEquals(3, this.recordsInThePages(pages));
	}

	@Ignore("finding 43: every record must land in some page, in ceil(size / 10) pages")
	@Test
	public void twentyFiveRecordsFillThreePagesWithoutLosingAny() {
		List<CcpBulkItem> pages = this.pages(25);

		assertEquals(3, pages.size());
		assertEquals(25, this.recordsInThePages(pages));
	}
}
