package com.vis.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.Test;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.dependency.injection.CcpDependencyInjection;
import com.ccp.implementations.json.gson.CcpGsonJsonHandler;
import com.vis.json.fields.validation.VisJsonCommonsFields;

/**
 * The rules of {@link VisSkillsInText#find}, the engine that finds the known skills in a free text (the screen of the
 * resume and the load of the positions of Telegram), apart from where the words of the skills come from: here they come
 * from a fixed catalog, so no index is read.
 * <ul>
 * <li>the text is read in upper case; an empty text finds nothing;</li>
 * <li>a word of 7 characters or more is found anywhere in the text, unless it is a part of a longer skill word also
 * found;</li>
 * <li>a shorter word is found only as a whole word; inside another word it is discarded as {@code isPieceOfOtherWord};</li>
 * <li>"/" is not a delimiter: a slashed skill word ({@code PL/SQL}) stays whole and its sides are discarded as
 * {@code isPieceOfOtherSkill} unless they also stand alone; in any other slashed word each side is a word too;</li>
 * <li>excluded skills are left out (in any case) and echoed back;</li>
 * <li>each skill is kept once, by the shortest label; the other words of it are discarded as
 * {@code skillAlreadyAdded};</li>
 * <li>the label is the skill, or {@code word (skill)} when found by another word; the suffix {@code 123} of a parent is
 * dropped.</li>
 * </ul>
 */
public class OnFindingSkillsInAText {

	static {
		CcpDependencyInjection.loadAllDependencies(CcpGsonJsonHandler.INSTANCE);
	}

	private static CcpJsonRepresentation skillWord(String skill, String word, String... parents) {
		List<String> parentList = Arrays.asList(parents);
		CcpJsonRepresentation skillWord = CcpOtherConstants.EMPTY_JSON
				.put(VisJsonCommonsFields.skill, skill)
				.put(VisJsonCommonsFields.word, word)
				.put(VisJsonCommonsFields.parent, parentList);
		return skillWord;
	}

	private static final List<CcpJsonRepresentation> CATALOG = Arrays.asList(
			skillWord("JAVA", "JAVA"),
			skillWord("SQL", "SQL", "SQLDATABASE123"),
			skillWord("PLSQL", "PL/SQL", "SQLDATABASE123", "ORACLE"),
			skillWord("SCRUM", "SCRUM"),
			skillWord("KANBAN", "KANBAN"),
			skillWord("QA", "QA"),
			skillWord("GO", "GO"),
			skillWord("ANGULAR", "ANGULAR"),
			skillWord("ANGULARJS", "ANGULARJS"),
			skillWord("POSTGRES", "POSTGRES"),
			skillWord("KUBERNETES", "K8S"),
			skillWord("JAVASCRIPT", "JAVASCRIPT"),
			skillWord("JAVASCRIPT", "ECMASCRIPT"),
			skillWord("SPRING", "SPRING"),
			skillWord("SPRING BOOT", "SPRING BOOT"),
			skillWord("JAVASCRIPT", "JS"),
			skillWord("ORM", "ORM"),
			skillWord("JSORM", "JS ORM"),
			skillWord("SQLSERVER", "MS SQL"),
			skillWord("SAP", "SAP"),
			skillWord("SAP FI", "SAP FI"),
			skillWord("VB", "VB"),
			skillWord("VB.NET", "VB.NET")
			);

	private final List<String> wordsAskedToTheCatalog = new ArrayList<>();

	private final Function<Collection<String>, List<CcpJsonRepresentation>> catalog = words -> { this.wordsAskedToTheCatalog.addAll(words); return CATALOG; };

	private CcpJsonRepresentation find(String text, CcpJsonRepresentation... excludedSkills) {
		List<CcpJsonRepresentation> excludedSkillList = Arrays.asList(excludedSkills);
		CcpJsonRepresentation request = CcpOtherConstants.EMPTY_JSON
				.put(Fields.text, text)
				.put(Fields.excludedSkill, excludedSkillList);
		CcpJsonRepresentation response = VisSkillsInText.find(request, this.catalog);
		return response;
	}

	private List<String> chosen(CcpJsonRepresentation response) {
		List<CcpJsonRepresentation> skills = response.getAsJsonList(VisJsonCommonsFields.skill);
		List<String> names = skills.stream().map(skill -> skill.getAsString(VisJsonCommonsFields.skill)).collect(Collectors.toList());
		return names;
	}

	private List<CcpJsonRepresentation> discarded(CcpJsonRepresentation response, Fields reason) {
		CcpJsonRepresentation discardedSkills = response.getInnerJson(Fields.discardedSkills);
		List<CcpJsonRepresentation> discarded = discardedSkills.getAsJsonList(reason);
		return discarded;
	}

	private CcpJsonRepresentation chosenSkill(CcpJsonRepresentation response, String skillName) {
		List<CcpJsonRepresentation> skills = response.getAsJsonList(VisJsonCommonsFields.skill);
		CcpJsonRepresentation skill = skills.stream().filter(x -> skillName.equals(x.getAsString(VisJsonCommonsFields.skill))).findFirst().get();
		return skill;
	}

