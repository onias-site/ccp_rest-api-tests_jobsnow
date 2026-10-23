package com.ccp.random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.ccp.constants.CcpOtherConstants;
import com.ccp.decorators.CcpFieldName;
import com.ccp.decorators.CcpFileDecorator;
import com.ccp.decorators.CcpJsonRepresentation;
import com.ccp.decorators.CcpStringDecorator;
import com.ccp.random.CcpRandomScripts.JsonFieldNames;
import com.ccp.random.CcpRandomScripts.JsonFields;

/**
 * The pieces of the one-off scripts of {@link CcpRandomScripts}, each one run over a small input of its own (files in a
 * temporary folder where the piece reads or writes files).
 */
public class RandomScriptsHelpersTest {

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	private static CcpJsonRepresentation word(String word) {
		return CcpOtherConstants.EMPTY_JSON.put(JsonFields.word, word);
	}

	@Test
	public void aValidWordHasFrom3To50LettersAndIsNeitherRemovedNorExcluded() {
		RandomScriptsValidWord validWord = new RandomScriptsValidWord(List.of("REMOVIDA"), JsonFields.word, "EXCLUIDA", false);

		assertTrue(validWord.test(word("JAVA")));
		assertFalse(validWord.test(word("JS")));
		assertFalse(validWord.test(word("X".repeat(51))));
		assertFalse(validWord.test(word("REMOVIDA")));
		assertFalse(validWord.test(word("EXCLUIDA")));
	}

	@Test
	public void theUnderscoreCanCountAsASpace() {
		RandomScriptsValidWord validWord = new RandomScriptsValidWord(List.of("SPRING BOOT"), JsonFields.word, "", true);

		assertFalse(validWord.test(word("SPRING_BOOT")));
		assertTrue(validWord.test(word("SPRING_DATA")));
	}

	private static CcpJsonRepresentation recruiter(String email) {
		return CcpOtherConstants.EMPTY_JSON.put(JsonFields.id, email);
	}

	@Test
	public void theCompaniesAreGroupedByInitialsAndTheGroupsOrderedByTheirSize() {
		CcpRandomScripts.groupedCompanies = CcpOtherConstants.EMPTY_JSON;

		RandomScriptsCompanyGrouper.INSTANCE.accept(recruiter("ana@jobsnow.com"));
		RandomScriptsCompanyGrouper.INSTANCE.accept(recruiter("bia@jobsbrasil.com"));
		RandomScriptsCompanyGrouper.INSTANCE.accept(recruiter("caio@gmail.com"));
		RandomScriptsCompanyGrouper.INSTANCE.accept(recruiter("not an email"));
		RandomScriptsCompanyGrouper.INSTANCE.accept(recruiter("davi@ab.com"));

		Set<String> jobGroup = CcpRandomScripts.groupedCompanies.getAsObject(new CcpFieldName("JOB"));
		assertEquals(List.of("Jobsnow", "Jobsbrasil"), new ArrayList<>(jobGroup));
		assertEquals(2, CcpRandomScripts.groupedCompanies.fieldSet().size());
		List<String> initials = new ArrayList<>(List.of("GMA", "JOB"));
		initials.sort(RandomScriptsGroupSizeComparator.INSTANCE);
		assertEquals(List.of("JOB", "GMA"), initials);
		CcpRandomScripts.groupedCompanies = CcpOtherConstants.EMPTY_JSON;
	}

	@Test
	public void theEmailsOfTheGivenFieldsAreCollectedTrimmedAndInLowerCase() {
		Set<Object> emails = new HashSet<>();
		RandomScriptsEmailCollector collector = new RandomScriptsEmailCollector(emails, "email", "mail");
		CcpJsonRepresentation json = CcpOtherConstants.EMPTY_JSON
				.put(JsonFieldNames.email, " Ana@Jobsnow.com ")
				.put(JsonFieldNames.mail, "");

		collector.accept(json);

		assertEquals(Set.of("ana@jobsnow.com"), emails);
	}

	@Test
	public void theIdOfTheHitGoesIntoItsSource() {
		CcpJsonRepresentation source = CcpOtherConstants.EMPTY_JSON.put(JsonFieldNames.name, "Ana");
		CcpJsonRepresentation hit = CcpOtherConstants.EMPTY_JSON
				.put(JsonFieldNames._id, "abc")
				.put(JsonFieldNames._source, source);

		CcpJsonRepresentation record = RandomScriptsSourceWithId.INSTANCE.apply(hit);

		assertEquals("abc", record.getAsString(JsonFieldNames.id));
		assertEquals("Ana", record.getAsString(JsonFieldNames.name));
	}

	@Test
	public void eachPositionIsWrittenNumberedInOneLine() {
		File output = new File(this.temporaryFolder.getRoot(), "positions.txt");
		CcpFileDecorator file = new CcpStringDecorator(output.getAbsolutePath()).file();
		RandomScriptsPositionWriter writer = new RandomScriptsPositionWriter(file);
		CcpRandomScripts.counter = 0;

		writer.accept(CcpOtherConstants.EMPTY_JSON.put(JsonFieldNames.vaga, " Dev\nJava "));
		writer.accept(CcpOtherConstants.EMPTY_JSON.put(JsonFieldNames.vaga, "Tester"));

		assertEquals("000001: DevJava\n000002: Tester\n", file.getStringContent());
		CcpRandomScripts.counter = 0;
	}

	@Test
	public void eachJsonFileOfTheFolderBecomesAnEntryNamedAfterIt() throws Exception {
		File jsonFile = this.temporaryFolder.newFile("skill.json");
		new CcpStringDecorator(jsonFile.getAbsolutePath()).file().write("{\"name\":\"Ana\"}");
		Map<String, Object> json = new LinkedHashMap<>();

		new RandomScriptsSubJsonCollector(json).accept(new CcpStringDecorator(jsonFile.getAbsolutePath()).file());

		assertEquals(Set.of("skill"), json.keySet());
		@SuppressWarnings("unchecked")
		Map<String, Object> subJson = (Map<String, Object>) json.get("skill");
		assertEquals("Ana", subJson.get("name"));
	}

	@Test
	public void eachFieldOfTheMappingPointsToTheEntitiesThatHaveIt() throws Exception {
		File mapping = this.temporaryFolder.newFile("skill");
		new CcpStringDecorator(mapping.getAbsolutePath()).file().write("{\"mappings\":{\"properties\":{\"name\":{},\"email\":{}}}}");
		Map<String, List<String>> entitiesByField = new LinkedHashMap<>();

		new RandomScriptsFieldsByEntityCollector(entitiesByField).accept(new CcpStringDecorator(mapping.getAbsolutePath()).file());

		assertEquals(Set.of("name", "email"), entitiesByField.keySet());
		List<String> entities = entitiesByField.get("name");
		assertEquals(1, entities.size());
		assertTrue(entities.get(0).startsWith("JnEntity"));
	}

	@Test
	public void theGitignoreOfAFolderUnderGithubMovesToTheSameFolderOutsideIt() throws Exception {
		File githubFolder = this.temporaryFolder.newFolder("githubrepo");
		File targetFolder = this.temporaryFolder.newFolder("repo");
		File gitignore = new File(githubFolder, ".gitignore");
		gitignore.createNewFile();

		RandomScriptsGitignoreMover.INSTANCE.accept(new CcpStringDecorator(githubFolder.getAbsolutePath()).file());

		assertFalse(gitignore.exists());
		assertTrue(new File(targetFolder, ".gitignore").exists());
	}
}
