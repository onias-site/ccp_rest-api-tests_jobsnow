package com.vis.business.recruiter.resumes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Year;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpTimeDecorator;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.vis.business.position.VisBusinessFiltersOfThePosition;
import com.vis.business.position.VisBusinessResumeMatchesTheFilters;
import com.vis.business.position.VisBusinessResumesOfTheFilters;
import com.vis.entities.VisEntityVirtualPositionPublishingDay;
import com.vis.json.transformers.VisPositionPublishingDayCombinations;

/**
 * 2026-11-24: the resumes of the positions that receive them (instantly, hourly, daily, weekly) and the days of the
 * publishing of the positions in the groups of instant messages. The position becomes the body of the count of its
 * resumes; a single resume is compared with it in Java, with the rules of the Painless script of the count; the rows go
 * in the order chosen for the position.
 */
public class ResumesOfThePositionsTest {

	static {
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE);
	}

	private static final int CURRENT_YEAR = Year.now().getValue();

	private static CcpJsonRepresentation position(String extraFields) {
		return new CcpJsonRepresentation("{\"title\":\"Desenvolvedor Java\",\"recruiter\":\"rh@empresa.com.br\",\"resumeType\":1,"
				+ "\"ddd\":[11,21],\"disponibility\":30,\"temporallyJobTime\":0,\"seniority\":[\"pleno\",\"senior\"],"
				+ "\"resumesEmail\":\"RH@Empresa.com.br\",\"frequencyType\":\"daily\"" + extraFields + "}");
	}

	private static CcpJsonRepresentation resume(String extraFields) {
		int sixYearsAgo = CURRENT_YEAR - 6;
		return new CcpJsonRepresentation("{\"email\":\"hash1\",\"resumeType\":1,\"ddd\":[11],\"disponibility\":15,\"experience\":" + sixYearsAgo
				+ ",\"clt\":6000" + extraFields + "}");
	}

	private static boolean matches(CcpJsonRepresentation position, CcpJsonRepresentation resume) {
		CcpJsonRepresentation filters = VisBusinessFiltersOfThePosition.INSTANCE.execute(position);
		CcpJsonRepresentation input = CcpOtherConstants.EMPTY_JSON
				.put(VisBusinessResumeMatchesTheFilters.Fields.filters, filters)
				.put(VisBusinessResumeMatchesTheFilters.Fields.resume, resume);
		CcpJsonRepresentation output = VisBusinessResumeMatchesTheFilters.INSTANCE.execute(input);
		return output.getAsBoolean(VisBusinessResumeMatchesTheFilters.Fields.matches);
	}

	@Test
	public void thePositionBecomesTheBodyOfTheCountOfItsResumes() {
		CcpJsonRepresentation position = position(",\"pcd\":true,\"travel\":false,\"clt\":{\"min\":5000,\"max\":8000},"
				+ "\"skill\":[{\"skill\":\"JAVA\",\"category\":\"skillsFromText\"},{\"skill\":\"DOCKER\",\"category\":\"shouldSkills\"}],"
				+ "\"spokenLanguage\":[{\"name\":\"Inglês\",\"level\":2}]");
		CcpJsonRepresentation filters = VisBusinessFiltersOfThePosition.INSTANCE.execute(position);
		assertEquals(List.of("11", "21"), filters.getAsStringList(VisBusinessResumesOfTheFilters.Fields.ddd).stream().map(ddd -> ddd.replace(".0", "")).collect(Collectors.toList()));
		assertEquals(30, filters.getAsIntegerNumber(VisBusinessResumesOfTheFilters.Fields.disponibility).intValue());
		assertFalse("0 months of temporary job does not filter", filters.containsAllFields(VisBusinessResumesOfTheFilters.Fields.temporallyJobTime));
		assertEquals(List.of("JAVA"), filters.getAsStringList(VisBusinessResumesOfTheFilters.Fields.skill));
		assertTrue(filters.getAsBoolean(VisBusinessResumesOfTheFilters.Fields.pcd));
		assertFalse("an unchecked flag does not filter", filters.containsAllFields(VisBusinessResumesOfTheFilters.Fields.travel));
		assertEquals("empresa.com.br", filters.getAsString(VisBusinessResumesOfTheFilters.Fields.recruiterDomain));
		assertEquals(1, filters.getAsJsonList(VisBusinessResumesOfTheFilters.Fields.spokenLanguage).size());
	}

	@Test
	public void thePositionOfTheHomeOfficeTakesEveryDddAndTheOldSeniorityText() {
		CcpJsonRepresentation position = position("").put(() -> "ddd", List.of(11, 10)).put(() -> "seniority", "senior");
		CcpJsonRepresentation filters = VisBusinessFiltersOfThePosition.INSTANCE.execute(position);
		assertFalse(filters.containsAllFields(VisBusinessResumesOfTheFilters.Fields.ddd));
		assertEquals(List.of("senior"), filters.getAsStringList(VisBusinessResumesOfTheFilters.Fields.seniority));
		assertTrue("a resume of any ddd", matches(position, resume("").put(() -> "ddd", List.of(99))));
	}

	@Test
	public void theResumeIsComparedWithThePositionAsTheCountDoes() {
		CcpJsonRepresentation position = position(",\"clt\":{\"min\":5000,\"max\":8000},\"spokenLanguage\":[{\"name\":\"Inglês\",\"level\":2}]");
		CcpJsonRepresentation fluent = resume(",\"spokenLanguage\":[{\"name\":\"Inglês\",\"level\":3}]");
		assertTrue(matches(position, fluent));
		assertFalse("other area", matches(position, fluent.put(() -> "resumeType", 2)));
		assertFalse("no ddd of the position", matches(position, fluent.put(() -> "ddd", List.of(31, 10))));
		assertFalse("starts later", matches(position, fluent.put(() -> "disponibility", 60)));
		assertFalse("claim above the budget", matches(position, fluent.put(() -> "clt", 9000)));
		assertFalse("no claim of the budget", matches(position, fluent.removeFields(() -> "clt").put(() -> "pj", 7000)));
		assertFalse("level below the one asked", matches(position, resume(",\"spokenLanguage\":[{\"name\":\"Inglês\",\"level\":1}]")));
		assertFalse("junior, the position asks pleno or senior", matches(position, fluent.put(() -> "experience", CURRENT_YEAR - 1)));
		assertFalse("forbids the company of the e-mail of the position", matches(position, fluent.put(() -> "notAllowedCompany", List.of("Empresa.com.br"))));
		CcpJsonRepresentation positionForPcd = position(",\"pcd\":true");
		assertFalse(matches(positionForPcd, resume("")));
		assertTrue(matches(positionForPcd, resume(",\"pcd\":true")));
	}

	@Test
	public void bothBudgetsTakeAResumeWithOneClaimInside() {
		CcpJsonRepresentation position = position(",\"clt\":{\"min\":5000,\"max\":8000},\"pj\":{\"min\":10000,\"max\":15000}");
		assertTrue("CLT inside, PJ outside", matches(position, resume(",\"pj\":30000")));
		assertTrue("PJ only, inside", matches(position, resume("").removeFields(() -> "clt").put(() -> "pj", 12000)));
		assertFalse("both outside", matches(position, resume("").put(() -> "clt", 20000).put(() -> "pj", 30000)));
	}

	private static CcpJsonRepresentation row(String name, String sortValues) {
		return new CcpJsonRepresentation("{\"name\":\"" + name + "\",\"sortValues\":" + sortValues + "}");
	}

	private static List<String> sorted(CcpJsonRepresentation position, List<CcpJsonRepresentation> rows) {
		CcpJsonRepresentation input = CcpOtherConstants.EMPTY_JSON
				.put(VisBusinessSortResumesOfThePosition.Fields.position, position)
				.put(VisBusinessSortResumesOfThePosition.Fields.resumes, rows);
		CcpJsonRepresentation output = VisBusinessSortResumesOfThePosition.INSTANCE.execute(input);
		List<CcpJsonRepresentation> sortedRows = output.getAsJsonList(VisBusinessSortResumesOfThePosition.Fields.resumes);
		for (CcpJsonRepresentation sortedRow : sortedRows) {
			assertFalse("the keys of the sorting do not stay in the row", sortedRow.containsAllFields(VisBusinessSortResumesOfThePosition.Fields.positionSortKeys));
		}
		return sortedRows.stream().map(sortedRow -> sortedRow.getAsString(() -> "name")).collect(Collectors.toList());
	}

	@Test
	public void theRowsGoInTheOrderOfThePosition() {
		CcpJsonRepresentation position = position(",\"sort\":[{\"type\":\"asc\",\"field\":[\"clt\"]},{\"type\":\"desc\",\"field\":[\"experience\"]}]");
		List<CcpJsonRepresentation> rows = List.of(
				row("withoutClaim", "{\"experience\":20}"),
				row("expensive", "{\"clt\":9000,\"experience\":3}"),
				row("cheapJunior", "{\"clt\":4000,\"experience\":1}"),
				row("cheapSenior", "{\"clt\":4000,\"experience\":8}"));
		assertEquals(List.of("cheapSenior", "cheapJunior", "expensive", "withoutClaim"), sorted(position, rows));
		CcpJsonRepresentation descending = position(",\"sort\":[{\"type\":\"desc\",\"field\":[\"clt\"]}]");
		assertEquals("the missing claim goes to the end in both directions; a tie keeps the order",
				List.of("expensive", "cheapJunior", "cheapSenior", "withoutClaim"), sorted(descending, rows));
	}

	@Test
	public void theDesiredSkillsTheLanguageAndOneSkill() {
		CcpJsonRepresentation position = position(",\"skill\":[{\"skill\":\"DOCKER\",\"category\":\"shouldSkills\"},{\"skill\":\"AWS\",\"category\":\"shouldSkills\"}],"
				+ "\"sort\":[{\"type\":\"desc\",\"field\":[\"shouldSkillsTotal\"]},{\"type\":\"desc\",\"field\":[\"spokenLanguage\"],\"value\":\"inglês\"},"
				+ "{\"type\":\"asc\",\"field\":[\"skill\"],\"value\":\"KAFKA\"}]");
		List<CcpJsonRepresentation> rows = List.of(
				row("none", "{}"),
				row("dockerAndEnglish", "{\"skill\":[\"DOCKER\"],\"spokenLanguage\":{\"Inglês\":3}}"),
				row("dockerWithKafka", "{\"skill\":[\"DOCKER\",\"KAFKA\"]}"),
				row("dockerAndAwsAbove", "{\"skill\":[\"DOCKER\"],\"parentSkill\":[\"AWS\"]}"));
		assertEquals(List.of("dockerAndAwsAbove", "dockerAndEnglish", "dockerWithKafka", "none"), sorted(position, rows));
	}

	private static List<String> publishingDays(String extraFields) {
		CcpJsonRepresentation position = position(extraFields);
		List<CcpJsonRepresentation> combinations = VisPositionPublishingDayCombinations.INSTANCE.apply(position);
		return combinations.stream().map(combination -> combination.getAsString(VisEntityVirtualPositionPublishingDay.Fields.day)).collect(Collectors.toList());
	}

	private static String day(long time) {
		return new CcpTimeDecorator(time).getFormattedDateTime("yyyyMMdd");
	}

	@Test
	public void thePositionIsPublishedOnEveryDayOfItsValidityFromToday() {
		long now = System.currentTimeMillis();
		long oneDay = 86_400_000L;
		String validity = ",\"expiration\":{\"min\":\"" + (now - 3 * oneDay) + "\",\"max\":\"" + (now + 2 * oneDay) + "\"}";
		assertEquals(List.of(day(now), day(now + oneDay), day(now + 2 * oneDay)), publishingDays(validity + ",\"mustSendInGroups\":true"));
		assertEquals("unchecked, no day", List.of(), publishingDays(validity + ",\"mustSendInGroups\":false"));
		String expired = ",\"expiration\":{\"min\":\"" + (now - 9 * oneDay) + "\",\"max\":\"" + (now - 2 * oneDay) + "\"}";
		assertEquals("expired, no day", List.of(), publishingDays(expired + ",\"mustSendInGroups\":true"));
		String future = ",\"expiration\":{\"min\":\"" + (now + 5 * oneDay) + "\",\"max\":\"" + (now + 6 * oneDay) + "\"}";
		assertEquals("from the start of the validity", List.of(day(now + 5 * oneDay), day(now + 6 * oneDay)), publishingDays(future + ",\"mustSendInGroups\":true"));
	}
}