	@Test
	public void anEmptyTextFindsNothingAndDoesNotAskTheCatalog() {
		CcpJsonRepresentation response = this.find(" \n\t ");

		assertTrue(response.isEmpty());
		assertTrue(this.wordsAskedToTheCatalog.isEmpty());
	}

	@Test
	public void theTextIsReadInUpperCase() {
		List<String> names = this.chosen(this.find("Developer with java and scrum"));

		assertEquals(Arrays.asList("JAVA", "SCRUM"), names);
	}

	@Test
	public void theCatalogIsAskedForTheWordsOfTheTextInUpperCasePlusTheSidesOfTheSlashedWords() {
		this.find("Java, Scrum/Kanban");

		assertTrue(this.wordsAskedToTheCatalog.toString(), this.wordsAskedToTheCatalog.containsAll(Arrays.asList("JAVA", "SCRUM/KANBAN", "SCRUM", "KANBAN")));
		assertFalse(this.wordsAskedToTheCatalog.toString(), this.wordsAskedToTheCatalog.contains("Java"));
	}

	@Test
	public void aWordThatIsNotInTheTextIsNotFound() {
		List<String> names = this.chosen(this.find("JAVA"));

		assertFalse(names.toString(), names.contains("SQL"));
		assertFalse(names.toString(), names.contains("POSTGRES"));
	}

	@Test
	public void theLabelIsTheSkillWhenFoundByItsOwnName() {
		CcpJsonRepresentation java = this.chosenSkill(this.find("JAVA"), "JAVA");

		assertEquals("JAVA", java.getAsString(Fields.label));
	}

	@Test
	public void theLabelNamesTheWordWhenFoundByAnotherWord() {
		CcpJsonRepresentation kubernetes = this.chosenSkill(this.find("Deploy em K8S"), "KUBERNETES");

		assertEquals("K8S (KUBERNETES)", kubernetes.getAsString(Fields.label));
	}

	@Test
	public void theSuffix123OfTheParentsIsDropped() {
		CcpJsonRepresentation plsql = this.chosenSkill(this.find("PL/SQL"), "PLSQL");

		assertEquals(Arrays.asList("SQLDATABASE", "ORACLE"), plsql.getAsStringList(VisJsonCommonsFields.parent));
	}

	@Test
	public void aShortWordIsFoundAsAWholeWord() {
		List<String> names = this.chosen(this.find("Testes de QA (manual), Go; SQL!"));

		assertTrue(names.toString(), names.containsAll(Arrays.asList("QA", "GO", "SQL")));
	}

	@Test
	public void aShortWordInsideAnotherWordIsDiscardedAsPieceOfOtherWord() {
		CcpJsonRepresentation response = this.find("Trabalhou no GOOGLE com MYSQL");

		List<String> names = this.chosen(response);
		assertFalse(names.toString(), names.contains("GO"));
		assertFalse(names.toString(), names.contains("SQL"));

		List<CcpJsonRepresentation> discarded = this.discarded(response, Fields.isPieceOfOtherWord);
		List<String> associated = discarded.stream().map(x -> x.getAsString(Fields.associated)).collect(Collectors.toList());
		assertTrue(associated.toString(), associated.containsAll(Arrays.asList("GOOGLE", "MYSQL")));
	}

	@Test
	public void aShortSkillStaysWhenItIsAlsoAWholeWordInsideALongerSkillFound() {
		List<String> names = this.chosen(this.find("SPRING BOOT"));

		assertTrue(names.toString(), names.containsAll(Arrays.asList("SPRING", "SPRING BOOT")));
	}

	@Test
	public void aLongWordIsFoundInsideAnotherWord() {
		List<String> names = this.chosen(this.find("Banco POSTGRESQL"));

		assertTrue(names.toString(), names.contains("POSTGRES"));
	}

	@Test
	public void aLongWordThatIsPartOfALongerSkillFoundIsDiscardedAsPieceOfOtherSkill() {
		CcpJsonRepresentation response = this.find("Front end com ANGULARJS");

		List<String> names = this.chosen(response);
		assertTrue(names.toString(), names.contains("ANGULARJS"));
		assertFalse(names.toString(), names.contains("ANGULAR"));

		List<CcpJsonRepresentation> discarded = this.discarded(response, Fields.isPieceOfOtherSkill);
		assertEquals(1, discarded.size());
		assertEquals("ANGULAR", discarded.get(0).getAsString(VisJsonCommonsFields.skill));
		assertEquals("ANGULARJS", discarded.get(0).getAsString(Fields.associated));
	}

	@Test
	public void eachSideOfASlashedWordIsAWord() {
		List<String> names = this.chosen(this.find("SCRUM/KANBAN e JAVA/SQL"));

		assertTrue(names.toString(), names.containsAll(Arrays.asList("SCRUM", "KANBAN", "JAVA", "SQL")));
	}

