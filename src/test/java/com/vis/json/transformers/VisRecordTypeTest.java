package com.vis.json.transformers;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;

/**
 * Proves {@link VisRecordType}: the values a record contributes to the hash combinations. A resume (recordToSave) is found
 * by every search that asks for its value or less (its experience covers the smaller ones, its salary the bigger
 * thresholds), and a search (recordToSearch) asks for its value or more of the other side.
 */
public class VisRecordTypeTest {

	private final CcpFieldName field = new CcpFieldName("value");

	private CcpJsonRepresentation with(Object value) {
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON.put(this.field, value);
		return json;
	}

	@Test
	public void aRecordToSaveWithTheFlagMatchesBothSearchesAndWithoutItOnlyTheOneThatDoesNotRequireIt() {
		assertArrayEquals(new Integer[] {1, 2}, VisRecordType.recordToSave.fromBooleanField(this.field, this.with(true)));
		assertArrayEquals(new Integer[] {1}, VisRecordType.recordToSave.fromBooleanField(this.field, this.with(false)));
	}

	@Test
	public void aSearchThatRequiresTheFlagFindsOnlyTheRecordsWithItAndOneThatDoesNotFindsBoth() {
		assertArrayEquals(new Integer[] {1}, VisRecordType.recordToSearch.fromBooleanField(this.field, this.with(true)));
		assertArrayEquals(new Integer[] {1, 2}, VisRecordType.recordToSearch.fromBooleanField(this.field, this.with(false)));
	}

	@Test
	public void aRecordToSaveGoesFromItsValueUpToTheThreshold() {
		assertArrayEquals(new Integer[] {3, 4, 5}, VisRecordType.recordToSave.fromIntegerField(this.field, this.with(3), 5));
		assertArrayEquals(new Integer[] {5}, VisRecordType.recordToSave.fromIntegerField(this.field, this.with(5), 5));
		assertArrayEquals(new Integer[] {}, VisRecordType.recordToSave.fromIntegerField(this.field, this.with(6), 5));
	}

	@Test
	public void aRecordToSearchGoesFromItsValueDownToTheThreshold() {
		assertArrayEquals(new Integer[] {3, 2, 1}, VisRecordType.recordToSearch.fromIntegerField(this.field, this.with(3), 1));
		assertArrayEquals(new Integer[] {}, VisRecordType.recordToSearch.fromIntegerField(this.field, this.with(0), 1));
	}

	@Test
	public void aRecordToSaveRoundsItsDecimalValueUpToTheIncrementAndGoesUpToTheThreshold() {
		assertArrayEquals(new Integer[] {3000, 4000, 5000}, VisRecordType.recordToSave.fromDoubleField(this.field, this.with(2500d), 5000, 1000));
		assertArrayEquals(new Integer[] {3000, 4000, 5000}, VisRecordType.recordToSave.fromDoubleField(this.field, this.with(3000d), 5000, 1000));
	}

	@Test
	public void aRecordToSearchRoundsItsDecimalValueDownToTheIncrementAndGoesDownToTheThreshold() {
		assertArrayEquals(new Integer[] {2000, 1000}, VisRecordType.recordToSearch.fromDoubleField(this.field, this.with(2500d), 1000, 1000));
		assertArrayEquals(new Integer[] {}, VisRecordType.recordToSearch.fromDoubleField(this.field, this.with(500d), 1000, 1000));
	}
}
