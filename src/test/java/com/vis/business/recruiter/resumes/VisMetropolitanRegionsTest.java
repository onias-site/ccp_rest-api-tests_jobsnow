package com.vis.business.recruiter.resumes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.Test;

/**
 * The metropolitan region of each DDD ({@link VisMetropolitanRegions}): every item has a name with its state, the
 * names do not repeat, each item is found by its own DDD, and the DDD 10 (home office) and the unknown DDDs have no
 * region.
 */
public class VisMetropolitanRegionsTest {

	private static int dddOf(VisMetropolitanRegions region) {
		String itemName = region.name();
		String ddd = itemName.substring(itemName.indexOf('_') + 1);
		return Integer.parseInt(ddd);
	}

	@Test
	public void everyRegionHasANameEndingWithItsState() {
		for (VisMetropolitanRegions region : VisMetropolitanRegions.values()) {
			String name = region.getValue();
			assertTrue("the region of the " + region + " has no state: " + name, name.matches(".+ – [A-Z]{2}"));
		}
	}

	@Test
	public void noTwoDddsHaveTheSameRegion() {
		Set<String> names = new HashSet<>();
		for (VisMetropolitanRegions region : VisMetropolitanRegions.values()) {
			String name = region.getValue();
			assertTrue("repeated region: " + name, names.add(name));
		}
	}

	@Test
	public void eachRegionIsFoundByItsOwnDdd() {
		for (VisMetropolitanRegions region : VisMetropolitanRegions.values()) {
			int ddd = dddOf(region);
			Optional<VisMetropolitanRegions> found = VisMetropolitanRegions.fromDdd(ddd);
			assertEquals(Optional.of(region), found);
		}
	}

	@Test
	public void someWellKnownDddsHaveTheirCapitals() {
		assertEquals("São Paulo – SP", VisMetropolitanRegions.fromDdd(11).get().getValue());
		assertEquals("Rio de Janeiro – RJ", VisMetropolitanRegions.fromDdd(21).get().getValue());
		assertEquals("Manaus – AM", VisMetropolitanRegions.fromDdd(92).get().getValue());
	}

	@Test
	public void theDdd10OfHomeOfficeIsNotARegion() {
		assertFalse(VisMetropolitanRegions.fromDdd(10).isPresent());
	}

	@Test
	public void anUnknownDddHasNoRegion() {
		assertFalse("there is no DDD 20 in Brazil", VisMetropolitanRegions.fromDdd(20).isPresent());
		assertFalse(VisMetropolitanRegions.fromDdd(100).isPresent());
		assertFalse(VisMetropolitanRegions.fromDdd(0).isPresent());
	}
}