	@Test
	public void aSlashedSkillWordStaysWholeAndItsSideIsDiscardedAsPieceOfOtherSkill() {
		CcpJsonRepresentation response = this.find("Procedures em PL/SQL");

		List<String> names = this.chosen(response);
		assertEquals(Arrays.asList("PLSQL"), names);

		List<CcpJsonRepresentation> discarded = this.discarded(response, Fields.isPieceOfOtherSkill);
		assertEquals(1, discarded.size());
		assertEquals("SQL", discarded.get(0).getAsString(VisJsonCommonsFields.skill));
		assertEquals("PL/SQL", discarded.get(0).getAsString(Fields.associated));
	}

	@Test
	public void theSideOfASlashedSkillWordIsASkillWhenItAlsoStandsAlone() {
		List<String> names = this.chosen(this.find("PL/SQL e SQL"));

		assertTrue(names.toString(), names.containsAll(Arrays.asList("PLSQL", "SQL")));
	}

	@Test
	public void aShortWordWithDelimitersIsFoundAsASequenceOfWholeWords() {
		List<String> names = this.chosen(this.find("Consultor SAP FI, sistemas em VB.NET e banco MS SQL"));

		assertTrue(names.toString(), names.containsAll(Arrays.asList("SAP FI", "VB.NET", "SQLSERVER")));
	}

	@Test
	public void thePiecesOfAShortWordWithDelimitersAreDiscardedAsPieceOfOtherSkill() {
		CcpJsonRepresentation response = this.find("Sistemas em VB.NET com JS ORM");

		List<String> names = this.chosen(response);
		assertEquals(Arrays.asList("JSORM", "VB.NET"), names.stream().sorted().collect(Collectors.toList()));

		List<CcpJsonRepresentation> discarded = this.discarded(response, Fields.isPieceOfOtherSkill);
		List<String> pieces = discarded.stream().map(x -> x.getAsString(VisJsonCommonsFields.word) + " in " + x.getAsString(Fields.associated)).sorted().collect(Collectors.toList());
		assertEquals(Arrays.asList("JS in JS ORM", "ORM in JS ORM", "VB in VB.NET"), pieces);
	}

	@Test
	public void thePieceOfAShortWordWithDelimitersIsASkillWhenItAlsoStandsAlone() {
		List<String> names = this.chosen(this.find("MS SQL, mas também SQL puro"));

		assertTrue(names.toString(), names.containsAll(Arrays.asList("SQLSERVER", "SQL")));
	}

	@Test
	public void aShortWordWithDelimitersIsNotFoundWhenItsWordsAreOnlyThePrefixOfOthers() {
		List<String> names = this.chosen(this.find("Time de MS SQLITE"));

		assertFalse(names.toString(), names.contains("SQLSERVER"));
	}

	@Test
	public void anExcludedSkillIsLeftOutInAnyCaseAndEchoedBack() {
		CcpJsonRepresentation excludedJava = CcpOtherConstants.EMPTY_JSON
				.put(VisJsonCommonsFields.skill, "JAVA")
				.put(VisJsonCommonsFields.word, "java");

		CcpJsonRepresentation response = this.find("JAVA e SCRUM", excludedJava);

		assertEquals(Arrays.asList("SCRUM"), this.chosen(response));
		List<CcpJsonRepresentation> excludedSkill = response.getAsJsonList(Fields.excludedSkill);
		assertEquals(1, excludedSkill.size());
		assertEquals("java", excludedSkill.get(0).getAsString(VisJsonCommonsFields.word));
	}

	@Test
	public void aSkillFoundByTwoWordsIsKeptOnceByTheShortestLabel() {
		CcpJsonRepresentation response = this.find("ECMASCRIPT, ou seja, JAVASCRIPT");

		List<String> names = this.chosen(response);
		assertEquals(Arrays.asList("JAVASCRIPT"), names);
		assertEquals("JAVASCRIPT", this.chosenSkill(response, "JAVASCRIPT").getAsString(Fields.label));

		List<CcpJsonRepresentation> repeated = this.discarded(response, Fields.skillAlreadyAdded);
		assertEquals(1, repeated.size());
		assertEquals("ECMASCRIPT", repeated.get(0).getAsString(VisJsonCommonsFields.word));
		assertEquals("JAVASCRIPT", repeated.get(0).getAsString(Fields.associated));
	}

	@Test
	public void theChosenSkillsComeOrderedByTheLengthOfTheLabel() {
		CcpJsonRepresentation response = this.find("ANGULARJS, SCRUM, QA, K8S");

		List<CcpJsonRepresentation> skills = response.getAsJsonList(VisJsonCommonsFields.skill);
		List<String> labels = skills.stream().map(x -> x.getAsString(Fields.label)).collect(Collectors.toList());
		assertEquals(Arrays.asList("QA", "SCRUM", "ANGULARJS", "K8S (KUBERNETES)"), labels);
	}

	@Test
	public void aTextWithNoSkillFindsNoneAndDiscardsNone() {
		CcpJsonRepresentation response = this.find("Texto sem nenhuma habilidade conhecida");

		assertTrue(this.chosen(response).isEmpty());
		assertTrue(response.getInnerJson(Fields.discardedSkills).isEmpty());
	}
}
