package com.vis.rest.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.http.HttpResponse;
import java.util.List;

import org.junit.Test;

import com.ccp.decorators.CcpJsonFieldName;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpTimeDecorator;

/**
 * Proves {@code VisRestApiSkillStatistics} (public routes, no session) and the {@code Cache-Control} that
 * {@code VisRestApiSkillStatisticsCacheHeader} puts in its successful answers, one per route
 * ({@code VisSkillStatisticsCacheControl}): a past month and the groups stay 1 year in the CDN, the current month and the
 * months of a skill 1 hour, and an error never gets the header.
 */
public class VisRestApiSkillStatisticsTest extends VisRestApiTestTemplate {

	enum JsonFieldNames implements CcpJsonFieldName {
		LONG {
			public String getValue() {
				return "public, max-age=300, s-maxage=31536000";
			}
		},
		SHORT {
			public String getValue() {
				return "public, max-age=60, s-maxage=3600";
			}
		},
		YEAR_MONTH_PATTERN {
			public String getValue() {
				return "yyyyMM";
			}
		},
		skills, total, months, groups
	}

	private static final String STATISTICS = "statistics/skills/";

	private HttpResponse<String> get(String path) {
		HttpResponse<String> response = this.sendWithNoSession(Fields.GET, STATISTICS + path);
		return response;
	}

	@Test
	public void aPastMonthStaysOneYearInTheCdn() {
		HttpResponse<String> response = this.get("202501/senior/0");

		this.assertStatus(200, response);
		assertEquals(JsonFieldNames.LONG.getValue(), this.cacheControlOf(response));
		CcpJsonRepresentation body = this.bodyOf(response);
		assertTrue(body.toString(), body.containsField(JsonFieldNames.skills));
		assertTrue(body.toString(), body.containsField(JsonFieldNames.total));
	}

	@Test
	public void theCurrentMonthStaysOneHourInTheCdn() {
		CcpTimeDecorator now = new CcpTimeDecorator();
		String yearMonthPattern = JsonFieldNames.YEAR_MONTH_PATTERN.getValue();
		String currentMonth = now.getFormattedDateTime(yearMonthPattern);

		HttpResponse<String> response = this.get(currentMonth + "/senior/0");

		this.assertStatus(200, response);
		assertEquals(JsonFieldNames.SHORT.getValue(), this.cacheControlOf(response));
	}

	@Test
	public void theMonthsOfASkillStayOneHourInTheCdn() {
		HttpResponse<String> response = this.get("senior/0/months?skill=JAVA");

		this.assertStatus(200, response);
		assertEquals(JsonFieldNames.SHORT.getValue(), this.cacheControlOf(response));
		CcpJsonRepresentation body = this.bodyOf(response);
		assertTrue(body.toString(), body.containsField(JsonFieldNames.months));
	}

	@Test
	public void theTelegramGroupsStayOneYearInTheCdn() {
		HttpResponse<String> response = this.get("telegramGroups");

		this.assertStatus(200, response);
		assertEquals(JsonFieldNames.LONG.getValue(), this.cacheControlOf(response));
		CcpJsonRepresentation body = this.bodyOf(response);
		List<String> groups = body.getAsStringList(JsonFieldNames.groups);
		assertFalse(body.toString(), groups.isEmpty());
	}

	@Test
	public void anUnknownSeniorityIsAValidationErrorWithNoCacheHeader() {
		HttpResponse<String> response = this.get("202501/ALL/0");

		this.assertStatus(422, response);
		assertEquals("", this.cacheControlOf(response));
	}

	@Test
	public void aMonthBefore2015IsAValidationError() {
		HttpResponse<String> response = this.get("201412/senior/0");

		this.assertStatus(422, response);
	}

	@Test
	public void aDddAbove99IsAValidationError() {
		HttpResponse<String> response = this.get("202501/senior/100");

		this.assertStatus(422, response);
	}

	/** A text where the number goes is a client error: until 2026-11-03 it answered 500 and was recorded as a system error. */
	@Test
	public void aDddThatIsNotANumberIsABadRequestWithNoCacheHeader() {
		HttpResponse<String> response = this.get("202501/senior/abc");

		this.assertStatus(400, response);
		assertEquals("", this.cacheControlOf(response));
	}

	@Test
	public void theMonthsWithADddThatIsNotANumberAreABadRequest() {
		HttpResponse<String> response = this.get("senior/abc/months?skill=JAVA");

		this.assertStatus(400, response);
	}

	@Test
	public void theMonthsWithAnEmptySkillAreAValidationError() {
		HttpResponse<String> response = this.get("senior/0/months?skill=");

		this.assertStatus(422, response);
	}

	@Test
	public void theMonthsWithNoSkillAreABadRequest() {
		HttpResponse<String> response = this.get("senior/0/months");

		this.assertStatus(400, response);
		assertEquals("", this.cacheControlOf(response));
	}
}
