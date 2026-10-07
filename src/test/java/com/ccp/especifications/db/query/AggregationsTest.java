package com.ccp.especifications.db.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.especifications.db.utils.entity.fields.CcpEntityField;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;

/**
 * Proves the aggregations of the request ({@link CcpQueryAggregations} and {@link BucketAggregation}) as they come out
 * in the JSON sent to Elasticsearch.
 */
public class AggregationsTest {

	{
		CcpDependencyInjection.loadAllDependencies(new CcpGsonJsonHandler());
	}

	private static final CcpEntityField SALARY = new CcpEntityField("salary", false, true, json -> json);

	private CcpJsonRepresentation aggs(CcpQueryOptions request) {
		CcpJsonRepresentation reparsed = new CcpJsonRepresentation(request.json.asUgglyJson());
		CcpJsonRepresentation aggs = reparsed.getInnerJson(new CcpFieldName("aggs"));
		return aggs;
	}

	private String fieldOf(CcpJsonRepresentation aggs, String name, String type) {
		CcpJsonRepresentation aggregation = aggs.getInnerJsonFromPath(new CcpFieldName(name), new CcpFieldName(type));
		String field = aggregation.getAsString(new CcpFieldName("field"));
		return field;
	}

	/** Finding 54: until 2026-10-07 the field came out as the serialized CcpEntityField. */
	@Test
	public void metricAggregationsNameTheirTypeAndField() {
		CcpQueryOptions request = CcpQueryOptions.INSTANCE.startAggregations()
				.addMinAggregation("lowest", SALARY)
				.addMaxAggregation("highest", SALARY)
				.addAvgAggregation("average", SALARY)
				.addSumAggregation("total", SALARY)
				.endAggregationsAndBackToRequest();

		CcpJsonRepresentation aggs = this.aggs(request);

		assertEquals("salary", this.fieldOf(aggs, "lowest", "min"));
		assertEquals("salary", this.fieldOf(aggs, "highest", "max"));
		assertEquals("salary", this.fieldOf(aggs, "average", "avg"));
		assertEquals("salary", this.fieldOf(aggs, "total", "sum"));
	}

	/** Finding 54: until 2026-10-07 the field came out as the serialized CcpEntityField. */
	@Test
	public void aTermsBucketCarriesItsSizeAndItsSubAggregations() {
		CcpQueryOptions request = CcpQueryOptions.INSTANCE.startAggregations()
				.startBucket("bySalary", SALARY, 5)
					.startAggregations().addAvgAggregation("average", SALARY).endAggregationsAndBackToBucket()
				.endTermsBuckedAndBackToAggregations()
				.endAggregationsAndBackToRequest();

		CcpJsonRepresentation bucket = this.aggs(request).getInnerJson(new CcpFieldName("bySalary"));

		CcpJsonRepresentation terms = bucket.getInnerJson(new CcpFieldName("terms"));
		assertEquals("salary", terms.getAsString(new CcpFieldName("field")));
		assertEquals(5, (int) terms.getAsIntegerNumber(new CcpFieldName("size")));
		assertTrue(bucket.toString(), bucket.getInnerJson(new CcpFieldName("aggs")).containsField(new CcpFieldName("average")));
	}

	@Test
	public void aHistogramBucketCarriesItsInterval() {
		CcpQueryOptions request = CcpQueryOptions.INSTANCE.startAggregations()
				.startBucket("salaryRanges", SALARY, 1000)
				.endHistogramBuckedAndBackToAggregations()
				.endAggregationsAndBackToRequest();

		CcpJsonRepresentation histogram = this.aggs(request).getInnerJsonFromPath(new CcpFieldName("salaryRanges"), new CcpFieldName("histogram"));

		assertEquals(1000, (int) histogram.getAsIntegerNumber(new CcpFieldName("interval")));
	}
}
